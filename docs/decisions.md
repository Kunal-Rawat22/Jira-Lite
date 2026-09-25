# Decisions

Architecture decision records for Jira-Lite. Newest first.

## 2026-09-25 — JRL-n commits and PRs off main

**Status:** Accepted

Git commit subjects are `JRL-<ticket>: <imperative summary>`. Work ships via a feature branch and pull request into `main`. See [rules/git.md](../rules/git.md).

## 2026-09-25 — Prompt history includes Cursor mode

**Status:** Accepted

Entries in [prompt-history.md](prompt-history.md) include **Mode**: Plan, Ask, Debug, Agent, or Plan then Agent.

## 2026-09-25 — Compose is the default run path

**Status:** Accepted

`docker compose up --build` starts Postgres, Spring Boot, and Next.js. No extra Gradle/npm commands are required. Host `bootRun` / `npm run dev` remain optional for local iteration.

## 2026-09-25 — Spring Boot Gradle + Next.js skeletons

**Status:** Accepted

- API: Spring Boot 3.4, Java 21, Gradle wrapper in `backend/`. JPA + Postgres, Actuator `/actuator/health`. No ticket API yet.
- UI: Next.js TypeScript App Router in `frontend/` (not Vite). `/api` rewrites to Spring Boot. Docker image is Node standalone, not nginx.
- Compose runs Postgres (`support_ticket`), backend `:8080`, frontend `:3000`.
- Supercedes the “apps deferred” and “Vite SPA” decisions below for stack and scaffolding.

## 2026-09-25 — Docs-first; apps deferred

**Status:** Superceded (skeletons now exist)

`backend/` and `frontend/` were placeholders. Spec, rules, skills, commands, and docs were written first.

## 2026-09-25 — Repo-root layout

**Status:** Accepted

The requested `support-ticket-management/` tree is flattened onto this git root (`Jira-Lite`). Nesting another folder would duplicate the project name without benefit.

## 2026-09-25 — Stack (for future code)

**Status:** Superceded by “Spring Boot Gradle + Next.js skeletons”

- API: Java 21, Spring Boot 3.x, layered architecture (still true)
- UI: was React + TypeScript + Vite; now Next.js
- DB: Compose Postgres database `support_ticket`
- Identity: no auth in MVP; user ids in the body/header as a placeholder ([spec/architecture.md](../spec/architecture.md))

## 2026-09-25 — Ticket lifecycle

**Status:** Accepted

Statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`. Transitions and closed-ticket rules live only in [spec/state-machine.md](../spec/state-machine.md).
