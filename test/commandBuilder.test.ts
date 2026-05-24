import path from "node:path";
import { describe, expect, it } from "vitest";
import { buildCommand } from "../src/runner/commandBuilder.js";

const cwd = path.resolve("D:/workspace/demo");
const gradlewPath = path.resolve(cwd, "gradlew.bat");
const gradleArgs = [":app:assembleDebug", "--console=plain", "--warning-mode=summary"];

describe("buildCommand", () => {
  it("builds direct commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath,
      gradleArgs,
      shell: "direct",
      shellArgs: [],
    });

    expect(result.command).toBe(gradlewPath);
    expect(result.args).toEqual(gradleArgs);
    expect(result.usesShell).toBe(false);
  });

  it("builds powershell commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath,
      gradleArgs,
      shell: "powershell",
      shellArgs: [],
    });

    expect(result.command).toBe("powershell.exe");
    expect(result.args.slice(0, 4)).toEqual(["-NoProfile", "-ExecutionPolicy", "Bypass", "-Command"]);
    expect(result.args[4]).toContain(".\\gradlew.bat");
  });

  it("builds pwsh commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath,
      gradleArgs,
      shell: "pwsh",
      shellArgs: [],
    });

    expect(result.command).toBe("pwsh");
    expect(result.args.slice(0, 2)).toEqual(["-NoProfile", "-Command"]);
  });

  it("builds cmd commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath,
      gradleArgs,
      shell: "cmd",
      shellArgs: [],
    });

    expect(result.command).toBe("cmd.exe");
    expect(result.args.slice(0, 3)).toEqual(["/d", "/s", "/c"]);
    expect(result.args[3]).toContain(".\\gradlew.bat");
  });

  it("builds bash commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath: path.resolve(cwd, "gradlew"),
      gradleArgs,
      shell: "bash",
      shellArgs: [],
    });

    expect(result.command).toBe("bash");
    expect(result.args[0]).toBe("-lc");
    expect(result.args[1]).toContain("./gradlew");
  });

  it("builds sh commands", () => {
    const result = buildCommand({
      cwd,
      gradlewPath: path.resolve(cwd, "gradlew"),
      gradleArgs,
      shell: "sh",
      shellArgs: [],
    });

    expect(result.command).toBe("sh");
    expect(result.args[0]).toBe("-lc");
    expect(result.args[1]).toContain("./gradlew");
  });

  it("injects user shell args before command string", () => {
    const result = buildCommand({
      cwd,
      gradlewPath,
      gradleArgs,
      shell: "pwsh",
      shellArgs: ["-WorkingDirectory", "D:/workspace/demo"],
    });

    expect(result.args).toEqual([
      "-NoProfile",
      "-Command",
      "-WorkingDirectory",
      "D:/workspace/demo",
      expect.any(String),
    ]);
  });
});
