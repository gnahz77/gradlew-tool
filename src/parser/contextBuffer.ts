export class ContextBuffer {
  private readonly lines: string[] = [];

  constructor(private readonly limit: number) {}

  add(line: string): void {
    this.lines.push(line);
    if (this.lines.length > this.limit) {
      this.lines.shift();
    }
  }

  values(): string[] {
    return [...this.lines];
  }
}
