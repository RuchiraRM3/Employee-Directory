import { Link } from 'react-router-dom';
import { useToast } from '../toastContext';
import { STATUS_LABELS, type Employee } from '../types';
import { Avatar } from './Avatar';
import { CopyIcon, MailIcon } from './Icons';

export function EmployeeCard({ employee }: { employee: Employee }) {
  const showToast = useToast();

  function copyEmail() {
    // The Clipboard API only exists on secure origins (https or localhost); e.g. a phone opening
    // http://<LAN-IP>:5173 has none, so show the address instead of throwing.
    if (!navigator.clipboard) {
      showToast(`Copying isn't available here. The email is ${employee.email}`);
      return;
    }
    navigator.clipboard
      .writeText(employee.email)
      .then(() => showToast(`Copied ${employee.email}`))
      .catch(() => showToast(`Could not copy. The email is ${employee.email}`));
  }

  return (
    // The name link stretches over the whole card (CSS ::after), so the card stays one big click target
    // while the action buttons, which sit above it, remain separately clickable. Buttons can't live inside <a>.
    <li className="card">
      <div className="card__head">
        <Avatar name={employee.name} departmentId={employee.department.id} />
        <div className="card__text">
          <Link to={`/employees/${employee.id}`} className="card__link">
            {employee.name}
          </Link>
          <span className="card__title">{employee.jobTitle}</span>
        </div>
      </div>
      <div className="card__meta">
        <span>{employee.department.name}</span>
        <span className={`badge badge--${employee.status.toLowerCase()}`}>{STATUS_LABELS[employee.status]}</span>
      </div>
      <div className="card__actions">
        <a className="chip-button" href={`mailto:${employee.email}`} aria-label={`Email ${employee.name}`}>
          <MailIcon /> Email
        </a>
        <button type="button" className="chip-button" onClick={copyEmail} aria-label={`Copy email address of ${employee.name}`}>
          <CopyIcon /> Copy email
        </button>
      </div>
    </li>
  );
}
