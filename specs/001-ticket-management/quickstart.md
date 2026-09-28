# Quickstart validation: 001-ticket-management

Prove the feature after implementation. Not copy-paste application code.

## Prerequisites

- Docker Compose: PostgreSQL, MongoDB, backend (`:8080`), frontend Nginx (`:3000` or mapped host port)
- At least one product, memberships, and user rows in PostgreSQL
- Env-based DB credentials (no secrets in git)

## Backend

`cd backend && ./gradlew test` (JUnit 5, Mockito, MockMvc) and the contract in [contracts/rest-api.md](contracts/rest-api.md).

1. Create with title + reporter, no assignee → **201**, OPEN, assignee = reporter, product set (single membership).
2. Create with blank title → **400**.
3. Create without product when actor has two memberships → **400**.
4. List as user of product X does not include product Y tickets.
5. `GET /api/tickets` with multiple `status` and `assigneeId` → AND across groups.
6. `POST /api/tickets/{id}/status` allowed edges → **200** + activity row; CLOSED → OPEN → **409**; PATCH with `status` is not a supported contract (field PATCH does not change status).
7. PATCH fields while CLOSED → **409**; `POST .../status` with `REOPEN` → **200**.
8. Empty comment → **400**; comment on CLOSED → **409**; comments have no parent.
9. Stale `version` on PATCH or status POST → **409**.
10. Restart Compose; ticket still in MongoDB; user/product still in PostgreSQL.

## Frontend (React SPA via Nginx in Compose)

Open the Nginx-served origin (typically http://localhost:3000). API calls go to `/api` (Nginx proxy).

1. Blank title / empty comment / illegal status: inline error, **no** matching API call.
2. Priority dropdown LOW/MEDIUM/HIGH.
3. Empty assignee → assigned to reporter; product picker only if multiple memberships.
4. Multi-select filters and keyword search; no other-product rows.
5. Detail: Comments and Activity tabs; no reply control.
6. CLOSED/CANCELLED: no field save, no comment composer; reopen only via the status operation.

UI checks do not replace backend tests above.
