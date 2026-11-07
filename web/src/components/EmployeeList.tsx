import type { SortKey, ViewKey } from '../employeeView';
import type { Employee } from '../types';
import { EmployeeCard } from './EmployeeCard';
import { EmployeeRow } from './EmployeeRow';
import { EmployeeTable } from './EmployeeTable';
import { ErrorMessage, LoadingState } from './StatusMessages';

interface EmployeeListProps {
  employees: Employee[];
  view: ViewKey;
  sort: SortKey;
  loading: boolean;
  error: string | null;
  hasFilters: boolean;
  onSortChange: (sort: SortKey) => void;
  onRetry: () => void;
  onClearFilters: () => void;
}

/** Presentational: renders exactly one of loading / error / empty / results (as tiles, list or table). */
export function EmployeeList({
  employees,
  view,
  sort,
  loading,
  error,
  hasFilters,
  onSortChange,
  onRetry,
  onClearFilters,
}: EmployeeListProps) {
  if (loading) return <LoadingState label="Loading employees…" />;
  if (error) return <ErrorMessage message={error} onRetry={onRetry} />;
  if (employees.length === 0) {
    return (
      <div className="status">
        <p>{hasFilters ? 'No employees match your search.' : 'No employees yet.'}</p>
        {hasFilters && (
          <button type="button" className="button button--ghost" onClick={onClearFilters}>
            Clear filters
          </button>
        )}
      </div>
    );
  }
  if (view === 'table') return <EmployeeTable employees={employees} sort={sort} onSortChange={onSortChange} />;
  if (view === 'list') {
    return (
      <ul className="row-list">
        {employees.map((e) => (
          <EmployeeRow key={e.id} employee={e} />
        ))}
      </ul>
    );
  }
  return (
    <ul className="card-grid">
      {employees.map((e) => (
        <EmployeeCard key={e.id} employee={e} />
      ))}
    </ul>
  );
}
