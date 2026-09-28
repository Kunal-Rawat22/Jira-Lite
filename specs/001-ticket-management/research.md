# Research: 001-ticket-management

## Stack — backend

**Decision:** Java 21, Spring Boot 3.x, Spring Data JPA, Bean Validation, Spring Data MongoDB. PostgreSQL for users/products/memberships. MongoDB for tickets, comments, activity.

**Rationale:** Stakeholder constraints plus FR-033 storage split. JPA applies only to relational master data; tickets are documents.

**Alternatives considered:** Postgres-only (constitution YAGNI) — rejected by spec. Separate ticket microservice — rejected (one API). Hibernate `ddl-auto=update` for schema — rejected (`rules/java-springboot.md`; Flyway for Postgres).

## Stack — frontend

**Decision:** React with TypeScript (`strict`). Production container: Nginx serving the static build and proxying `/api` to the backend. Dev may use the React dev server with a proxy; Compose production-like path is Nginx.

**Rationale:** Stakeholder constraint. TypeScript is appropriate because API enums/DTOs must match the contract without `any`.

**Alternatives considered:** Next.js App Router (current `frontend/` skeleton and constitution preference) — superseded for this feature by the React+Nginx constraint. SSR is not required for the MVP screens.

## Stack — infrastructure

**Decision:** Docker Compose services: PostgreSQL, MongoDB, backend (Spring Boot), frontend (Nginx). Credentials via environment variables.

**Rationale:** Stakeholder constraint. Matches local/demo deployment.

**Alternatives considered:** Redis (session/cache) — rejected (YAGNI, stakeholder “no unnecessary infrastructure”). Kafka / other brokers — rejected (no async domain events in spec).

## Architecture layers

**Decision:** Controller → Service interface → Service implementation → Repository. Controllers stay thin (HTTP + Bean Validation + DTO mapping). Implementations own transactions and call JPA and/or Mongo repositories. Map persistence types to DTOs in the service (or a dedicated mapper used by the service), never return entities/documents from controllers.

**Rationale:** Stakeholder constraint; constitution additional constraints (DTOs, thin controllers). Mockito tests mock the service interface or repositories as appropriate.

**Alternatives considered:** Exposing JPA entities as JSON — forbidden. Status logic in controllers or entity listeners — forbidden (constitution IV).

## State machine and status API

**Decision:** Explicit allowed-edge table (or equivalent) used only by a dedicated status-change operation (e.g. `changeStatus` on the ticket service). Field-update API MUST NOT accept `status`. Illegal edges → `INVALID_STATE_TRANSITION`. CLOSED/CANCELLED reject field edits and comments except the published reopen transition (to `REOPEN`) → `TICKET_FROZEN`.

**Rationale:** Constitution IV plus stakeholder “dedicated business operation”. Spec FR-012–FR-015, FR-022.

**Alternatives considered:** PATCH ticket with optional `status` (older `spec/api-contract.md`) — rejected for this feature so status cannot bypass the machine via a generic update. Client-only transitions — rejected (constitution III).

## Dual validation

**Decision:** UI blocks blank title, missing product (when required), empty comments, illegal status — no API call. Server still returns `400`/`409` for the same cases.

**Rationale:** Spec FR-011, FR-015, FR-026, FR-032.

**Alternatives considered:** UI-only — rejected (constitution III).

## Assignee and product on create

**Decision:** Null assignee → reporter. One membership → that product. Several → required `productId` from memberships. Zero memberships → cannot create.

**Rationale:** Spec US1 / FR-025 / FR-030.

**Alternatives considered:** Null assignee after persist (older `spec/`) — superseded in this feature spec.

## List filters

**Decision:** List/search is `POST /api/tickets/list` with a JSON **request payload** (`searchKey`, `status`, `assignee`, `reporter`, `product`, `user`, `size`, `page`), not GET query params. Empty arrays skip that filter. Omitted/null `searchKey` = no keyword filter; `""` accepted untrimmed; backend handles null vs empty string. Combine groups with AND, values with OR. Results scoped to the actor’s product memberships. Backend `page` zero-based; UI one-based. Page-size options are frontend-owned. No specified max `size`. List paginated, `createdAt` DESC.

**Rationale:** Spec US2 / FR-008 / FR-029.

**Alternatives considered:** Single-select only — rejected.

## Comments and activity

**Decision:** Comments are a flat collection keyed by ticket id; no parentId. Activity documents written only after successful mutations (field or status). `from`/`to` are per-field JSON maps of changed fields only.

**Rationale:** Spec US5 / US4 / FR-006 / FR-031.

**Alternatives considered:** Threaded comments — out of scope.

## Optimistic locking

**Decision:** Ticket `version` is a per-ticket integer (create = 1, +1 on success). Stale field update or stale status change → `STALE_VERSION`. Status operation requires the current version. Identity is JWT, not `X-User-Id`.

**Rationale:** FR-023. JPA `@Version` does not apply to Mongo tickets.

**Alternatives considered:** Last-write-wins — rejected.

## Testing

**Decision:** JUnit 5, Mockito, MockMvc for API and service/state-machine tests. Dual stores: test slices or containers as needed when persistence is proven; MockMvc + mocked services for HTTP mapping where that is sufficient.

**Rationale:** Stakeholder test stack plus `rules/testing.md`. Avoid extra test infra beyond what is needed to prove both stores.

## Product `spec/` vs this feature

**Decision:** This directory (`specs/001-ticket-management/`) is the working plan for the clarified feature. Ratified `spec/*.md` still describes a different MVP (four statuses, `CLOSED`→`OPEN` reopen, Postgres-only tickets, `URGENT` priority, ticket `key`, nullable assignee, Next.js). Per constitution II and the sync-spec skill, **do not silently overwrite `spec/`**. A human must accept the merge.

**Rationale:** Human Engineering Authority. Feature spec assumptions already record this split.
