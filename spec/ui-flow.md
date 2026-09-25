# UI flow

Next.js (TypeScript, App Router) in `frontend/`. Screens map to [api-contract.md](api-contract.md). The current app is a home placeholder; ticket screens are Phase 3.

## Screens

### 1. Ticket list (`/`)

- Table or card list: key, title, status, priority, assignee, updatedAt.
- Filters: status, priority, assignee, reporter, title search (`q`).
- Pagination controls.
- Primary action: **New ticket** → create screen.
- Row click → ticket detail.

Empty state: “No tickets match these filters” plus create CTA.

### 2. Create ticket (`/tickets/new`)

- Fields: title (required), description, priority (default Medium), reporter (user picker).
- Submit `POST /api/tickets`.
- Success: navigate to `/tickets/{id}`.
- Validation errors shown inline; 404 reporter shown as form error.

### 3. Ticket detail (`/tickets/{id}`)

- Header: key, title, status badge, priority.
- Body: description, reporter, assignee, timestamps.
- **Status** control: only target statuses allowed from current state ([state-machine.md](state-machine.md)); illegal options hidden or disabled.
- **Assignee** picker: Agents and Admins; allow “Unassigned”. Disabled when status is `CLOSED`.
- Save field edits via `PATCH /api/tickets/{id}`.
- 409: toast with message (illegal transition or closed ticket).

### 4. Comments (on detail)

- Chronological list from `GET /api/tickets/{id}/comments`.
- Composer: body + author picker; `POST /api/tickets/{id}/comments`.
- Hide composer when status is `CLOSED`.

### 5. Users (admin-lite, `/users`)

- Read-only directory from `GET /api/users` for debugging pickers. No create/edit UI in MVP unless needed for local seeding.

## Shared UX

- Loading and error banners for failed fetches.
- Confirm only for reopen (`CLOSED` → `OPEN`) and close-from-open.
- No auth screens in MVP; optional local “acting as” user id stored in memory for `X-User-Id` / author fields.
