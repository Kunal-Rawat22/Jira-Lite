# Java / Spring Boot

Apply when writing or reviewing anything under `backend/`.

## Stack

- Java 21, Spring Boot 3.x
- Spring Web, Validation, Data JPA
- PostgreSQL; Flyway for schema
- Constructor injection only; no field `@Autowired`

## Package layout

```
com.jiralite.tickets
  api          # controllers, request/response DTOs
  domain       # entities, enums (TicketStatus, TicketPriority, UserRole)
  service      # use cases and state machine
  persistence  # Spring Data repositories
  error        # problem details / exception handlers
```

## Layering

- Controllers map HTTP and Bean Validation only. No JPA calls, no status-transition logic.
- Services own transactions (`@Transactional`), ticket keys, and [spec/state-machine.md](../spec/state-machine.md).
- Repositories stay as Spring Data interfaces unless a custom query is required.
- Entities are not JSON response types. Map to DTOs defined in [spec/api-contract.md](../spec/api-contract.md).

## Persistence

- UUID primary keys; `OffsetDateTime` for timestamps in UTC.
- Optimistic locking on `Ticket.version`.
- Never use native SQL concatenated with user input.

## Errors

- `400` validation, `404` missing entity, `409` illegal transition or closed-ticket mutation or lock failure.
- Match the error envelope in [api-standards.md](api-standards.md).

## Forbidden

- `eval`, runtime script engines, hardcoded passwords, `spring.jpa.hibernate.ddl-auto=update` in anything beyond local experiments (prefer Flyway).
- Business rules in controllers or `@Entity` listeners for status changes.
