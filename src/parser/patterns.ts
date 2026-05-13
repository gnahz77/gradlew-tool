export const TASK_LINE_PATTERN = /^> Task\s+.+$/;
export const WARNING_LINE_PATTERN = /^warning[:\s]/i;
export const BUILD_SUCCESS_PATTERN = /\bBUILD SUCCESSFUL\b/i;
export const BUILD_FAILED_PATTERN = /\bBUILD FAILED\b/i;
export const FAILED_TASK_PATTERN = /Execution failed for task '([^']+)'/;
export const FILE_LOCATION_PATTERN =
  /(?<file>[A-Za-z]:\\[^:(]+|\S+\.(?:kt|kts|java|xml|gradle|groovy|properties|txt|png|webp|jpg|jpeg))(?:[:(](?<line>\d+)(?:[: ,](?<column>\d+))?)?/;
