import { defineConfig } from "tsup";

export default defineConfig({
  entry: ["src/cli.ts", "src/index.ts"],
  format: ["cjs"],
  dts: true,
  clean: true,
  outDir: "dist",
  splitting: false,
  banner: {
    js: "#!/usr/bin/env node",
  },
});
