import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { EmployeeForm } from '../components/EmployeeForm';
import { ErrorMessage, LoadingState } from '../components/StatusMessages';
import { ApiError, createEmployee, getDepartments, getEmployee, updateEmployee } from '../services/api';
import { useToast } from '../toastContext';
import type { Department, EmployeeInput } from '../types';

const EMPTY: EmployeeInput = { name: '', email: '', jobTitle: '', departmentId: null, hireDate: '', status: 'ACTIVE' };

/** /employees/new (POST) and /employees/:id/edit (PUT) share this page and EmployeeForm. */
export function EmployeeFormPage() {
  const { id } = useParams();
  const isEdit = id !== undefined;
  const navigate = useNavigate();
  const showToast = useToast();
  const [departments, setDepartments] = useState<Department[] | null>(null);
  const [initial, setInitial] = useState<EmployeeInput | null>(isEdit ? null : EMPTY);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let current = true;
    Promise.all([getDepartments(), isEdit ? getEmployee(id) : Promise.resolve(null)])
      .then(([deps, employee]) => {
        if (!current) return;
        setDepartments(deps);
        if (employee) {
          setInitial({
            name: employee.name,
            email: employee.email,
            jobTitle: employee.jobTitle,
            departmentId: employee.department.id,
            hireDate: employee.hireDate,
            status: employee.status,
          });
        }
      })
      .catch((err) => current && setError(err instanceof ApiError ? err.message : 'Could not load the form.'));
    return () => {
      current = false;
    };
  }, [id, isEdit]);

  async function handleSubmit(input: EmployeeInput) {
    // If the API rejects the save (400/409), this throws before the toast and EmployeeForm shows the error.
    const saved = isEdit ? await updateEmployee(Number(id), input) : await createEmployee(input);
    showToast(isEdit ? 'Changes saved' : `${saved.name} was added`);
    // replace: the form leaves history, so Back from the details page doesn't reopen a stale form.
    navigate(`/employees/${saved.id}`, { replace: true });
  }

  return (
    <>
      <Link to={isEdit ? `/employees/${id}` : '/'} className="back-link">
        ← Back
      </Link>
      <h1>{isEdit ? 'Edit employee' : 'Add employee'}</h1>
      {error ? (
        <ErrorMessage message={error} />
      ) : departments && initial ? (
        <EmployeeForm
          initial={initial}
          departments={departments}
          submitLabel={isEdit ? 'Save changes' : 'Add employee'}
          onSubmit={handleSubmit}
          // A fixed target, not navigate(-1): opened from a bookmark/link there is no in-app page to go back to.
          onCancel={() => navigate(isEdit ? `/employees/${id}` : '/')}
        />
      ) : (
        <LoadingState />
      )}
    </>
  );
}
