---
description: "Task list for 001-ticket-management implementation"
---

# Tasks: Support Ticket Management

**Input**: Design documents from `/specs/001-ticket-management/`

**Source of truth (approved only)**: [spec.md](spec.md), [plan.md](plan.md), [data-model.md](data-model.md), [contracts/rest-api.md](contracts/rest-api.md)

**Prerequisites**: plan.md, spec.md, data-model.md, contracts/rest-api.md

**Tests**: Required by FR-019 (backend) and FR-038 (frontend). Tests are listed with the slice they prove; remaining contract/isolation/machine coverage is in the later test phase.

**Organization**: Phases follow the approved implementation dependency order (baseline → stores → auth → envelope → catalogs → ticket APIs → frontend → compose). User-story labels (`[US1]`–`[US7]`) remain for traceability.

**Do not implement**: Redis, Kafka, SSO, attachments, notifications, realtime, distributed transactions, GET query-param list/search, `reportee`, status on field PATCH, Next.js production runtime.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story from spec.md (`[US1]` create, `[US2]` list/search, `[US3]` detail, `[US4]` field update, `[US5]` comments, `[US6]` status machine, `[US7]` sign-in)
- Setup, foundational, and polish tasks have **no** story label
- Every task includes layer (`backend` / `frontend` / `database` / `infrastructure` / `test`) and a file path

## Path Conventions

- Backend: `backend/src/main/java/com/jiralite/tickets/` (`api/`, `domain/`, `service/`, `persistence/`, `error/`)
- Backend tests: `backend/src/test/java/com/jiralite/tickets/`
- Flyway: `backend/src/main/resources/db/migration/`
- Frontend: `frontend/src/` (React + TypeScript SPA); production `frontend/nginx.conf`
- Infra: `docker-compose.yml`

## User stories (spec.md)

| ID | Priority | Story |
| --- | --- | --- |
| US1 | P1 | Create a support ticket |
| US2 | P1 | Browse and find tickets |
| US3 | P1 | Inspect ticket details |
| US4 | P2 | Update ticket fields |
| US5 | P2 | Comment on a ticket |
| US6 | P1 | Move a ticket through allowed statuses |
| US7 | P1 | Sign in |

Suggested MVP after Phase 2: **US7 (backend) + US1** (authenticated create). Full list/detail/status UI follows.

---

## Phase 1: Setup — project / backend / frontend baseline

**Purpose**: Align the repo with plan.md (Java 21 Spring Boot 3.x; React + TypeScript SPA; Controller → Service interface → impl → Repository). Existing Next.js app is **not** the delivery target for this feature.

**Independent test**: Backend compiles; frontend is a TypeScript SPA that builds; no product APIs yet.

