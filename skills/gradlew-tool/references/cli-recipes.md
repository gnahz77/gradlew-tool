# gradlew-tool CLI recipes

Use these as defaults and adapt the Gradle task list as needed.

## Compact JSON runs

Assemble:

```bash
gradlew-tool --json --cwd android-demo -- :app:assembleDebug
```

Unit tests:

```bash
gradlew-tool --json --cwd android-demo -- :app:testDebugUnitTest
```

Lint:

```bash
gradlew-tool --json --cwd android-demo -- :app:lintDebug
```

Generic build:

```bash
gradlew-tool --json --cwd <project-dir> -- build
```

## Safer argument passing

Use `--` when mixing tool flags and Gradle flags:

```bash
gradlew-tool --json --cwd <project-dir> -- :app:assembleDebug --continue --stacktrace
```

## When compact output is not enough

Show warnings:

```bash
gradlew-tool --json --show-warnings --cwd <project-dir> -- :app:assembleDebug
```

Show a longer filtered tail:

```bash
gradlew-tool --json --tail 160 --cwd <project-dir> -- :app:assembleDebug
```

Show the raw Gradle stream including task lines:

```bash
gradlew-tool --full-output --cwd <project-dir> -- :app:assembleDebug
```

## Wrapper and working directory overrides

Use an explicit wrapper path:

```bash
gradlew-tool --json --cwd <project-dir> --gradlew <path-to-gradlew-or-gradlew.bat> -- :app:assembleDebug
```

Preview the final command without running it:

```bash
gradlew-tool --dry-run --cwd <project-dir> -- :app:assembleDebug
```

## Shell examples

PowerShell:

```bash
gradlew-tool --json --shell powershell --cwd <project-dir> -- :app:assembleDebug
```

cmd:

```bash
gradlew-tool --json --shell cmd --cwd <project-dir> -- :app:assembleDebug
```

bash:

```bash
gradlew-tool --json --shell bash --cwd <project-dir> -- :app:assembleDebug
```

## Result-reading checklist

After each run, inspect fields in this order:

1. `status`
2. `exitCode`
3. `category`
4. `failedTask`
5. `errors`
6. `fullLog`

Only inspect `LOG_TAIL` or rerun with more verbosity when those fields do not already identify the next action.
