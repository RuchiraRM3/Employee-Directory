import { FirebaseError } from 'firebase/app';

/** Plain-language messages for the Firebase Auth errors HR users can hit. */
const MESSAGES: Record<string, string> = {
  // With Email Enumeration Protection on (Firebase default), wrong password AND unknown user
  // both arrive as invalid-credential, so they cannot be told apart.
  'auth/invalid-credential': 'Email or password is incorrect.',
  'auth/wrong-password': 'That password is incorrect.',
  'auth/user-not-found': 'No account exists with that email. Register first.',
  'auth/email-already-in-use': 'An account with this email already exists. Sign in instead.',
  'auth/weak-password': 'Password is too weak. Use at least 6 characters.',
  'auth/invalid-email': 'Enter a valid email address.',
  'auth/too-many-requests': 'Too many attempts. Wait a moment and try again.',
  'auth/network-request-failed': 'Network error. Check your connection and try again.',
  // Project setup problems (not user mistakes): say so, so they are not mistaken for app bugs.
  'auth/configuration-not-found':
    'Sign-in is not set up for this Firebase project. In the Firebase console, open Authentication, click Get started and enable Email/Password.',
  'auth/operation-not-allowed':
    'Email/Password sign-in is disabled. Enable it in the Firebase console under Authentication > Sign-in method.',
};

export function authErrorMessage(error: unknown): string {
  if (error instanceof FirebaseError && MESSAGES[error.code]) return MESSAGES[error.code];
  return 'Something went wrong. Please try again.';
}
