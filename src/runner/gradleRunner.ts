import fs from "node:fs";
import path from "node:path";
import { spawn } from "node:child_process";
import { parseBuildResult } from "../parser/parseBuildResult.js";
import { buildCommand } from "./commandBuilder.js";
import { createLineCollector } from "./lineReader.js";
import { resolveGradlew } from "./resolveGradlew.js";
import { createLogFilePath } from "../utils/fs.js";
import { formatLogPath } from "../utils/path.js";
import { quoteForCmd } from "../utils/quote.js";
import type { CliOptions } from "../types/options.js";
import type { BuildSummary } from "../types/result.js";

interface ExecutionOutcome {
  exitCode: number;
  timedOut: boolean;
}

export async function runGradle(options: CliOptions): Promise<BuildSummary> {
  const wrapper = resolveGradlew({
    cwd: options.cwd,
    gradlew: options.gradlew,
  });

  const logFilePath = createLogFilePath(options.cwd, options.logDir);
  const displayLogPath = formatLogPath(options.cwd, logFilePath);

  if (!wrapper.found) {
    return {
      status: "wrapper-not-found",
      exitCode: 1,
      category: "gradle-wrapper-not-found",
      failedTask: null,
      shell: options.shell,
      command: "",
      errors: [
        {
          severity: "error",
          category: "gradle-wrapper-not-found",
          message: `Gradle Wrapper not found. Checked ${wrapper.path}`,
        },
      ],
      warnings: [],
      warningsSuppressed: 0,
      taskLinesSuppressed: 0,
      fullLog: displayLogPath,
      durationMs: 0,
      tail: [],
      suggestion: "Add a project-local `gradlew` or `gradlew.bat`, or pass `--gradlew <path>`.",
    };
  }

  const gradleArgs = withDefaultGradleFlags(options.gradleArgs, options.addDefaultGradleFlags);
  const builtCommand = buildCommand({
    cwd: options.cwd,
    gradlewPath: wrapper.path,
    gradleArgs,
    shell: options.shell,
    shellExec: options.shellExec,
    shellArgs: options.shellArgs,
  });

  if (options.dryRun) {
    return {
      status: "dry-run",
      exitCode: 0,
      category: "none",
      failedTask: null,
      shell: options.shell,
      command: builtCommand.displayCommand,
      errors: [],
      warnings: [],
      warningsSuppressed: 0,
      taskLinesSuppressed: 0,
      fullLog: displayLogPath,
      durationMs: 0,
      tail: [],
    };
  }

  return await new Promise<BuildSummary>((resolve, reject) => {
    const startedAt = Date.now();
    const lines: string[] = [];
    const logStream = fs.createWriteStream(logFilePath, { encoding: "utf8" });
    const spawnSpec = createSpawnSpec(options.shell, wrapper.path, builtCommand.command, builtCommand.args);
    const child = spawn(spawnSpec.command, spawnSpec.args, {
      cwd: options.cwd,
      shell: false,
      stdio: ["ignore", "pipe", "pipe"],
      windowsHide: true,
      env: process.env,
    });
    let finished = false;
    let timedOut = false;
    let timeoutHandle: NodeJS.Timeout | undefined;

    const finalize = async (outcome: ExecutionOutcome) => {
      if (finished) {
        return;
      }

      finished = true;
      if (timeoutHandle) {
        clearTimeout(timeoutHandle);
      }

      lineHandler.flush();

      try {
        await endWritableStream(logStream);
      } catch (error) {
        reject(error);
        return;
      }

      const durationMs = Date.now() - startedAt;
      if (outcome.timedOut) {
        const message = `Process exceeded timeout of ${options.timeout}ms and was terminated.`;
        lines.push(message);
        const parsed = parseBuildResult(
          {
            exitCode: 124,
            shell: options.shell,
            command: builtCommand.displayCommand,
            fullLog: displayLogPath,
            durationMs,
            lines,
          },
          {
            maxErrorLines: options.maxErrorLines,
            showWarnings: options.showWarnings,
            tail: options.tail,
          },
        );

        resolve({
          ...parsed,
          status: "timed-out",
          exitCode: 124,
          category: "process-timeout",
          errors: [
            {
              severity: "error",
              category: "process-timeout",
              message,
            },
            ...parsed.errors.filter((issue) => issue.message !== message),
          ],
          suggestion: "Increase `--timeout`, inspect the saved log, or optimize the Gradle task before retrying.",
        });
        return;
      }

      resolve(
        parseBuildResult(
          {
            exitCode: outcome.exitCode,
            shell: options.shell,
            command: builtCommand.displayCommand,
            fullLog: displayLogPath,
            durationMs,
            lines,
          },
          {
            maxErrorLines: options.maxErrorLines,
            showWarnings: options.showWarnings,
            tail: options.tail,
          },
        ),
      );
    };

    timeoutHandle = setTimeout(() => {
      timedOut = true;
      logStream.write(`\nProcess exceeded timeout of ${options.timeout}ms and was terminated.\n`);
      child.kill();
      setTimeout(() => {
        child.kill("SIGKILL");
      }, 5000).unref();
    }, options.timeout);
    timeoutHandle.unref();

    const lineHandler = createLineCollector((line) => {
      lines.push(line);
      if (options.fullOutput) {
        process.stdout.write(`${line}\n`);
      }
    });

    child.stdout.on("data", (chunk: Buffer | string) => {
      const value = chunk.toString();
      logStream.write(value);
      lineHandler.push(value);
    });

    child.stderr.on("data", (chunk: Buffer | string) => {
      const value = chunk.toString();
      logStream.write(value);
      lineHandler.push(value);
    });

    child.on("error", (error) => {
      if (timeoutHandle) {
        clearTimeout(timeoutHandle);
      }
      reject(error);
    });

    child.on("close", (code) => {
      void finalize({
        exitCode: code ?? 1,
        timedOut,
      });
    });
  });
}

function withDefaultGradleFlags(args: string[], enabled: boolean): string[] {
  if (!enabled) {
    return [...args];
  }

  const nextArgs = [...args];
  if (!args.some((arg) => arg.startsWith("--console"))) {
    nextArgs.push("--console=plain");
  }

  if (!args.some((arg) => arg.startsWith("--warning-mode"))) {
    nextArgs.push("--warning-mode=summary");
  }

  return nextArgs;
}

function createSpawnSpec(shell: string, gradlewPath: string, command: string, args: string[]): { command: string; args: string[] } {
  if (shell === "direct" && process.platform === "win32" && gradlewPath.toLowerCase().endsWith(".bat")) {
    return {
      command: "cmd.exe",
      args: ["/d", "/s", "/c", quoteForCmd([gradlewPath, ...args])],
    };
  }

  return { command, args };
}

function endWritableStream(stream: fs.WriteStream): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    const cleanup = () => {
      stream.off("finish", handleFinish);
      stream.off("error", handleError);
    };

    const handleFinish = () => {
      cleanup();
      resolve();
    };

    const handleError = (error: Error) => {
      cleanup();
      reject(error);
    };

    stream.once("finish", handleFinish);
    stream.once("error", handleError);
    stream.end();
  });
}
