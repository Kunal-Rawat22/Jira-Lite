# REST contract: 001-ticket-management

Prefix `/api`. JSON camelCase. Errors: `{ "code", "message", "details" }`. **DTOs only** — never serialize JPA entities or Mongo documents.

**TicketResponse** includes `id`, `productId`, `title`, `description`, `status`, `priority`, `reporterId`, `assigneeId`, `version`, `createdAt`, `updatedAt`.

`status` ∈ OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED | REOPEN  
`priority` ∈ LOW | MEDIUM | HIGH  
`assigneeId` never null on a stored ticket.

**CommentResponse:** `id`, `ticketId`, `authorId`, `body`, `createdAt`  
**ActivityResponse:** `id`, `ticketId`, `actorId`, `at`, `field`, `from`, `to`  
**UserResponse:** `id`, `email`, `displayName`  
**ProductResponse:** `id`, `name`

**Page:** `content`, `page`, `size`, `totalElements`, `totalPages`  
`page` default 0, `size` default 20, max 100.

All ticket reads/writes are scoped to the actor’s product memberships (`X-User-Id` or equivalent identity placeholder).

## `GET /api/users`

**200** — users (optionally filtered to a product).

## `GET /api/products`

Products the actor belongs to.

**200** — list of `ProductResponse`

## `GET /api/tickets`

Query (all optional, repeatable for multi-select): `status`, `assigneeId`, `reporterId`, `userId` (involved), `q`.

**200** — `Page<TicketResponse>` (membership-scoped)

## `POST /api/tickets`

```json
{
  "title": "string",
  "description": "string | null",
  "priority": "MEDIUM",
  "reporterId": "uuid",
  "assigneeId": "uuid | null",
  "productId": "uuid | null"
}
```

Server: `status=OPEN`; assignee default reporter; if actor has one product, ignore/omit `productId` and use that product; if several, `productId` required and must be a membership. Create MUST NOT accept a client-supplied status.

**201** — `TicketResponse`  
**400** — validation  
**404** — user/product not found or not a membership

## `GET /api/tickets/{id}`

**200** / **404** (404 also if other product)

## `PATCH /api/tickets/{id}`

Field update only. Current `version` required. MUST NOT include `status`. Same field-mutation rules as spec.md (OPEN / IN_PROGRESS / RESOLVED / REOPEN; reject CLOSED / CANCELLED).

```json
{
  "version": 1,
  "title": "string",
  "description": "string | null",
  "priority": "HIGH",
  "assigneeId": "uuid"
}
```

**200** / **400** / **404** / **409**

Successful field change also writes Activity.

## `POST /api/tickets/{id}/status`

Dedicated status-change operation. Server validates the edge with the state machine. Current `version` required.

```json
{
  "version": 1,
  "status": "IN_PROGRESS"
}
```

`status` is the **target** status. Allowed edges only ([data-model.md](../data-model.md)). CLOSED / CANCELLED accept only `REOPEN`.

**200** — `TicketResponse`  
**400** — validation (missing version, unknown enum)  
**404** — unknown or out-of-product ticket  
**409** — illegal transition (`ILLEGAL_TRANSITION`) or stale version (`OPTIMISTIC_LOCK`)

Successful change writes an Activity row (`field`: `status`).

## `GET /api/tickets/{id}/comments`

Chronological flat list. **200** / **404**

## `POST /api/tickets/{id}/comments`

```json
{ "authorId": "uuid", "body": "string" }
```

**201** / **400** (empty body) / **404** / **409** (CLOSED/CANCELLED)

## `GET /api/tickets/{id}/activity`

Chronological. **200** / **404**

No PUT/DELETE for tickets or comments. No comment reply resource. No status field on PATCH.
