import type { Department } from '../types';

interface DepartmentFilterProps {
  departments: Department[];
  value: string; // '' = all departments
  onChange: (value: string) => void;
}

export function DepartmentFilter({ departments, value, onChange }: DepartmentFilterProps) {
  return (
    <label className="field">
      <span className="field__label">Department</span>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">All departments</option>
        {departments.map((d) => (
          <option key={d.id} value={String(d.id)}>
            {d.name}
          </option>
        ))}
      </select>
    </label>
  );
}
