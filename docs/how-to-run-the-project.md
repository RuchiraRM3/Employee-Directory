# How to Run the Employee Directory Project

This guide explains the software you need and how to run the Employee Directory on your own computer. The project has three parts:

- **Web application:** React, TypeScript, and Vite (`web/`)
- **API:** Java and Spring Boot (`api/`)
- **Database:** MySQL (`db/`)

The web application runs at `http://localhost:5173` and the API runs at `http://localhost:8080`.

## Software Required

Install the following before starting:

| Software | Required version | Why it is needed |
| --- | --- | --- |
| Git | Any current version | To download or manage the project source code |
| Java Development Kit (JDK) | 17 or newer; JDK 21 recommended | To run the Spring Boot API |
| MySQL Server | 8.x | To store employee and department data locally |
| Node.js | 20 or newer | To run the React web application |
| npm | Installed with Node.js | To install and run web dependencies |
| Firebase account and project | Free Spark plan is sufficient | For email/password sign-in |

You do **not** need to install Maven separately. The API includes the Maven Wrapper (`api/mvnw`), which downloads the required Maven version automatically.

### Check installations

Open a terminal and run:

```bash
java -version
mysql --version
node --version
npm --version
git --version
```

If a command is not found, install that software and then restart the terminal.

## 1. Start MySQL and Create the Databases

Make sure the MySQL server is running. From the project root, run the following commands. They assume the MySQL `root` account has no password; add `-p` after `root` if it does.

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS employee_directory"
mysql -u root employee_directory < db/schema.sql
mysql -u root employee_directory < db/seed.sql
mysql -u root -e "CREATE DATABASE IF NOT EXISTS employee_directory_test"
```

What these commands do:

- Create the main `employee_directory` database.
- Create the database tables.
- Add safe sample departments and employees.
- Create `employee_directory_test`, which is required when running API tests.

> **Important:** Running `db/schema.sql` drops and recreates the project tables. Do not use it against a database containing real data.

## 2. Create a Firebase Project

Firebase is used for login. Complete these one-time steps in the [Firebase console](https://console.firebase.google.com):

1. Create a Firebase project. Google Analytics is optional and not required.
2. Go to **Build → Authentication → Get started → Sign-in method**.
3. Enable **Email/Password** authentication.
4. Go to **Project settings → General → Your apps** and add a **Web app**.
5. Go to **Project settings → Service accounts** and generate a new private key.
6. Save the downloaded service-account JSON file outside this repository, for example:

   ```text
   ~/.secrets/employee-directory-firebase.json
   ```

Keep this JSON file private. It is a server secret and must never be committed to Git or placed in the `web/` folder.

## 3. Configure and Start the API

Open a terminal at the project root and run:

```bash
cd api
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.secrets/employee-directory-firebase.json"
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### Optional database settings

The defaults expect MySQL on `localhost`, port `3306`, using the `root` account with no password. If your MySQL setup is different, set these before starting the API:

```bash
export DB_URL=jdbc:mysql://localhost:3306/employee_directory
export DB_USERNAME=root
export DB_PASSWORD=your-mysql-password
export APP_CORS_ORIGIN=http://localhost:5173
```

### Run without Firebase temporarily

For local API experimentation only, Firebase authentication can be disabled:

```bash
cd api
APP_AUTH_ENABLED=false ./mvnw spring-boot:run
```

Do not use this mode for a demonstration or deployment because the API will be unprotected.

## 4. Configure and Start the Web Application

Open a **second terminal** at the project root. Create the local Firebase configuration file:

```bash
cd web
cp .env.example .env.local
```

Open `web/.env.local` and replace the placeholder Firebase values with the web app configuration from Firebase Console → **Project settings → General → Your apps**.

Example:

```env
VITE_FIREBASE_API_KEY=your-api-key
VITE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your-project-id
VITE_FIREBASE_APP_ID=your-app-id
VITE_API_BASE_URL=http://localhost:8080
```

Then install the dependencies and start the web app:

```bash
npm install
npm run dev
```

Open `http://localhost:5173` in a browser. Register a new email/password account, then sign in.

## 5. Normal Startup Order

Each time you want to run the project:

1. Start MySQL.
2. Start the API in one terminal:

   ```bash
   cd api
   export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.secrets/employee-directory-firebase.json"
   ./mvnw spring-boot:run
   ```

3. Start the web app in another terminal:

   ```bash
   cd web
   npm run dev
   ```

4. Visit `http://localhost:5173`.

## Run Tests and Checks

Run API tests:

```bash
cd api
./mvnw test
```

Run web build and lint checks:

```bash
cd web
npm run build
npm run lint
```

## Common Problems

| Problem | Likely cause and solution |
| --- | --- |
| API cannot connect to MySQL | Start MySQL and check `DB_USERNAME`, `DB_PASSWORD`, and `DB_URL`. |
| `Unknown database 'employee_directory_test'` when testing | Create the test database using the command in step 1. |
| API says default Google credentials were not found | Set `GOOGLE_APPLICATION_CREDENTIALS` to the service-account JSON file. |
| Browser shows a CORS error | Ensure the web app is at `http://localhost:5173` and `APP_CORS_ORIGIN` has the same value. |
| All API requests return 401 | Ensure the frontend Firebase configuration and backend service-account JSON belong to the same Firebase project. |
| Browser shows `auth/invalid-api-key` or a blank page | Check that `web/.env.local` exists and all Firebase values were replaced. |
| Port 5173 or 8080 is already in use | Stop the other process using the port, or change the web origin and API CORS setting together. |

## Security Notes

- Use only local MySQL and fake/sample data for this project.
- Do not commit `.env.local`, MySQL passwords, or the Firebase service-account JSON file.
- Never share the Firebase service-account private key.
