export type BuildStatus = "success" | "failed" | "timed-out" | "wrapper-not-found" | "dry-run";

export type ErrorCategory =
  | "none"
  | "gradle-cache-lock"
  | "dependency-resolution"
  | "kotlin-compile-error"
  | "java-compile-error"
  | "android-resource-error"
  | "manifest-merge-error"
  | "aapt-error"
  | "lint-error"
  | "test-failure"
  | "sdk-environment-error"
  | "permission-error"
  | "process-timeout"
  | "gradle-wrapper-not-found"
  | "unknown-build-failure";

export interface BuildIssue {
  severity: "error" | "warning";
  category: ErrorCategory;
  message: string;
  file?: string;
  line?: number;
  column?: number;
}

export interface BuildSummary {
  status: BuildStatus;
  exitCode: number;
  category: ErrorCategory;
  failedTask: string | null;
  shell: string;
  command: string;
  errors: BuildIssue[];
  warnings: string[];
  warningsSuppressed: number;
  taskLinesSuppressed: number;
  fullLog: string;
  durationMs: number;
  tail: string[];
  suggestion?: string;
}