- [x] T001 Infrastructure: Confirm Java 21 / Spring Boot 3.x Gradle baseline and package root `com.jiralite.tickets` in `backend/build.gradle` and `backend/src/main/java/com/jiralite/tickets/TicketsApplication.java`
- [x] T002 Backend: Add Spring Data MongoDB, Flyway, Spring Security, and JWT libraries (no Redis/Kafka) in `backend/build.gradle`
- [x] T003 Backend: Create empty layer packages `api/`, `domain/`, `service/`, `persistence/`, `error/` under `backend/src/main/java/com/jiralite/tickets/`
- [x] T004 [P] Frontend: Replace the Next.js app with a React + TypeScript (`strict`) SPA skeleton (Vite or equivalent; no Next.js runtime) in `frontend/package.json` and `frontend/src/`
- [x] T005 [P] Infrastructure: Document non-secret env placeholders (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MONGO_URI`, JWT secret via env) in `.env.example` (never commit real secrets)
- [x] T006 Backend: Keep `spring.jpa.hibernate.ddl-auto: none` and env-based Postgres/Mongo URIs in `backend/src/main/resources/application.yml`

---

## Phase 2: Foundational — PostgreSQL schema, Flyway, JPA

**Purpose**: PostgreSQL owns users, products, memberships (FR-033). UUID PKs, `timestamptz` UTC.

**⚠️ CRITICAL**: Ticket Mongo writes must validate these rows first. No user-story ticket APIs until T007–T016 exist.

- [x] T007 Database: Add Flyway config for PostgreSQL only in `backend/src/main/resources/application.yml` and `backend/src/main/resources/db/migration/`
- [x] T008 Database: Create `products` table (`id` UUID PK, `name` required unique, `created_at` timestamptz) in `backend/src/main/resources/db/migration/V1__products.sql`
- [x] T009 Database: Create `users` table (`id` UUID PK, `username` unique required, `password` BCrypt hash column, `email` unique required, `display_name` required, `created_at`) in `backend/src/main/resources/db/migration/V2__users.sql`
- [x] T010 Database: Create `user_product` membership table (`user_id`, `product_id` FKs, `role` required, unique `(user_id, product_id)`) in `backend/src/main/resources/db/migration/V3__user_product.sql`
- [x] T011 [P] Backend: Add JPA `Product` entity (`id`, `name` required unique, `createdAt` server-set) in `backend/src/main/java/com/jiralite/tickets/domain/Product.java`
- [x] T012 [P] Backend: Add JPA `User` entity (`id`, `username`, `password` never serialized, `email`, `displayName`, `createdAt`) in `backend/src/main/java/com/jiralite/tickets/domain/User.java`
- [x] T013 [P] Backend: Add JPA `UserProduct` entity and `ProductRole` enum (`PRODUCT_OWNER`, `PRODUCT_MANAGER`, `DEVELOPER`, `BA`, `QA`) in `backend/src/main/java/com/jiralite/tickets/domain/UserProduct.java` and `backend/src/main/java/com/jiralite/tickets/domain/ProductRole.java`
- [x] T014 [P] Backend: Add JPA repositories `ProductRepository`, `UserRepository`, `UserProductRepository` in `backend/src/main/java/com/jiralite/tickets/persistence/`
- [x] T015 Test: Assert Flyway creates tables and unique constraints in `backend/src/test/java/com/jiralite/tickets/persistence/FlywaySchemaTest.java`
- [x] T016 Database: Add deterministic local/dev seed (idempotent/reproducible): at least one product, users, and memberships; known/deterministic UUIDs where appropriate; BCrypt password hashes; no real secrets in `backend/src/main/resources/db/migration/` or `backend/src/main/resources/db/seed/`

---

## Phase 3: Foundational — MongoDB documents and repositories

**Purpose**: MongoDB owns tickets, comments, activity (FR-033). Ticket `id` UUID string. No distributed transaction.

- [x] T017 Backend: Enable Spring Data MongoDB mapping under `com.jiralite.tickets` in `backend/src/main/resources/application.yml`
- [x] T018 [P] Backend: Add Mongo `Ticket` document with `id`, `productId` required, `title` required 1–40 chars as submitted (no auto-trim; whitespace-only invalid), `description` optional max 1000 chars as submitted (no auto-trim), `status`, `priority`, `reporterId` required immutable, `assigneeId` required after persist, `version` integer, `createdAt`/`updatedAt` in `backend/src/main/java/com/jiralite/tickets/domain/Ticket.java`
- [x] T019 [P] Backend: Add Mongo `Comment` document (`id`, `ticketId` required, no parent id, `authorId`, `body` required 1–200 chars as submitted no auto-trim, whitespace-only invalid, `createdAt` immutable) in `backend/src/main/java/com/jiralite/tickets/domain/Comment.java`
- [x] T020 [P] Backend: Add Mongo `Activity` document (`id`, `ticketId`, `actorId`, `at`, `field`, `from`/`to` per-field JSON maps of changed fields only) in `backend/src/main/java/com/jiralite/tickets/domain/Activity.java`
- [x] T021 [P] Backend: Add Mongo repositories `TicketRepository`, `CommentRepository`, `ActivityRepository` in `backend/src/main/java/com/jiralite/tickets/persistence/`
- [x] T022 Test: Persist/load a Ticket document only (no API) in `backend/src/test/java/com/jiralite/tickets/persistence/TicketMongoRepositoryTest.java`

---

## Phase 4: Foundational — authentication, BCrypt, JWT (US7 backend)

**Purpose**: FR-034 / US7. Username/password login; JWT in `Authorization`; TTL 30 minutes; BCrypt hashes; identity from JWT. No SSO.

**Independent test**: Valid login returns JWT; bad login and missing JWT use `AUTHENTICATION_FAILED`.

- [x] T023 [US7] Backend: Configure `BCryptPasswordEncoder` (no specified cost factor) in `backend/src/main/java/com/jiralite/tickets/service/SecurityConfig.java`
- [x] T024 [US7] Backend: Issue JWT with 30-minute TTL from env secret in `backend/src/main/java/com/jiralite/tickets/service/JwtService.java`
- [x] T025 [US7] Backend: Add `LoginRequest` / login response DTOs (password never echoed) in `backend/src/main/java/com/jiralite/tickets/api/dto/LoginRequest.java`
- [x] T026 [US7] Backend: Implement `POST /api/auth/login` (no JWT required) in `backend/src/main/java/com/jiralite/tickets/api/AuthController.java` and `backend/src/main/java/com/jiralite/tickets/service/AuthService.java`
- [x] T027 [US7] Backend: Require `Authorization: Bearer <JWT>` on all `/api` routes except login; set SecurityContext from JWT in `backend/src/main/java/com/jiralite/tickets/api/JwtAuthFilter.java`
- [x] T028 [P] [US7] Test: MockMvc login success (200, JWT in `data`, TTL 30 minutes) and invalid credentials (401, `code`=`AUTHENTICATION_FAILED`, envelope `status`=`failed`, `data`=null); stored password remains BCrypt (not plaintext) in `backend/src/test/java/com/jiralite/tickets/api/AuthControllerTest.java`
- [x] T029 [P] [US7] Test: Missing/invalid JWT on a protected stub returns 401 `AUTHENTICATION_FAILED` in `backend/src/test/java/com/jiralite/tickets/api/JwtAuthFilterTest.java`

---

## Phase 5: Foundational — DTOs and response/error envelope

**Purpose**: FR-035. Every response is `{ message, code, status, data }`. Backend i18n maps `code` → `message`. DTOs only; never serialize JPA/Mongo types.

- [x] T030 Backend: Add `ApiResponse<T>` (`message`, `code`, `status` `success`|`failed`, `data`) in `backend/src/main/java/com/jiralite/tickets/api/dto/ApiResponse.java`
- [x] T031 Backend: Add i18n message bundles keyed by result `code` (no invented fixed success copy) in `backend/src/main/resources/messages.properties` and `backend/src/main/java/com/jiralite/tickets/error/MessageResolver.java`
- [x] T032 Backend: Map ticket-domain codes `VALIDATION_ERROR` (400), `TICKET_NOT_FOUND` (404), `PRODUCT_ACCESS_DENIED` (403), `INVALID_STATE_TRANSITION` (409), `TICKET_FROZEN` (409), `STALE_VERSION` (409) and `AUTHENTICATION_FAILED` (401; not a ticket-domain code) in `backend/src/main/java/com/jiralite/tickets/error/ErrorCodes.java` and `backend/src/main/java/com/jiralite/tickets/error/GlobalExceptionHandler.java`
- [x] T033 Backend: Add shared DTOs `TicketResponse`, `CommentResponse`, `ActivityResponse`, `UserResponse` (never password), `ProductResponse`, `PageResponse` (`content`, `page`, `size`, `totalElements`, `totalPages`; `page` zero-based) in `backend/src/main/java/com/jiralite/tickets/api/dto/`
- [x] T034 Test: Failed and success envelopes always include `message`, `code`, `status`, `data`; error `data` is null in `backend/src/test/java/com/jiralite/tickets/error/ApiResponseEnvelopeTest.java`
- [x] T035 Test: Jackson serialization of controllers never includes JPA entity or Mongo document types in `backend/src/test/java/com/jiralite/tickets/api/DtoOnlySerializationTest.java`

---

## Phase 6: Foundational — product / user / membership APIs

**Purpose**: FR-037 catalogs. PostgreSQL-backed `GET /api/users` and `GET /api/products`. Membership roles do not restrict assignment beyond product membership.

**Independent test**: Actor sees only products they belong to; user picker can be scoped to a membership product.

- [x] T036 Backend: Implement membership lookup (user–product, role stored but unused for assignee extra rules) in `backend/src/main/java/com/jiralite/tickets/service/MembershipService.java`
- [x] T037 [P] Backend: Implement JWT-protected `GET /api/products` returning `ProductResponse` list of actor memberships in `backend/src/main/java/com/jiralite/tickets/api/ProductController.java`
- [x] T038 [P] Backend: Implement JWT-protected `GET /api/users` for filter/assignee pickers (PostgreSQL; optionally scoped to a product the actor belongs to) in `backend/src/main/java/com/jiralite/tickets/api/UserController.java`
- [x] T039 Test: Non-member cannot obtain another product via `GET /api/products`; user catalog does not leak other-product-only users when scoped in `backend/src/test/java/com/jiralite/tickets/api/CatalogControllerTest.java`

**Checkpoint**: Foundation ready — ticket user stories may start.

---

## Phase 7: Ticket domain model and validation (blocks US1–US6)

**Purpose**: Enums, constraints, service seam, dual-store read-before-write (no 2PC).

- [x] T040 [P] Backend: Add `TicketStatus` (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`, `REOPEN`) and `TicketPriority` (`LOW`, `MEDIUM`, `HIGH`) in `backend/src/main/java/com/jiralite/tickets/domain/TicketStatus.java` and `backend/src/main/java/com/jiralite/tickets/domain/TicketPriority.java`
- [x] T041 Backend: Add Bean Validation + service rules: title required 1–40 as submitted, no auto-trim, whitespace-only invalid; description max 1000 no auto-trim; unknown priority/status → `VALIDATION_ERROR` in `backend/src/main/java/com/jiralite/tickets/api/dto/` and `backend/src/main/java/com/jiralite/tickets/service/TicketValidation.java`
- [x] T042 Backend: Add `TicketService` interface (create, list, get, patch fields, change status, comments, activity) in `backend/src/main/java/com/jiralite/tickets/service/TicketService.java`
- [x] T043 Backend: Before each Mongo write, validate only the PostgreSQL data relevant to that operation (users, products, and/or memberships as needed — do not query every Postgres table for every call); no distributed transaction. Postgres owns users/products/memberships; Mongo owns tickets/comments/activity in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T044 Test: Unknown priority/status strings map to `VALIDATION_ERROR` (not `INVALID_STATE_TRANSITION`) in `backend/src/test/java/com/jiralite/tickets/service/TicketValidationTest.java`

