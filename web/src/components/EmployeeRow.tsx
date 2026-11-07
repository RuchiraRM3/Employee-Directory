import { Link } from 'react-router-dom';
import { STATUS_LABELS, type Employee } from '../types';
import { Avatar } from './Avatar';

/** Compact one-line entry for the List view. Like the card, the name link stretches over the whole row. */
export function EmployeeRow({ employee }: { employee: Employee }) {
  return (
    <li className="row">
      <Avatar name={employee.name} departmentId={employee.department.id} />
      <div className="row__main">
        <Link to={`/employees/${employee.id}`} className="card__link">
          {employee.name}
        </Link>
        <span className="card__title">{employee.jobTitle}</span>
      </div>
      <span className="row__dept">{employee.department.name}</span>
      <span className={`badge badge--${employee.status.toLowerCase()}`}>{STATUS_LABELS[employee.status]}</span>
    </li>
  );
}
