import type { JSX } from 'react';
import { VIEW_OPTIONS, type ViewKey } from '../employeeView';
import { GridIcon, ListIcon, TableIcon } from './Icons';

const ICONS: Record<ViewKey, () => JSX.Element> = { tiles: GridIcon, list: ListIcon, table: TableIcon };

/** Segmented control: one pressed button per view. Text labels stay visible (icons alone are ambiguous). */
export function ViewSwitcher({ value, onChange }: { value: ViewKey; onChange: (view: ViewKey) => void }) {
  return (
    <div className="view-switch" role="group" aria-label="View as">
      {(Object.keys(VIEW_OPTIONS) as ViewKey[]).map((key) => {
        const Icon = ICONS[key];
        return (
          <button
            key={key}
            type="button"
            className="view-switch__option"
            aria-pressed={value === key}
            onClick={() => onChange(key)}
          >
            <Icon /> {VIEW_OPTIONS[key]}
          </button>
        );
      })}
    </div>
  );
}
