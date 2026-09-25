# Architecture

## System shape

```
[React + TypeScript SPA] --REST/JSON--> [Spring Boot API] --JPA--> [PostgreSQL]
```

- **Frontend**: single-page app in `frontend/` (not scaffolded yet). Talks only to the API; no direct DB access.
- **Backend**: Spring Boot in `backend/` (not scaffolded yet). Layered: controller → service → repository.
- **Database**: PostgreSQL 16, started via root `docker-compose.yml`.

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

- Single Postgres database `tickets`.
- Flyway (or Liquibase) when the backend is implemented; schema matches [data-model.md](data-model.md).
- Transactions at the service method boundary.

## Future (not this repo phase)

- Spring Security
- Backend and frontend Compose services
- Reverse proxy / HTTPS