---

## Phase 8: User Story 1 — Create a support ticket (Priority: P1) 🎯 MVP

**Goal**: `POST /api/tickets` creates a ticket. Reporter from JWT (never client `reporterId`). Status OPEN, version 1. Assignee omitted → reporter. Product from single membership or required `productId`. Assignee must belong to the product.

**Independent test**: Create with title + membership product; OPEN; version 1; assignee default; blank title 400; non-member assignee 400.

### Tests for User Story 1

- [x] T045 [P] [US1] Test: Contract MockMvc `POST /api/tickets` 201 envelope + `TicketResponse` in `data`; rejects `reporterId`/`status` in body in `backend/src/test/java/com/jiralite/tickets/api/TicketCreateContractTest.java`

### Implementation for User Story 1

- [x] T046 [US1] Backend: Add `CreateTicketRequest` (`title`, `description`, `priority` default MEDIUM, `assigneeId` nullable, `productId` nullable; no reporter/status) in `backend/src/main/java/com/jiralite/tickets/api/dto/CreateTicketRequest.java`
- [x] T047 [US1] Backend: Implement create: reporter=JWT; status always `OPEN` (never `REOPEN`); version=1; timestamps; omitted assignee → reporter; persist Mongo only after Postgres checks in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T048 [US1] Backend: Product rules — one membership uses that product; several require `productId` in memberships else `VALIDATION_ERROR`; zero memberships cannot create; product not a membership → `PRODUCT_ACCESS_DENIED` in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T049 [US1] Backend: Reject assignee not in chosen product (`VALIDATION_ERROR`); after persist `assigneeId` never null in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T050 [US1] Backend: Expose `POST /api/tickets` (201) thin controller in `backend/src/main/java/com/jiralite/tickets/api/TicketController.java`
- [x] T051 [P] [US1] Test: Assignee omitted → stored assignee = reporter; priority omitted → MEDIUM; illegal priority → `VALIDATION_ERROR` in `backend/src/test/java/com/jiralite/tickets/service/TicketCreateServiceTest.java`
- [x] T052 [P] [US1] Test: Blank/whitespace title and title > 40 / description > 1000 → `VALIDATION_ERROR` and no Mongo insert in `backend/src/test/java/com/jiralite/tickets/api/TicketCreateValidationTest.java`
- [x] T053 [P] [US1] Test: Missing product with two memberships → `VALIDATION_ERROR`; single membership auto-product; unauthenticated → `AUTHENTICATION_FAILED` in `backend/src/test/java/com/jiralite/tickets/api/TicketCreateProductTest.java`
- [x] T054 [US1] Test: Assignee outside product → `VALIDATION_ERROR`; reporter not taken from client body in `backend/src/test/java/com/jiralite/tickets/api/TicketCreateAssigneeTest.java`

**Checkpoint**: US1 independently testable via API.

---

## Phase 9: User Story 2 — Browse and find tickets (Priority: P1)

