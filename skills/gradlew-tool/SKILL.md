---
name: gradlew-tool
description: Run Android and Gradle Wrapper builds in an AI-agent-friendly way with compact output, saved full logs, and structured failure summaries. Use when working on Android or Gradle projects that contain gradlew or gradlew.bat and the task involves build, assemble, test, lint, or wrapper-based verification, especially when raw Gradle task output would waste context.
license: MIT
---

# gradlew-tool

Use `gradlew-tool` instead of calling `./gradlew` or `gradlew.bat` directly when the goal is to keep agent context small while preserving actionable build diagnostics.

Assume a project-local `gradlew` or `gradlew.bat` exists. Use either an installed `gradlew-tool` binary or this repository's built `dist/cli.js`. Expect Node.js 18+ and shell access. Network access may still be required if Gradle Wrapper needs to download its distribution.

## Default workflow

1. Resolve the project directory that contains `gradlew` or `gradlew.bat`.
2. Prefer JSON output so the result is easy to inspect programmatically.
3. Pass Gradle arguments through `--` when there is any chance of conflict with tool flags.
4. Read the structured result first:
   - `status`
   - `exitCode`
   - `category`
   - `failedTask`
   - `errors`
   - `fullLog`
5. Escalate output only if the compact result is insufficient.

Default command pattern:

```bash
gradlew-tool --json --cwd <project-dir> -- <gradle-args...>
```

## Output policy

Treat the compact output as the primary interface.

- Use the default mode first. It suppresses noisy `> Task ...` lines while preserving error lines and key failure context.
- Use `--show-warnings` when warnings may explain a failure or future regression.
- Use `--tail <n>` when the default tail is too short.
- Use `--full-output` only when you explicitly need the raw Gradle stream, including task-by-task detail.
- Use the saved `fullLog` file for deep inspection rather than rerunning with noisy output unless the task specifically needs live raw output.

## Execution guidance

- Prefer wrapper tasks such as `assembleDebug`, `testDebugUnitTest`, `lint`, or fully qualified tasks like `:app:assembleDebug`.
- Let `gradlew-tool` locate the wrapper automatically unless the workspace layout is unusual. In unusual layouts, pass `--gradlew <path>`.
- Do not replace a missing wrapper with system `gradle`. `gradlew-tool` intentionally reports `gradle-wrapper-not-found`.
- Keep the tool's default Gradle flags unless you have a specific reason to change console or warning behavior.
- Use `--dry-run` to verify wrapper resolution, shell mode, and final command before execution.

## Failure-handling sequence

1. Run the target task with `--json`.
2. Inspect `category`, `failedTask`, and `errors`.
3. If the error is clear, fix the issue and rerun in compact mode.
4. If the compact result is insufficient, inspect `fullLog`.
5. Only then rerun with one of:
   - `--show-warnings`
   - `--tail <n>`
   - `--full-output`

Use this progression to avoid flooding the context window.

## Shell selection

Prefer the default `--shell direct`.

Use an explicit shell only when the environment requires it:

- `--shell powershell` or `--shell pwsh` for Windows PowerShell flows
- `--shell cmd` for explicit `cmd.exe` execution
- `--shell bash` or `--shell sh` for POSIX shell execution

Add `--shell-exec` or repeated `--shell-arg` only when the environment has non-default shell locations or startup requirements.

## Common categories

Interpret these categories as first-pass routing hints:

- `kotlin-compile-error`, `java-compile-error`: open the cited source file and fix compiler errors first.
- `android-resource-error`, `aapt-error`, `manifest-merge-error`: inspect Android resources, manifests, and generated merge context.
- `dependency-resolution`: inspect repositories, coordinates, credentials, and network access.
- `sdk-environment-error`: inspect Android SDK configuration such as `local.properties`, `ANDROID_HOME`, or `ANDROID_SDK_ROOT`.
- `gradle-cache-lock`: stop competing Gradle processes and retry.
- `test-failure`: inspect the failing test and rerun the relevant test task if needed.
- `permission-error`: inspect filesystem permissions, antivirus interference, or blocked network/download operations.

## References

- Read [references/cli-recipes.md](references/cli-recipes.md) for common command patterns.
