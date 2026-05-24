import type { BuildSummary } from "../types/result.js";

export function formatTextResult(result: BuildSummary): string {
  const lines: string[] = [
    `BUILD_STATUS: ${result.status.toUpperCase()}`,
    `EXIT_CODE: ${result.exitCode}`,
    `CATEGORY: ${result.category}`,
    `SHELL: ${result.shell}`,
    `COMMAND: ${result.command}`,
    `FULL_LOG: ${result.fullLog}`,
  ];

  if (result.failedTask) {
    lines.push(`FAILED_TASK: ${result.failedTask}`);
  }

  if (result.taskLinesSuppressed > 0) {
    lines.push(`TASK_LINES_SUPPRESSED: ${result.taskLinesSuppressed}`);
  }

  if (result.errors.length > 0) {
    lines.push("");
    lines.push("ERROR_SUMMARY:");
    lines.push(...result.errors.map((issue) => issue.message));
  }

  if (result.warnings.length > 0) {
    lines.push("");
    lines.push("WARNINGS:");
    lines.push(...result.warnings);
  }

  if (result.suggestion) {
    lines.push("");
    lines.push("SUGGESTED_FIX:");
    lines.push(result.suggestion);
  }

  if (result.status === "failed" && result.tail.length > 0) {
    lines.push("");
    lines.push("LOG_TAIL:");
    lines.push(...result.tail);
  }

  return lines.join("\n");
}
