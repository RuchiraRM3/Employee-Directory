import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { DepartmentFilter } from '../components/DepartmentFilter';
import { EmployeeList } from '../components/EmployeeList';
import { PlusIcon } from '../components/Icons';
import { SearchBox } from '../components/SearchBox';
import { SortSelect } from '../components/SortSelect';
import { StatusSummary } from '../components/StatusSummary';
import { ViewSwitcher } from '../components/ViewSwitcher';
import { countByStatus, parseSort, parseStatus, parseView, sortEmployees, type ViewKey } from '../employeeView';
import { ApiError, getDepartments, searchEmployees } from '../services/api';
import type { Department, Employee } from '../types';

const SEARCH_DELAY_MS = 250; // small debounce so typing doesn't send one request per keystroke
const VIEW_STORAGE_KEY = 'employee-directory.view';

type FilterKey = 'name' | 'departmentId' | 'status' | 'sort' | 'view';

// The chosen view is remembered per browser (a convenience only, so storage failures are ignored).
function storedView(): ViewKey | null {
  try {
    return parseView(localStorage.getItem(VIEW_STORAGE_KEY));
  } catch {
    return null;
  }
}

function storeView(view: ViewKey) {
  try {
    localStorage.setItem(VIEW_STORAGE_KEY, view);
  } catch {
    // private mode / blocked storage: the URL still carries the view
  }
}

export function DirectoryPage() {
  // Filters live in the URL (?name=&departmentId=&status=&sort=) so they survive Back navigation and can be shared.
  const [params, setParams] = useSearchParams();
  const name = params.get('name') ?? '';
  const departmentId = params.get('departmentId') ?? '';
  const status = parseStatus(params.get('status'));
  const sort = parseSort(params.get('sort'));
  // URL first (shareable links), then this browser's last choice, then tiles.
  const view = parseView(params.get('view')) ?? storedView() ?? 'tiles';

  const [departments, setDepartments] = useState<Department[]>([]);
  const [reloadCount, setReloadCount] = useState(0);

  // Each result remembers which query produced it; while it doesn't match the current query we are loading.
  // Deriving `loading` this way also covers Back/Forward, which change the URL without any handler running.
  // Status and sort are applied in the browser, so they are deliberately not part of the API query key.
  const queryKey = `${name}|${departmentId}|${reloadCount}`;
  const [result, setResult] = useState<{ key: string; employees: Employee[]; error: string | null } | null>(null);
  const loading = result?.key !== queryKey;

  useEffect(() => {
    // A failure here also breaks the employee request, whose error is shown below.
    getDepartments().then(setDepartments).catch(() => setDepartments([]));
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const key = `${name}|${departmentId}|${reloadCount}`;
    const timer = setTimeout(() => {
      // Both filters are always sent together; the API combines them with AND.
      searchEmployees(name, departmentId, controller.signal)
        .then((employees) => setResult({ key, employees, error: null }))
        .catch((err) => {
          if (!controller.signal.aborted) {
            const error = err instanceof ApiError ? err.message : 'Could not load employees.';
            setResult({ key, employees: [], error });
          }
        });
    }, SEARCH_DELAY_MS);
    // Cleanup cancels the pending/in-flight request, so an older slow response can't overwrite a newer one.
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [name, departmentId, reloadCount]);

  function setFilter(key: FilterKey, value: string) {
    const next = new URLSearchParams(params); // copy keeps the *other* filters intact
    if (value) next.set(key, value);
    else next.delete(key);
    setParams(next, { replace: true });
  }

  function clearFilters() {
    // Sort and view are display preferences, not filters, so they survive "Clear filters".
    const next = new URLSearchParams();
    for (const key of ['sort', 'view'] as const) {
      const value = params.get(key);
      if (value) next.set(key, value);
    }
    setParams(next, { replace: true });
  }

  function changeView(next: ViewKey) {
    storeView(next);
    setFilter('view', next);
  }

  const matching = result?.employees ?? []; // name + department matches, before the status filter
  const visible = sortEmployees(status ? matching.filter((e) => e.status === status) : matching, sort);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Employees</h1>
          <p className="muted">Search, filter and manage your team.</p>
        </div>
        <Link to="/employees/new" className="button">
          <PlusIcon /> Add employee
        </Link>
      </div>

      <StatusSummary
        counts={countByStatus(matching)}
        total={matching.length}
        value={status}
        loading={loading}
        onChange={(v) => setFilter('status', v)}
      />

      <section className="filters" aria-label="Filters">
        <SearchBox value={name} onChange={(v) => setFilter('name', v)} />
        <DepartmentFilter departments={departments} value={departmentId} onChange={(v) => setFilter('departmentId', v)} />
        <SortSelect value={sort} onChange={(v) => setFilter('sort', v === 'name' ? '' : v)} />
      </section>

      <div className="results-bar">
        <p className="result-count" aria-live="polite">
          {!loading && !result?.error && `${visible.length} ${visible.length === 1 ? 'employee' : 'employees'}`}
        </p>
        <ViewSwitcher value={view} onChange={changeView} />
      </div>

      <EmployeeList
        employees={visible}
        view={view}
        sort={sort}
        onSortChange={(v) => setFilter('sort', v === 'name' ? '' : v)}
        loading={loading}
        error={result?.error ?? null}
        hasFilters={Boolean(name.trim() || departmentId || status)}
        onRetry={() => setReloadCount((c) => c + 1)}
        onClearFilters={clearFilters}
      />
    </>
  );
}
