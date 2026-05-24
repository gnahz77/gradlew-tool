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
      windowsHide: true,
      env: process.env,
    });

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
      logStream.end();
      reject(error);
    });

    child.on("close", (code) => {
      lineHandler.flush();
      logStream.end();
      resolve(
        parseBuildResult(
          {
            exitCode: code ?? 1,
            shell: options.shell,
            command: builtCommand.displayCommand,
            fullLog: displayLogPath,
            durationMs: Date.now() - startedAt,
            lines,
          },
          {
            maxErrorLines: options.maxErrorLines,
            showWarnings: options.showWarnings,
            tail: options.tail,
          },
        ),
      );
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
