# Architecture

## System shape

```
[Next.js TypeScript App Router] --REST/JSON--> [Spring Boot API] --JPA--> [PostgreSQL]
```

- **Frontend**: Next.js (TypeScript, App Router) in `frontend/`. Talks only to the API via `/api` rewrites; no direct DB access.
- **Backend**: Spring Boot 3.4 (Java 21, Gradle) in `backend/`. Layered: controller → service → repository. Skeleton is in place (health + JPA/Postgres config); ticket APIs come later.
- **Database**: PostgreSQL 15 via root `docker-compose.yml`, database name `support_ticket`.

## Backend layers

| Layer | Responsibility |
| --- | --- |
| Controller | HTTP mapping, request validation annotations, map DTOs; no business rules |
| Service | Ticket state machine, assignment rules, key generation |
| Repository | Spring Data JPA persistence |
| Entity | JPA mappings aligned with [data-model.md](data-model.md) |

REST only. No GraphQL, message bus, or cache in MVP.

## API surface

All JSON APIs live under `/api` as defined in [api-contract.md](api-contract.md). CORS should allow the local SPA origin during development.

## Identity (MVP)

No auth filter. Clients send user ids in request bodies (`reporterId`, `authorId`) or as an optional `X-User-Id` header for UI convenience. The API still validates that referenced users exist. This is a placeholder, not production security.

## Persistence

- Single Postgres database `support_ticket` (Compose). Datasource URL/user/password from `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Flyway when ticket tables are added; schema matches [data-model.md](data-model.md). Until then `ddl-auto` is `none`.
- Transactions at the service method boundary.

## Compose

Root `docker-compose.yml` runs Postgres, the Spring Boot API (`8080`), and Next.js (`3000`). Next.js proxies `/api/*` to the backend using `API_INTERNAL_URL`.

## Future (not this repo phase)

- Spring Security
- Reverse proxy / HTTPS
