# Employee Directory (HR)

A small, local-only HR employee directory: sign in with Firebase, search employees by name, filter by department, view details, and add or edit employees. Department and job-title changes are written to an audit table.

| Layer | Tech |
|---|---|
| Database | MySQL 8 (`db/`) |
| API | Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Bean Validation, Firebase Admin SDK 9.9 (`api/`) |
| Web | React 19, TypeScript, Vite 8, React Router 7, Firebase JS SDK 13 (`web/`) |

```
React (Vite :5173) --Bearer Firebase ID token--> Spring Boot API (:8080) --JPA--> MySQL 8 (:3306)
        └── Firebase Auth (email/password)            └── Firebase Admin SDK verifies every token
```

Design notes, decisions and open questions are in `implementation.md` (see §21). Task progress is in `PROGRESS.md`.

## 1. Prerequisites (versions verified on 2026-11-09)

- JDK 17+ (tested with OpenJDK 21.0.13). Maven itself is optional, because `api/mvnw` downloads it.
- MySQL 8 running locally (tested with 8.4.13).
- Node.js 20+ and npm (tested with Node 24.22.1 and npm 11.19.1).
- A **personal** Firebase project (free tier is enough).

## 2. Database

Use a **local** MySQL only. These commands assume the `root` user. Add `-p` if your root user has a password.

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS employee_directory"
mysql -u root employee_directory < db/schema.sql   # DROPS and recreates the 3 tables
mysql -u root employee_directory < db/seed.sql     # 4 departments, 16 fake employees, 5 change rows
mysql -u root -t employee_directory < db/queries.sql   # the five report queries
```

For the API tests, also create the test database. The tests rebuild it from the same scripts every time they run:

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS employee_directory_test"
```

The ER diagram is in `db/er-diagram.mmd` (Mermaid; it renders on GitHub).

**Already have a database from before the archive (delete) feature?** Run the one-time migration instead of rebuilding, so you keep your data:

```bash
mysql -u root employee_directory < db/migrations/2026-11-09-employee-archive.sql
```

The API refuses to start against a database without the `archived_at`/`archived_by` columns (`ddl-auto=validate`).

## 3. Firebase setup (one time)

