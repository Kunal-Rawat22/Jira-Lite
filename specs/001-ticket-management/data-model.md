# Data model: 001-ticket-management

## Stores

**PostgreSQL (JPA + Flyway):** User, Product, UserProduct (membership). UUID PKs, `timestamptz` UTC.

**MongoDB (Spring Data MongoDB):** Ticket, Comment, Activity documents. Ticket `id` is UUID (string). Timestamps ISO-8601 UTC.

JPA entities and Mongo documents are persistence types only. APIs use DTOs ([contracts/rest-api.md](contracts/rest-api.md)).

## Enumerations

**TicketStatus:** `OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED` | `REOPEN`

**TicketPriority:** `LOW` | `MEDIUM` | `HIGH`

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
| email | Unique, required |
| displayName | Required |
| createdAt | Server-set |

### UserProduct

| Field | Notes |
| --- | --- |
| userId | FK User |
| productId | FK Product |
| Unique (userId, productId) | |

## MongoDB

### Ticket

| Field | Notes |
| --- | --- |
| id | UUID |
| productId | Required; must be a product the reporter/actor belongs to |
| title | Required, 1–200 chars after trim |
| description | Optional, max 10_000 |
| status | Create always `OPEN`; thereafter only via the status operation |
| priority | Default `MEDIUM` |
| reporterId | Required, immutable |
| assigneeId | Required after persist; create default = reporterId |
| version | Integer; increment on successful field or status update |
| createdAt / updatedAt | Server-set |

### Comment

| Field | Notes |
| --- | --- |
| id | UUID |
| ticketId | Required; no parent comment id |
| authorId | User id |
| body | Required, 1–5000 chars |
| createdAt | Immutable |

Append-only, flat.

### Activity

| Field | Notes |
| --- | --- |
| id | UUID |
| ticketId | Required |
| actorId | User id |
| at | Timestamp |
| field | e.g. title, status, assigneeId |
| from / to | Prior and new values |

Written only after a successful mutation.

## Relationships

```
User * -- * Product (membership)
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

The field-update path MUST NOT mutate `status`. Repositories MUST NOT be used from controllers to set status.

## Mutation rules

| Status | Field edits | New comments | Status change |
| --- | --- | --- | --- |
| OPEN, IN_PROGRESS, RESOLVED, REOPEN | Allowed + current version | Allowed | Allowed edges only, via status operation |
| CLOSED, CANCELLED | Rejected | Rejected | Only → REOPEN via status operation |

Access: actor must be a member of `ticket.productId`.

## Validation (server)

- Blank title, missing reporter/product, unknown enums, blank comment → `400` `VALIDATION_ERROR`
- Unknown or out-of-product ticket → `404` `NOT_FOUND` (do not leak other products)
- Illegal transition or frozen-ticket mutation → `409`
- Stale version → `409` `OPTIMISTIC_LOCK`