**Goal**: `POST /api/tickets/list` JSON body (not query params). Fields `searchKey`, `status`, `assignee`, `reporter` (never `reportee`), `product`, `user`, `size`, `page`. Default page=0, size=20. No backend max size. Membership isolation always.

**Independent test**: Two products; member sees only own products; AND/OR filters; search on title/description only.

### Tests for User Story 2

- [x] T055 [P] [US2] Test: Contract `POST /api/tickets/list` body shape; response `data` is `Page` with zero-based `page`; empty `content` still paginated in `backend/src/test/java/com/jiralite/tickets/api/TicketListContractTest.java`

### Implementation for User Story 2

- [x] T056 [US2] Backend: Add `TicketListRequest` with `searchKey`, `status`, `assignee`, `reporter`, `product`, `user`, `size`, `page` in `backend/src/main/java/com/jiralite/tickets/api/dto/TicketListRequest.java`
- [x] T057 [US2] Backend: Defaults page=0 size=20; apply requested `size` with no backend maximum; order `createdAt` DESC in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T058 [US2] Backend: `searchKey` omitted/null → no keyword filter; `""` accepted as provided (no trim/transform); implement null vs empty-string search check; case-insensitive substring on title and description only (not comments); multi-word allowed in `backend/src/main/java/com/jiralite/tickets/persistence/TicketRepository.java`
- [x] T059 [US2] Backend: Combined multi-select: groups AND, values OR; empty arrays skip that group; `user` = reporter OR assignee in set; `product` limited to actor memberships; membership hides other products even if filters ask in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T060 [US2] Backend: `POST /api/tickets/list` in `backend/src/main/java/com/jiralite/tickets/api/TicketController.java`
- [x] T061 [P] [US2] Test: Isolation — user in X never lists Y tickets; union of X+Y for dual member in `backend/src/test/java/com/jiralite/tickets/api/TicketListIsolationTest.java`
- [x] T062 [P] [US2] Test: Two statuses AND assignees; empty arrays; `reporter` field name only; comments not searched in `backend/src/test/java/com/jiralite/tickets/api/TicketListFilterSearchTest.java`
- [x] T063 [US2] Test: Pagination page 0/1, size 20 vs custom size, empty page envelope in `backend/src/test/java/com/jiralite/tickets/api/TicketListPaginationTest.java`
- [x] T064 [US2] Test: Zero memberships → empty membership-scoped list (not other products) in `backend/src/test/java/com/jiralite/tickets/api/TicketListZeroMembershipTest.java`

**Checkpoint**: US2 independently testable via API.

---

## Phase 10: User Story 3 — Inspect ticket details (Priority: P1)

**Goal**: `GET /api/tickets/{id}` with membership isolation. Unknown id `TICKET_NOT_FOUND`; other product `PRODUCT_ACCESS_DENIED` and no ticket payload.

**Independent test**: Member sees full `TicketResponse`; non-member 403 empty data.

- [x] T065 [P] [US3] Test: Contract GET 200 / 404 / 403 envelopes in `backend/src/test/java/com/jiralite/tickets/api/TicketGetContractTest.java`
- [x] T066 [US3] Backend: Implement get-by-id mapping to `TicketResponse` in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T067 [US3] Backend: `GET /api/tickets/{id}` in `backend/src/main/java/com/jiralite/tickets/api/TicketController.java`
- [x] T068 [US3] Test: Non-member GET does not return ticket fields (`data` null) in `backend/src/test/java/com/jiralite/tickets/api/TicketGetIsolationTest.java`

**Checkpoint**: US3 independently testable (comments/activity endpoints in later phases).

---

## Phase 11: User Story 4 — Update ticket fields (Priority: P2)

**Goal**: `PATCH /api/tickets/{id}` with current `version`. MUST NOT include `status`. Version starts at 1; +1 only after successful update. Stale → `STALE_VERSION`, no data/version change. Assignee cannot be cleared; must belong to ticket product. CLOSED/CANCELLED → `TICKET_FROZEN`. Reporter immutable.

**Independent test**: Patch OPEN title; version 2; PATCH with status 400; stale 409; CLOSED 409.

### Tests for User Story 4

- [x] T069 [P] [US4] Contract test: Verify PATCH `/api/tickets/{id}` requires `version`, allows `title`, `description`, `priority`, and `assigneeId` as individually optional fields, accepts a valid partial body such as `{ "version": 1, "title": "Updated title" }`, preserves omitted fields, rejects `status`, and rejects an explicitly null `assigneeId` in `backend/src/test/java/com/jiralite/tickets/api/TicketPatchContractTest.java`

### Implementation for User Story 4

