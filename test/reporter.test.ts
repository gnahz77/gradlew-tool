import { describe, expect, it } from "vitest";
import { formatJsonResult } from "../src/reporter/jsonReporter.js";
import { formatTextResult } from "../src/reporter/textReporter.js";
import type { BuildSummary } from "../src/types/result.js";

const sample: BuildSummary = {
  status: "failed",
  exitCode: 1,
  category: "gradle-cache-lock",
  failedTask: null,
  shell: "direct",
  command: "gradlew.bat :app:assembleDebug --console=plain --warning-mode=summary",
  errors: [
    {
      severity: "error",
      category: "gradle-cache-lock",
      message: "Timeout waiting to lock journal cache",
    },
  ],
  warnings: [],
  warningsSuppressed: 2,
  taskLinesSuppressed: 10,
  fullLog: ".agent-build/gradle-20260513-153012.log",
  durationMs: 64000,
  tail: ["BUILD FAILED in 4s"],
  suggestion: "Another Gradle process may be using the cache.",
};

describe("reporters", () => {
  it("formats compact text", () => {
    const result = formatTextResult(sample);
    expect(result).toContain("BUILD_STATUS: FAILED");
    expect(result).toContain("ERROR_SUMMARY:");
    expect(result).toContain("SUGGESTED_FIX:");
    expect(result).not.toContain("WARNINGS_SUPPRESSED:");
    expect(result).not.toContain("> Task ");
  });

  it("formats json", () => {
    const result = formatJsonResult(sample);
    expect(JSON.parse(result).category).toBe("gradle-cache-lock");
  });

  it("shows log tail for timed out results", () => {
    const result = formatTextResult({
      ...sample,
      status: "timed-out",
      category: "process-timeout",
      exitCode: 124,
      errors: [
        {
          severity: "error",
          category: "process-timeout",
          message: "Process exceeded timeout of 600000ms and was terminated.",
        },
      ],
      tail: ["starting build", "Process exceeded timeout of 600000ms and was terminated."],
    });

    expect(result).toContain("BUILD_STATUS: TIMED-OUT");
    expect(result).toContain("LOG_TAIL:");
  });
});
