import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './authContext';
import { LoadingState } from '../components/StatusMessages';

/**
 * Hides directory screens from signed-out users. This is UX only:
 * the API protects the data by verifying the Firebase ID token on every request.
 */
export function ProtectedRoute() {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) return <LoadingState label="Checking your session…" />;
  // Keep the query string too, so filters like ?status=ON_LEAVE survive the trip through Login.
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  return <Outlet />;
}