- [x] T070 [US4] Backend: Add `UpdateTicketRequest` for `PATCH /api/tickets/{id}`: `version` required; `title`, `description`, `priority`, and `assigneeId` each optional; PATCH may update one or more of those fields; omitted fields stay unchanged; do not treat the four editable fields as all mandatory; do not accept `status` (`status` present → `VALIDATION_ERROR`); `assigneeId` cannot be cleared/null; omitted `assigneeId` leaves the existing assignee unchanged. Example valid body: `{ "version": 1, "title": "Updated title" }` in `backend/src/main/java/com/jiralite/tickets/api/dto/UpdateTicketRequest.java`
- [x] T071 [US4] Backend: Allow field edits only for OPEN, IN_PROGRESS, RESOLVED, REOPEN; CLOSED/CANCELLED → `TICKET_FROZEN`; failed update persists nothing in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T072 [US4] Backend: Optimistic version: mismatch → `STALE_VERSION` no store change; success → version +1 and `updatedAt` in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T073 [US4] Backend: Reject null/empty assignee after create and assignee not in `ticket.productId` (`VALIDATION_ERROR`); roles do not add extra assignee rules; do not change `reporterId` in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T074 [US4] Backend: Title/description same length/whitespace/no-trim rules as create in `backend/src/main/java/com/jiralite/tickets/service/TicketValidation.java`
- [x] T075 [US4] Backend: `PATCH /api/tickets/{id}` in `backend/src/main/java/com/jiralite/tickets/api/TicketController.java` (controllers/repos MUST NOT set status)
- [x] T076 [P] [US4] Test: Successful field PATCH increments version by 1; failed/stale does not in `backend/src/test/java/com/jiralite/tickets/api/TicketPatchVersionTest.java`
- [x] T076A [P] [US4] Test: Partial PATCH with only one or a subset of title/description/priority/assigneeId changes only the supplied fields, preserves omitted fields, and increments version exactly once in `backend/src/test/java/com/jiralite/tickets/api/TicketPatchPartialUpdateTest.java`
- [x] T077 [P] [US4] Test: Status in PATCH → `VALIDATION_ERROR` and unchanged status/fields in `backend/src/test/java/com/jiralite/tickets/api/TicketPatchRejectsStatusTest.java`
- [x] T078 [US4] Test: Frozen CLOSED/CANCELLED ticket rejects field PATCH with TICKET_FROZEN, leaves ticket version unchanged, and creates no Activity in `backend/src/test/java/com/jiralite/tickets/api/TicketFrozenUpdateTest.java`
- [x] T079 [US4] Test: Clear assignee and out-of-product assignee → `VALIDATION_ERROR`; product isolation 403 on PATCH in `backend/src/test/java/com/jiralite/tickets/api/TicketPatchAssigneeIsolationTest.java`

**Checkpoint**: US4 independently testable (Activity write wired in Phase 14).

---

## Phase 12: User Story 6 — State machine and dedicated status operation (Priority: P1)

**Goal**: Status changes **only** via `POST /api/tickets/{id}/status` and FR-013. Self-transitions illegal. Unknown status `VALIDATION_ERROR`. Illegal edge `INVALID_STATE_TRANSITION`. Version +1 on success. Field PATCH still must not change status.

**Independent test**: Table-driven allowed and rejected transitions; CLOSED→OPEN 409; OPEN→OPEN 409.

### Tests for User Story 6

- [x] T080 [P] [US6] Test: Contract `POST /api/tickets/{id}/status` with `version` and target `status` in `backend/src/test/java/com/jiralite/tickets/api/TicketStatusContractTest.java`

### Implementation for User Story 6

- [x] T081 [US6] Backend: Encode allowed edges only — OPEN→IN_PROGRESS|CANCELLED; IN_PROGRESS→RESOLVED|CANCELLED; RESOLVED→CLOSED|REOPEN; CLOSED→REOPEN; CANCELLED→REOPEN; REOPEN→IN_PROGRESS|CANCELLED — in `backend/src/main/java/com/jiralite/tickets/service/TicketStateMachine.java`
- [x] T082 [US6] Backend: Dedicated `changeStatus` on `TicketService`; controllers/repositories MUST NOT write status except through this operation in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T083 [US6] Backend: Add `ChangeStatusRequest` (`version`, target `status`) and `POST /api/tickets/{id}/status` in `backend/src/main/java/com/jiralite/tickets/api/dto/ChangeStatusRequest.java` and `backend/src/main/java/com/jiralite/tickets/api/TicketController.java`
- [x] T084 [US6] Backend: Success: status update, version +1, `updatedAt`; failure: no partial ticket write; stale version `STALE_VERSION`; membership 403; missing 404 in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T085 [US6] Test: Table-driven **every allowed** FR-013 transition succeeds in `backend/src/test/java/com/jiralite/tickets/service/TicketStateMachineAllowedTest.java`
- [x] T086 [US6] Test: Table-driven **every rejected** pair including all self-transitions and documented illegals (CLOSED→OPEN, RESOLVED→OPEN, CANCELLED→OPEN, IN_PROGRESS→CLOSED) → `INVALID_STATE_TRANSITION`, status unchanged in `backend/src/test/java/com/jiralite/tickets/service/TicketStateMachineRejectedTest.java`
- [x] T087 [P] [US6] Test: Unknown status string → `VALIDATION_ERROR`; create still OPEN never REOPEN in `backend/src/test/java/com/jiralite/tickets/api/TicketStatusValidationTest.java`
- [x] T088 [P] [US6] Test: Stale version on status POST → `STALE_VERSION` no change; REOPEN then field-edit allowed (same working rules as OPEN) in `backend/src/test/java/com/jiralite/tickets/api/TicketStatusVersionReopenTest.java`

**Checkpoint**: US6 independently testable.

---

## Phase 13: User Story 5 — Comments (Priority: P2)

**Goal**: Flat comments; no replies; no Activity for comments. Author = JWT. Body 1–200 as submitted, no auto-trim. CLOSED/CANCELLED `TICKET_FROZEN`.

**Independent test**: Two comments on ticket A not on B; empty body 400; CLOSED 409.

- [x] T089 [P] [US5] Test: Contract GET/POST comments envelopes in `backend/src/test/java/com/jiralite/tickets/api/CommentContractTest.java`
- [x] T090 [US5] Backend: `GET /api/tickets/{id}/comments` chronological flat list; `POST` body `{ "body" }` only (no `authorId`) in `backend/src/main/java/com/jiralite/tickets/api/TicketCommentController.java`
- [x] T091 [US5] Backend: Allow comments on OPEN, IN_PROGRESS, RESOLVED, REOPEN; CLOSED/CANCELLED `TICKET_FROZEN`; empty/whitespace/over-200 `VALIDATION_ERROR`; no parent comment field in `backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java`
- [x] T092 [P] [US5] Test: Empty/whitespace and 201-char body rejected, nothing stored, no trim in `backend/src/test/java/com/jiralite/tickets/api/CommentValidationTest.java`
- [x] T093 [P] [US5] Test: CLOSED/CANCELLED comment `TICKET_FROZEN`; isolation 403; comments belong to one ticket only in `backend/src/test/java/com/jiralite/tickets/api/CommentFrozenIsolationTest.java`
- [x] T094 [US5] Test: Successful comment does **not** insert Activity in `backend/src/test/java/com/jiralite/tickets/api/CommentDoesNotCreateActivityTest.java`

