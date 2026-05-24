export interface BuiltCommand {
  command: string;
  args: string[];
  displayCommand: string;
  usesShell: boolean;
}

export interface CommandBuildOptions {
  cwd: string;
  gradlewPath: string;
  gradleArgs: string[];
  shell: "direct" | "powershell" | "pwsh" | "cmd" | "bash" | "sh";
  shellExec?: string;
  shellArgs: string[];
}
