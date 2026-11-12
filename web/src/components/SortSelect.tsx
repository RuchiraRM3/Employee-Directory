import { SORT_OPTIONS, type SortKey } from '../employeeView';

export function SortSelect({ value, onChange }: { value: SortKey; onChange: (value: SortKey) => void }) {
  return (
    <label className="field">
      <span className="field__label">Sort by</span>
      <select value={value} onChange={(e) => onChange(e.target.value as SortKey)}>
        {(Object.keys(SORT_OPTIONS) as SortKey[]).map((key) => (
          <option key={key} value={key}>
            {SORT_OPTIONS[key]}
          </option>
        ))}
      </select>
    </label>
  );
}
