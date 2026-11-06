import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';

const root = createRoot(document.getElementById('root')!);

// firebase.ts throws at import time when its config is missing, which would leave a blank page.
// So check first, and only load the app (and with it Firebase) once the config is present.
const missing = (
  ['VITE_FIREBASE_API_KEY', 'VITE_FIREBASE_AUTH_DOMAIN', 'VITE_FIREBASE_PROJECT_ID', 'VITE_FIREBASE_APP_ID'] as const
).filter((key) => !import.meta.env[key]);

if (missing.length > 0) {
  root.render(
    <main className="content">
      <div className="status status--error" role="alert">
        <p>
          <strong>Setup needed:</strong> <code>web/.env.local</code> is missing {missing.join(', ')}. Copy{' '}
          <code>web/.env.example</code> to <code>web/.env.local</code>, fill in your Firebase web-app config, then
          restart the dev server.
        </p>
      </div>
    </main>,
  );
} else {
  void Promise.all([import('./App'), import('./auth/AuthProvider')]).then(([{ default: App }, { AuthProvider }]) =>
    root.render(
      <StrictMode>
        <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </StrictMode>,
    ),
  );
}