**Checkpoint**: US5 independently testable.

---

## Phase 14: Activity audit (US4 + US6)

**Goal**: FR-031. Exactly one Activity document per successful field-update request; exactly one per successful status change; none on failure; comments never. `from`/`to` per-field JSON maps of **changed fields only**. `GET /api/tickets/{id}/activity`.

**Independent test**: Multi-field PATCH → one Activity; status change → one Activity; failed PATCH → zero.

- [x] T095 [US4] Backend: After successful field PATCH, write one Activity (`field` e.g. `fields`) with `from`/`to` maps containing only changed keys (title, priority, assigneeId, …) in `backend/src/main/java/com/jiralite/tickets/service/ActivityRecorder.java`
- [x] T096 [US6] Backend: After successful status change, write one Activity (`field`=`status`) with `from`/`to` `{ "status": "..." }` in `backend/src/main/java/com/jiralite/tickets/service/ActivityRecorder.java`
- [x] T097 [US3] Backend: `GET /api/tickets/{id}/activity` chronological `ActivityResponse`; membership 403 / 404 in `backend/src/main/java/com/jiralite/tickets/api/TicketActivityController.java`
- [x] T098 [P] [US4] Test: Single-field vs multi-field PATCH each create exactly one Activity with only changed keys; failure creates none in `backend/src/test/java/com/jiralite/tickets/api/ActivityFieldUpdateTest.java`
- [x] T099 [P] [US6] Test: Successful status → one Activity; illegal/stale status → none in `backend/src/test/java/com/jiralite/tickets/api/ActivityStatusChangeTest.java`
- [x] T100 [US3] Test: Activity GET isolation; UI payload is stored `from`/`to` only (no extra history fields) in `backend/src/test/java/com/jiralite/tickets/api/ActivityGetIsolationTest.java`

**Checkpoint**: Detail can load comments + activity APIs.

---

## Phase 15: User Story 7 — Frontend authentication (Priority: P1)

**Goal**: Sign-in UI stores JWT for `Authorization`. Invalid login shows `AUTHENTICATION_FAILED` message. Missing JWT must not look successful.

**Independent test**: Login then call `/api/products`; logout/expired token shows error.

- [x] T101 [US7] Frontend: API client attaches `Authorization: Bearer` and parses `{ message, code, status, data }` in `frontend/src/api/client.ts`
- [x] T102 [US7] Frontend: Login screen username/password → `POST /api/auth/login`; persist JWT for later calls in `frontend/src/pages/LoginPage.tsx` and `frontend/src/auth/session.ts`
- [x] T103 [US7] Frontend: On `AUTHENTICATION_FAILED` or missing JWT, do not treat session as signed in; show backend `message` in `frontend/src/auth/session.ts`
- [x] T104 [P] [US7] Test: Login success stores token; invalid credentials show error and no authenticated session in `frontend/src/auth/LoginPage.test.tsx`

---

## Phase 16: Frontend ticket list / search / filter / pagination (US2)

**Goal**: UI calls `POST /api/tickets/list`. API page 0 displayed as page 1. Default page 0 size 20. Page-size dropdown bottom-right (frontend-owned options). Filter catalogs from Postgres APIs; six statuses hardcoded.

**Independent test**: Default payload page 0 size 20; UI shows page 1; filters AND/OR; no other-product rows.

- [x] T105 [US2] Frontend: List page columns title, status, priority, assignee, reporter, version, product in `frontend/src/pages/TicketListPage.tsx`
- [x] T106 [US2] Frontend: Always send list JSON including `searchKey` (null when unused); empty filter arrays; field `reporter` never `reportee` in `frontend/src/api/tickets.ts`
- [x] T107 [US2] Frontend: Map UI page 1 ↔ API page 0; size dropdown bottom-right; no invented backend max size in `frontend/src/pages/TicketListPage.tsx`
- [x] T108 [US2] Frontend: Multi-select filters status/assignee/reporter/user/product; options from `GET /api/users` and `GET /api/products`; status = six FR-017 values in `frontend/src/components/TicketFilters.tsx`
- [x] T109 [US2] Frontend: Keyword search (no auto-trim); empty list uses envelope empty `content` messaging in `frontend/src/pages/TicketListPage.tsx`
- [x] T110 [P] [US2] Test: Default request page 0 size 20; UI label page 1; `searchKey` null omitted-or-null behavior in `frontend/src/pages/TicketListPage.test.tsx`
- [x] T111 [P] [US2] Test: Combined filters + search; isolation (fixture does not render other-product tickets) in `frontend/src/pages/TicketListFilters.test.tsx`

---

## Phase 17: Frontend ticket detail / comments / activity (US3, US5)

**Goal**: Detail shows FR-018 fields, comments, Activity beside Comments. Activity UI shows stored `from`/`to` only. No reply control. Comments are not activity events.

**Independent test**: Open known ticket; tabs; 403/404 messaging without leaking fields.

- [x] T112 [US3] Frontend: Detail view id, title, description, priority, status, assignee, reporter, version, product, timestamps in `frontend/src/pages/TicketDetailPage.tsx`
- [x] T113 [US3] Frontend: Adjacent Comments and Activity tabs; Activity renders `from`/`to` values only in `frontend/src/components/CommentsActivityTabs.tsx`
- [x] T114 [US5] Frontend: Flat comment list; no reply-to-comment action; composer posts `{ body }` in `frontend/src/components/CommentList.tsx`
- [x] T115 [US3] Frontend: TICKET_NOT_FOUND / PRODUCT_ACCESS_DENIED show i18n `message`; do not display ticket payload in `frontend/src/pages/TicketDetailPage.tsx`
- [x] T116 [P] [US3] Test: Detail renders fields + adjacent tabs; comments not listed as activity in `frontend/src/pages/TicketDetailPage.test.tsx`
- [x] T117 [P] [US5] Test: No reply control in comments UI in `frontend/src/components/CommentList.test.tsx`

