import { createContext, useContext } from 'react';
import type { User } from 'firebase/auth';

export interface AuthState {
  user: User | null;
  /** True until Firebase has restored (or ruled out) a session. Prevents a flash of the wrong screen. */
  loading: boolean;
}

export const AuthContext = createContext<AuthState>({ user: null, loading: true });

export function useAuth() {
  return useContext(AuthContext);
}
