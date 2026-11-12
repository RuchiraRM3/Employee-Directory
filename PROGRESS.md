# Employee Directory — Implementation Progress

Tracks the status of every task in `task.md`. Baseline + amendments: `implementation.md` (§21 has provisional decisions D-001…D-009).

**Legend:** ✅ Done (verified, evidence noted) · 🟡 In progress · ⬜ Todo · ⛔ Blocked (needs human/mentor) · 🔁 Done, pending mentor confirmation

_Last updated: 2026-11-09_

## Summary

| Epic | Status |
|---|---|
| E-00 Scope, environment, repo | 🟡 TASK-004 needs mentor |
| E-01 Design + database | ✅ (ERD awaiting mentor sign-off) |
| E-02 Spring Boot API | ✅ 31/31 tests |
| E-03 Firebase identity + API protection | 🟡 code done; needs Firebase project |
| E-04 React directory | 🟡 code done; signed-in browser checks pending |
| E-05 Docs, verification, PRs | 🟡 docs drafted; no commits/PR yet |
| E-06 Teammate Inventory Tracker fix | ⛔ needs mentor-assigned repo/issue |
| E-07 Demo + knowledge transfer | ⬜ |

## Task status

| Task | Title | Status | Evidence / notes |
|---|---|---|---|
| TASK-001 | Scope + security ambiguity | 🔁 | Provisional decisions D-001…D-009 recorded in implementation.md §21.2; mentor must confirm |
| TASK-002 | Prerequisites | ✅ | JDK 21.0.13, Maven 3.10.0, MySQL 8.4.13 (running), Node 24.22.1, npm 11.19.1 |
| TASK-003 | Repo skeleton + safe git | ✅ | Branch `feat/api`; `git check-ignore` confirms `web/.env.local` and `application-local.properties` are ignored; nothing committed yet |
| TASK-004 | Claude Code team setup | ⛔ | Team config must come from mentor; warm-up is a human learning task |
| TASK-005 | Generate API + web apps | ✅ | API: Boot 4.1.2 on :8080. Web: Vite 8 + React 19 + TS on :5173 |
| TASK-006 | ERD, endpoints, component tree | 🔁 | `db/er-diagram.mmd`, README endpoint table, `web/src/components`; needs mentor sign-off |
| TASK-007 | `db/schema.sql` | ✅ | PK/FK/NOT NULL/unique email/CHECK status; 2 query-justified indexes |
| TASK-008 | `db/seed.sql` | ✅ | 4 depts, 16 fake employees, 5 change rows (verified counts) |
| TASK-009 | `db/queries.sql` | ✅ | All 5 ran; 2 checked by hand → `docs/sql-verification.md` |
| TASK-010 | DB rebuild + integrity checks | ✅ | FK, duplicate email (case-insensitive) and bad status all rejected |
| TASK-011 | API DB config | ✅ | Env vars `DB_URL/DB_USERNAME/DB_PASSWORD`; no password committed; `ddl-auto=validate` |
| TASK-012 | Entities + repositories | ✅ | Parameterized JPQL search, AND semantics; entities never leave the service |
| TASK-013 | DTOs + validation | ✅ | Record DTOs; normalization before validation |
| TASK-014 | Service + audit | ✅ | One audit row per dept/title change, same transaction; tests verify |
| TASK-015 | Controllers + error mapping | ✅ | 5 endpoints; 400/404/409/401 JSON; curl evidence captured 2026-11-09 |
| TASK-016 | API tests | ✅ | `./mvnw test` → **35 run, 0 failures** (real MySQL); 4 regression tests added test-first on 2026-11-09 |
| TASK-017 | Firebase project | 🟡 | Project `employee-directory-35603` exists. **Service-account key was misplaced in `web/.env.local`, `web/` and `web/src/` and exposed in a Claude session → must be rotated** |
| TASK-018 | Web Firebase env | ✅ | `VITE_FIREBASE_*` values present; API key accepted by Firebase. (File still also contains the service-account JSON — harmless to Vite, but should be removed.) |
| TASK-019 | Register/Login/Logout | 🟡 | Register + Logout verified in Playwright with test user `ui-check@example.test`; wrong-password/unknown-user cases still to run |
| TASK-020 | Route guard + token verification | 🟡 | ProtectedRoute verified in Playwright (signed-out redirect). Backend filter verified by tests; real-token check blocked on TASK-017 |
| TASK-021 | API client + CORS | ✅ | Bearer token attached; CORS exact origin; preflight/401+CORS tests pass |
| TASK-022 | EmployeeList / EmployeeCard | 🟡 | Built, typechecks, lints; browser check blocked on TASK-017 |
| TASK-023 | SearchBox / DepartmentFilter | 🟡 | URL-backed filters, debounce, abort stale; browser check blocked |
| TASK-024 | EmployeeDetails / EmployeeForm | 🟡 | Field-level 400/409 display; browser check blocked |
| TASK-025 | Playwright responsive checks | ✅ | Real Firebase sign-in. `docs/screenshots/`: login, directory (1280 + 390), details. 390px: no horizontal scroll, all controls ≥ 44px |
| TASK-026 | README | 🟡 | Written; fresh-clone run-through still to do |
| TASK-027 | Manual test checklist | ✅ | All 21 cases **Pass** with observed results (2026-11-09, real Firebase + API + MySQL) |
| TASK-028 | Claude note + daily log | 🟡 | `docs/claude-code-notes.md` filled; `docs/daily-log.md` is the intern's to write |
| TASK-029 | Final verification + review | ⬜ | |
| TASK-030 | Own PR | ⬜ | |
| TASK-031 | Teammate bug reproduce | ⛔ | Needs mentor-assigned repo + issue |
| TASK-032 | Teammate fix PR | ⛔ | Depends on TASK-031 |
| TASK-033 | Demo | ⬜ | |
| TASK-034 | Viva prep | ⬜ | Human task |

