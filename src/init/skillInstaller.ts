import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import type { InitAgent, InitInstallResult, InitInstallTarget, InitOptions, InitTarget } from "../types/options.js";

const AGENTS: InitAgent[] = ["agent", "codex", "claude-code", "opencode"];

const GLOBAL_DIRS: Record<InitAgent, string[]> = {
  agent: [".agents", "skills"],
  codex: [".codex", "skills"],
  "claude-code": [".claude", "skills"],
  opencode: [".config", "opencode", "skills"],
};

const PROJECT_DIRS: Record<InitAgent, string[]> = {
  agent: [".agents", "skills"],
  codex: [".codex", "skills"],
  "claude-code": [".claude", "skills"],
  opencode: [".opencode", "skills"],
};

const PROJECT_MARKERS: Record<InitAgent, string[]> = {
  agent: [".agents"],
  codex: [".codex"],
  "claude-code": [".claude"],
  opencode: [".opencode"],
};

export function runInit(options: InitOptions): InitInstallResult {
  const sourceSkill = resolveBundledSkillPath();
  if (!fs.existsSync(sourceSkill)) {
    throw new Error(`Bundled skill not found at ${sourceSkill}`);
  }

  const targets = resolveInstallTargets(options);
  for (const target of targets) {
    fs.mkdirSync(target.directory, { recursive: true });
    fs.cpSync(sourceSkill, path.join(target.directory, "gradlew-tool"), {
      recursive: true,
      force: true,
    });
  }

  return {
    status: "installed",
    sourceSkill,
    installed: targets,
    requestedTargets: options.targets,
    message:
      targets.length > 0
        ? `Installed gradlew-tool skill to ${targets.length} location(s).`
        : "No matching agent skill directories were detected.",
  };
}

export function resolveInstallTargets(options: InitOptions): InitInstallTarget[] {
  const requested: InitTarget[] = options.targets.length > 0 ? options.targets : ["current", ...detectGlobalAgents()];
  const targets = new Map<string, InitInstallTarget>();

  for (const target of requested) {
    if (target === "current") {
      for (const resolved of detectProjectTargets(options.cwd)) {
        targets.set(getTargetKey(resolved), resolved);
      }
      continue;
    }

    const resolved: InitInstallTarget = {
      scope: "global",
      agent: target,
      directory: resolveGlobalSkillDir(target),
    };
    targets.set(getTargetKey(resolved), resolved);
  }

  if (targets.size === 0) {
    const fallback = getDefaultProjectTarget(options.cwd);
    targets.set(getTargetKey(fallback), fallback);
  }

  return [...targets.values()];
}

export function normalizeInitTargets(rawTargets: string[]): InitTarget[] {
  const normalized: InitTarget[] = [];

  for (let index = 0; index < rawTargets.length; index += 1) {
    const raw = rawTargets[index]?.trim().toLowerCase();
    if (!raw) {
      continue;
    }

    if (raw === "claude" && rawTargets[index + 1]?.trim().toLowerCase() === "code") {
      normalized.push("claude-code");
      index += 1;
      continue;
    }

    if (raw === "current" || raw === "project" || raw === "local") {
      normalized.push("current");
      continue;
    }

    if (raw === "claude-code" || raw === "claude_code" || raw === "claude code" || raw === "claudecode") {
      normalized.push("claude-code");
      continue;
    }

    if (raw === "agent" || raw === "codex" || raw === "opencode") {
      normalized.push(raw);
      continue;
    }

    throw new Error(`Unsupported init target: ${rawTargets[index]}`);
  }

  return [...new Set(normalized)];
}

export function detectGlobalAgents(homeDir = os.homedir()): InitAgent[] {
  return AGENTS.filter((agent) => fs.existsSync(resolveGlobalSkillDir(agent, homeDir)));
}

export function detectProjectTargets(cwd: string): InitInstallTarget[] {
  const detected = AGENTS.filter((agent) => {
    const markerPath = path.resolve(cwd, ...PROJECT_MARKERS[agent]);
    return fs.existsSync(markerPath);
  }).map<InitInstallTarget>((agent) => ({
    scope: "project",
    agent,
    directory: path.resolve(cwd, ...PROJECT_DIRS[agent]),
  }));

  if (detected.length > 0) {
    return detected;
  }

  return [getDefaultProjectTarget(cwd)];
}

export function getDefaultProjectTarget(cwd: string): InitInstallTarget {
  return {
    scope: "project",
    agent: "agent",
    directory: path.resolve(cwd, ...PROJECT_DIRS.agent),
  };
}

export function resolveGlobalSkillDir(agent: InitAgent, homeDir = os.homedir()): string {
  return path.resolve(homeDir, ...GLOBAL_DIRS[agent]);
}

export function resolveBundledSkillPath(baseDir = __dirname): string {
  const candidates = [
    path.resolve(baseDir, "..", "skills", "gradlew-tool"),
    path.resolve(baseDir, "..", "..", "skills", "gradlew-tool"),
  ];

  return candidates.find((candidate) => fs.existsSync(candidate)) ?? candidates[0];
}

function getTargetKey(target: InitInstallTarget): string {
  return `${target.scope}:${target.agent}:${target.directory}`;
}
