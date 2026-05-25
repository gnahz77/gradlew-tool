# gradlew-tool examples

## Basic forms

```bash
gradlew-tool build
gradlew-tool --cwd <project-dir> -- build
gradlew-tool --json --cwd <project-dir> -- test
gradlew-tool --dry-run --cwd <project-dir> -- assemble
```

## Explicit Gradle arguments

```bash
gradlew-tool --json --cwd <project-dir> -- :module:taskName --continue --stacktrace
```

## Output control

```bash
gradlew-tool --show-warnings --cwd <project-dir> -- build
gradlew-tool --tail 160 --cwd <project-dir> -- build
gradlew-tool --timeout 900000 --cwd <project-dir> -- build
gradlew-tool --full-output --cwd <project-dir> -- build
```

## Wrapper and cwd overrides

```bash
gradlew-tool --cwd <project-dir> --gradlew <path-to-gradlew-or-gradlew.bat> -- build
```

## Shell selection

```bash
gradlew-tool --shell powershell --cwd <project-dir> -- build
gradlew-tool --shell cmd --cwd <project-dir> -- build
gradlew-tool --shell bash --cwd <project-dir> -- build
gradlew-tool --shell pwsh --shell-exec <path-to-pwsh> --cwd <project-dir> -- build
```
