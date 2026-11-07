import { auth } from '../firebase';
import type { Department, Employee, EmployeeInput } from '../types';

// Default: same-origin /api, forwarded to the API by the Vite dev server (see vite.config.ts).
// Never default to http://localhost:8080: on another device "localhost" is that device, not this server.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/** Mirrors the API's JSON error body. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, code: string, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  // getIdToken() returns a cached token and refreshes it automatically when close to expiry.
  const token = await auth.currentUser?.getIdToken();
  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...init,
      headers: {
        ...(init.body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
  } catch {
    throw new ApiError(0, 'NETWORK', 'Cannot reach the server. Is the API running?');
  }
  if (response.status === 204) return undefined as T; // e.g. DELETE: success without a body
  if (response.ok) return (await response.json()) as T;

  const body = await response.json().catch(() => null);
  // The dev-server proxy answers 502/503/504 (not JSON) when the API itself is down or unreachable.
  if (!body && [502, 503, 504].includes(response.status)) {
    throw new ApiError(0, 'NETWORK', 'Cannot reach the server. Is the API running?');
  }
  throw new ApiError(
    response.status,
    body?.code ?? 'UNKNOWN',
    body?.message ?? 'Unexpected server error. Please try again.',
    body?.fieldErrors ?? {},
  );
}

export function searchEmployees(name: string, departmentId: string, signal?: AbortSignal) {
  const params = new URLSearchParams();
  if (name.trim()) params.set('name', name.trim());
  if (departmentId) params.set('departmentId', departmentId);
  const query = params.toString();
  return request<Employee[]>(`/employees${query ? `?${query}` : ''}`, { signal });
}

export const getEmployee = (id: string) => request<Employee>(`/employees/${encodeURIComponent(id)}`);

export const getDepartments = () => request<Department[]>('/departments');

export const createEmployee = (input: EmployeeInput) =>
  request<Employee>('/employees', { method: 'POST', body: JSON.stringify(input) });

export const updateEmployee = (id: number, input: EmployeeInput) =>
  request<Employee>(`/employees/${id}`, { method: 'PUT', body: JSON.stringify(input) });

/** "Delete" = archive on the server: hidden from the directory, record and history kept. */
export const deleteEmployee = (id: number) => request<void>(`/employees/${id}`, { method: 'DELETE' });
