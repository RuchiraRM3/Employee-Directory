import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { AppLayout } from './components/AppLayout';
import { AuthPage } from './pages/AuthPage';
import { DirectoryPage } from './pages/DirectoryPage';
import { EmployeeFormPage } from './pages/EmployeeFormPage';
import { EmployeePage } from './pages/EmployeePage';

// Registration lives at an unadvertised path set in web/.env.local, so it never appears in Git.
// This only keeps casual visitors away: the path is still visible in the shipped JavaScript, and
// Firebase sign-up can be called directly. If it is not set, there is no register page at all.
const REGISTER_PATH = import.meta.env.VITE_REGISTER_PATH?.trim();

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<AuthPage mode="login" />} />
      {REGISTER_PATH && <Route path={REGISTER_PATH} element={<AuthPage mode="register" />} />}
      {/* Every directory route sits under ProtectedRoute. */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DirectoryPage />} />
          <Route path="employees/new" element={<EmployeeFormPage key="new" />} />
          <Route path="employees/:id" element={<EmployeePage />} />
          <Route path="employees/:id/edit" element={<EmployeeFormPage key="edit" />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