---

## Phase 18: Frontend ticket create / edit / status flows (US1, US4, US6)

**Goal**: Create, field edit, dedicated status control. Priority dropdown LOW/MEDIUM/HIGH default MEDIUM. Product picker only when multiple memberships.

**Independent test**: Create appears OPEN in list; legal status move; illegal status not submitted.

- [x] T118 [US1] Frontend: Create form title, optional description, priority dropdown (not free-typed), optional assignee, product picker iff >1 membership in `frontend/src/pages/TicketCreatePage.tsx`
- [x] T119 [US1] Frontend: One membership → no product picker (server uses that product); zero memberships → cannot submit, meaningful explanation in `frontend/src/pages/TicketCreatePage.tsx`
- [x] T120 [US4] Frontend: Field edit form (title, description, priority, assignee) calls PATCH without `status`; includes current `version` in `frontend/src/pages/TicketEditForm.tsx`
- [x] T121 [US6] Frontend: Status control uses only `POST /api/tickets/{id}/status` with current `version` and target status in `frontend/src/components/StatusControl.tsx`
- [x] T122 [P] [US1] Test: Successful create flow; default priority MEDIUM; omitted assignee UX in `frontend/src/pages/TicketCreatePage.test.tsx`
- [x] T123 [P] [US4] Test: Successful field update flow in `frontend/src/pages/TicketEditForm.test.tsx`
- [x] T124 [P] [US6] Test: Successful allowed status-change flow in `frontend/src/components/StatusControl.test.tsx`

---

## Phase 19: Frontend validation and error / stale / frozen / network handling (FR-011, FR-015, FR-023, FR-038)

**Goal**: Screen MUST NOT call the API for empty title, empty comment, illegal status, frozen field/comment submits. Server remains authoritative. Stale version: show backend message, reload latest, never silent overwrite. Network/timeout/unexpected: generic message, keep existing UI data, allow retry.

**Independent test**: Spy on fetch — blank title/comment/illegal status produce **zero** matching API calls.

- [x] T125 [US1] Frontend: Empty/whitespace title and title > 40 / description > 1000: show validation, **do not** call create in `frontend/src/pages/TicketCreatePage.tsx`
- [x] T126 [US1] Frontend: Multi-product create without product: **do not** submit in `frontend/src/pages/TicketCreatePage.tsx`
- [x] T127 [US4] Frontend: Empty/whitespace title on edit: **do not** call PATCH in `frontend/src/pages/TicketEditForm.tsx`
- [x] T128 [US5] Frontend: Empty/whitespace comment: **do not** POST comments in `frontend/src/components/CommentList.tsx`
- [x] T129 [US6] Frontend: Validate against the complete FR-013 matrix (OPEN→IN_PROGRESS, OPEN→CANCELLED, IN_PROGRESS→RESOLVED, IN_PROGRESS→CANCELLED, RESOLVED→CLOSED, RESOLVED→REOPEN, CLOSED→REOPEN, CANCELLED→REOPEN, REOPEN→IN_PROGRESS, REOPEN→CANCELLED); all other pairs including every self-transition are illegal; show an appropriate error and **do not** call the status API for an illegal transition; backend remains authoritative in `frontend/src/components/StatusControl.tsx`
- [x] T130 [US4][US5] Frontend: CLOSED/CANCELLED: hide/disable field save and comment composer; only reopen via status operation in `frontend/src/pages/TicketDetailPage.tsx`
- [x] T131 [US4] Frontend: `STALE_VERSION`: show backend `message`, then GET latest ticket; never overwrite with stale form in `frontend/src/pages/TicketEditForm.tsx`
- [x] T132 Frontend: API down/timeout/unexpected: do not mark success; generic user-facing message; keep existing data; retry available; recognized codes show backend `message` in `frontend/src/api/client.ts`
- [x] T133 [P] [US1] Test: No-submit create validation (empty title, over-length, missing product when many memberships) in `frontend/src/pages/TicketCreateValidation.test.tsx`
- [x] T134 [P] [US5] Test: No-submit empty comment in `frontend/src/components/CommentValidation.test.tsx`
- [x] T135 [US6] Frontend: Test that every illegal status transition from the approved state-transition matrix, including self-transitions and at least one unlisted transition, is blocked before the status API is called in `frontend/src/components/StatusControl.test.tsx`
- [x] T136 [P] [US4] Test: CLOSED/CANCELLED UI restrictions; stale-version message then reload in `frontend/src/pages/TicketStaleFrozen.test.tsx`
- [x] T137 [P] Test: Backend error `message` display vs generic unexpected/network error and preserved UI data in `frontend/src/api/client.error.test.ts`

---

## Phase 20: Remaining backend and frontend tests (FR-019, FR-038, SC-005)

**Purpose**: Close coverage gaps not already proven in story phases. Do not duplicate passing tests; add only missing cases.

- [x] T138 Test: Backend FR-019 matrix — state machine, CLOSED/CANCELLED immutability, stale version, assignee default, blank title, empty comments, product isolation, missing product on create, unknown enums, self-transitions, assignee product membership — in `backend/src/test/java/com/jiralite/tickets/TicketFr019CoverageTest.java`
- [x] T139 Test: Security — unauthenticated ticket/list/create/patch/status/comment/activity/catalog calls `AUTHENTICATION_FAILED` in `backend/src/test/java/com/jiralite/tickets/api/SecurityAuthenticationTest.java`
- [x] T140 Test: Membership isolation on list, get, patch, status, comments, activity (403, no payload) in `backend/src/test/java/com/jiralite/tickets/api/MembershipIsolationSuiteTest.java`
- [x] T141 Test: Envelope + HTTP mapping for all listed codes including `AUTHENTICATION_FAILED` not in ticket-domain list in `backend/src/test/java/com/jiralite/tickets/error/ErrorEnvelopeContractTest.java`
- [x] T142 Test: Frontend FR-038 checklist file covering create validation, empty title, comment validation, priority selection, one-vs-many product, illegal status no-call, backend errors, stale version, frozen UI, list filters/search, successful create/update/status in `frontend/src/fr038/Fr038Coverage.test.tsx`

