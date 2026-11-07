const GRADIENT_COUNT = 6;

function initials(name: string): string {
  const parts = name.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? '';
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase();
}

/** Decorative initials badge; the gradient is picked by department so teams are easy to scan. */
export function Avatar({ name, departmentId, large = false }: { name: string; departmentId: number; large?: boolean }) {
  return (
    <span className={`avatar avatar--g${departmentId % GRADIENT_COUNT}${large ? ' avatar--lg' : ''}`} aria-hidden="true">
      {initials(name)}
    </span>
  );
}
