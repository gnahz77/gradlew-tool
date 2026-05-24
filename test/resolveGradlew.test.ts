import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { afterEach, describe, expect, it } from "vitest";
import { resolveGradlew } from "../src/runner/resolveGradlew.js";

const tempDirs: string[] = [];

afterEach(() => {
  while (tempDirs.length > 0) {
    fs.rmSync(tempDirs.pop()!, { recursive: true, force: true });
  }
});

function createTempDir(): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "gradlew-tool-"));
  tempDirs.push(dir);
  return dir;
}

describe("resolveGradlew", () => {
  it("prefers explicit --gradlew path", () => {
    const cwd = createTempDir();
    const explicit = path.join(cwd, "tools", "gradlew");
    fs.mkdirSync(path.dirname(explicit), { recursive: true });
    fs.writeFileSync(explicit, "");

    const result = resolveGradlew({ cwd, gradlew: "./tools/gradlew", platform: "linux" });
    expect(result.found).toBe(true);
    expect(result.path).toBe(explicit);
  });

  it("uses gradlew.bat on win32", () => {
    const cwd = createTempDir();
    const wrapper = path.join(cwd, "gradlew.bat");
    fs.writeFileSync(wrapper, "");
    fs.writeFileSync(path.join(cwd, "gradlew"), "");

    const result = resolveGradlew({ cwd, platform: "win32" });
    expect(result.path).toBe(wrapper);
  });

  it("uses gradlew on unix", () => {
    const cwd = createTempDir();
    const wrapper = path.join(cwd, "gradlew");
    fs.writeFileSync(wrapper, "");

    const result = resolveGradlew({ cwd, platform: "linux" });
    expect(result.path).toBe(wrapper);
  });

  it("returns wrapper not found when missing", () => {
    const cwd = createTempDir();
    const result = resolveGradlew({ cwd, platform: "linux" });
    expect(result.found).toBe(false);
    expect(result.category).toBe("gradle-wrapper-not-found");
  });
});
