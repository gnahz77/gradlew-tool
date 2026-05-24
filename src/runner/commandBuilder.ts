import path from "node:path";
import type { BuiltCommand, CommandBuildOptions } from "../types/command.js";
import { quoteForCmd, quoteForDisplay, quoteForPosixShell, quoteForPowerShell } from "../utils/quote.js";

function toDisplayArg(value: string): string {
  return value;
}

function normalizeGradlewPath(cwd: string, gradlewPath: string, shell: CommandBuildOptions["shell"]): string {
  const relative = path.relative(cwd, gradlewPath);
  if (relative && !relative.startsWith("..")) {
    const normalizedRelative = normalizeRelativePath(relative, shell);
    if (!relative.startsWith(".")) {
      return getRelativePrefix(shell) + normalizedRelative;
    }

    return normalizedRelative;
  }

  return normalizeAbsolutePath(gradlewPath, shell);
}

function normalizeRelativePath(relative: string, shell: CommandBuildOptions["shell"]): string {
  if (shell === "bash" || shell === "sh") {
    return relative.replace(/\\/g, "/");
  }

  if (shell === "cmd" || shell === "powershell" || shell === "pwsh") {
    return relative.replace(/\//g, "\\");
  }

  return relative;
}

function normalizeAbsolutePath(gradlewPath: string, shell: CommandBuildOptions["shell"]): string {
  if (shell === "bash" || shell === "sh") {
    return gradlewPath.replace(/\\/g, "/");
  }

  if (shell === "cmd" || shell === "powershell" || shell === "pwsh") {
    return gradlewPath.replace(/\//g, "\\");
  }

  return gradlewPath;
}

function getRelativePrefix(shell: CommandBuildOptions["shell"]): string {
  if (shell === "bash" || shell === "sh") {
    return "./";
  }

  if (shell === "cmd" || shell === "powershell" || shell === "pwsh") {
    return ".\\";
  }

  return `.${path.sep}`;
}

export function buildCommand(options: CommandBuildOptions): BuiltCommand {
  const gradlewPath = normalizeGradlewPath(options.cwd, options.gradlewPath, options.shell);
  const invocation = [gradlewPath, ...options.gradleArgs];

  if (options.shell === "direct") {
    return {
      command: options.gradlewPath,
      args: options.gradleArgs,
      displayCommand: quoteForDisplay(invocation),
      usesShell: false,
    };
  }

  const shellArgs = [...getDefaultShellArgs(options.shell), ...options.shellArgs];
  const commandString = buildCommandString(options.shell, invocation);
  const command = options.shellExec ?? getDefaultShellExec(options.shell);
  const args = [...shellArgs, commandString];

  return {
    command,
    args,
    displayCommand: quoteForDisplay([command, ...args.map(toDisplayArg)]),
    usesShell: true,
  };
}

function buildCommandString(shell: CommandBuildOptions["shell"], invocation: string[]): string {
  switch (shell) {
    case "powershell":
    case "pwsh":
      return quoteForPowerShell(invocation);
    case "cmd":
      return quoteForCmd(invocation);
    case "bash":
    case "sh":
      return quoteForPosixShell(invocation);
    default:
      return quoteForDisplay(invocation);
  }
}

function getDefaultShellExec(shell: CommandBuildOptions["shell"]): string {
  switch (shell) {
    case "powershell":
      return "powershell.exe";
    case "pwsh":
      return "pwsh";
    case "cmd":
      return "cmd.exe";
    case "bash":
      return "bash";
    case "sh":
      return "sh";
    default:
      return "";
  }
}

function getDefaultShellArgs(shell: CommandBuildOptions["shell"]): string[] {
  switch (shell) {
    case "powershell":
      return ["-NoProfile", "-ExecutionPolicy", "Bypass", "-Command"];
    case "pwsh":
      return ["-NoProfile", "-Command"];
    case "cmd":
      return ["/d", "/s", "/c"];
    case "bash":
    case "sh":
      return ["-lc"];
    default:
      return [];
  }
}
