# gradlew-tool parameters

## Core options

`--json`

- Emit structured JSON instead of compact text

`--show-warnings`

- Include warning details in summarized output
- Default text mode otherwise suppresses warning detail

`--tail <n>`

- Set the number of tail lines retained for failed runs
- Default is `80`
- Tail output is filtered to remove `> Task ...` lines unless `--full-output` is used

`--full-output`

- Stream raw Gradle output directly
- Preserve task lines and all other log lines
- Still save the full log to disk

`--log-dir <dir>`

- Override the log output directory
- Default is `.agent-build`

`--gradlew <path>`

- Use an explicit wrapper path
- Overrides automatic wrapper resolution

`--cwd <path>`

- Set the working directory used for wrapper resolution and process execution

`--max-error-lines <n>`

- Limit the number of error lines included in the parsed summary
- Default is `160`

`--no-default-gradle-flags`

- Disable automatic addition of:
  - `--console=plain`
  - `--warning-mode=summary`

`--dry-run`

- Print the resolved command without executing it

## Shell options

`--shell <direct|powershell|pwsh|cmd|bash|sh>`

- Select the execution mode
- Default is `direct`

`--shell-exec <path>`

- Override the executable used for the selected shell
- Example values: `powershell.exe`, `pwsh`, `cmd.exe`, `bash`

`--shell-arg <arg>`

- Add an extra shell argument
- May be repeated

## Metadata options

`--help`

- Print help text

`--version`

- Print the CLI version

## Pass-through behavior

Unknown arguments are forwarded to Gradle.

Use `--` when mixing tool options and Gradle options:

```bash
gradlew-tool --json --cwd <project-dir> -- build --continue --stacktrace
```
