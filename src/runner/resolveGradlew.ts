import fs from "node:fs";
import path from "node:path";
import type { WrapperResolution } from "../types/options.js";

export interface ResolveGradlewOptions {
  cwd: string;
  platform?: NodeJS.Platform;
  gradlew?: string;
}

export function resolveGradlew(options: ResolveGradlewOptions): WrapperResolution {
  const platform = options.platform ?? process.platform;

  if (options.gradlew) {
    const explicitPath = path.resolve(options.cwd, options.gradlew);
    return fs.existsSync(explicitPath)
      ? { found: true, path: explicitPath, category: "none" }
      : { found: false, path: explicitPath, category: "gradle-wrapper-not-found" };
  }

  const windowsWrapper = path.resolve(options.cwd, "gradlew.bat");
  if (platform === "win32" && fs.existsSync(windowsWrapper)) {
    return { found: true, path: windowsWrapper, category: "none" };
  }

  const unixWrapper = path.resolve(options.cwd, "gradlew");
  if (fs.existsSync(unixWrapper)) {
    return { found: true, path: unixWrapper, category: "none" };
  }

  return {
    found: false,
    path: unixWrapper,
    category: "gradle-wrapper-not-found",
  };
}
