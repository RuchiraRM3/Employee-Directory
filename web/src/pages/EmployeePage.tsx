import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmployeeDetails } from '../components/EmployeeDetails';
import { ErrorMessage, LoadingState } from '../components/StatusMessages';
import { ApiError, deleteEmployee, getEmployee } from '../services/api';
import { useToast } from '../toastContext';
import type { Employee } from '../types';

export function EmployeePage() {
  const { id = '' } = useParams();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const showToast = useToast();
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  async function handleDelete(target: Employee) {
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteEmployee(target.id);
      showToast(`${target.name} was deleted`);
      navigate('/', { replace: true }); // the details page no longer exists, so don't leave it in history
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : 'Could not delete. Please try again.');
      setDeleting(false);
    }
  }

  useEffect(() => {
    let current = true;
    getEmployee(id)
      .then((e) => current && setEmployee(e))
      .catch((err) => {
        if (!current) return;
        setError(err instanceof ApiError && err.status === 404 ? 'This employee does not exist.' : err.message);
      });
    return () => {
      current = false;
    };
  }, [id]);

  return (
    <>
      <Link to="/" className="back-link">
        ← Back to directory
      </Link>
      {error ? (
        <ErrorMessage message={error} />
      ) : employee ? (
        <>
          <EmployeeDetails employee={employee} onDelete={() => setConfirming(true)} />
          <ConfirmDialog
            open={confirming}
            title={`Delete ${employee.name}?`}
            confirmLabel="Delete employee"
            busyLabel="Deleting…"
            busy={deleting}
            error={deleteError}
            onConfirm={() => handleDelete(employee)}
            onCancel={() => {
              setConfirming(false);
              setDeleteError(null);
            }}
          >
            <p>
              They will be removed from the directory. HR keeps their record and change history, and their email stays
              reserved.
            </p>
          </ConfirmDialog>
        </>
      ) : (
        <LoadingState />
      )}
    </>
  );
}
