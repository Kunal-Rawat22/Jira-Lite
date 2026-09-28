# Data model: 001-ticket-management

## Stores

**PostgreSQL (JPA + Flyway):** User, Product, UserProduct (membership). UUID PKs, `timestamptz` UTC.

**MongoDB (Spring Data MongoDB):** Ticket, Comment, Activity documents. Ticket `id` is UUID (string). Timestamps ISO-8601 UTC.

No distributed transaction between stores. Validate PostgreSQL membership/user/product before Mongo writes when practical.

JPA entities and Mongo documents are persistence types only. APIs use DTOs ([contracts/rest-api.md](contracts/rest-api.md)).

## Enumerations

**TicketStatus:** `OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED` | `REOPEN`

**TicketPriority:** `LOW` | `MEDIUM` | `HIGH`

**ProductRole:** `PRODUCT_OWNER` | `PRODUCT_MANAGER` | `DEVELOPER` | `BA` | `QA`

Product roles label membership only. They MUST NOT add assignee restrictions beyond “user belongs to the ticket’s product.”

## PostgreSQL

### Product

| Field | Notes |
| --- | --- |
| id | UUID PK |
| name | Required, unique |
| createdAt | Server-set |

At least one row while tickets are used.

### User

| Field | Notes |
| --- | --- |
| id | UUID PK |
| username | Unique, required; login identifier |
| password | Stored as BCrypt hash; never returned in API JSON; BCrypt cost/work factor is not specified |
| email | Unique, required |
| displayName | Required |
| createdAt | Server-set |

### UserProduct

| Field | Notes |
| --- | --- |
| userId | FK User |
| productId | FK Product |
| role | ProductRole, required |
| Unique (userId, productId) | |

## MongoDB

### Ticket

| Field | Notes |
| --- | --- |
| id | UUID |
| productId | Required; actor and assignee MUST belong to this product |
| title | Required, 1–40 chars as submitted (no auto-trim); whitespace-only invalid |
| description | Optional, max 1000 chars as submitted (no auto-trim) |
| status | Create always `OPEN`; thereafter only via the status operation |
| priority | Default `MEDIUM` |
| reporterId | Required, immutable; set from JWT identity on create |
| assigneeId | Required after persist; create default = reporterId; MUST NOT be cleared later; MUST be a member of `productId` |
| version | Integer; create = 1; +1 on successful field or status update only |
| createdAt / updatedAt | Server-set |

Version example `SVI-1425-1` means ticket-specific revision `1`, not a global constant string.

### Comment

| Field | Notes |
| --- | --- |
| id | UUID |
| ticketId | Required; no parent comment id |
| authorId | Authenticated user id |
| body | Required, 1–200 chars as submitted (no auto-trim); whitespace-only invalid |
| createdAt | Immutable |

Append-only, flat.

### Activity

| Field | Notes |
| --- | --- |
| id | UUID |
| ticketId | Required |
| actorId | Authenticated user id |
| at | Timestamp |
| field | e.g. `fields` (one entry for a multi-field save) or `status` |
| from / to | Per-field JSON objects of **changed** fields only. Example: `from: { "title", "priority", "assigneeId" }` old values; `to` new values. Status change: `{ "status": "OPEN" }` → `{ "status": "IN_PROGRESS" }`. UI before/after only; no extra history fields. |

Written only after a successful mutation. One document per successful field-update request; one document per successful status change. Comments never written here.

## Relationships

```
User * -- * Product (membership + ProductRole)
Product 1 -- * Ticket
User 1 -- * Ticket (reporter)
User 1 -- * Ticket (assignee)
Ticket 1 -- * Comment
Ticket 1 -- * Activity
```

## State transitions (dedicated status operation only)

```
OPEN → IN_PROGRESS | CANCELLED
IN_PROGRESS → RESOLVED | CANCELLED
RESOLVED → CLOSED | REOPEN
CLOSED → REOPEN
CANCELLED → REOPEN
REOPEN → IN_PROGRESS | CANCELLED
```

The field-update path MUST NOT mutate `status`. Repositories MUST NOT be used from controllers to set status. Self-transitions are illegal.

## Mutation rules

| Status | Field edits | New comments | Status change |
| --- | --- | --- | --- |
| OPEN, IN_PROGRESS, RESOLVED, REOPEN | Allowed + current version | Allowed | Allowed edges only, via status operation |
| CLOSED, CANCELLED | Rejected `TICKET_FROZEN` | Rejected `TICKET_FROZEN` | Only → REOPEN via status operation |

Access: actor must be a member of `ticket.productId`.

## Validation (server)

Envelope: `{ "message", "code", "status", "data" }` (`status`: `success` | `failed`). Errors: `data` null. `code` identifies the condition; backend i18n supplies `message`. This is the **response** envelope, not the list request payload.

| Situation | `code` |
| --- | --- |
| Blank/whitespace title or comment, over-max length, unknown enum, missing product when required, status on field PATCH, clear assignee, assignee not in product | `VALIDATION_ERROR` |
| Unknown ticket id | `TICKET_NOT_FOUND` |
| Ticket exists but actor is not a product member | `PRODUCT_ACCESS_DENIED` |
| Transition not in the allowed list (including self-transition) | `INVALID_STATE_TRANSITION` |
| Field edit or comment while CLOSED/CANCELLED | `TICKET_FROZEN` |
| Stale version | `STALE_VERSION` |
