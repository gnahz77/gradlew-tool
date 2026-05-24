import fs from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { parseBuildResult } from "../src/parser/parseBuildResult.js";

function parseFixture(name: string) {
  const lines = fs.readFileSync(path.resolve("test/fixtures", name), "utf8").split(/\r?\n/);
  return parseBuildResult(
    {
      exitCode: 1,
      shell: "direct",
      command: "./gradlew assembleDebug",
      fullLog: ".agent-build/gradle.log",
      durationMs: 1000,
      lines,
    },
    {
      maxErrorLines: 160,
      showWarnings: false,
      tail: 80,
    },
  );
}

describe("classifier", () => {
  it("classifies gradle cache lock", () => {
    expect(parseFixture("gradle-cache-lock.log").category).toBe("gradle-cache-lock");
  });

  it("classifies manifest merge failures", () => {
    expect(parseFixture("manifest-merge-error.log").category).toBe("manifest-merge-error");
  });

  it("classifies aapt and resource failures", () => {
    const result = parseFixture("aapt-error.log");
    expect(["aapt-error", "android-resource-error"]).toContain(result.category);
  });
});
