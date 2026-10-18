# CO2 Management System

Web application for recording, evaluating and visualising corporate CO2 emissions across multiple sites. Prototype developed as part of a university case study.

## Tech stack

- Backend: Spring Boot 4, Java 25, Spring Data JPA, Spring Security with JWT
- Database: PostgreSQL 18, started automatically via Docker Compose
- Frontend: React with TypeScript, Vite, MUI

## Prerequisites

- Java 25
- Node.js 24
- Docker (Docker Desktop on Windows and macOS)

## Getting started

### 1. Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows use `.\mvnw.cmd spring-boot:run`.

The PostgreSQL database starts automatically with the backend via Docker Compose. Docker must be running. On first start, demo sites and accounts are created.

### 2. Start the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

### 3. Open the application

Open **http://127.0.0.1:5173** in your browser.

Please use `127.0.0.1` instead of `localhost`. On Windows, `localhost` may cause delays of about two seconds per request.

## Demo accounts

| Username | Role | Site |
|---|---|---|
| `admin` | Administration | – |
| `nachhaltigkeit` | Sustainability officer | – |
| `fuehrungskraft` | Manager | Werk Nord |
| `benutzer` | User | Werk Süd |

All demo accounts share the password configured as `app.demo-password` in `backend/src/main/resources/application.properties`. These accounts are intended for local demonstration only.

## Ports

| Service | Port |
|---|---|
| Frontend (Vite dev server) | 5173 |
| Backend | 8081 |
| PostgreSQL | 5432 |

If a local PostgreSQL installation already uses port 5432, change the host port in `backend/compose.yaml`, for example to `5433:5432`.

## Running the tests

```bash
cd backend
./mvnw test
```

On Windows use `.\mvnw.cmd test`. The integration tests start their own PostgreSQL container via Testcontainers, so Docker must be running.

## Configuration

The JWT signing key is read from the environment variable `JWT_SECRET`. If it is not set, a development key from `application.properties` is used. Never use the development key outside local development.

## Demo data

On first start the backend creates the emission sources with example factors for 2025 and 2026, three sites, four demo accounts and example entries from October 2025 to September 2026 (Werk West from July 2026). The emission factors are example values, not official factors.

To reset all data to this initial state, stop the backend and run in the `backend` folder:

    docker compose down -v

## License

MIT