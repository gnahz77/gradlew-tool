import fs from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { parseBuildResult } from "../src/parser/parseBuildResult.js";

function readFixture(name: string): string[] {
  const value = fs.readFileSync(path.resolve("test/fixtures", name), "utf8");
  return value.split(/\r?\n/).filter((line) => line.length > 0);
}

describe("parseBuildResult", () => {
  it("parses success logs", () => {
    const result = parseBuildResult(
      {
        exitCode: 0,
        shell: "direct",
        command: "./gradlew build",
        fullLog: ".agent-build/gradle.log",
        durationMs: 1000,
        lines: readFixture("success.log"),
      },
      {
        maxErrorLines: 160,
        showWarnings: false,
        tail: 80,
      },
    );

    expect(result.status).toBe("success");
    expect(result.category).toBe("none");
    expect(result.taskLinesSuppressed).toBeGreaterThan(0);
  });

  it("extracts failed task and kotlin errors", () => {
    const result = parseBuildResult(
      {
        exitCode: 1,
        shell: "direct",
        command: "./gradlew :app:assembleDebug",
        fullLog: ".agent-build/gradle.log",
        durationMs: 2000,
        lines: readFixture("kotlin-compile-error.log"),
      },
      {
        maxErrorLines: 160,
        showWarnings: false,
        tail: 80,
      },
    );

    expect(result.status).toBe("failed");
    expect(result.failedTask).toBe(":app:compileDebugKotlin");
    expect(result.category).toBe("kotlin-compile-error");
    expect(result.errors[0]?.file).toContain("MainActivity.kt");
    expect(result.tail.every((line) => !line.startsWith("> Task "))).toBe(true);
  });

  it("strips inline task noise from errors and tail", () => {
    const result = parseBuildResult(
      {
        exitCode: 1,
        shell: "direct",
        command: "./gradlew :app:assembleDebug",
        fullLog: ".agent-build/gradle.log",
        durationMs: 2000,
        lines: [
          "> Task :app:preBuild UP-TO-DATE",
          "e: file:///workspace/app/src/main/java/com/example/MainActivity.kt:12:5 Unresolved reference: doesNotExist> Task :app:compileDebugKotlin FAILED",
          "FAILURE: Build failed with an exception.",
          "BUILD FAILED in 9s",
        ],
      },
      {
        maxErrorLines: 160,
        showWarnings: false,
        tail: 80,
      },
    );

    expect(result.errors[0]?.message).not.toContain("> Task");
    expect(result.tail.some((line) => line.includes("> Task"))).toBe(false);
  });
});
