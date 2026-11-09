#!/usr/bin/env bash
# One command to run the Employee Directory locally: MySQL check -> API (:8080) + web (:5173).
#   ./run.sh              start both apps (Ctrl+C stops both)
#   ./run.sh --reset-db   first rebuild the local DB from db/schema.sql + db/seed.sql (DROPS the tables)
set -euo pipefail
cd "$(dirname "$0")"
ROOT="$PWD"
DB_NAME=employee_directory
MYSQL=(mysql -u "${DB_USERNAME:-root}")
[ -n "${DB_PASSWORD:-}" ] && MYSQL+=("-p$DB_PASSWORD")

fail() { echo "✗ $*" >&2; exit 1; }
ok()   { echo "✓ $*"; }

# 1. Ports must be free (a previous run may still be alive).
for port in 8080 5173; do
  pid=$(lsof -ti tcp:$port -sTCP:LISTEN || true)
  [ -z "$pid" ] || fail "Port $port is in use by PID $pid. Stop it first: kill $pid"
done

# 2. MySQL running + database present (create and seed it on first run).
"${MYSQL[@]}" -e "SELECT 1" >/dev/null 2>&1 || fail "Cannot connect to MySQL. Start it: brew services start mysql@8.4"
if [ "${1:-}" = "--reset-db" ] || ! "${MYSQL[@]}" -e "SELECT 1 FROM $DB_NAME.employee LIMIT 1" >/dev/null 2>&1; then
  "${MYSQL[@]}" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME"
  "${MYSQL[@]}" "$DB_NAME" < db/schema.sql
  "${MYSQL[@]}" "$DB_NAME" < db/seed.sql
  ok "Database $DB_NAME rebuilt from db/schema.sql + db/seed.sql"
else
  ok "Database $DB_NAME found (use --reset-db to rebuild)"
fi

# 3. Firebase Admin credentials for the API: env var wins, else the git-ignored key in web/.
if [ -z "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]; then
  key=$(ls "$ROOT"/web/*firebase-adminsdk*.json 2>/dev/null | head -1 || true)
  [ -n "$key" ] || fail "No Firebase service-account key. Set GOOGLE_APPLICATION_CREDENTIALS (README §3)."
  export GOOGLE_APPLICATION_CREDENTIALS="$key"
fi
ok "Firebase Admin key: ${GOOGLE_APPLICATION_CREDENTIALS#"$ROOT/"}"

# 4. Web config: warn (don't block) if web/.env.local lacks the Firebase web values.
grep -q '^VITE_FIREBASE_API_KEY=.' web/.env.local 2>/dev/null \
  || echo "⚠ web/.env.local has no VITE_FIREBASE_* values (see web/.env.example); sign-in will not work."

# 5. Web dependencies on first run.
[ -d web/node_modules ] || (cd web && npm install)

# 6. Start both; Ctrl+C (or either one exiting) stops both.
pids=()
cleanup() { trap - INT TERM EXIT; echo; echo "Stopping…"; kill "${pids[@]}" 2>/dev/null || true; wait 2>/dev/null || true; }
trap cleanup INT TERM EXIT

(cd api && exec ./mvnw -q spring-boot:run) & pids+=($!)
(cd web && exec npm run dev -- --strictPort) & pids+=($!)

echo "API → http://localhost:8080   Web → http://localhost:5173   (Ctrl+C to stop)"
# bash 3.2 has no `wait -n`: poll until one of the two exits.
while kill -0 "${pids[0]}" 2>/dev/null && kill -0 "${pids[1]}" 2>/dev/null; do sleep 1; done
echo "One of the apps exited; shutting down the other."
