import { useState, type FormEvent } from 'react';
import { ApiError } from '../services/api';
import { STATUS_LABELS, type Department, type EmployeeInput, type EmployeeStatus } from '../types';
import { ErrorMessage } from './StatusMessages';

interface EmployeeFormProps {
  initial: EmployeeInput;
  departments: Department[];
  submitLabel: string;
  onSubmit: (input: EmployeeInput) => Promise<void>;
  onCancel: () => void;
}

type FieldErrors = Partial<Record<keyof EmployeeInput, string>>;

/** Light client checks for fast feedback; the API remains the authority (400/409 are shown per field). */
function validate(v: EmployeeInput): FieldErrors {
  const errors: FieldErrors = {};
  if (!v.name.trim()) errors.name = 'Name is required.';
  if (!/^\S+@\S+\.\S+$/.test(v.email.trim())) errors.email = 'Enter a valid email address.';
  if (!v.jobTitle.trim()) errors.jobTitle = 'Job title is required.';
  if (!v.departmentId) errors.departmentId = 'Choose a department.';
  if (!v.hireDate) errors.hireDate = 'Hire date is required.';
  return errors;
}

/** Shared by Add and Edit; the parent decides whether submit means POST or PUT. */
export function EmployeeForm({ initial, departments, submitLabel, onSubmit, onCancel }: EmployeeFormProps) {
  const [values, setValues] = useState<EmployeeInput>(initial);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function set<K extends keyof EmployeeInput>(key: K, value: EmployeeInput[K]) {
    setValues((v) => ({ ...v, [key]: value }));
    setErrors((e) => ({ ...e, [key]: undefined }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const clientErrors = validate(values);
    setErrors(clientErrors);
    setFormError(null);
    if (Object.keys(clientErrors).length > 0) return;

    setSaving(true);
    try {
      await onSubmit(values);
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setErrors(err.fieldErrors as FieldErrors); // e.g. 409 duplicate email lands on the email field
      } else {
        setFormError(err instanceof ApiError ? err.message : 'Could not save. Please try again.');
      }
    } finally {
      setSaving(false);
    }
  }

  const fieldProps = (key: keyof EmployeeInput) => ({
    'aria-invalid': Boolean(errors[key]),
    'aria-describedby': errors[key] ? `${key}-error` : undefined,
  });
  const fieldError = (key: keyof EmployeeInput) =>
    errors[key] && (
      <span id={`${key}-error`} className="field__error">
        {errors[key]}
      </span>
    );

  return (
    <form className="panel form" onSubmit={handleSubmit} noValidate>
      {formError && <ErrorMessage message={formError} />}

      <label className="field">
        <span className="field__label">Full name</span>
        <input value={values.name} onChange={(e) => set('name', e.target.value)} maxLength={160} {...fieldProps('name')} />
        {fieldError('name')}
      </label>

      <label className="field">
        <span className="field__label">Email</span>
        <input type="email" value={values.email} onChange={(e) => set('email', e.target.value)} maxLength={254} {...fieldProps('email')} />
        {fieldError('email')}
      </label>

      <label className="field">
        <span className="field__label">Job title</span>
        <input value={values.jobTitle} onChange={(e) => set('jobTitle', e.target.value)} maxLength={120} {...fieldProps('jobTitle')} />
        {fieldError('jobTitle')}
      </label>

      <div className="form__row">
        <label className="field">
          <span className="field__label">Department</span>
          <select
            value={values.departmentId ?? ''}
            onChange={(e) => set('departmentId', e.target.value ? Number(e.target.value) : null)}
            {...fieldProps('departmentId')}
          >
            <option value="">Choose…</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>
          {fieldError('departmentId')}
        </label>

        <label className="field">
          <span className="field__label">Hire date</span>
          <input type="date" value={values.hireDate} onChange={(e) => set('hireDate', e.target.value)} {...fieldProps('hireDate')} />
          {fieldError('hireDate')}
        </label>

        <label className="field">
          <span className="field__label">Status</span>
          <select value={values.status} onChange={(e) => set('status', e.target.value as EmployeeStatus)} {...fieldProps('status')}>
            {Object.entries(STATUS_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
          {fieldError('status')}
        </label>
      </div>

      <div className="form__actions">
        <button type="button" className="button button--ghost" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="button" disabled={saving}>
          {saving ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
