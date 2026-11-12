export function LoadingState({ label = 'Loading…' }: { label?: string }) {
  return (
    <p className="status" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" /> {label}
    </p>
  );
}

export function ErrorMessage({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="status status--error" role="alert">
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="button button--ghost" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}
