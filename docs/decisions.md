# Decisions

Architecture decision records for Jira-Lite. Newest first.

## 2026-09-25 — Docs-first; apps deferred

**Status:** Accepted

`backend/` and `frontend/` are placeholders (`.gitkeep` only). Spec, rules, skills, commands, and docs are written so implementation can follow them. No Maven/npm projects until Phase 1–3 in [spec/implementation-plan.md](../spec/implementation-plan.md).

## 2026-09-25 — Repo-root layout

**Status:** Accepted

The requested `support-ticket-management/` tree is flattened onto this git root (`Jira-Lite`). Nesting another folder would duplicate the project name without benefit.

## 2026-09-25 — Stack (for future code)

**Status:** Accepted

- API: Java 21, Spring Boot 3.x, layered architecture
- UI: React + TypeScript + Vite
- DB: PostgreSQL 16 via root `docker-compose.yml`
- Identity: no auth in MVP; user ids in the body/header as a placeholder ([spec/architecture.md](../spec/architecture.md))

## 2026-09-25 — Ticket lifecycle

**Status:** Accepted

Statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`. Transitions and closed-ticket rules live only in [spec/state-machine.md](../spec/state-machine.md).
