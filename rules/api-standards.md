# API standards

Align with [spec/api-contract.md](../spec/api-contract.md).

## URL and methods

- Prefix: `/api`.
- Nouns, plural: `/api/tickets`, `/api/tickets/{id}/comments`, `/api/users`.
- `GET` read, `POST` create, `PATCH` partial update. No `PUT` for tickets in MVP. No `DELETE`.
- Path `{id}` is the ticket UUID, not `TCK-n`.

## JSON

- camelCase fields matching the contract (`reporterId`, `createdAt`).
- Enums as strings: `OPEN`, `MEDIUM`, `AGENT`.
- Timestamps ISO-8601 UTC (`...Z`).

## Pagination

- Query: `page` (0-based, default 0), `size` (default 20, max 100).
- Body: `content`, `page`, `size`, `totalElements`, `totalPages`.

## Errors

Single envelope:

```json
{
  "code": "ILLEGAL_TRANSITION",
  "message": "Cannot move CLOSED to RESOLVED",
  "details": {}
}
```

| HTTP | `code` examples |
| --- | --- |
| 400 | `VALIDATION_ERROR` |
| 404 | `NOT_FOUND` |
| 409 | `ILLEGAL_TRANSITION`, `TICKET_CLOSED`, `OPTIMISTIC_LOCK` |

No stack traces in responses.

## Compatibility

- Do not remove or rename contract fields without updating `spec/api-contract.md` first.
- Additive optional fields are allowed if documented.
