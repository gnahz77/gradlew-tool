import type { BuildSummary } from "../types/result.js";

export function formatJsonResult(result: BuildSummary): string {
  return JSON.stringify(result, null, 2);
}
