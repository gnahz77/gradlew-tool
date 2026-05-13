function quoteWindowsArg(arg: string): string {
  if (!/[\s"&|<>^]/.test(arg)) {
    return arg;
  }

  return `"${arg.replace(/"/g, '""')}"`;
}

function quotePosixArg(arg: string): string {
  if (/^[A-Za-z0-9_./:@%+=,-]+$/.test(arg)) {
    return arg;
  }

  return `'${arg.replace(/'/g, `'\\''`)}'`;
}

export function quoteForPowerShell(args: string[]): string {
  return args
    .map((arg) => {
      if (!/[\s"'`$]/.test(arg)) {
        return arg;
      }

      return `'${arg.replace(/'/g, "''")}'`;
    })
    .join(" ");
}

export function quoteForCmd(args: string[]): string {
  return args.map(quoteWindowsArg).join(" ");
}

export function quoteForPosixShell(args: string[]): string {
  return args.map(quotePosixArg).join(" ");
}

export function quoteForDisplay(args: string[]): string {
  return args
    .map((arg) => {
      if (/^[A-Za-z0-9_./:@%+=,-]+$/.test(arg)) {
        return arg;
      }

      return JSON.stringify(arg);
    })
    .join(" ");
}
