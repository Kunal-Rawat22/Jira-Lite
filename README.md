# Jira-Lite — Support Ticket Management

A spec-driven learning project for a Jira-like support ticket system.

Current code is a **skeleton**: Spring Boot API (health + Postgres config) and a Next.js home page. Ticket features follow `spec/` and `rules/`.

## Folder map

| Path | Purpose |
| --- | --- |
| `spec/` | Product and engineering source of truth |
| `rules/` | Coding constraints for humans and AI |
| `skills/` | Cursor skill for keeping docs in sync |
| `commands/` | Prompts: review code, review spec, review PR, review code changes, generate tests, commit-and-push |
| `docs/` | Process, prompt history, and architecture decisions |
| `backend/` | Spring Boot 3.4, Java 21, Gradle |
| `frontend/` | Next.js (TypeScript, App Router) |
| `.specstory/history/` | Local conversation history placeholder |

## How to work

1. Change product behavior in `spec/` first (especially `requirements.md`, `data-model.md`, `api-contract.md`, `state-machine.md`).
2. Keep `rules/` in mind when implementing.
3. Use `commands/` prompts for reviews and test generation.
4. Record notable choices in `docs/decisions.md`. Log prompts in `docs/prompt-history.md` with **Mode** (`Plan`, `Ask`, `Debug`, `Agent`, or `Plan then Agent`).

See `docs/ai-context-strategy.md` for which files to load for a given task.

## Run

One command starts Postgres, the API, and Next.js:

```bash
docker compose up --build
```

- App: http://localhost:3000
- API health: http://localhost:8080/actuator/health

Use `--build` after code changes. Stop with `Ctrl+C`, or `docker compose up --build -d` then `docker compose down`.

### Optional: run apps on the host

If you prefer Gradle/npm locally (Postgres still via Compose):

```bash
docker compose up -d postgres
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

## Tests

- Backend: `cd backend && ./gradlew test` (context load; datasource auto-config excluded so Postgres is not required).
- Frontend: `cd frontend && npm run build`. Ticket UI tests come with Phase 3 ([spec/test-strategy.md](spec/test-strategy.md)).
