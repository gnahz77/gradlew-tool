export function createLineCollector(onLine: (line: string) => void): {
  push: (chunk: string) => void;
  flush: () => void;
} {
  let pending = "";

  return {
    push(chunk: string) {
      pending += chunk;
      const parts = pending.split(/\r?\n/);
      pending = parts.pop() ?? "";

      for (const part of parts) {
        onLine(part);
      }
    },
    flush() {
      if (pending.length > 0) {
        onLine(pending);
        pending = "";
      }
    },
  };
}
