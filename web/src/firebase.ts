import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

// Web config comes from web/.env.local (see .env.example). It is public client config, not a secret.
const app = initializeApp({
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
});

export const auth = getAuth(app);
