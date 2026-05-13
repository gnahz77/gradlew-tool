import path from "node:path";
import { formatJsonResult } from "./reporter/jsonReporter.js";
import { formatTextResult } from "./reporter/textReporter.js";
import { runGradle } from "./runner/gradleRunner.js";
import type { CliOptions, ShellMode } from "./types/options.js";

export const VERSION = "0.1.0";
const SHELL_MODES = new Set<ShellMode>(["direct", "powershell", "pwsh", "cmd", "bash", "sh"]);

export async function main(argv = process.argv.slice(2)): Promise<number> {
  const options = parseCliArgs(argv);

  if (options.help) {
    process.stdout.write(`${getHelpText()}\n`);
    return 0;
  }

  if (options.version) {
    process.stdout.write(`${VERSION}\n`);
    return 0;
  }

  const result = await runGradle(options);
  const output = options.json ? formatJsonResult(result) : formatTextResult(result);
  process.stdout.write(`${output}\n`);

  if (result.status === "wrapper-not-found") {
    return 1;
  }

  return result.exitCode;
}

export function parseCliArgs(argv: string[]): CliOptions {
  const options: CliOptions = {
    json: false,
    showWarnings: false,
    tail: 80,
    fullOutput: false,
    logDir: ".agent-build",
    cwd: process.cwd(),
    maxErrorLines: 160,
    addDefaultGradleFlags: true,
    shell: "direct",
    shellArgs: [],
    dryRun: false,
    help: false,
    version: false,
    gradleArgs: [],
  };

  let passThrough = false;

  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index];

    if (passThrough) {
      options.gradleArgs.push(arg);
      continue;
    }

    if (arg === "--") {
      passThrough = true;
      continue;
    }

    switch (arg) {
      case "--json":
        options.json = true;
        continue;
      case "--show-warnings":
        options.showWarnings = true;
        continue;
      case "--full-output":
        options.fullOutput = true;
        continue;
      case "--dry-run":
        options.dryRun = true;
        continue;
      case "--no-default-gradle-flags":
        options.addDefaultGradleFlags = false;
        continue;
      case "--help":
      case "-h":
        options.help = true;
        continue;
      case "--version":
      case "-v":
        options.version = true;
        continue;
      case "--tail":
        options.tail = readNumberArg(argv, ++index, "--tail");
        continue;
      case "--log-dir":
        options.logDir = readStringArg(argv, ++index, "--log-dir");
        continue;
      case "--gradlew":
        options.gradlew = readStringArg(argv, ++index, "--gradlew");
        continue;
      case "--cwd":
        options.cwd = path.resolve(readStringArg(argv, ++index, "--cwd"));
        continue;
      case "--max-error-lines":
        options.maxErrorLines = readNumberArg(argv, ++index, "--max-error-lines");
        continue;
      case "--shell": {
        const shell = readStringArg(argv, ++index, "--shell") as ShellMode;
        if (!SHELL_MODES.has(shell)) {
          throw new Error(`Unsupported shell mode: ${shell}`);
        }
        options.shell = shell;
        continue;
      }
      case "--shell-exec":
        options.shellExec = readStringArg(argv, ++index, "--shell-exec");
        continue;
      case "--shell-arg":
        options.shellArgs.push(readStringArg(argv, ++index, "--shell-arg"));
        continue;
      default:
        options.gradleArgs.push(arg);
    }
  }

  return options;
}

function readStringArg(argv: string[], index: number, optionName: string): string {
  const value = argv[index];
  if (!value) {
    throw new Error(`Missing value for ${optionName}`);
  }

  return value;
}

function readNumberArg(argv: string[], index: number, optionName: string): number {
  const value = Number(readStringArg(argv, index, optionName));
  if (!Number.isFinite(value) || value < 0) {
    throw new Error(`Invalid numeric value for ${optionName}`);
  }

  return value;
}

function getHelpText(): string {
  return `gradlew-tool [options] -- [gradleArgs...]

Options:
  --json
  --show-warnings
  --tail <n>
  --full-output
  --log-dir <dir>
  --gradlew <path>
  --cwd <path>
  --max-error-lines <n>
  --no-default-gradle-flags
  --shell <direct|powershell|pwsh|cmd|bash|sh>
  --shell-exec <path>
  --shell-arg <arg>
  --dry-run
  --help
  --version`;
}
