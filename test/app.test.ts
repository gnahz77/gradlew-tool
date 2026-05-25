import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawn } from "node:child_process";
import { afterEach, describe, expect, it } from "vitest";
import { parseCliArgs } from "../src/app.js";

const tempDirs: string[] = [];
const isWindows = process.platform === "win32";

afterEach(() => {
  for (const dir of tempDirs.splice(0)) {
    fs.rmSync(dir, { recursive: true, force: true });
  }
});

function createProject(files: Record<string, string>): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "gradlew-tool-app-"));
  tempDirs.push(dir);

  for (const [relativePath, content] of Object.entries(files)) {
    const fullPath = path.join(dir, relativePath);
    fs.mkdirSync(path.dirname(fullPath), { recursive: true });
    fs.writeFileSync(fullPath, content, { encoding: "utf8" });
  }

  return dir;
}

describe("parseCliArgs", () => {
  it("uses the default timeout", () => {
    const result = parseCliArgs(["build"]);
    expect(result.timeout).toBe(600000);
  });

  it("reads a custom timeout", () => {
    const result = parseCliArgs(["--timeout", "1234", "build"]);
    expect(result.timeout).toBe(1234);
    expect(result.gradleArgs).toEqual(["build"]);
  });

  it("rejects negative timeout values", () => {
    expect(() => parseCliArgs(["--timeout", "-1", "build"])).toThrow("Invalid numeric value for --timeout");
  });

  it.runIf(isWindows)("exits promptly after printing JSON output on Windows", async () => {
    const cwd = createProject({
      "gradlew.bat": "@echo off\r\necho BUILD SUCCESSFUL in 1s\r\nexit /b 0\r\n",
    });

    const tsxCliPath = path.resolve("node_modules", "tsx", "dist", "cli.mjs");
    const cliPath = path.resolve("src", "cli.ts");

    const result = await new Promise<{ code: number | null; stdout: string; stderr: string }>((resolve, reject) => {
      const child = spawn(process.execPath, [tsxCliPath, cliPath, "--json", "--", "help"], {
        cwd,
        env: process.env,
        stdio: ["ignore", "pipe", "pipe"],
        windowsHide: true,
      });

      let stdout = "";
      let stderr = "";
      const timeout = setTimeout(() => {
        child.kill("SIGKILL");
        reject(new Error(`CLI did not exit promptly. stdout:\n${stdout}\nstderr:\n${stderr}`));
      }, 5000);

      child.stdout.on("data", (chunk: Buffer | string) => {
        stdout += chunk.toString();
      });

      child.stderr.on("data", (chunk: Buffer | string) => {
        stderr += chunk.toString();
      });

      child.on("error", (error) => {
        clearTimeout(timeout);
        reject(error);
      });

      child.on("close", (code) => {
        clearTimeout(timeout);
        resolve({ code, stdout, stderr });
      });
    });

    expect(result.code).toBe(0);
    expect(result.stderr).toBe("");
    expect(JSON.parse(result.stdout)).toMatchObject({
      status: "success",
      exitCode: 0,
      category: "none",
    });
  });
});
