# gradlew-tool

[中文文档](README.zh.md)

`gradlew-tool` is an AI-friendly Gradle Wrapper CLI for Android and Gradle projects. It runs the local `gradlew` or `gradlew.bat`, saves the full build log, filters noisy task output, and returns a compact result focused on success/failure and actionable errors.

## Install

```bash
npm install -g gradlew-tool
```

Or for local development:

```bash
npm install
npm run build
npm link
```

Then you can run:

```bash
gradlew-tool :app:assembleDebug
```

Install the bundled skill into detected agent skill directories:

```bash
gradlew-tool init
gradlew-tool init codex
gradlew-tool init "claude code"
gradlew-tool init current
```

## Usage

Basic usage:

```bash
gradlew-tool build
gradlew-tool :app:assembleDebug
gradlew-tool test --continue
gradlew-tool --json -- :app:assembleDebug --continue
gradlew-tool init
```

Unknown arguments are forwarded to Gradle by default. You can also use `--` to separate tool options from Gradle arguments explicitly.

By default, `gradlew-tool` appends:

```text
--console=plain
--warning-mode=summary
```

unless you already supplied them or you pass `--no-default-gradle-flags`.

## Output behavior

Default text output is compact and AI-agent friendly:

- Build status
- Exit code
- Error category
- Failed task
- Log path
- Error summary
- Optional warning summary
- Optional log tail

By default, `gradlew-tool` times out the Gradle process after `600000` ms (10 minutes). Use `--timeout <ms>` to override it.

The full build log is always saved to:

```text
.agent-build/gradle-YYYYMMDD-HHmmss.log
```

## JSON output

Use `--json` for structured output:

```bash
gradlew-tool --json -- :app:assembleDebug
```

Example:

```json
{
  "status": "failed",
  "exitCode": 1,
  "category": "kotlin-compile-error",
  "failedTask": ":app:compileDebugKotlin",
  "shell": "direct",
  "command": "gradlew.bat :app:assembleDebug --console=plain --warning-mode=summary",
  "errors": [
    {
      "severity": "error",
      "category": "kotlin-compile-error",
      "message": "Unresolved reference: foo",
      "file": "app/src/main/java/com/example/MainActivity.kt",
      "line": 12,
      "column": 5
    }
  ],
  "warningsSuppressed": 18,
  "taskLinesSuppressed": 241,
  "fullLog": ".agent-build/gradle-20260513-153012.log",
  "durationMs": 64000
}
```

## CLI options

```text
init [agent|codex|"claude code"|opencode|current|project|local...]
--json
--show-warnings
--tail <n>
--timeout <ms>
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
--version
```

## Skill installation

`gradlew-tool init` installs the bundled `gradlew-tool` skill for supported agents.

Supported agent targets:

- `agent`
- `codex`
- `claude code`
- `opencode`
- `current` / `project` / `local`

Global skill directories:

- `agent`: `~/.agents/skills/`
- `codex`: `~/.codex/skills/`
- `claude code`: `~/.claude/skills/`
- `opencode`: `~/.config/opencode/skills/`

Project skill directories:

- `agent`: `.agents/skills/`
- `codex`: `.codex/skills/`
- `claude code`: `.claude/skills/`
- `opencode`: `.opencode/skills/`

Rules:

- `gradlew-tool init` with no target installs to the current project plus any detected global agent skill directories.
- `gradlew-tool init current` installs into the current project directory.
- If the current project does not contain any of `.codex`, `.claude`, `.opencode`, or `.agents`, project installation defaults to `.agents/skills/`.
- Passing an explicit agent name installs to that agent's global skill directory.

## Shell execution modes

Default mode is `direct`, which executes the Gradle Wrapper directly with `spawn`.

Examples:

```bash
gradlew-tool --shell powershell :app:assembleDebug
gradlew-tool --shell pwsh :app:assembleDebug
gradlew-tool --shell cmd :app:assembleDebug
gradlew-tool --shell bash :app:assembleDebug
gradlew-tool --shell sh :app:assembleDebug
```

Custom shell executable:

```bash
gradlew-tool --shell pwsh --shell-exec /usr/local/bin/pwsh :app:testDebugUnitTest
```

Additional shell args can be repeated:

```bash
gradlew-tool --shell bash --shell-arg -O --shell-arg extglob :app:assembleDebug
```

`--dry-run` prints the resolved command without running it.

## Wrapper resolution

`gradlew-tool` only runs the project wrapper and never falls back to system `gradle`.

Resolution order:

1. `--gradlew <path>`
2. On Windows, `<cwd>/gradlew.bat`
3. `<cwd>/gradlew`
4. Otherwise return `gradle-wrapper-not-found`

## AI Agent recommendation

For AI agents, the most useful default pattern is:

```bash
gradlew-tool --json --cwd android-demo -- :app:assembleDebug
```

This keeps terminal context small while preserving the full log on disk for follow-up inspection. When debugging a stubborn failure, add one of:

```bash
gradlew-tool --show-warnings --cwd android-demo -- :app:assembleDebug
gradlew-tool --tail 120 --cwd android-demo -- :app:assembleDebug
gradlew-tool --timeout 900000 --cwd android-demo -- :app:assembleDebug
gradlew-tool --full-output --cwd android-demo -- :app:assembleDebug
```
