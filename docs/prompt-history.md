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

## 2026-09-25 — Jira-Lite constitution v1.0.0

**Mode:** Agent

**Prompt:** Create the project constitution for a Support Ticket Management System using Specification-Driven Development and ten listed principles (spec first, human authority, backend rules, state machine, testability, simple architecture, traceability, AI context, security, incremental implementation).

**Decisions captured:**

- Ratified [`.specify/memory/constitution.md`](../.specify/memory/constitution.md) as version **1.0.0**.
- Product behavior stays in `spec/`; this file governs how work is done.

## 2026-09-25 — Project rules and Cursor rules

**Mode:** Plan then Agent

**Prompt:** Add testing, Java/Spring Boot, API, and frontend rules in `rules/*.md`, then create Cursor project rules from those markdown files.

**Decisions captured:**

- Merged requested bullets into existing `rules/` docs without dropping Jira-Lite specifics.
- Cursor rules live in `.cursor/rules/*.mdc` with file globs; git rule always applies.

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

## 2026-09-25 — Spec Kit plan: ticket management stack

**Mode:** Plan

**Prompt:** `/speckit-plan` with Java 21, Spring Boot, Spring Data JPA, Bean Validation, PostgreSQL, MongoDB, JUnit 5, Mockito, MockMvc; React + TypeScript SPA; Nginx production container; Compose (Postgres, Mongo, backend, frontend/Nginx); Controller → Service interface → implementation → Repository; DTOs only; dedicated status operation + state machine; no Redis/Kafka.

**Decisions captured:**

- Feature design lives under `specs/001-ticket-management/` (`plan.md`, `research.md`, `data-model.md`, `contracts/rest-api.md`, `quickstart.md`).
- Status changes: `POST /api/tickets/{id}/status` only; PATCH is fields-only.
- Tickets/comments/activity in MongoDB; users/products/memberships in PostgreSQL.
- Ratified `spec/` was **not** overwritten (lifecycle, storage, and Next.js still differ); human review required before `spec/` merge.

## 2026-09-25 — MongoDB in Docker Compose

**Mode:** Plan then Agent

**Prompt:** Update docker-compose and Dockerfiles to integrate MongoDB.

**Decisions captured:**

- Compose service `mongodb` (`mongo:7`, port 27017, volume `mongodb_data`, unauthenticated local URI, `mongosh` healthcheck).
- Backend waits on healthy Postgres and Mongo; `MONGO_URI=mongodb://mongodb:27017/support_ticket`.
- `application.yml` maps `spring.data.mongodb.uri` from `MONGO_URI` (localhost default for host `bootRun`). No Spring Data Mongo starter yet.
- Backend/frontend Dockerfiles unchanged (no Mongo client in the JVM image).

## 2026-09-28 — Git commit/PR workflow and preview-merge

**Mode:** Plan then Agent

**Prompt:** Change commit-and-push so we always pull origin main onto main, create the new branch from main, commit and push there, open a merge request, and switch back to main. Add a command to preview PRs, test them, merge into the destination branch, and pull origin to local.

**Decisions captured:**

- `/commit-and-push` always: stash if needed, `checkout main` + `pull origin main`, branch `cursor/jrl-<n>-…`, commit, push, `gh pr create` into `main`, `checkout main`.
- New `/preview-merge-pr`: review diff, `./gradlew test` + `npm run build` on the PR head, `gh pr merge --merge` into the PR base, then pull that base locally.
- GitHub PRs are the merge-request equivalent. `review-pr` remains review-only.

## 2026-09-28 — Track Spec Kit specs and SpecStory history

**Mode:** Agent

**Prompt:** `/commit-and-push` include skills/documentation, specs/*, .specstory/*, .cursor/*

**Decisions captured:**

- Feature working papers under `specs/001-ticket-management/` are in git.
- Companion skill `.cursor/skills/sync-spec-from-speckit/` and a pointer from `skills/documentation/skill.md`.
- SpecStory session markdown under `.specstory/history/` is tracked (root `.gitignore` no longer ignores those dumps).




