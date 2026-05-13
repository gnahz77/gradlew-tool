import path from "node:path";

export function toPosixPath(value: string): string {
  return value.replace(/\\/g, "/");
}

export function formatLogPath(cwd: string, filePath: string): string {
  const relative = path.relative(cwd, filePath);
  return relative.length > 0 && !relative.startsWith("..") ? toPosixPath(relative) : filePath;
}
