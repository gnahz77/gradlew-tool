import type { ErrorCategory } from "./result.js";

export type ShellMode = "direct" | "powershell" | "pwsh" | "cmd" | "bash" | "sh";

export interface CliOptions {
  json: boolean;
  showWarnings: boolean;
  tail: number;
  fullOutput: boolean;
  logDir: string;
  gradlew?: string;
  cwd: string;
  maxErrorLines: number;
  addDefaultGradleFlags: boolean;
  shell: ShellMode;
  shellExec?: string;
  shellArgs: string[];
  dryRun: boolean;
  help: boolean;
  version: boolean;
  gradleArgs: string[];
}

export interface ParseOptions {
  maxErrorLines: number;
  showWarnings: boolean;
  tail: number;
}

export interface WrapperResolution {
  found: boolean;
  path: string;
  category: ErrorCategory;
}
