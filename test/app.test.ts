import { describe, expect, it } from "vitest";
import { parseCliArgs } from "../src/app.js";

describe("parseCliArgs", () => {
  it("uses the default timeout", () => {
    const result = parseCliArgs(["build"]);
    expect(result.timeout).toBe(600000);
  });

  it("reads a custom timeout", () => {
    const result = parseCliArgs(["--timeout", "1234", "build"]);
    expect(result.timeout).toBe(1234);
    expect(result.gradleArgs).toEqual(["build"]);
  });

  it("rejects negative timeout values", () => {
    expect(() => parseCliArgs(["--timeout", "-1", "build"])).toThrow("Invalid numeric value for --timeout");
  });
});
