import { classifyIssues, getSuggestedFix } from "./classifiers.js";
import { ContextBuffer } from "./contextBuffer.js";
import { parseLine } from "./parseLine.js";
import { BUILD_FAILED_PATTERN, BUILD_SUCCESS_PATTERN, FAILED_TASK_PATTERN } from "./patterns.js";
import type { ParseOptions } from "../types/options.js";
import type { BuildIssue, BuildSummary } from "../types/result.js";

export interface ParseBuildInput {
  exitCode: number;
  shell: string;
  command: string;
  fullLog: string;
  durationMs: number;
  lines: string[];
}

export function parseBuildResult(input: ParseBuildInput, options: ParseOptions): BuildSummary {
  const context = new ContextBuffer(options.tail);
  const warnings: string[] = [];
  const errors: BuildIssue[] = [];
  const errorMessages = new Set<string>();
  let failedTask: string | null = null;
  let taskLinesSuppressed = 0;
  let warningsSuppressed = 0;
  let sawSuccess = false;
  let sawFailure = false;

  for (const rawLine of input.lines) {
    const parsed = parseLine(rawLine);
    context.add(parsed.clean);

    if (BUILD_SUCCESS_PATTERN.test(parsed.clean)) {
      sawSuccess = true;
    }

    if (BUILD_FAILED_PATTERN.test(parsed.clean)) {
      sawFailure = true;
    }

    const failedTaskMatch = FAILED_TASK_PATTERN.exec(parsed.clean);
    if (failedTaskMatch) {
      failedTask = failedTaskMatch[1] ?? failedTask;
    }

    if (parsed.isTaskNoise) {
      taskLinesSuppressed += 1;
      continue;
    }

    if (parsed.isWarning) {
      warningsSuppressed += 1;
      if (options.showWarnings) {
        warnings.push(parsed.clean);
      }
    }

    if (parsed.issue && !errorMessages.has(parsed.issue.message)) {
      errorMessages.add(parsed.issue.message);
      errors.push(parsed.issue);
    }
  }

  const normalizedErrors = errors.slice(0, options.maxErrorLines);
  const tail = input.exitCode === 0 ? [] : context.values();
  const status = input.exitCode === 0 && sawSuccess ? "success" : input.exitCode === 0 && !sawFailure ? "success" : "failed";
  const category = status === "success" ? "none" : classifyIssues(normalizedErrors, tail.join("\n"));

  return {
    status,
    exitCode: input.exitCode,
    category,
    failedTask,
    shell: input.shell,
    command: input.command,
    errors: normalizedErrors.map((issue) => ({
      ...issue,
      category: issue.category === "unknown-build-failure" ? category : issue.category,
    })),
    warnings,
    warningsSuppressed,
    taskLinesSuppressed,
    fullLog: input.fullLog,
    durationMs: input.durationMs,
    tail,
    suggestion: getSuggestedFix(category),
  };
}
