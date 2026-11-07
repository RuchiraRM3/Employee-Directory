import { createContext, useContext } from 'react';

/** Shows a short, auto-dismissing confirmation message (announced to screen readers). */
export const ToastContext = createContext<(message: string) => void>(() => {});

export function useToast() {
  return useContext(ToastContext);
}
