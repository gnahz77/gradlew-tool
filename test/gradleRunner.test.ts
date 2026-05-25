import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { afterEach, describe, expect, it } from "vitest";
import { runGradle } from "../src/runner/gradleRunner.js";
import type { CliOptions } from "../src/types/options.js";

const tempDirs: string[] = [];
const isWindows = process.platform === "win32";

afterEach(() => {
  for (const dir of tempDirs.splice(0)) {
    fs.rmSync(dir, { recursive: true, force: true });
  }
});

function createProject(files: Record<string, string>): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "gradlew-tool-"));
  tempDirs.push(dir);

  for (const [relativePath, content] of Object.entries(files)) {
    const fullPath = path.join(dir, relativePath);
    fs.mkdirSync(path.dirname(fullPath), { recursive: true });
    fs.writeFileSync(fullPath, content, { encoding: "utf8" });
  }

  return dir;
}

function createWrapperFiles(): { wrapperName: string } {
  if (isWindows) {
    return {
      wrapperName: "gradlew.bat",
    };
  }

  return {
    wrapperName: "gradlew",
  };
}

function createOptions(cwd: string, gradleArgs: string[], timeout: number): CliOptions {
  return {
    json: false,
    showWarnings: false,
    tail: 80,
    timeout,
    fullOutput: false,
    logDir: ".agent-build",
    cwd,
    maxErrorLines: 160,
    addDefaultGradleFlags: true,
    shell: "direct",
    shellArgs: [],
    dryRun: false,
    help: false,
    version: false,
    gradleArgs,
  };
}

describe("runGradle", () => {
  it("times out long-running wrapper processes", async () => {
    const wrapper = createWrapperFiles();
    const cwd = createProject({
      [wrapper.wrapperName]: isWindows
        ? "@echo off\r\necho starting build\r\nping -n 3 127.0.0.1 >nul\r\necho BUILD SUCCESSFUL\r\n"
        : "#!/bin/sh\necho 'starting build'\nsleep 2\necho 'BUILD SUCCESSFUL'\n",
    });

    if (!isWindows) {
      fs.chmodSync(path.join(cwd, wrapper.wrapperName), 0o755);
    }

    const result = await runGradle(createOptions(cwd, ["help"], 50));

    expect(result.status).toBe("timed-out");
    expect(result.exitCode).toBe(124);
    expect(result.category).toBe("process-timeout");
    expect(result.errors[0]?.message).toContain("Process exceeded timeout of 50ms");
    expect(result.tail.join("\n")).toContain("Process exceeded timeout of 50ms");

    const logPath = path.resolve(cwd, result.fullLog);
    expect(fs.readFileSync(logPath, "utf8")).toContain("Process exceeded timeout of 50ms");
  });

  it("completes before timeout", async () => {
    const wrapper = createWrapperFiles();
    const cwd = createProject({
      [wrapper.wrapperName]: isWindows
        ? "@echo off\r\necho BUILD SUCCESSFUL in 1s\r\nexit /b 0\r\n"
        : "#!/bin/sh\necho 'BUILD SUCCESSFUL in 1s'\nexit 0\n",
    });

    if (!isWindows) {
      fs.chmodSync(path.join(cwd, wrapper.wrapperName), 0o755);
    }

    const result = await runGradle(createOptions(cwd, ["help"], 1000));

    expect(result.status).toBe("success");
    expect(result.category).toBe("none");

    const logPath = path.resolve(cwd, result.fullLog);
    expect(fs.readFileSync(logPath, "utf8")).toContain("BUILD SUCCESSFUL in 1s");
  });
});
