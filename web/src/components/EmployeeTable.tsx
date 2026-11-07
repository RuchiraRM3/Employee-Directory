import { Link, useNavigate } from 'react-router-dom';
import type { SortKey } from '../employeeView';
import { STATUS_LABELS, type Employee } from '../types';
import { Avatar } from './Avatar';

interface Column {
  label: string;
  /** Sort applied when the header is clicked; hire date toggles newest/oldest. */
  sortFor: (current: SortKey) => SortKey;
  ariaSort: (current: SortKey) => 'ascending' | 'descending' | 'none';
}

const byKey = (key: SortKey): Pick<Column, 'sortFor' | 'ariaSort'> => ({
  sortFor: () => key,
  ariaSort: (current) => (current === key ? 'ascending' : 'none'),
});

const COLUMNS: Column[] = [
  { label: 'Name', ...byKey('name') },
  { label: 'Job title', ...byKey('title') },
  { label: 'Department', ...byKey('department') },
  {
    label: 'Hire date',
    sortFor: (current) => (current === 'newest' ? 'oldest' : 'newest'),
    ariaSort: (current) => (current === 'newest' ? 'descending' : current === 'oldest' ? 'ascending' : 'none'),
  },
  { label: 'Status', ...byKey('status') },
];

const ARROWS = { ascending: '▲', descending: '▼', none: '' } as const;

interface EmployeeTableProps {
  employees: Employee[];
  sort: SortKey;
  onSortChange: (sort: SortKey) => void;
}

/** Table view: header buttons drive the same sort as the "Sort by" menu, so both always agree. */
export function EmployeeTable({ employees, sort, onSortChange }: EmployeeTableProps) {
  const navigate = useNavigate();
  return (
    // Wide tables scroll inside their own box on small screens; the page itself never scrolls sideways.
    <div className="table-wrap">
      <table className="emp-table">
        <caption className="sr-only">Employees. Column headers sort the table.</caption>
        <thead>
          <tr>
            {COLUMNS.map((col) => {
              const state = col.ariaSort(sort);
              return (
                <th key={col.label} scope="col" aria-sort={state}>
                  <button type="button" className="th-sort" onClick={() => onSortChange(col.sortFor(sort))}>
                    {col.label}
                    <span className="th-sort__arrow" aria-hidden="true">
                      {ARROWS[state]}
                    </span>
                  </button>
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {employees.map((e) => (
            // Row click is a mouse convenience; the name link is the keyboard/screen-reader path.
            <tr key={e.id} onClick={() => navigate(`/employees/${e.id}`)}>
              <td>
                <span className="emp-table__name">
                  <Avatar name={e.name} departmentId={e.department.id} />
                  <Link to={`/employees/${e.id}`} onClick={(ev) => ev.stopPropagation()}>
                    {e.name}
                  </Link>
                </span>
              </td>
              <td>{e.jobTitle}</td>
              <td>{e.department.name}</td>
              <td className="emp-table__date">{e.hireDate}</td>
              <td>
                <span className={`badge badge--${e.status.toLowerCase()}`}>{STATUS_LABELS[e.status]}</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
