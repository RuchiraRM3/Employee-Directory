import { useState, type FormEvent } from 'react';
import { createUserWithEmailAndPassword, signInWithEmailAndPassword } from 'firebase/auth';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { auth } from '../firebase';
import { useAuth } from '../auth/authContext';
import { authErrorMessage } from '../auth/authErrors';
import { LoadingState } from '../components/StatusMessages';

/** Login and Register differ only in the Firebase call and copy, so they share one page. */
export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const isLogin = mode === 'login';

  if (loading) return <LoadingState label="Checking your session…" />;
  // Successful sign-in updates auth state; this redirect then takes the user where they were going.
  if (user) return <Navigate to={(location.state as { from?: string } | null)?.from ?? '/'} replace />;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (isLogin) await signInWithEmailAndPassword(auth, email.trim(), password);
      else await createUserWithEmailAndPassword(auth, email.trim(), password);
    } catch (err) {
      setError(authErrorMessage(err));
      setSubmitting(false);
    }
  }

  return (
    <main className="auth">
      <form className="panel form auth__form" onSubmit={handleSubmit}>
        <p className="auth__brand">Employee Directory</p>
        <h1>{isLogin ? 'Sign in' : 'Create your account'}</h1>
        {error && (
          <p className="status status--error" role="alert">
            {error}
          </p>
        )}
        <label className="field">
          <span className="field__label">Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </label>
        <label className="field">
          <span className="field__label">Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete={isLogin ? 'current-password' : 'new-password'}
            required
          />
          {!isLogin && <span className="field__hint">At least 6 characters.</span>}
        </label>
        <button type="submit" className="button" disabled={submitting}>
          {submitting ? 'Please wait…' : isLogin ? 'Sign in' : 'Register'}
        </button>
        {/* Registration is not advertised on the login screen; it stays reachable at VITE_REGISTER_PATH
            because the onboarding brief requires the Register flow (demo + manual checklist). */}
        {!isLogin && (
          <p className="muted auth__switch">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        )}
      </form>
    </main>
  );
}
