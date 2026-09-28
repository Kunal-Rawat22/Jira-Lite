# Implementation Plan: Support Ticket Management

**Branch**: `001-ticket-management` | **Date**: 2026-09-25 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-ticket-management/spec.md` plus stakeholder stack constraints (Java 21 / Spring Boot / JPA / Bean Validation / PostgreSQL / MongoDB / JUnit 5 / Mockito / MockMvc; React + TypeScript SPA served by Nginx; Docker Compose; Controller → Service interface → Service implementation → Repository; DTOs only; dedicated status operation; no Redis/Kafka).

## Summary

Web support-ticket app: create/list/detail, field updates, flat comments, keyword search, combined multi-select filters, product isolation, explicit six-status machine, Activity audit.

**Backend:** one Spring Boot 3.x app (Java 21). PostgreSQL holds users, products, and memberships (JPA). MongoDB holds tickets, comments, and activity (Spring Data MongoDB). Layers are Controller → Service interface → Service implementation → Repository. API JSON is DTOs only (no JPA or Mongo document types on the wire). Status changes go through a dedicated service operation that consults the state machine; generic field updates cannot set status.

**Frontend:** React + TypeScript SPA. Production image is Nginx serving the built static files and proxying `/api` to the backend. No Next.js in this feature’s delivery.

**Infra:** Docker Compose runs PostgreSQL, MongoDB, the backend container, and the frontend/Nginx container. No Redis, Kafka, or other brokers/caches.

Server owns all rules; UI blocks invalid submits without calling the API and still shows server errors. Identity is username/password login and JWT `Authorization` (not `X-User-Id`). Tests cover the machine, isolation, dual validation, and FR-038 frontend cases.

## Technical Context

**Language/Version**: Java 21; TypeScript (`strict`) for the React SPA

**Primary Dependencies**: Spring Boot 3.x (Web, Bean Validation, Data JPA, Data MongoDB); React; Nginx (production frontend container)

**Storage**: PostgreSQL (`users`, `products`, memberships); MongoDB (tickets, comments, activity)

**Testing**: JUnit 5, Mockito, MockMvc (Spring Boot Test); frontend component tests as needed for dual-validation (no-submit)

**Target Platform**: Local/Docker web app (browser + API). Compose publishes API and Nginx-served UI.

**Project Type**: Web application (frontend + backend)

**Performance Goals**: Interactive MVP (SC-001, SC-004), not a throughput SLA

**Constraints**: Thin controllers; DTOs; never expose JPA entities; status only via dedicated business operation + state machine; no Redis/Kafka; no secrets in git. Dual store is a stakeholder mandate (see Complexity Tracking).

**Scale/Scope**: Single org; at least one product; six statuses; no SSO

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Plan |
| --- | --- |
| I Specification First | Implement after this plan and `tasks.md` are human-reviewed |
| II Human Engineering Authority | Drafts until review/merge. Feature `specs/001-*` currently **diverges** from ratified `spec/` (see research.md); do not treat this plan as replacing `spec/` until a human accepts the merge |
| III Backend Business Rule Authority | Server validates (Bean Validation + service). UI checks do not replace server rejection |
| IV State Machine Integrity | Status only via dedicated service operation + allowed-edge table; `INVALID_STATE_TRANSITION` otherwise. Controllers, repositories, and field-update DTOs MUST NOT write status |
| V Testability | Table-driven transitions, isolation, empty comment/title, product on create; JUnit 5 / Mockito / MockMvc |
| VI Simple Architecture | **Exception:** MongoDB added because the spec mandates ticket/comment/activity there. **Frontend:** React SPA + Nginx instead of constitution’s preferred Next.js App Router — stakeholder constraint for this feature (static UI + reverse proxy, no extra runtime). Redis/Kafka explicitly out |
| VII Traceability | FRs mapped in data-model and contracts |
| VIII AI Context Discipline | Load spec.md, this plan, rules for the layer |
| IX Security | Env-based credentials; no secrets in spec |
| X Incremental Implementation | Postgres master data → Mongo ticket store → API + machine → React UI |

**Post-design re-check:** Dual store remains the only extra datastore. Dedicated status endpoint strengthens Principle IV. Validation and transitions stay on the server. Gate passes with the documented exceptions (MongoDB; React+Nginx vs Next.js). Unjustified brokers/caches are absent.

## Project Structure

### Documentation (this feature)

```text
specs/001-ticket-management/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/rest-api.md
├── spec.md
└── tasks.md             # /speckit-tasks — not created here
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/jiralite/tickets/
│   ├── api/              # controllers + request/response DTOs
│   ├── domain/           # enums, JPA entities (user/product), Mongo documents (ticket/comment/activity)
│   ├── service/          # interfaces + impl + state machine
│   ├── persistence/      # JPA repositories + Mongo repositories
│   └── error/
├── src/main/resources/db/migration/   # Flyway for PostgreSQL only
└── src/test/java/

frontend/
├── src/                  # React + TypeScript SPA
└── nginx.conf            # production: static files + /api proxy

docker-compose.yml        # postgres, mongodb, backend, frontend (Nginx)
```

**Structure Decision:** One Spring Boot app, one React SPA. Two databases, not two backends. Production UI is Nginx in a container, not a Node server.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Second datastore (MongoDB) besides PostgreSQL | Spec FR-033: tickets, comments, activity in MongoDB; users/products in Postgres | Postgres-only contradicts the stakeholder storage split |
| React SPA + Nginx instead of Next.js App Router (constitution VI preference) | Stakeholder plan constraint: React, TypeScript, Nginx production container | Next.js would add a Node production runtime not requested; SPA+Nginx matches Compose shape |
| Service interface + implementation (not a single service class) | Stakeholder layering constraint | Direct controller→impl would skip the requested seam for tests (Mockito against the interface) |
