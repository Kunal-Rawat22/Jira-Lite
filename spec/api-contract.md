# API contract

Base path: `/api`. JSON, UTF-8. Errors use the envelope in [rules/api-standards.md](../rules/api-standards.md).

## Common types

**TicketResponse**

```json
{
  "id": "uuid",
  "key": "TCK-1",
  "title": "Cannot reset password",
  "description": "Steps...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "reporterId": "uuid",
  "assigneeId": null,
  "createdAt": "2026-09-25T00:00:00Z",
  "updatedAt": "2026-09-25T00:00:00Z"
}
```

`status` ∈ `OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED`.  
`priority` ∈ `LOW` | `MEDIUM` | `HIGH` | `URGENT`.

**CommentResponse**

```json
{
  "id": "uuid",
  "ticketId": "uuid",
  "authorId": "uuid",
  "body": "Looking into this.",
  "createdAt": "2026-09-25T00:00:00Z"
}
```

**UserResponse**

```json
{
  "id": "uuid",
  "email": "agent@example.com",
  "displayName": "Alex Agent",
  "role": "AGENT",
  "active": true
}
```

**Page** wrapping lists:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Query params for pagination: `page` (0-based, default 0), `size` (default 20, max 100).

---

## Users

### `GET /api/users`

List users for pickers. Optional query: `role`, `active` (boolean).

**200** — `Page<UserResponse>`

---

## Tickets

### `GET /api/tickets`

Filters (all optional): `status`, `priority`, `assigneeId`, `reporterId`, `q` (title contains, case-insensitive).

**200** — `Page<TicketResponse>`

### `POST /api/tickets`

```json
{
  "title": "string",
  "description": "string | null",
  "priority": "MEDIUM",
  "reporterId": "uuid"
}
```

Server sets `status=OPEN`, `assigneeId=null`, generates `key`.

**201** — `TicketResponse`  
**400** — validation  
**404** — reporter not found or inactive

### `GET /api/tickets/{id}`

`{id}` is the ticket UUID (not `key`).

**200** — `TicketResponse`  
**404** — unknown id

### `PATCH /api/tickets/{id}`

Partial update. Omitted fields unchanged. `null` for `assigneeId` clears assignee.

```json
{
  "title": "string",
  "description": "string | null",
  "priority": "HIGH",
  "assigneeId": "uuid | null",
  "status": "IN_PROGRESS"
}
```

**200** — `TicketResponse`  
**400** — validation  
**404** — ticket or referenced user missing  
**409** — illegal status transition (see [state-machine.md](state-machine.md)) or optimistic lock failure

Closed tickets: only `status` change `CLOSED` → `OPEN` is accepted; other field updates return **409**.

---

## Comments

### `GET /api/tickets/{id}/comments`

Chronological, paginated.

**200** — `Page<CommentResponse>`  
**404** — ticket missing

### `POST /api/tickets/{id}/comments`

```json
{
  "authorId": "uuid",
  "body": "string"
}
```

**201** — `CommentResponse`  
**400** — validation  
**404** — ticket or author missing  
**409** — ticket status is `CLOSED` (comments not allowed)

No `PATCH`/`DELETE` for comments in MVP.
