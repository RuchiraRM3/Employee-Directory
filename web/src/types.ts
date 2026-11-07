export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED';

export const STATUS_LABELS: Record<EmployeeStatus, string> = {
  ACTIVE: 'Active',
  ON_LEAVE: 'On leave',
  TERMINATED: 'Terminated',
};

export interface Department {
  id: number;
  name: string;
}

export interface Employee {
  id: number;
  name: string;
  email: string;
  jobTitle: string;
  department: Department;
  hireDate: string; // ISO yyyy-mm-dd
  status: EmployeeStatus;
}

export interface EmployeeInput {
  name: string;
  email: string;
  jobTitle: string;
  departmentId: number | null;
  hireDate: string;
  status: EmployeeStatus;
}