## Human / mentor action items

- [ ] Confirm D-001…D-009 (esp. status values and backend token verification).
- [ ] Create a personal Firebase project, enable Email/Password, put web config in `web/.env.local`, and download a service-account JSON **outside the repo**.
- [ ] Provide the team Claude Code configuration (TASK-004).
- [ ] Assign the Inventory Tracker repo + approved bug (TASK-031).
- [ ] Decide on separate wrong-password / unknown-user messages vs. Firebase Email Enumeration Protection (see manual checklist note ¹).

## Next steps (in order)
0. **Security:** delete the exposed service-account key in Firebase/GCP console, generate a new one, store it outside the repo (e.g. `~/.config/employee-directory/`), delete the 3 in-repo copies, and put the web-app config in `web/.env.local`.
1. Create the Firebase project → fill `web/.env.local` + `GOOGLE_APPLICATION_CREDENTIALS` → run both apps.
2. Execute `docs/manual-test-checklist.md` cases 1–17, then signed-in screenshots (desktop + mobile).
3. Fresh-clone run-through of README; fix any gaps.
4. Commit on `feat/api` (and `feat/web` if split), `/code-review`, open PR for mentor review.

## Change log

- 2026-11-09 — **Delete (archive) + Tiles/List/Table views** (user-requested; deletion was out of scope per §4/ASM-010 → tell mentor). DB: `employee.archived_at/archived_by` + migration `db/migrations/2026-11-09-employee-archive.sql` (applied to local dev DB, data kept); report queries exclude archived. API: `DELETE /employees/{id}` → 204 archive; archived = 404 for get/edit/delete, hidden from search; email stays reserved (409 says "archived"); CORS allows DELETE. Tests first: 41/41. Web: confirm `<dialog>` (Esc/Cancel safe, focus on Cancel); view switcher in URL + localStorage; table headers sort (new sort keys title/department/status). Black-box: API 50/50, UI 32/32 (one timing false-negative re-verified). Checklist rows 22–26 added.
- 2026-11-09 — **Fix: "Cannot reach the server" when opened from another device.** Cause (reproduced from a non-localhost origin): web called `http://localhost:8080` (= the *other* device) and the API's CORS only allows `http://localhost:5173`. Fix: browser calls same-origin `/api`, Vite dev-server proxy forwards to `localhost:8080` (drops Origin header); only port 5173 needs forwarding; API not exposed; 502/503/504 from the proxy map back to the "Cannot reach the server" message. UI black-box re-run from a different origin: 26/26. README updated.
- 2026-11-09 — **System audit + fixes + black-box test.** Bugs found and fixed (each reproduced first, verified after): (1) API name search treated `%`/`_` as LIKE wildcards → escaped; (2) 404/405/415 returned Spring's default body → ApiError shape, plus logged 500 fallback; (3) Copy email crashed where the Clipboard API is missing (http on LAN IP) → toast with address; (4) form Cancel used history.back → left the app when opened directly; (5) Back after save reopened stale form → replace navigation; (6) sign-in redirect dropped `?status/sort` filters. API unit tests 35/35. Black-box: API 39/39 (curl-style, real Firebase token, DB-checked audit rows, CORS); UI 26/26 (Playwright, checklist 1–18 + new features). Test data removed (DB back to 16; test Firebase accounts deleted).
- 2026-11-09 — Register moved from `/register` to an unadvertised path read from `VITE_REGISTER_PATH` in git-ignored `web/.env.local` (no route if unset). Verified signed-out: `/register` → `/login`; new path shows Register; login has no link. Caveat documented: path is visible in the JS bundle and Firebase sign-up is callable directly — real access control would be an API allow-list (Q-007, mentor).
- 2026-11-09 — Login no longer links to Register (user choice); `/register` kept reachable by URL because the brief requires the Register flow (demo + checklist). Mobile pass: Playwright audit of 7 pages/states × 320/390/768 px. Fixed 320px horizontal scroll (Add-employee button), 44px tap targets for Back + auth links, heading/form spacing. Re-audit: 0 overflow, 0 sub-44px controls. tsc/lint/build clean.
- 2026-11-09 — **UI refresh + features (user-requested; scope expansion → mention to mentor).** Glassmorphism/gradient restyle (ui-ux-pro-max design system: blue/violet, Plus Jakarta Sans, 4.5:1 contrast, reduced-motion). Features: status summary tiles that double as status filter, sort (name/newest/oldest), department-gradient initials avatars, Email/Copy-email card actions, toast on save/copy. Frontend only; API unchanged. Verified in Playwright: filter→`?status=ON_LEAVE` (2), copy toast, edit→'Changes saved' toast, newest-hire order matches DB, logout + signed-out redirect. tsc/lint/build clean.
- 2026-11-09 — Register showed generic error. Root cause: Firebase signUp → `CONFIGURATION_NOT_FOUND` (Authentication never initialised in console). Added clear messages for `auth/configuration-not-found` and `auth/operation-not-allowed`; tsc + lint clean. **User must click Authentication → Get started → enable Email/Password.**
- 2026-11-09 — White screen diagnosed: `auth/invalid-api-key` thrown by `getAuth()` at import (no `VITE_FIREBASE_*` in `.env.local`). `main.tsx` now shows a 'Setup needed' message instead; tsc + lint clean; verified in Playwright. Fixed `run.sh` port check to LISTEN sockets only (VS Code client connection caused false 'port in use').
- 2026-11-09 — Added `run.sh` (one-command start: MySQL check/seed, key lookup, API + web, clean shutdown). Verified: API 401 / web 200 when up; port-in-use guard; SIGTERM frees both ports. README quick-start added.
- 2026-11-09 — Plan reviewed; §21 amendments added; tracker created; branch `feat/api`.
- 2026-11-09 — User decision: keep the service-account key in-repo (gitignored) for now; API runs with `GOOGLE_APPLICATION_CREDENTIALS=web/employee-directory-35603-firebase-adminsdk-…json`, real verification ON; no-token and bad-token → 401 verified. Management API shows **no Web app registered** and Auth **not initialised** (identitytoolkit config 404) → console steps needed.
- 2026-11-09 — Found service-account key in `web/` (3 copies, untracked, not imported/bundled). Added `*firebase-adminsdk*.json` / `service-account*.json` to `.gitignore`. Re-ran `./mvnw test` → 31 run, 0 failures.
- 2026-11-09 — DB scripts built and verified; API implemented, 31/31 tests passing; web app implemented (build + lint clean); README and docs drafted.
