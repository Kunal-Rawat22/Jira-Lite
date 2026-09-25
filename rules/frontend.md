# Frontend

Apply when writing or reviewing anything under `frontend/`. Stack is **React + TypeScript + Vite**.

## Structure (when scaffolded)

- `src/pages` — list, create, detail
- `src/components` — filters, comment list, status control
- `src/api` — typed client for [spec/api-contract.md](../spec/api-contract.md)
- `src/types` — enums and DTOs matching the API (do not drift)

## TypeScript

- `strict` true. No `any`.
- Shared union types: `TicketStatus`, `TicketPriority` — same string literals as the API.

## UI rules

- Implement screens in [spec/ui-flow.md](../spec/ui-flow.md).
- Status dropdown only offers allowed targets from [spec/state-machine.md](../spec/state-machine.md).
- Hide comment composer and disable field edits when status is `CLOSED`, except reopen.
- Show API `message` on 400/409; do not swallow errors.

## Data fetching

- One API module; no `fetch` scattered in random components.
- Handle loading and empty list states from the spec.

## Forbidden

- Talking to Postgres from the browser
- Storing secrets in frontend env that would ship to the client
- Inventing extra ticket fields without a spec change
