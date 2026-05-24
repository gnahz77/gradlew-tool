import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { afterEach, describe, expect, it } from "vitest";
import {
  detectGlobalAgents,
  detectProjectTargets,
  getDefaultProjectTarget,
  normalizeInitTargets,
  resolveGlobalSkillDir,
  resolveInstallTargets,
} from "../src/init/skillInstaller.js";

const tempDirs: string[] = [];

afterEach(() => {
  while (tempDirs.length > 0) {
    fs.rmSync(tempDirs.pop()!, { recursive: true, force: true });
  }
});

function createTempDir(): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "gradlew-tool-init-"));
  tempDirs.push(dir);
  return dir;
}

describe("normalizeInitTargets", () => {
  it("normalizes agent names and aliases", () => {
    expect(normalizeInitTargets(["agent", "project", "claude code", "opencode"])).toEqual([
      "agent",
      "current",
      "claude-code",
      "opencode",
    ]);
    expect(normalizeInitTargets(["agent", "project", "claude", "code", "opencode"])).toEqual([
      "agent",
      "current",
      "claude-code",
      "opencode",
    ]);
  });

  it("throws on unsupported targets", () => {
    expect(() => normalizeInitTargets(["unknown"])).toThrow("Unsupported init target");
  });
});

describe("detectGlobalAgents", () => {
  it("detects existing global skill directories", () => {
    const homeDir = createTempDir();
    fs.mkdirSync(resolveGlobalSkillDir("codex", homeDir), { recursive: true });
    fs.mkdirSync(resolveGlobalSkillDir("opencode", homeDir), { recursive: true });

    expect(detectGlobalAgents(homeDir)).toEqual(["codex", "opencode"]);
  });
});

describe("detectProjectTargets", () => {
  it("detects project-local agent directories", () => {
    const cwd = createTempDir();
    fs.mkdirSync(path.join(cwd, ".codex"), { recursive: true });
    fs.mkdirSync(path.join(cwd, ".opencode"), { recursive: true });

    const targets = detectProjectTargets(cwd);
    expect(targets.map((item) => `${item.scope}:${item.agent}`)).toEqual(["project:codex", "project:opencode"]);
    expect(targets[0]?.directory).toBe(path.join(cwd, ".codex", "skills"));
  });

  it("falls back to project agent skills when no local dirs exist", () => {
    const cwd = createTempDir();
    expect(detectProjectTargets(cwd)).toEqual([getDefaultProjectTarget(cwd)]);
  });
});

describe("resolveInstallTargets", () => {
  it("resolves explicit global targets", () => {
    const cwd = createTempDir();
    const targets = resolveInstallTargets({
      json: false,
      cwd,
      targets: ["codex", "claude-code"],
    });

    expect(targets.map((item) => `${item.scope}:${item.agent}`)).toEqual(["global:codex", "global:claude-code"]);
  });

  it("resolves current target to detected local dirs", () => {
    const cwd = createTempDir();
    fs.mkdirSync(path.join(cwd, ".claude"), { recursive: true });

    const targets = resolveInstallTargets({
      json: false,
      cwd,
      targets: ["current"],
    });

    expect(targets).toEqual([
      {
        scope: "project",
        agent: "claude-code",
        directory: path.join(cwd, ".claude", "skills"),
      },
    ]);
  });
});
