import { STATUS_LABELS, type Employee, type EmployeeStatus } from './types';

/**
 * Client-side view helpers for the directory. The API only filters by name + department;
 * status filtering and sorting run on that (small, bounded) result in the browser.
 */

export const SORT_OPTIONS = {
  name: 'Name (A–Z)',
  newest: 'Newest hire',
  oldest: 'Oldest hire',
  title: 'Job title (A–Z)',
  department: 'Department (A–Z)',
  status: 'Status',
} as const;

export type SortKey = keyof typeof SORT_OPTIONS;

export function parseSort(value: string | null): SortKey {
  return value && value in SORT_OPTIONS ? (value as SortKey) : 'name';
}

/** '' means "all statuses"; anything unknown in the URL is treated as all. */
export function parseStatus(value: string | null): EmployeeStatus | '' {
  return value && value in STATUS_LABELS ? (value as EmployeeStatus) : '';
}

export function countByStatus(employees: Employee[]): Record<EmployeeStatus, number> {
  const counts: Record<EmployeeStatus, number> = { ACTIVE: 0, ON_LEAVE: 0, TERMINATED: 0 };
  for (const e of employees) counts[e.status]++;
  return counts;
}

const STATUS_ORDER = Object.keys(STATUS_LABELS) as EmployeeStatus[]; // Active, On leave, Terminated

export function sortEmployees(employees: Employee[], sort: SortKey): Employee[] {
  // Sort by name first; Array.sort is stable, so equal titles/departments/statuses stay A–Z by name.
  const copy = [...employees].sort((a, b) => a.name.localeCompare(b.name));
  // hireDate is ISO yyyy-mm-dd, so plain string comparison is chronological.
  if (sort === 'newest') return copy.sort((a, b) => b.hireDate.localeCompare(a.hireDate));
  if (sort === 'oldest') return copy.sort((a, b) => a.hireDate.localeCompare(b.hireDate));
  if (sort === 'title') return copy.sort((a, b) => a.jobTitle.localeCompare(b.jobTitle));
  if (sort === 'department') return copy.sort((a, b) => a.department.name.localeCompare(b.department.name));
  if (sort === 'status') return copy.sort((a, b) => STATUS_ORDER.indexOf(a.status) - STATUS_ORDER.indexOf(b.status));
  return copy;
}

export const VIEW_OPTIONS = { tiles: 'Tiles', list: 'List', table: 'Table' } as const;

export type ViewKey = keyof typeof VIEW_OPTIONS;

export function parseView(value: string | null): ViewKey | null {
  return value && value in VIEW_OPTIONS ? (value as ViewKey) : null;
}
