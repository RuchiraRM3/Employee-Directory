import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { ToastContext } from '../toastContext';

const TOAST_MS = 3500;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<{ id: number; message: string } | null>(null);
  const show = useCallback((message: string) => setToast({ id: Date.now(), message }), []);

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(null), TOAST_MS);
    return () => clearTimeout(timer);
  }, [toast]);

  return (
    <ToastContext.Provider value={show}>
      {children}
      {/* The live region is always mounted, so screen readers announce each new message. */}
      <div className="toast-region" role="status" aria-live="polite">
        {toast && (
          <div key={toast.id} className="toast">
            {toast.message}
          </div>
        )}
      </div>
    </ToastContext.Provider>
  );
}
