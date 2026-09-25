# Implementation plan

Ordered phases **after** this documentation scaffold. Do not skip spec updates if a phase changes behavior.

## Phase 0 — Done

Repo layout, spec, rules, commands, docs, Postgres Compose only.

## Phase 1 — Database and backend skeleton

- Spring Boot project in `backend/` (Java 21, Spring Web, Validation, Data JPA, PostgreSQL).
- Flyway migrations for users, tickets, comments.
- Entities and repositories matching [data-model.md](data-model.md).
- Health endpoint; connect to Compose Postgres via env (no secrets in git).

## Phase 2 — Ticket API

- Implement [api-contract.md](api-contract.md): users list, tickets CRUD-lite, comments.
- Encode [state-machine.md](state-machine.md) in the service layer.
- Global exception handlers for 400 / 404 / 409.
- API tests from [test-strategy.md](test-strategy.md).

## Phase 3 — Frontend

- React + TypeScript + Vite in `frontend/`.
- Screens in [ui-flow.md](ui-flow.md): list, create, detail, comments.
- Shared API client typed to the contract.

## Phase 4 — Hardening

- Seed data for local demo.
- Optional Compose services for API and SPA.
- Then consider auth (out of current MVP).

## Dependencies

```
Phase 1 → Phase 2 → Phase 3 → Phase 4
```

Phase 2 must not start without migrations and a running Postgres. Phase 3 must not invent endpoints; extend [api-contract.md](api-contract.md) first.