---

## Phase 21: Docker Compose / Nginx integration

**Purpose**: Compose runs PostgreSQL, MongoDB, backend, frontend/Nginx. Nginx serves SPA and proxies `/api`. No Redis/Kafka.

- [x] T143 Infrastructure: Compose services postgres, mongodb, backend, frontend in `docker-compose.yml` (env credentials, no secrets in git)
- [x] T144 Infrastructure: Backend image uses `backend/Dockerfile`
- [x] T145 Frontend: Production Nginx static files + `/api` reverse proxy in `frontend/nginx.conf` and `frontend/Dockerfile`
- [x] T146 Test: Compose health — backend `/actuator/health`, Nginx origin serves SPA in `specs/001-ticket-management/quickstart.md` (manual) and optional `backend/src/test/java/com/jiralite/tickets/ActuatorHealthTest.java`

---

## Phase 22: Polish — end-to-end / integration verification

**Purpose**: Quickstart walkthrough SC-001–SC-011 without changing requirements.

- [ ] T147 Test: Execute backend quickstart steps 1–10 (create, list payload, isolation, status, frozen, comments, stale version, restart persistence Mongo vs Postgres) using `specs/001-ticket-management/quickstart.md`
- [ ] T148 Test: Execute frontend quickstart steps 1–6 (no-submit validation, dropdowns, filters, detail tabs, frozen/reopen) against Nginx origin in `specs/001-ticket-management/quickstart.md`
- [x] T149 Infrastructure: Confirm restart leaves tickets/comments/activity in MongoDB and users/products/memberships in PostgreSQL (`docker-compose.yml` volumes)
- [x] T150 Polish: Run `cd backend && ./gradlew test` and frontend test script; fix only defects vs approved spec (do not add out-of-scope features)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 Setup**: Start immediately
- **Phases 2–7 Foundational**: Sequential stores → auth → envelope → catalogs → ticket domain; **blocks** ticket story APIs
- **Phase 8 US1 Create**: After T044
- **Phase 9 US2 List**: After US1 (needs tickets) or parallel with seeded Mongo fixtures
- **Phase 10 US3 Detail**: After create
- **Phase 11 US4 Patch**: After get + version field
- **Phase 12 US6 Status**: After domain enums; may proceed after US1 without US4 UI
- **Phase 13 US5 Comments**: After get + frozen rules
- **Phase 14 Activity**: After successful US4 and US6 writes
- **Phases 15–19 Frontend**: After corresponding APIs (auth, list, detail, create/patch/status)
- **Phase 20 Tests**: After story slices; fill FR-019/FR-038 gaps
- **Phase 21 Compose/Nginx**: Can draft Dockerfiles earlier; full stack after SPA exists
- **Phase 22 E2E**: After Compose

### User Story Dependencies

- **US7 backend (T023–T029)**: After users table; blocks all protected APIs
- **US1**: After foundational + auth
- **US2 / US3**: After US1 data (or fixtures)
- **US4**: After US3 version semantics
- **US6**: After US1; independent of comments
- **US5**: After US3 frozen statuses
- **Activity**: After US4 and US6 success paths
- **US7 frontend**: After login API
- Frontend stories follow backend APIs above

### Within Each User Story

- Contract tests first where listed; ensure they fail before implementation
- DTOs → service → controller
- Isolation and validation tests with the slice
- Do not serialize persistence types

### Parallel Opportunities

- T004/T005; T011–T014; T018–T021; T028/T029; T037/T038
- US1 tests T051–T054; US2 T061–T062; US4 T076–T078 and T076A; US6 T087–T088; US5 T092–T093; Activity T098–T099
- Frontend tests marked `[P]` within a phase
- After foundation, backend US5 comments vs US6 status can proceed in parallel on different files (`TicketCommentController.java` vs `TicketStateMachine.java`)

---

## Parallel Example: User Story 1

```bash
# Contract + validation tests (fail first):
Task: "Contract MockMvc POST /api/tickets in backend/src/test/java/com/jiralite/tickets/api/TicketCreateContractTest.java"
Task: "Blank title / over-length in backend/src/test/java/com/jiralite/tickets/api/TicketCreateValidationTest.java"

# Then implementation:
Task: "CreateTicketRequest in backend/src/main/java/com/jiralite/tickets/api/dto/CreateTicketRequest.java"
Task: "TicketServiceImpl create in backend/src/main/java/com/jiralite/tickets/service/TicketServiceImpl.java"
```

---

## Implementation Strategy

### MVP First (US7 backend + US1)

1. Phases 1–7 (setup + foundation)
2. Phase 8 create API
3. Stop and validate US1 + login independently
4. Demo: authenticated create → OPEN, version 1

### Incremental Delivery

1. List/search (US2) → detail (US3) → patch (US4) → status (US6) → comments (US5) → activity
2. Frontend login → list → detail → create/edit/status → dual validation
3. Compose + quickstart
4. Each slice keeps prior envelope and isolation behavior

### Parallel Team Strategy

1. Together: Phases 1–7
2. Then: Dev A US1/US2, Dev B US6/US4, Dev C US5/Activity, Dev D frontend US7/list
3. Integrate on envelope + JWT only

---

## Notes

- `[P]` = different files, no incomplete-task coupling
- `[USn]` maps to spec.md stories
- Ratified `spec/` (singular) is **not** this feature’s source of truth until a human merge; implement from `specs/001-ticket-management/`
- Commit after each task or logical group (`JRL-n: …`)
- Avoid GET list, `reportee`, Redis/Kafka/SSO, status-on-PATCH, distributed transactions
