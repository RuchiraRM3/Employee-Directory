import { useEffect, useState, type ReactNode } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from '../firebase';
import { AuthContext, type AuthState } from './authContext';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ user: null, loading: true });

  // onAuthStateChanged fires once on load (restored session or null) and again on sign-in/sign-out.
  // It returns the unsubscribe function, which React calls on unmount.
  useEffect(() => onAuthStateChanged(auth, (user) => setState({ user, loading: false })), []);

  return <AuthContext.Provider value={state}>{children}</AuthContext.Provider>;
}
