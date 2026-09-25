# Frontend

Apply when writing or reviewing anything under `frontend/`. Stack is **Next.js (TypeScript, App Router)**.

## Structure

- `app/` — routes (`page.tsx`, layouts)
- `app/` ticket routes later: `/`, `/tickets/new`, `/tickets/[id]`
- `lib/api` (when added) — typed client for [spec/api-contract.md](../spec/api-contract.md)
- `lib/types` — enums and DTOs matching the API (do not drift)

## TypeScript

- `strict` true. No `any`.
- Shared union types: `TicketStatus`, `TicketPriority` — same string literals as the API.

## UI rules

- Implement screens in [spec/ui-flow.md](../spec/ui-flow.md) in Phase 3.
- Status dropdown only offers allowed targets from [spec/state-machine.md](../spec/state-machine.md).
- Hide comment composer and disable field edits when status is `CLOSED`, except reopen.
- Show API `message` on 400/409; do not swallow errors.

## Data fetching

- Browser calls `/api/...` on the Next.js origin; `next.config.ts` rewrites to Spring Boot (`API_INTERNAL_URL` in Docker, `http://localhost:8080` locally).
- One API module; no `fetch` scattered in random components.
- Handle loading and empty list states from the spec.

## Forbidden

- Talking to Postgres from the browser or from Next.js server components
- Storing secrets in `NEXT_PUBLIC_*` that would ship to the client
- Inventing extra ticket fields without a spec change
