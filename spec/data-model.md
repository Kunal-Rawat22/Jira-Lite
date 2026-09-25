# Data model

PostgreSQL. Names are logical; physical tables may use snake_case (`tickets`, `ticket_comments`, `users`).

## Enumerations

**TicketStatus** (see [state-machine.md](state-machine.md)):

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`

**TicketPriority**:

- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

**UserRole** (informational in MVP):

- `REQUESTER`
- `AGENT`
- `ADMIN`

## User

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | PK |
| email | string | Unique, required |
| displayName | string | Required |
| role | UserRole | Required |
| active | boolean | Default true |
| createdAt | timestamptz | Server-set |

## Ticket

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | PK |
| key | string | Unique, format `TCK-{n}` |
| title | string | Required, 1–200 chars |
| description | string | Optional, max 10_000 chars |
| status | TicketStatus | Default `OPEN` |
| priority | TicketPriority | Default `MEDIUM` |
| reporterId | UUID | FK → User, required |
| assigneeId | UUID | FK → User, nullable |
| createdAt | timestamptz | Server-set |
| updatedAt | timestamptz | Server-set on every mutation |
| version | integer | Optimistic lock |

Constraints:

- `reporterId` must reference an active user at create time.
- `assigneeId`, when set, must reference an active user with role `AGENT` or `ADMIN`.

## Comment

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | PK |
| ticketId | UUID | FK → Ticket, required |
| authorId | UUID | FK → User, required |
| body | string | Required, 1–5000 chars |
| createdAt | timestamptz | Server-set; immutable |

Comments are append-only.

## Relationships

```
User 1 -- * Ticket (as reporter)
User 1 -- * Ticket (as assignee, optional)
Ticket 1 -- * Comment
User 1 -- * Comment (as author)
```

## Indexes (recommended)

- `tickets(status)`
- `tickets(priority)`
- `tickets(assignee_id)`
- `tickets(reporter_id)`
- `tickets(key)` unique
- `ticket_comments(ticket_id, created_at)`
