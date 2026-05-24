import fs from "node:fs";
import path from "node:path";
import { formatTimestamp } from "./time.js";

export function ensureDirSync(dir: string): void {
  fs.mkdirSync(dir, { recursive: true });
}

export function createLogFilePath(cwd: string, logDir: string, now = new Date()): string {
  const directory = path.resolve(cwd, logDir);
  ensureDirSync(directory);
  return path.join(directory, `gradle-${formatTimestamp(now)}.log`);
}
