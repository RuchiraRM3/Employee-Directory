import { Link } from 'react-router-dom';
import { STATUS_LABELS, type Employee } from '../types';
import { Avatar } from './Avatar';
import { TrashIcon } from './Icons';

export function EmployeeDetails({ employee, onDelete }: { employee: Employee; onDelete: () => void }) {
  return (
    <article className="panel">
      <header className="panel__header">
        <div className="panel__identity">
          <Avatar name={employee.name} departmentId={employee.department.id} large />
          <div>
            <h1>{employee.name}</h1>
            <p className="muted">{employee.jobTitle}</p>
          </div>
        </div>
        <div className="panel__actions">
          <Link to={`/employees/${employee.id}/edit`} className="button">
            Edit
          </Link>
          <button type="button" className="button button--danger-ghost" onClick={onDelete}>
            <TrashIcon /> Delete
          </button>
        </div>
      </header>
      <dl className="details">
        <dt>Email</dt>
        <dd>
          <a href={`mailto:${employee.email}`}>{employee.email}</a>
        </dd>
        <dt>Department</dt>
        <dd>{employee.department.name}</dd>
        <dt>Hire date</dt>
        <dd>{employee.hireDate}</dd>
        <dt>Status</dt>
        <dd>
          <span className={`badge badge--${employee.status.toLowerCase()}`}>{STATUS_LABELS[employee.status]}</span>
        </dd>
      </dl>
    </article>
  );
}
