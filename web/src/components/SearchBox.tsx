interface SearchBoxProps {
  value: string;
  onChange: (value: string) => void;
}

/** Controlled input: the parent owns the value, so it can combine it with the department filter. */
export function SearchBox({ value, onChange }: SearchBoxProps) {
  return (
    <label className="field">
      <span className="field__label">Search by name</span>
      <input
        type="search"
        value={value}
        placeholder="e.g. Avery"
        onChange={(e) => onChange(e.target.value)}
        autoComplete="off"
      />
    </label>
  );
}
