# gradlew-tool

[English README](README.md)

`gradlew-tool` 是一个面向 AI Agent 场景的 Gradle Wrapper CLI，适用于 Android 和 Gradle 项目。它会执行项目本地的 `gradlew` 或 `gradlew.bat`，保存完整构建日志，过滤大量 Gradle task 噪音，并返回聚焦于构建结果和关键错误信息的紧凑输出。

## 安装

全局安装：

```bash
npm install -g gradlew-tool
```

本地开发：

```bash
npm install
npm run build
npm link
```

安装后可直接执行：

```bash
gradlew-tool :app:assembleDebug
```

也可以安装项目内置的 skill 到已检测到的智能体 skill 目录：

```bash
gradlew-tool init
gradlew-tool init codex
gradlew-tool init "claude code"
gradlew-tool init current
```

## 用法

基础用法：

```bash
gradlew-tool build
gradlew-tool :app:assembleDebug
gradlew-tool test --continue
gradlew-tool --json -- :app:assembleDebug --continue
gradlew-tool init
```

默认情况下，无法识别的参数都会透传给 Gradle。为了避免和工具自身参数冲突，建议在需要时使用 `--` 分隔：

```bash
gradlew-tool --json -- :app:assembleDebug --continue
```

默认会自动追加：

```text
--console=plain
--warning-mode=summary
```

如果你已经手动传入，或者指定了 `--no-default-gradle-flags`，则不会重复追加。

## 输出行为

默认文本输出是为 AI Agent 优化的紧凑格式，主要包含：

- 构建状态
- 退出码
- 错误分类
- 失败任务
- 日志路径
- 错误摘要
- 可选的 warning 摘要
- 可选的日志尾部摘要

默认情况下，`gradlew-tool` 会在 `600000` 毫秒（10 分钟）后终止仍未结束的 Gradle 进程。可通过 `--timeout <ms>` 覆盖。

完整日志始终会保存到：

```text
.agent-build/gradle-YYYYMMDD-HHmmss.log
```

默认失败输出会过滤掉 `> Task ...` 行，尽量保留错误行和必要上下文。如果需要完整 task 明细，请使用 `--full-output`。

## JSON 输出

使用 `--json` 可以得到结构化结果，更适合 AI Agent 或脚本消费：

```bash
gradlew-tool --json -- :app:assembleDebug
```

示例：

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

## CLI 参数

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

## Skill 安装

`gradlew-tool init` 用于安装内置的 `gradlew-tool` skill。

支持的目标智能体：

- `agent`
- `codex`
- `claude code`
- `opencode`
- `current` / `project` / `local`

全局 skill 目录：

- `agent`: `~/.agents/skills/`
- `codex`: `~/.codex/skills/`
- `claude code`: `~/.claude/skills/`
- `opencode`: `~/.config/opencode/skills/`

项目 skill 目录：

- `agent`: `.agents/skills/`
- `codex`: `.codex/skills/`
- `claude code`: `.claude/skills/`
- `opencode`: `.opencode/skills/`

规则：

- `gradlew-tool init` 未指定目标时，会安装到当前项目检测到的本地 skill 目录，并同时安装到已检测到的全局智能体 skill 目录。
- `gradlew-tool init current` 会安装到当前运行目录。
- 如果当前项目没有 `.codex`、`.claude`、`.opencode`、`.agents` 目录，则项目内默认安装到 `.agents/skills/`。
- 显式传入 `agent`、`codex`、`claude code`、`opencode` 时，会安装到对应智能体的全局 skill 目录。

## Shell 执行模式

默认模式是 `direct`，即直接执行 Gradle Wrapper。

示例：

```bash
gradlew-tool --shell powershell :app:assembleDebug
gradlew-tool --shell pwsh :app:assembleDebug
gradlew-tool --shell cmd :app:assembleDebug
gradlew-tool --shell bash :app:assembleDebug
gradlew-tool --shell sh :app:assembleDebug
```

指定自定义 shell 可执行文件：

```bash
gradlew-tool --shell pwsh --shell-exec /usr/local/bin/pwsh :app:testDebugUnitTest
```

重复追加 shell 参数：

```bash
gradlew-tool --shell bash --shell-arg -O --shell-arg extglob :app:assembleDebug
```

`--dry-run` 会打印最终执行命令，但不真正执行。

## Gradle Wrapper 查找规则

`gradlew-tool` 只会执行项目自带的 wrapper，不会回退到系统 `gradle`。

查找顺序：

1. `--gradlew <path>`
2. Windows 下优先使用 `<cwd>/gradlew.bat`
3. `<cwd>/gradlew`
4. 如果都不存在，则返回 `gradle-wrapper-not-found`

## AI Agent 推荐用法

对于 AI Agent，推荐优先使用：

```bash
gradlew-tool --json --cwd android-demo -- :app:assembleDebug
```

这样可以把终端上下文压缩到最小，同时保留完整日志供后续分析。如果需要更多细节，可以逐步升级输出：

```bash
gradlew-tool --show-warnings --cwd android-demo -- :app:assembleDebug
gradlew-tool --tail 120 --cwd android-demo -- :app:assembleDebug
gradlew-tool --timeout 900000 --cwd android-demo -- :app:assembleDebug
gradlew-tool --full-output --cwd android-demo -- :app:assembleDebug
```

推荐排查顺序：

1. 先看 `status`、`category`、`failedTask`、`errors`
2. 不够时查看 `fullLog`
3. 仍不够时再使用 `--show-warnings`、`--tail` 或 `--full-output`
