import type { BuildIssue, ErrorCategory } from "../types/result.js";

interface CategoryRule {
  category: ErrorCategory;
  pattern: RegExp;
}

const CATEGORY_RULES: CategoryRule[] = [
  { category: "gradle-cache-lock", pattern: /Timeout waiting to lock|Could not create service of type FileAccessTimeJournal|cache.*lock/i },
  { category: "dependency-resolution", pattern: /Could not resolve|failed to resolve|Could not download|Could not GET|Could not HEAD|dependency/i },
  { category: "kotlin-compile-error", pattern: /\be:\s|Compilation error.*kotlin|Unresolved reference|Kotlin compilation/i },
  { category: "android-resource-error", pattern: /resource .* not found|Android resource linking failed|failed linking references/i },
  { category: "manifest-merge-error", pattern: /Manifest merger failed|manifest merge/i },
  { category: "aapt-error", pattern: /\bAAPT:|AAPT2?|process.*resources failed/i },
  { category: "java-compile-error", pattern: /error: |Compilation failed; see the compiler output below|cannot find symbol|package .* does not exist/i },
  { category: "lint-error", pattern: /\bLint found errors\b|lintVital|lintDebug|lintRelease|NewApi|ObsoleteSdkInt/i },
  { category: "test-failure", pattern: /There were failing tests|FAILED TESTS|Tests? run: .*Failures:|AssertionFailedError|expected:.*but was:/i },
  { category: "sdk-environment-error", pattern: /SDK location not found|ANDROID_HOME|ANDROID_SDK_ROOT|No installed build tools found|Platform Android SDK/i },
  { category: "permission-error", pattern: /Permission denied|Access is denied|EACCES|EPERM/i },
];

const SUGGESTIONS: Partial<Record<ErrorCategory, string>> = {
  "gradle-cache-lock": "Another Gradle process may be using the cache. Try `./gradlew --stop` and rerun.",
  "dependency-resolution": "Check repository configuration, dependency coordinates, network access, and proxy settings.",
  "sdk-environment-error": "Verify local Android SDK setup such as `local.properties`, `ANDROID_HOME`, or `ANDROID_SDK_ROOT`.",
  "permission-error": "Check file permissions, antivirus interference, and whether the build directory is writable.",
};

export function classifyText(text: string): ErrorCategory {
  for (const rule of CATEGORY_RULES) {
    if (rule.pattern.test(text)) {
      return rule.category;
    }
  }

  return "unknown-build-failure";
}

export function classifyIssues(issues: BuildIssue[], fallbackText: string): ErrorCategory {
  for (const issue of issues) {
    if (issue.category !== "unknown-build-failure") {
      return issue.category;
    }
  }

  return classifyText(fallbackText);
}

export function getSuggestedFix(category: ErrorCategory): string | undefined {
  return SUGGESTIONS[category];
}
