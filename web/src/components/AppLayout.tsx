import { signOut } from 'firebase/auth';
import { Link, Outlet } from 'react-router-dom';
import { auth } from '../firebase';
import { useAuth } from '../auth/authContext';
import { UsersIcon } from './Icons';
import { ToastProvider } from './ToastProvider';

export function AppLayout() {
  const { user } = useAuth();
  return (
    // The toast lives in the layout (not a page), so a "Saved" message survives the navigation that follows a save.
    <ToastProvider>
      <div className="app">
        <header className="topbar">
          <Link to="/" className="topbar__brand">
            <span className="topbar__logo">
              <UsersIcon />
            </span>
            Employee Directory
          </Link>
          <div className="topbar__user">
            <span className="muted topbar__email">{user?.email}</span>
            {/* signOut fires onAuthStateChanged(null); ProtectedRoute then redirects to /login. */}
            <button type="button" className="button button--ghost" onClick={() => signOut(auth)}>
              Log out
            </button>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </ToastProvider>
  );
}
