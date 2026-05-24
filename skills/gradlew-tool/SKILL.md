---
name: gradlew-tool
description: Use the gradlew-tool command instead of executing gradlew directly. Explain how to use the gradlew-tool command, including invocation format, wrapper resolution, output modes, and CLI parameters.
license: MIT
---

# gradlew-tool

Use this skill as a command reference for `gradlew-tool`.

`gradlew-tool` is a wrapper around a project-local `gradlew` or `gradlew.bat`. It forwards unknown arguments to Gradle, saves the full log to disk, and prints a compact summary by default.

## Command forms

Use either form:

```bash
gradlew-tool [tool-options] -- [gradle-args...]
gradlew-tool [tool-options] [gradle-args...]
```

Prefer the first form when there is any chance that a Gradle argument could be mistaken for a tool option.

Examples:

```bash
gradlew-tool build
gradlew-tool --json --cwd <project-dir> -- build
gradlew-tool --shell cmd :app:assembleDebug
```

## Wrapper resolution

`gradlew-tool` only runs a project-local wrapper. It does not fall back to system `gradle`.

Resolution order:

1. `--gradlew <path>`
2. On Windows, `<cwd>/gradlew.bat`
3. `<cwd>/gradlew`
4. Otherwise return `gradle-wrapper-not-found`

## Default behavior

- Forward unknown arguments to Gradle
- Append `--console=plain` unless already provided
- Append `--warning-mode=summary` unless already provided
- Save the full log to `.agent-build/gradle-YYYYMMDD-HHmmss.log`
- Preserve the Gradle process exit code
- Filter noisy `> Task ...` lines from default summarized output
- Omit warning details from default text output unless `--show-warnings` is set

Use `--no-default-gradle-flags` to prevent automatic addition of `--console=plain` and `--warning-mode=summary`.

## Output modes

### Default text output

Default text output is compact. It includes high-signal fields such as:

- `BUILD_STATUS`
- `EXIT_CODE`
- `CATEGORY`
- `SHELL`
- `COMMAND`
- `FULL_LOG`
- `FAILED_TASK` when available
- `TASK_LINES_SUPPRESSED` when applicable
- `ERROR_SUMMARY` when errors are detected
- `SUGGESTED_FIX` when available
- `LOG_TAIL` for failed runs

By default, `LOG_TAIL` is filtered to remove `> Task ...` lines.

### JSON output

Use `--json` for structured output.

Important JSON fields:

- `status`
- `exitCode`
- `category`
- `failedTask`
- `shell`
- `command`
- `errors`
- `warnings`
- `warningsSuppressed`
- `taskLinesSuppressed`
- `fullLog`
- `durationMs`
- `tail`
- `suggestion`

Example:

```bash
gradlew-tool --json build
```

## CLI parameters

Read [references/parameters.md](references/parameters.md) for the full parameter reference.

Most commonly used parameters:

- `--json`
- `--show-warnings`
- `--tail <n>`
- `--full-output`
- `--log-dir <dir>`
- `--gradlew <path>`
- `--cwd <path>`
- `--max-error-lines <n>`
- `--no-default-gradle-flags`
- `--shell <direct|powershell|pwsh|cmd|bash|sh>`
- `--shell-exec <path>`
- `--shell-arg <arg>`
- `--dry-run`
- `--help`
- `--version`

## Shell handling

Default shell mode is `direct`.

Available shell modes:

- `direct`
- `powershell`
- `pwsh`
- `cmd`
- `bash`
- `sh`

Use `--shell-exec <path>` to override the executable used for the selected shell.

Use repeated `--shell-arg <arg>` values to insert additional shell arguments before the generated command string.

## Usage notes

- Use `--json` when a structured result is easier to consume.
- Use `--full-output` when raw Gradle output, including task lines, is required.
- Use `--show-warnings` when warning details are required in summarized output.
- Use `--tail <n>` to increase the number of filtered tail lines shown for failed runs.
- Use `--dry-run` to inspect the final command without executing it.

## References

- Read [references/parameters.md](references/parameters.md) for option-by-option behavior.
- Read [references/examples.md](references/examples.md) for concise command examples.
