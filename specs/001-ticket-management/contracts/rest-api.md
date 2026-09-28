# REST contract: 001-ticket-management

Prefix `/api`. JSON camelCase. **DTOs only** — never serialize JPA entities or Mongo documents.

Identity: `Authorization: Bearer <JWT>` on all ticket/product/user APIs except login. JWT TTL is 30 minutes. Reporter, comment author, Activity `actorId`, and membership checks use that identity. Passwords are stored with BCrypt (cost/work factor not specified).

## Response envelope (not the list request)

Every **response**:

```json
{
  "message": "string | null",
  "code": "string | null",
  "status": "success | failed",
  "data": {}
}
```

Success: `status` = `success`; `data` = payload. Error: `status` = `failed`; `data` = null. Ticket-domain `code`: `VALIDATION_ERROR`, `TICKET_NOT_FOUND`, `PRODUCT_ACCESS_DENIED`, `INVALID_STATE_TRANSITION`, `TICKET_FROZEN`, `STALE_VERSION`. Login/auth failures: `AUTHENTICATION_FAILED` (not a ticket-domain code). Backend i18n maps `code` to `message`; do not invent a fixed success message.

Typical HTTP: 400 validation; 401 `AUTHENTICATION_FAILED` (missing/invalid JWT or bad login); 404 `TICKET_NOT_FOUND`; 403 `PRODUCT_ACCESS_DENIED`; 409 `INVALID_STATE_TRANSITION`, `TICKET_FROZEN`, `STALE_VERSION`.

**TicketResponse** includes `id`, `productId`, `title`, `description`, `status`, `priority`, `reporterId`, `assigneeId`, `version`, `createdAt`, `updatedAt`.

`status` ∈ OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED | REOPEN  
`priority` ∈ LOW | MEDIUM | HIGH  
`assigneeId` never null on a stored ticket.  
`version` integer; create returns `1`.

**CommentResponse:** `id`, `ticketId`, `authorId`, `body`, `createdAt`  
**ActivityResponse:** `id`, `ticketId`, `actorId`, `at`, `field`, `from` (per-field JSON map of old values for changed fields), `to` (per-field JSON map of new values)  
**UserResponse:** `id`, `username`, `email`, `displayName` (never password)  
**ProductResponse:** `id`, `name`  
**MembershipRole:** PRODUCT_OWNER | PRODUCT_MANAGER | DEVELOPER | BA | QA

**Page** (inside success **response** `data` only): `content`, `page`, `size`, `totalElements`, `totalPages`  
`page` in the list **request** and in `data` is zero-based (first page `0`). UI displays one-based numbers (API `0` = page 1). Default request `page` 0, `size` 20. Page-size options are frontend-owned. No backend maximum `size`. Page/size semantics (not offset). Empty list: `content` []. Ticket list order: `createdAt` DESC.

## `POST /api/auth/login`

```json
{ "username": "string", "password": "string" }
```

**200** — `data` includes JWT (and optionally user summary).  
**401** — invalid credentials; `code` = `AUTHENTICATION_FAILED`.

No SSO. No second auth mechanism.

## `GET /api/users`

**200** — users for filter/assignee pickers (PostgreSQL). Optionally scoped to a product the actor belongs to.

## `GET /api/products`

Products the actor belongs to (PostgreSQL).

**200** — list of `ProductResponse`

## `POST /api/tickets/list`

Ticket **list/search request payload** (JSON body). Not query parameters. Not the response envelope. Create remains `POST /api/tickets`.

```json
{
  "searchKey": "begdfhjgefhj",
  "status": ["OPEN", "IN_PROGRESS"],
  "assignee": ["user-uuid-1", "user-uuid-2"],
  "reporter": [],
  "product": [],
  "user": [],
  "size": 20,
  "page": 0
}
```

UUIDs are examples only. Field `reporter` MUST NOT be named `reportee`.

- `searchKey` omitted or `null`: no keyword filter. `searchKey` `""`: accepted as provided (no trim/transform). Backend applies the null vs empty-string search check. Case-insensitive substring on title and description only; not comments; multi-word allowed.
- `status`, `assignee`, `reporter`, `product`, `user`: optional arrays; empty array = that filter is not applied. `user` = involved as reporter or assignee (existing semantics). Groups AND; values OR. `product` limited to memberships; membership still hides other products’ tickets.
- `page`: zero-based; default 0. `size`: requested page size; default 20; no specified backend maximum. Page-size options are frontend-owned.

**200** — response envelope with `data` = `Page<TicketResponse>` (membership-scoped)

## `POST /api/tickets`

```json
{
  "title": "string",
  "description": "string | null",
  "priority": "MEDIUM",
  "assigneeId": "uuid | null",
  "productId": "uuid | null"
}
```

Do not accept `reporterId` or `status` from the client. Server: reporter = JWT user; `status=OPEN`; `version=1`; assignee default reporter; if actor has one product, use that product; if several, `productId` required and must be a membership. Assignee, if present, MUST belong to that product.

**201** — `TicketResponse` in `data`  
**400** — `VALIDATION_ERROR`  
**401** — `AUTHENTICATION_FAILED`  
**403** — `PRODUCT_ACCESS_DENIED` when product is not a membership

## `GET /api/tickets/{id}`

**200** / **404** `TICKET_NOT_FOUND` / **403** `PRODUCT_ACCESS_DENIED`

## `PATCH /api/tickets/{id}`

Field update only. Current `version` required. MUST NOT include `status` (if present → `VALIDATION_ERROR`). Same field-mutation rules as spec.md.

```json
{
  "version": 1,
  "title": "string",
  "description": "string | null",
  "priority": "HIGH",
  "assigneeId": "uuid"
}
```

`assigneeId` required in the sense that it cannot be null; MUST be a product member.

**200** / **400** `VALIDATION_ERROR` / **403** / **404** / **409** `TICKET_FROZEN` or `STALE_VERSION`

Successful field change writes exactly one Activity document. `from`/`to` example (changed fields only):

```json
{
  "from": { "title": "Old title", "priority": "LOW", "assigneeId": "old-user-id" },
  "to": { "title": "New title", "priority": "HIGH", "assigneeId": "new-user-id" }
}
```

## `POST /api/tickets/{id}/status`

Dedicated status-change operation. Server validates the edge with the state machine. Current `version` required.

```json
{
  "version": 1,
  "status": "IN_PROGRESS"
}
```

`status` is the **target** status. Allowed edges only ([data-model.md](../data-model.md)). CLOSED / CANCELLED accept only `REOPEN`. Unknown enum → `VALIDATION_ERROR`. Illegal edge including self-transition → `INVALID_STATE_TRANSITION`.

**200** — `TicketResponse`  
**400** — `VALIDATION_ERROR`  
**403** / **404**  
**409** — `INVALID_STATE_TRANSITION`, `TICKET_FROZEN` (if used), or `STALE_VERSION`

Successful change writes one Activity (`field`: `status`). Version +1.

## `GET /api/tickets/{id}/comments`

Chronological flat list. **200** / **403** / **404**

## `POST /api/tickets/{id}/comments`

```json
{ "body": "string" }
```

Author = JWT user. Do not accept `authorId` from the client.

**201** / **400** `VALIDATION_ERROR` / **403** / **404** / **409** `TICKET_FROZEN`

No Activity for comments.

## `GET /api/tickets/{id}/activity`

Chronological. **200** / **403** / **404**

No PUT/DELETE for tickets or comments. No comment reply resource. No status field on PATCH.
