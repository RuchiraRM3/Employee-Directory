import { STATUS_LABELS, type EmployeeStatus } from '../types';

interface StatusSummaryProps {
  counts: Record<EmployeeStatus, number>;
  total: number;
  value: EmployeeStatus | ''; // '' = all
  loading: boolean;
  onChange: (value: EmployeeStatus | '') => void;
}

/** Headcount tiles that double as the status filter. Counts follow the current name/department search. */
export function StatusSummary({ counts, total, value, loading, onChange }: StatusSummaryProps) {
  const tiles: { key: EmployeeStatus | ''; label: string; count: number }[] = [
    { key: '', label: 'All employees', count: total },
    ...(Object.keys(STATUS_LABELS) as EmployeeStatus[]).map((s) => ({ key: s, label: STATUS_LABELS[s], count: counts[s] })),
  ];

  return (
    <div className="summary" role="group" aria-label="Filter by status">
      {tiles.map((t) => {
        const selected = value === t.key;
        return (
          <button
            key={t.key || 'ALL'}
            type="button"
            className={`summary__tile summary__tile--${(t.key || 'all').toLowerCase()}`}
            aria-pressed={selected}
            // Clicking the selected status again goes back to "all".
            onClick={() => onChange(selected ? '' : t.key)}
          >
            <span className="summary__count">{loading ? '–' : t.count}</span>
            <span className="summary__label">{t.label}</span>
          </button>
        );
      })}
    </div>
  );
}
