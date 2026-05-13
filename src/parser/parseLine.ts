import { FILE_LOCATION_PATTERN, TASK_LINE_PATTERN, WARNING_LINE_PATTERN } from "./patterns.js";
import { classifyText } from "./classifiers.js";
import type { BuildIssue } from "../types/result.js";
import { stripAnsi } from "../utils/ansi.js";

export interface ParsedLine {
  clean: string;
  isTaskNoise: boolean;
  isWarning: boolean;
  issue?: BuildIssue;
}

export function parseLine(line: string): ParsedLine {
  const clean = stripAnsi(line).trimEnd();
  const isTaskNoise = TASK_LINE_PATTERN.test(clean);
  const isWarning = WARNING_LINE_PATTERN.test(clean) || clean.startsWith("Warning:");
  const issue = parseIssue(clean);

  return { clean, isTaskNoise, isWarning, issue };
}

function parseIssue(line: string): BuildIssue | undefined {
  if (!line) {
    return undefined;
  }

  const looksLikeError =
    /^e:\s/i.test(line) ||
    /^error[:\s]/i.test(line) ||
    /\bFAILURE: Build failed/i.test(line) ||
    /\bFAILED\b/.test(line) ||
    /Exception|Caused by:|Manifest merger failed|resource .* not found|AAPT/i.test(line);

  if (!looksLikeError) {
    return undefined;
  }

  const match = FILE_LOCATION_PATTERN.exec(line);
  const file = match?.groups?.file;
  const lineNumber = match?.groups?.line ? Number(match.groups.line) : undefined;
  const columnNumber = match?.groups?.column ? Number(match.groups.column) : undefined;

  return {
    severity: "error",
    category: classifyText(line),
    message: line,
    file,
    line: Number.isFinite(lineNumber) ? lineNumber : undefined,
    column: Number.isFinite(columnNumber) ? columnNumber : undefined,
  };
}
