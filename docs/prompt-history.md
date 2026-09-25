# Prompt history

Log prompts that changed structure, product scope, or workflow. Do not paste secrets.

Each entry **must** include **Mode**: `Plan`, `Ask`, `Debug`, `Agent`, or `Plan then Agent` (plan first, then implemented in Agent).

## Template

```markdown
## YYYY-MM-DD — Short title

**Mode:** Plan then Agent

**Prompt:** …

**Decisions captured:**

- …
```

## 2026-09-25 — JRL-n commits, review commands, branch PR

**Mode:** Plan then Agent

**Prompt:** Commit all changes and push to GitHub. Add commands to review PR, review code changes, and commit-and-push. Commit messages must match the existing JRL-n template; document that in rules.

**Decisions captured:**

- Commit format: `JRL-<n>: <imperative summary>` ([rules/git.md](../rules/git.md)).
- Commands: `review-pr`, `review-code-changes`, `commit-and-push` (plus `.cursor/commands` slash commands).
- Default GitHub flow: feature branch and PR into `main`, not a direct push to `main`.

## 2026-09-25 — Prompt history records Cursor mode

**Mode:** Plan then Agent

**Prompt:** While maintaining prompt history, mention whether it was Plan, Ask, Debug, or Agent mode. Implement the template and backfill past entries.

**Decisions captured:**

- Every entry has a **Mode** line.
- Allowed values: Plan, Ask, Debug, Agent, Plan then Agent.
- Existing scaffold, Spring Boot/Next.js, and Compose entries tagged Plan then Agent.

## 2026-09-25 — One-command Compose stack

**Mode:** Plan then Agent

**Prompt:** All services should be up after `docker compose up`; no other commands to run.

**Decisions captured:**

- Default run path is `docker compose up --build`.
- Backend JRE image installs curl so `/actuator/health` healthchecks work.
- Frontend waits until the backend is healthy.
- Host Gradle/npm remains optional.

## 2026-09-25 — Spring Boot + Next.js skeletons

**Mode:** Plan then Agent

**Prompt:** Create a basic Spring Boot project using Gradle, Java 21, and Postgres; create a basic Next.js project with TypeScript.

**Decisions captured:**

- Gradle (not Maven); Spring Boot 3.4; Actuator health for Compose.
- Next.js App Router + TypeScript instead of Vite + nginx.
- Compose frontend port `3000:3000`; `API_INTERNAL_URL=http://backend:8080`.
- No ticket CRUD in this pass.

## 2026-09-25 — Project structure scaffold

**Mode:** Plan then Agent

**Prompt:** Create the complete project structure (`backend/`, `frontend/`, `spec/`, `rules/`, `skills/`, `commands/`, `docs/`, `.specstory/history/`, `docker-compose.yml`, `.gitignore`, `README.md`).

**Decisions captured:**

- Place the tree at the **repo root** (no nested `support-ticket-management/` folder).
- **Docs-first:** empty `backend/` and `frontend/` except `.gitkeep`; full spec/rules/skills/commands/docs.
- Postgres 16 via Compose for later API work; no app services yet.

See [decisions.md](decisions.md).
