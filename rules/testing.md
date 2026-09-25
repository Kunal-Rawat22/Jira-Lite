# Testing

Follow [spec/test-strategy.md](../spec/test-strategy.md). This file constrains how tests are written.

## Backend

- JUnit 5; AssertJ for assertions.
- Mockito for collaborators **other than** the database. Do not mock `TicketRepository` in tests whose purpose is persistence or query filters.
- State machine: table-driven tests for every allowed transition and representative illegal ones.
- API tests assert status code **and** JSON fields (`status`, `key`, error `code`).
- Use Testcontainers PostgreSQL for `@SpringBootTest` / `@DataJpaTest` that hit the DB.
- Do not `@MockBean` the service in a test meant to prove the contract.

## Frontend (when it exists)

- Testing Library: assert behavior (filters, disabled closed actions), not CSS class names.
- Mock `fetch`/client at the network boundary, not deep component internals.

## Hygiene

- No tests that only instantiate objects.
- No copied production secrets in test fixtures; use obvious fake UUIDs and `user@example.com`.
- Name tests: `createTicket_startsInOpenWithNullAssignee`.
