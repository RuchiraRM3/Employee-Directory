# Manual test checklist

Record the **observed** result for each case: Pass, Fail or Blocked, with the date and any notes. Never pre-fill a Pass.

| # | Case | Steps | Expected | Result | Date / notes |
|---|---|---|---|---|---|
| 1 | Search only | Type `er` | Only names containing "er" | **Pass** | 2026-11-09, Playwright/curl black-box. 6 results, all contain "er" |
| 2 | Department only | Choose Finance | 5 Finance employees | **Pass** | 2026-11-09, Playwright/curl black-box. 5 |
| 3 | Search + department | `er` + Engineering | Avery Quill, Piers Danvers | **Pass** | 2026-11-09, Playwright/curl black-box. Avery Quill, Piers Danvers |
| 4 | Clear one filter keeps the other | From #3, clear the search box | All 7 Engineering employees | **Pass** | 2026-11-09, Playwright/curl black-box. 7 Engineering |
| 5 | Empty result | Type `zzz` | "No employees match" + Clear filters button | **Pass** | 2026-11-09, Playwright/curl black-box. Clear filters restores all 16 |
| 6 | Loading state | Throttle the network in DevTools, then search | Spinner shown | **Pass** | 2026-11-09, Playwright/curl black-box. API response delayed 1.5 s via Playwright route |
| 7 | API error state | Stop the API, then reload | Plain error + Try again button | **Pass** | 2026-11-09, Playwright/curl black-box. API blocked via Playwright route: "Cannot reach the server…"; Try again recovers |
| 8 | Employee details | Click a card | All fields shown | **Pass** | 2026-11-09, Playwright/curl black-box. Email, department, hire date, status |
| 9 | Add employee | Add a valid employee | Redirect to details; appears in list | **Pass** | 2026-11-09, Playwright/curl black-box. Toast "… was added"; found by search |
| 10 | Edit employee | Change department + title | Saved; one `employee_change` row in DB | **Pass** | 2026-11-09, Playwright/curl black-box. Toast "Changes saved"; DB: one employee_change row 4→1, QA Analyst→Senior QA Analyst, changed_by set |
| 11 | Duplicate email | Add using `avery.quill@example.test` | Error shown under the Email field | **Pass** | 2026-11-09, Playwright/curl black-box. "Email is already in use." under Email |
| 12 | Register | Open the `VITE_REGISTER_PATH` page (not linked from login); new email + 6+ char password | Signed in, directory visible | **Pass** | 2026-11-09, Playwright/curl black-box. Registered at VITE_REGISTER_PATH; signed in; test account deleted afterwards |
| 13 | Weak password | Register with `123` | "Password is too weak…" | **Pass** | 2026-11-09, Playwright/curl black-box. "Password is too weak…" |
| 14 | Email in use | Register an existing account email | "An account with this email already exists…" | **Pass** | 2026-11-09, Playwright/curl black-box. "An account with this email already exists…" |
| 15 | Wrong password | Sign in with wrong password | "Email or password is incorrect." ¹ | **Pass** | 2026-11-09, Playwright/curl black-box. "Email or password is incorrect." |
| 16 | Unknown user | Sign in with unregistered email | "Email or password is incorrect." ¹ | **Pass** | 2026-11-09, Playwright/curl black-box. "Email or password is incorrect." (see ¹) |
| 17 | Logout | Click Log out | Back on /login | **Pass** | 2026-11-09, Playwright/curl black-box. Back on /login |
| 18 | Signed-out protected URL | While signed out, open `/employees/1/edit` | Redirected to /login | **Pass** | 2026-11-09, Playwright/curl black-box. Also: login page has no register link |
| 19 | API rejects no token | `curl -i localhost:8080/employees` (auth enabled) | 401 JSON | **Pass** | 2026-11-09, Playwright/curl black-box. curl black-box: 401 ApiError with CORS header |
| 20 | Desktop layout (1280px) | Check list, details and form | Usable | **Pass** | 2026-11-09, Playwright/curl black-box. List, details, form at 1280 px: `docs/screenshots/directory-desktop.png`, `details-desktop.png` |
| 21 | Mobile layout (390px) | Check list, details and form | Usable, no horizontal scroll | **Pass** | 2026-11-09, Playwright/curl black-box. 320/390/768 px audit: 0 horizontal overflow, all controls ≥ 44 px: `docs/screenshots/directory-mobile.png` |
| 22 | Delete (archive) | Employee page → Delete → confirm | Toast "… was deleted", back on directory, person gone; DB row kept with `archived_at` | **Pass** | 2026-11-09, Playwright + DB check. Esc / Cancel close the dialog without deleting; focus starts on Cancel |
| 23 | Archived email reserved | Add employee with an archived employee's email | "This email belongs to an archived (deleted) employee." under Email | **Pass** | 2026-11-09, Playwright |
| 24 | Views | Switch Tiles / List / Table | Same people and order in each; choice kept in URL and remembered after reload | **Pass** | 2026-11-09, Playwright (16 rows each) |
| 25 | Table header sort | Click Department, then Hire date twice | Department A→Z; newest then oldest hire; "Sort by" menu agrees | **Pass** | 2026-11-09, Playwright |
| 26 | Views on mobile (390px) | List and Table views | No page-level horizontal scroll; table scrolls inside its own box | **Pass** | 2026-11-09, Playwright (overflow 0) |

¹ Firebase **Email Enumeration Protection** is on by default, so a wrong password and an unknown user both return `auth/invalid-credential`. They cannot be shown as different messages unless the protection is disabled, which would weaken security. The app maps all three codes. **Mentor decision needed.**

All cases run on 2026-11-09 against the real system (MySQL + Spring Boot API + Firebase Auth + React), with the fake test account `ui-check@example.test`. Test employees used `bb-*@example.test` and were deleted afterwards.