1. Go to the [Firebase console](https://console.firebase.google.com) and choose **Add project**. Google Analytics is not needed.
2. Open **Build → Authentication → Get started → Sign-in method**, then enable **Email/Password**.
3. Open **Project settings → General → Your apps** and add a **Web app**. Copy its config values into `web/.env.local` (step 5).
4. Open **Project settings → Service accounts** and choose **Generate new private key**. Save the JSON file **outside this repository**, for example `~/.secrets/employee-directory-firebase.json`. This file is a real secret: never commit it, never paste it into chat or AI tools, and never put it in `web/`.

### Quick start: one command

After steps 1–3 and `web/.env.local` (step 5) are set up:

```bash
./run.sh              # checks MySQL, creates + seeds the DB on first run, starts API (:8080) and web (:5173)
./run.sh --reset-db   # same, but first rebuilds the DB from db/schema.sql + db/seed.sql (drops the tables)
```

Ctrl+C stops both apps. The script uses `GOOGLE_APPLICATION_CREDENTIALS` if it is set; otherwise it falls back to a git-ignored `web/*firebase-adminsdk*.json`. Sections 4–5 describe the manual steps the script runs for you.

## 4. Run the API

```bash
cd api
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.secrets/employee-directory-firebase.json"
# Optional overrides; defaults are shown:
# export DB_URL=jdbc:mysql://localhost:3306/employee_directory DB_USERNAME=root DB_PASSWORD=
# export APP_CORS_ORIGIN=http://localhost:5173
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`. Every endpoint requires `Authorization: Bearer <Firebase ID token>`. If the credentials are missing, the API **refuses to start**, so it never runs unprotected by accident.

> **No Firebase project yet?** Run `APP_AUTH_ENABLED=false ./mvnw spring-boot:run` to try the API with curl. This turns off all protection and logs a warning. Use it on your own machine only, never in the demo.

### Endpoints

| Method | Path | Success | Errors |
|---|---|---|---|
| GET | `/employees?name=&departmentId=` | 200 list; filters are optional and combine with AND; name matching is a case-insensitive substring | 400 for a non-numeric `departmentId` |
| GET | `/employees/{id}` | 200 | 404 (also for archived employees) |
| POST | `/employees` | 201 + `Location` | 400 validation or unknown department, 409 duplicate email |
| PUT | `/employees/{id}` | 200 | 400, 404, 409 |
| DELETE | `/employees/{id}` | 204: **archives** the employee (soft delete) | 404 if unknown or already archived |
| GET | `/departments` | 200 | |

"Delete" never removes data: it sets `archived_at`/`archived_by`, hides the employee from search, details and edit, and keeps their row and change history. Their email stays reserved (409 explains it belongs to an archived employee). The report queries in `db/queries.sql` leave archived employees out.

Every endpoint can also return 401 for a missing, invalid or expired token. All errors share one JSON shape:

```json
{"timestamp":"…","status":409,"code":"DUPLICATE_EMAIL","message":"An employee with this email already exists.","fieldErrors":{"email":"Email is already in use."}}
```

Request body for POST and PUT. All fields are required. `status` is one of `ACTIVE`, `ON_LEAVE` or `TERMINATED`; these values are provisional until HR confirms them.

```json
{"name":"Rowan Tate","email":"rowan.tate@example.test","jobTitle":"Analyst","departmentId":4,"hireDate":"2026-01-05","status":"ACTIVE"}
```

## 5. Run the web app

```bash
cd web
cp .env.example .env.local      # then fill in the Firebase web config from step 3.3
npm install
npm run dev                     # http://localhost:5173
```

The browser never calls the API directly: it calls `/api/...` on the web app's own address, and the Vite dev server forwards those requests to `http://localhost:8080`. So leave `VITE_API_BASE_URL` empty.

**Opening the app from another device (port forwarding, tunnel, phone):** forward **only port 5173**. The API stays private on this machine. Vite listens on `localhost` only, which is what port-forwarding and tunnel tools connect to. To reach it directly over your LAN instead, run `npm run dev -- --host`; this exposes the dev server to everyone on that network. A tunnel with its own hostname may also need `server.allowedHosts` in `vite.config.ts`. The forwarding is a dev-server feature: a production build would need a reverse proxy doing the same.

Register an account, then sign in. Signed-out users are always redirected to `/login`.

The directory can be shown as **Tiles**, **List** or **Table** (switcher above the results). The choice is kept in the URL (`?view=`) and remembered per browser. In Table view the column headers sort the list, in step with the "Sort by" menu. **Delete** is on an employee's page, behind a confirmation dialog.

The login screen does not link to registration. The Register page lives at the path you set in `VITE_REGISTER_PATH` in `web/.env.local` (for example `/join-` plus a random suffix); if it is empty, there is no register page. This only hides the page from casual visitors: the path is visible in the app's JavaScript, and any signed-in Firebase user can use the API.

## 6. Tests

```bash
cd api && ./mvnw test        # 41 JUnit/Spring Boot tests against local MySQL (employee_directory_test)
cd web && npm run build && npm run lint
```

The API tests drive the real filters, controllers, service and JPA against MySQL. Each test rolls back. Firebase is replaced by a Mockito stub of `TokenVerifier`, so the tests need no credentials. The manual browser checklist is in `docs/manual-test-checklist.md`.

## 7. Troubleshooting

| Symptom | Fix |
|---|---|
| API exits with `Your default credentials were not found` | Set `GOOGLE_APPLICATION_CREDENTIALS` to the service-account JSON path (or use `APP_AUTH_ENABLED=false` locally). |
| `Access denied for user` on startup or tests | Set `DB_USERNAME` / `DB_PASSWORD`, or check that MySQL is running (`brew services list`). |
| `Unknown database 'employee_directory_test'` | Run the `CREATE DATABASE` command from section 2. |
| "Cannot reach the server. Is the API running?" | Start the API (`./run.sh` starts both). From another device: leave `VITE_API_BASE_URL` empty so requests go through the web app's `/api` forwarding. |
| Browser shows a CORS error | Only happens if `VITE_API_BASE_URL` points straight at the API; then the web origin must exactly equal `APP_CORS_ORIGIN` (default `http://localhost:5173`). |
| Every API call returns 401 in the browser | The web and API apps point at different Firebase projects; compare `VITE_FIREBASE_PROJECT_ID` with the service account's `project_id`. |
| Blank page, or `auth/invalid-api-key` in the browser console | `web/.env.local` is missing or incomplete. |

## Security rules

- Never commit `.env.local`, `application-local.properties`, DB passwords or the service-account JSON (`.gitignore` covers the first two).
- Use fake data only. Seed emails use the reserved `example.test` domain.
- Local machine only. Do not deploy, and do not point the scripts at a shared database.
