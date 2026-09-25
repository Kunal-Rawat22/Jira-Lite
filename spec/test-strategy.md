# Test strategy

No application code exists yet. When `backend/` and `frontend/` are implemented, use this matrix.

## Layers

| Layer | Tooling | What to prove |
| --- | --- | --- |
| Domain / service | JUnit 5 | State machine, key generation, closed-ticket rules |
| API | Spring `@WebMvcTest` / `@SpringBootTest` | Contract status codes, validation, pagination |
| Persistence | `@DataJpaTest` + Testcontainers Postgres | Constraints, indexes used by queries |
| Frontend unit | Vitest + Testing Library | Filters, form validation, disabled closed-ticket actions |
| Frontend e2e (later) | Playwright | List → create → comment → status path |

Prefer Testcontainers over mocking the database for repository and full API tests. Mock only outbound systems we do not have (none in MVP).

## Must-cover cases

1. Create ticket → `201`, status `OPEN`, key `TCK-n`, assignee null.
2. List filters: status, priority, assigneeId, reporterId, `q`.
3. Each **allowed** status transition from [state-machine.md](state-machine.md) → `200`.
4. Each **illegal** transition (at least one representative per from-state) → `409`.
5. PATCH other fields while `CLOSED` → `409`; reopen → `200` and `OPEN`.
6. Comment on open ticket → `201`; comment on `CLOSED` → `409`.
7. Unknown ticket id → `404`.
8. Invalid title (blank / too long) → `400`.
9. Assignee must be `AGENT` or `ADMIN`; requester as assignee → `400`.
10. Pagination defaults and `size` cap at 100.

## What not to test

- Framework internals, generated getters, snapshot-only UI with no behavior.
- Auth (out of MVP).

## CI (when apps exist)

- Backend: `./mvnw test` (or Gradle equivalent).
- Frontend: unit tests on every PR; e2e on main or nightly.

Until apps exist, there is nothing to run; do not add placeholder test suites.
