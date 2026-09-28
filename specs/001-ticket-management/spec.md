# Feature Specification: Support Ticket Management

**Feature Branch**: `001-ticket-management`

**Created**: 2026-09-25

**Status**: Draft

**Input**: Support ticket web app (create, list, detail, updates, comments, search, filters, persistence, state machine, dual screen/server validation), plus product isolation, multi-select list filters, flat comments, activity audit, and split storage (tickets/comments/activity vs users/products).

## Clarifications

### Session 2026-09-25

- Q: Tickets will also have reporter, version → A: Each ticket MUST include reporter (who filed it) and version (revision used to detect conflicting concurrent updates).
- Q: After a ticket is REOPEN, which moves are allowed? → A: REOPEN behaves exactly like OPEN for further work; the distinct status only shows the ticket was previously resolved, closed, or cancelled and is not a fresh ticket. Allowed next moves: REOPEN → IN_PROGRESS and REOPEN → CANCELLED. CLOSED → OPEN, RESOLVED → OPEN, and CANCELLED → OPEN remain illegal.
- Q: Which statuses allow field edits and new comments? → A: OPEN, IN_PROGRESS, RESOLVED, and REOPEN allow title, description, priority, and assignee edits and new comments. CLOSED and CANCELLED allow only the published reopen transition (to REOPEN); other field changes and new comments MUST be rejected.
- Q: Create ticket when assignee is absent? → A: If assignee is absent on create, the system MUST set assignee to the reporter.
- Q: Empty title on create? → A: The UI MUST NOT call the create API; it MUST show frontend validation. If the create API is still invoked, the backend MUST reject empty or missing title.
- Q: How is priority chosen on create? → A: Priority MUST be selected from a dropdown of LOW, MEDIUM, and HIGH (default MEDIUM). The UI MUST NOT accept free-typed priority. The backend MUST still reject any value outside that set.
- Q: Product scoping? → A: At least one product exists. Every ticket belongs to exactly one product (mandatory). Users only see tickets for products they belong to. One membership → create uses that product. Several memberships → user must pick from that list.
- Q: List filters? → A: Combined multi-select filters: user (reporter or assignee), status, assignee, reporter. Groups AND; values inside a group OR.
- Q: Comments? → A: Flat list on one ticket; no replies. Screen and server reject empty comments.
- Q: Audit? → A: Successful field/status changes appear under Activity beside Comments.
- Q: Storage? → A: Tickets, comments, and ticket activity in MongoDB; users, products, and memberships in PostgreSQL.
- Q: Dual validation for status and comments? → A: Screen MUST NOT call the server for empty comments or illegal status; server MUST still reject those requests.

### Session 2026-09-28

- Q: How do list search, pagination, and filter option catalogs work? → A: Case-insensitive substring search on title and description only (comments excluded); `q` optional, default null, always sent by the UI including when null; null `q` means no keyword filter; do not auto-trim search input; multi-word values allowed. List is paginated (`page`/`size`, defaults 0 and 20, user-changeable), ordered `createdAt` DESC; empty pages use the standard list envelope. Filter option lists for people and products come from PostgreSQL-backed APIs; status options are the six published statuses. Membership still hides other products’ tickets regardless of filters.
- Q: What are field length, trim, version, and login rules? → A: Max lengths title 40, description 1000, comment 200; reject over-limit; do not auto-trim stored fields; whitespace-only title and comment are invalid. Version is ticket-specific optimistic concurrency: integer starting at 1 on create, +1 on successful update only; failed/stale updates do not change version or data. Example label SVI-1425-1 means ticket-specific revision 1, not a constant stored on every ticket. Login with username/password is required; identity comes from JWT in `Authorization`; that identity is reporter/assignee/membership/Activity actor. No second auth mechanism; SSO is out of scope.
- Q: How do assignee, status updates, and Activity work? → A: Assignee optional on create (defaults to reporter); after create cannot be cleared; any same-product member may be assignee; membership roles PRODUCT OWNER, PRODUCT MANAGER, DEVELOPER, BA, QA exist but do not further restrict assignment; assignee outside the ticket’s product is rejected. Field PATCH MUST NOT change status; status uses a dedicated operation through the state machine only; reject status on the field-update path; no partial persist on failure. One Activity row per successful multi-field update; one Activity row per successful status change; none on failure; comments are never Activity.
- Q: What is the error and stale-version / network UX contract? → A: Every API uses `{ message, code, status, data }` with `status` `success` or `failed`; success puts payload in `data`; errors set `data` null unless extra error data is specified. Codes: VALIDATION_ERROR, TICKET_NOT_FOUND, PRODUCT_ACCESS_DENIED, INVALID_STATE_TRANSITION, TICKET_FROZEN, STALE_VERSION. Stale version: reject, no store change, show backend error, then reload latest ticket; never silent overwrite. If the API is down, times out, or returns an unexpected error: do not mark success, show a meaningful error, keep existing UI data, allow retry; recognized backend errors show `message`; unexpected errors use a generic user-facing message.
- Q: Dual-store consistency, frontend tests, self-transitions, unknown enums? → A: MongoDB owns tickets/comments/activity; PostgreSQL owns users/products/memberships; no distributed transaction. Validate reads from both stores before a write when practical. No extra cross-database rules. Frontend tests MUST cover the listed create/validation/status/filter/error/stale/frozen flows; backend tests remain authoritative including FR-019. Self-transitions are illegal (none listed in FR-013). Unknown status or priority → VALIDATION_ERROR.
- Q: How does the frontend send list search and filters? → A: As a JSON **request payload** (not query parameters, not the response envelope) with `searchKey`, `status`, `assignee`, `reporter` (not `reportee`), `product`, `user`, `size`, and `page`. Empty arrays mean that filter is not applied. `searchKey` optional; null/omitted means no keyword filter; still sent when null. Same AND/OR and substring search rules as already approved.
- Q: Is API paging zero-based, and is there a max page size? → A: Backend `page` is zero-based (first page `0`). UI shows one-based numbers (API `0` = “page 1”). Defaults remain page `0`, size `20`. No backend maximum page size. The UI offers page-size choices in a dropdown at the bottom-right of the list.
- Q: JWT lifetime and password storage? → A: JWT TTL is 30 minutes. Passwords are hashed with BCrypt (no cost factor specified). Auth model otherwise unchanged (username/password, `Authorization` JWT, JWT identity authoritative).
- Q: What is stored in Activity `from`/`to`, and how are envelope messages produced? → A: `from` and `to` are JSON of old and new values for the UI before/after display; UI shows those values only. Activity cardinality rules unchanged. Envelope unchanged; `code` identifies the result; backend i18n supplies `message` from that code; do not invent fixed success messages or new error codes.
- Q: What is the list/search HTTP path? → A: `POST /api/tickets/list` with the JSON body. Create remains `POST /api/tickets`. No GET query parameters for list filters.
- Q: How do omitted, null, and empty `searchKey` differ? → A: Omitted or `null` → no keyword filter. `""` is accepted as provided (no trim/transform). Backend decides null vs empty-string handling for whether a keyword filter runs. Matching remains case-insensitive substring on title and description only.
- Q: What code is used for login/auth failures? → A: `AUTHENTICATION_FAILED`. It is not one of the six ticket-domain codes.
- Q: What JSON shape do Activity `from`/`to` use? → A: Per-field object/map of only changed fields (example: title, priority, assigneeId). Single-field updates include that one field. Status changes use the same per-field map. No extra history fields.
- Q: Who owns page-size options? → A: Frontend UI only. API accepts zero-based `page` and requested `size`. No backend max size. UI shows API page 0 as page 1.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a support ticket (Priority: P1)

A signed-in user records a new request with title, product, optional description, priority from a dropdown, and optional assignee. Reporter is the authenticated user. Missing assignee becomes the reporter. Status starts OPEN. Product is mandatory: one membership uses that product; several memberships require a choice from the user’s list.

**Why this priority**: Without create, no other ticket work exists.

**Independent Test**: Create with title, authenticated reporter, and product rules; confirm OPEN, version 1, assignee default, and product stored.

**Acceptance Scenarios**:

1. **Given** a valid title and assignee, **When** they submit, **Then** the ticket is stored with that assignee, reporter = authenticated user, status OPEN (not REOPEN), timestamps, and version 1.
2. **Given** omitted assignee, **When** they submit a valid title, **Then** stored assignee is the reporter.
3. **Given** default priority on the dropdown, **When** they submit, **Then** priority is MEDIUM. Priority is only LOW, MEDIUM, or HIGH from the dropdown.
4. **Given** empty or whitespace title in the UI, **When** they submit, **Then** the UI shows a validation error and MUST NOT call create. **Given** create is invoked with blank title, **When** the server receives it, **Then** the server rejects it with VALIDATION_ERROR.
5. **Given** a non-UI client sends an illegal priority, **When** they create, **Then** the server rejects it with VALIDATION_ERROR.
6. **Given** no authenticated user, **When** they create, **Then** the server rejects the request; the UI MUST NOT treat create as successful.
7. **Given** the user belongs to exactly one product, **When** they create, **Then** the ticket is stored under that product without a product picker.
8. **Given** the user belongs to more than one product, **When** they create, **Then** they must choose a product from their memberships; the UI MUST NOT submit without a product; the server MUST reject create without a product (VALIDATION_ERROR).
9. **Given** the user has no product membership, **When** they try to create, **Then** they see a meaningful explanation and cannot store a ticket.
10. **Given** title longer than 40 characters or description longer than 1000, **When** they create, **Then** the UI MUST NOT submit if it can detect the limit; the server rejects over-limit values with VALIDATION_ERROR and does not store the ticket.
11. **Given** an assignee who is not a member of the chosen product, **When** they create, **Then** the server rejects the request (VALIDATION_ERROR).

---

### User Story 2 - Browse and find tickets (Priority: P1)

A signed-in user lists only tickets for products they belong to, searches by keyword, pages the list, and combines multi-select filters: status, assignee, reporter, user (involved as reporter or assignee), and product where they have membership. Search and filters are sent as a JSON request payload (not as the response envelope).

**Why this priority**: Users cannot manage tickets they cannot find.

**Independent Test**: Two products and mixed tickets; confirm isolation, keyword search, pagination, and combined multi-selects.

**Acceptance Scenarios**:

1. **Given** tickets exist in the user’s products, **When** they open the list, **Then** they see title, status, priority, assignee, reporter, version, and product — and not tickets of products they do not belong to — newest created first.
2. **Given** a user in products X and Y, **When** they open the list, **Then** they see the union of X and Y tickets only.
3. **Given** mixed statuses, **When** they multi-select two statuses, **Then** only those statuses appear.
4. **Given** mixed assignees and reporters, **When** they multi-select assignees and/or reporters, **Then** tickets match those selections (OR within a group).
5. **Given** the user filter, **When** they multi-select people, **Then** a ticket appears if reporter or assignee is in that set.
6. **Given** two filter groups both have selections, **When** they view the list, **Then** a ticket must satisfy every selected group (AND).
7. **Given** a keyword in title or description (any case), **When** they search, **Then** matching tickets appear if the substring is in title or description; comments are not searched.
8. **Given** no matches (including an empty page), **When** they view the list, **Then** they see a clear empty result using the standard **response envelope** (`data.content` empty, pagination fields present). This envelope is not the list **request payload**.
9. **Given** omitted or null `searchKey` in the list request JSON, **When** they list, **Then** no keyword filter is applied. **Given** `searchKey` is `""`, **When** they list, **Then** the backend accepts it as provided (no trim) and applies its null/empty-string search check. Empty `status`/`assignee`/`reporter`/`product`/`user` arrays apply no extra restriction for that group. The UI calls `POST /api/tickets/list` (not GET query params).
10. **Given** default list request, **When** they open the list, **Then** the JSON payload uses `page` 0 and `size` 20, order is createdAt descending. The UI shows this as page 1. **Given** they change page or size, **When** they request again, **Then** the payload uses the selected zero-based `page` and `size` (not offset). Page size is chosen from a dropdown at the bottom-right of the list. The backend MUST NOT impose a separately specified maximum `size`.
11. **Given** filter pickers, **When** they open them, **Then** user/assignee/reporter/product options come from APIs backed by PostgreSQL user/product/membership data; status options are the six statuses. Filters MUST NOT reveal tickets outside memberships. The field name for reporters in the list payload is `reporter`, never `reportee`.

---

### User Story 3 - Inspect ticket details (Priority: P1)

A signed-in user opens a ticket they can access and sees all fields, comments, and Activity beside Comments.

**Why this priority**: Detail is required before informed updates.

**Independent Test**: Open a known ticket; confirm fields, comments, Activity, and isolation.

**Acceptance Scenarios**:

1. **Given** a persisted ticket in a product they belong to, **When** they open detail, **Then** they see id, title, description, priority, status, assignee, reporter, version, product, timestamps, comments, and Activity.
2. **Given** an unknown id, **When** they try to view, **Then** the system reports TICKET_NOT_FOUND in the standard error envelope.
3. **Given** a ticket in a product they do not belong to, **When** they try to view, **Then** the system reports PRODUCT_ACCESS_DENIED in the standard error envelope and MUST NOT return ticket fields.
4. **Given** Comments and Activity, **When** they look at detail, **Then** the two are adjacent tabs (or equivalent); comments are not listed as activity events. Activity UI shows the stored JSON `from` and `to` values only.

---

### User Story 4 - Update ticket fields (Priority: P2)

A signed-in user changes title, description, priority, or assignee when allowed. Successful changes appear as exactly one Activity entry for that update. Empty title is blocked on the screen and on the server. Status is not part of this update.

**Why this priority**: Field edits are core management.

**Independent Test**: Edit OPEN/REOPEN; reject CLOSED/CANCELLED; reject status on field update; confirm Activity and dual title validation.

**Acceptance Scenarios**:

1. **Given** OPEN, IN_PROGRESS, RESOLVED, or REOPEN, **When** they update allowed fields with the current version, **Then** changes persist, timestamps advance, version increments by 1, and exactly one Activity record describes the complete change.
2. **Given** empty title in the UI, **When** they save, **Then** the UI shows an error and MUST NOT call update. **Given** the server receives blank title, **Then** it rejects with VALIDATION_ERROR and data is unchanged.
3. **Given** CLOSED or CANCELLED, **When** they change title, description, priority, or assignee, **Then** the UI MUST NOT submit that save; the server rejects with TICKET_FROZEN if called.
4. **Given** a stale version, **When** they save, **Then** the server rejects with STALE_VERSION, stored data and version are unchanged, the UI shows that backend message, then loads and displays the latest ticket; the UI MUST NOT save over the other user’s changes.
5. **Given** a field-update request that includes status, **When** the server receives it, **Then** it rejects with VALIDATION_ERROR and does not change status or fields.
6. **Given** assignee set to empty/null after create, **When** they save, **Then** the server rejects with VALIDATION_ERROR. **Given** an assignee who is not a member of the ticket’s product, **When** they save, **Then** the server rejects with VALIDATION_ERROR. Product membership roles do not further restrict who may be assigned.
7. **Given** title > 40 or description > 1000 characters, **When** they save, **Then** the server rejects with VALIDATION_ERROR and does not persist. Stored values are not auto-trimmed.

---

### User Story 5 - Comment on a ticket (Priority: P2)

A signed-in user adds a flat comment on one ticket. No replies. Empty comments are blocked on the screen and on the server. CLOSED and CANCELLED do not accept comments. Author is the authenticated user.

**Why this priority**: Collaboration needs a stored history without threads.

**Independent Test**: Two comments on one ticket; none on another; no reply control; empty comment does not call the server.

**Acceptance Scenarios**:

1. **Given** OPEN, IN_PROGRESS, RESOLVED, or REOPEN, **When** they submit a non-empty comment ≤ 200 characters, **Then** it is stored on that ticket only, with author = authenticated user and timestamp, in a flat list. No Activity entry is created for the comment itself.
2. **Given** the comments area, **When** they inspect it, **Then** there is no reply-to-comment action.
3. **Given** empty or whitespace comment in the UI, **When** they submit, **Then** they see a meaningful error and the server is not called. **Given** the server receives an empty body, **Then** it rejects with VALIDATION_ERROR and stores nothing.
4. **Given** CLOSED or CANCELLED, **When** they try to comment, **Then** the UI MUST NOT submit; the server rejects with TICKET_FROZEN if called.
5. **Given** comment body longer than 200 characters, **When** they submit, **Then** the server rejects with VALIDATION_ERROR. The stored body is not auto-trimmed.

---

### User Story 6 - Move a ticket through allowed statuses (Priority: P1)

Status changes only along the published list, via a dedicated status operation. Illegal moves (including self-transitions) are blocked on the screen (no server call) and rejected by the server. Allowed moves are recorded in Activity. New tickets start OPEN.

**Why this priority**: Lifecycle integrity is a non-negotiable business rule.

**Independent Test**: All allowed edges; illegal CLOSED → OPEN from UI (no call) and from a direct request (reject). Field update cannot set status.

**Acceptance Scenarios**:

1. **Given** a newly created ticket, **When** it is stored, **Then** status is OPEN (never REOPEN on create).
2. **Given** each allowed edge (OPEN → IN_PROGRESS or CANCELLED; IN_PROGRESS → RESOLVED or CANCELLED; RESOLVED → CLOSED or REOPEN; CLOSED → REOPEN; CANCELLED → REOPEN; REOPEN → IN_PROGRESS or CANCELLED), **When** the user applies it through the status operation with the current version, **Then** status updates, version increments by 1, and one Activity record describes the transition.
3. **Given** CLOSED, RESOLVED, or CANCELLED, **When** the user attempts OPEN in the UI, **Then** they see a meaningful error and the update is not sent. **Given** the same request to the server, **Then** it rejects with INVALID_STATE_TRANSITION and status is unchanged.
4. **Given** IN_PROGRESS, **When** they attempt CLOSED, **Then** UI and server reject as above.
5. **Given** any pair not listed as allowed, including any self-transition (e.g. OPEN → OPEN), **When** attempted, **Then** UI and server reject the same way (server: INVALID_STATE_TRANSITION).
6. **Given** an unknown status string, **When** sent to the status operation, **Then** the server rejects with VALIDATION_ERROR.
7. **Given** a failed status change, **When** it is rejected, **Then** no Activity is written and no partial ticket write remains.

---

### User Story 7 - Sign in (Priority: P1)

A user signs in with username and password and then uses the app as that identity.

**Why this priority**: Reporter, membership, assignee checks, and Activity actor require an authenticated user.

**Independent Test**: Valid credentials issue a JWT; subsequent ticket APIs use `Authorization`; wrong credentials fail without treating the session as signed in.

**Acceptance Scenarios**:

1. **Given** valid username and password, **When** they sign in, **Then** they receive a JWT that expires after 30 minutes and the UI stores it for `Authorization` on later calls. Stored passwords use BCrypt.
2. **Given** invalid credentials, **When** they sign in, **Then** they see a meaningful error with `AUTHENTICATION_FAILED` and are not signed in.
3. **Given** a missing or invalid JWT, **When** they call a ticket API, **Then** the request is rejected with `AUTHENTICATION_FAILED` and the UI MUST NOT show the operation as successful.

---

### Edge Cases

- Ticket not found → TICKET_NOT_FOUND; ticket in another product → PRODUCT_ACCESS_DENIED on view, update, comment, or status change (do not return ticket payload).
- Invalid priority or status values → VALIDATION_ERROR (unknown enums). Self-transitions → INVALID_STATE_TRANSITION.
- Empty or whitespace-only title or comment; missing product when required; over-max-length title/description/comment.
- Keyword or filters with no matches; empty filter arrays in the list JSON payload mean no extra restriction for that group; omitted or null `searchKey` means no keyword filter; `searchKey` `""` is stored/sent as provided (no auto-trim); the backend handles null vs empty-string search checks; comments are not searched.
- Persistence across restart (tickets/comments/activity in MongoDB; users/products/memberships in PostgreSQL). No distributed transaction; validate related PostgreSQL data before Mongo writes when practical.
- Field update MUST NOT accept status. Status only via the dedicated operation and FR-013. Failed updates persist nothing.
- Stale version: STALE_VERSION; no partial apply; UI shows backend message then reloads latest ticket.
- Failed updates do not create Activity entries. Successful multi-field update → one Activity. Successful status change → one Activity. Comments are not Activity. Each Activity `from`/`to` is a per-field JSON object of only changed fields.
- User with zero memberships cannot list (empty membership-scoped list) or create tickets.
- Create product list shows only memberships.
- Assignee cannot be cleared after create; assignee MUST belong to the ticket’s product; membership role does not add extra assignee rules.
- Backend unavailable, timeout, or unexpected error: UI does not mark success, shows error, keeps existing data, allows retry; recognized errors show backend `message`; unexpected errors show a generic message.
- Values are stored as submitted (no auto-trim) after they pass validation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Authenticated users MUST be able to create a ticket with title, product, optional description, priority from LOW/MEDIUM/HIGH, and optional assignee. Reporter MUST be the authenticated user (not a client-supplied substitute).
- **FR-002**: New tickets MUST start in status OPEN. Create MUST NOT store status REOPEN.
- **FR-003**: Users MUST be able to view a paginated list of tickets they are allowed to see.
- **FR-004**: Users MUST be able to view full details of a single allowed ticket, including comments and Activity.
- **FR-005**: Users MUST be able to update title, description, priority, and assignee when status is OPEN, IN_PROGRESS, RESOLVED, or REOPEN. This path MUST NOT change status.
- **FR-006**: Users MUST be able to add comments when status is OPEN, IN_PROGRESS, RESOLVED, or REOPEN. Comments MUST belong to exactly one ticket. Replies and threads MUST NOT exist. Comment author MUST be the authenticated user.
- **FR-007**: Users MUST be able to search tickets by optional `searchKey` in the list **request payload** using case-insensitive substring match against title and description only (not comments), within visible products. Multi-word `searchKey` is allowed. Search input MUST NOT be auto-trimmed or otherwise transformed. Omitted `searchKey` or `searchKey: null` means no keyword filter. `searchKey: ""` MUST be accepted as provided. The backend MUST apply the search check for null and empty string.
- **FR-008**: List/search MUST use `POST /api/tickets/list` with a JSON **request payload** (not GET query parameters) with fields `searchKey`, `status`, `assignee`, `reporter`, `product`, `user`, `size`, and `page`. Create remains `POST /api/tickets`. Do not name `reporter` as `reportee`. Combined multi-select: `user` (reporter or assignee), `status`, `assignee`, `reporter`, and `product` (limited to the user’s memberships). Groups AND; values inside a group OR. Empty arrays mean that filter is not applied. This JSON is the request body; it is not the response envelope in FR-035.

  Conceptual list **request payload** (UUIDs are examples only):

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
- **FR-009**: Ticket, comment, and activity data MUST persist across sessions.
- **FR-010**: The server MUST reject invalid input (blank/whitespace title, missing product when required, empty/whitespace comment, illegal priority/status, over-max-length fields, assignee not in product, attempt to clear assignee after create) with VALIDATION_ERROR.
- **FR-011**: The screen MUST show meaningful errors. The server remains authoritative; screen checks MUST NOT replace server rejection. For recognized backend results, show the i18n `message` that corresponds to `code`. Unexpected/unavailable/timeout: generic user-facing error, do not mark success, do not replace existing UI data with empty/invalid data, allow retry.
- **FR-012**: Ticket status MUST only change through the dedicated status operation and the explicit transitions below. Field update MUST reject any attempt to set status. Direct field assignment of status is prohibited. Every status change MUST pass the state machine. A failed update MUST NOT persist partially.
- **FR-013**: Allowed transitions MUST be exactly:
  - OPEN → IN_PROGRESS
  - OPEN → CANCELLED
  - IN_PROGRESS → RESOLVED
  - IN_PROGRESS → CANCELLED
  - RESOLVED → CLOSED
  - RESOLVED → REOPEN
  - CLOSED → REOPEN
  - CANCELLED → REOPEN
  - REOPEN → IN_PROGRESS
  - REOPEN → CANCELLED
- **FR-014**: Any transition not listed in FR-013 MUST be rejected with INVALID_STATE_TRANSITION, including CLOSED → OPEN, RESOLVED → OPEN, CANCELLED → OPEN, IN_PROGRESS → CLOSED, and all self-transitions. No self-transition is listed in FR-013.
- **FR-015**: Illegal transitions MUST be blocked on the screen without calling the server, and rejected by the server if called, with a meaningful error; status MUST stay unchanged.
- **FR-016**: Priority MUST be one of LOW, MEDIUM, HIGH. Unknown priority MUST be VALIDATION_ERROR.
- **FR-017**: Status MUST be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED, REOPEN. REOPEN uses the same working rules as OPEN but is not a first-time ticket. Unknown status MUST be VALIDATION_ERROR.
- **FR-018**: Each ticket MUST have id, title, description, priority, status, assignee, reporter, product, version, created timestamp, and updated timestamp.
- **FR-019**: Automated backend tests MUST cover the state machine, CLOSED/CANCELLED immutability, stale version, assignee default, blank title, empty comments, product isolation, missing product on create, unknown enums, self-transitions, and assignee product membership.
- **FR-020**: Successful create MUST set version to 1 and set timestamps. Successful field or status update MUST refresh updated timestamp and increment version by 1. Failed updates MUST NOT change version or stored ticket data.
- **FR-021**: REOPEN MUST allow the same next statuses and edit/comment rules as OPEN.
- **FR-022**: While CLOSED or CANCELLED, the only permitted mutation is status → REOPEN via the status operation. Field edits and new comments MUST be rejected (screen and server) with TICKET_FROZEN.
- **FR-023**: Stale version MUST be rejected with STALE_VERSION; stored data MUST not change. The UI MUST display the backend error, then fetch and display the latest ticket, and MUST NOT silently overwrite another user’s changes.
- **FR-024**: Reporter is the authenticated user at create and MUST NOT change later.
- **FR-025**: If assignee is absent on create, the server MUST set assignee to the reporter. Stored assignee MUST NOT be empty after successful create. After create, assignee MUST NOT be cleared to null/empty. Assignee MUST be a user who belongs to the ticket’s product. Membership roles (PRODUCT OWNER, PRODUCT MANAGER, DEVELOPER, BA, QA) MUST NOT add extra assignee restrictions.
- **FR-026**: Empty or whitespace-only title on create or update MUST be blocked on the screen without a server call, and rejected by the server if called. Title max length is 40 characters. Description max length is 1000 characters. Values MUST NOT be auto-trimmed before store; reject values that exceed max length.
- **FR-027**: Create priority MUST be a dropdown of LOW, MEDIUM, HIGH (default MEDIUM). The server MUST reject other values with VALIDATION_ERROR.
- **FR-028**: The system MUST have at least one product while tickets are used. Every ticket MUST belong to exactly one product.
- **FR-029**: A user MUST only see and access tickets whose product they belong to. List request payload fields MUST NOT bypass this.
- **FR-030**: One product membership → create uses that product. Several → user MUST choose from memberships. Zero → cannot create.
- **FR-031**: Successful field or status changes MUST be recorded in an audit trail. A successful multi-field update MUST create exactly one Activity entry for the whole change. A successful status change MUST create one Activity entry for that transition. Failed operations MUST create none. `from` and `to` MUST be per-field JSON objects containing only fields changed by that successful update (single-field updates contain that one field). Example:

  ```json
  {
    "from": {
      "title": "Old title",
      "priority": "LOW",
      "assigneeId": "old-user-id"
    },
    "to": {
      "title": "New title",
      "priority": "HIGH",
      "assigneeId": "new-user-id"
    }
  }
  ```

  The Activity UI MUST display those `from` and `to` values only. Ticket detail MUST show Activity beside Comments; comments MUST NOT appear as activity events.
- **FR-032**: Empty or whitespace-only comments MUST be blocked on the screen without a server call, and rejected by the server if called. Comment max length is 200 characters. Comment body MUST NOT be auto-trimmed before store.
- **FR-033**: Tickets, comments, and ticket activity MUST be stored in MongoDB. Users, products, and memberships MUST be stored in PostgreSQL. There is no cross-database transaction requirement. Do not use distributed transactions or two-phase commit. When an operation needs both stores, validate/read required PostgreSQL data before the MongoDB write where practical. Do not add further cross-database consistency rules.
- **FR-034**: Users MUST sign in with username and password. Passwords MUST be stored using BCrypt (no cost/work factor is specified). Subsequent API calls MUST send the JWT in the `Authorization` header. JWT time-to-live MUST be 30 minutes. The backend MUST take authenticated identity from that JWT for reporter, assignee eligibility, product membership, and Activity actor. No additional authentication mechanism. SSO is out of scope.
- **FR-035**: All APIs MUST use the **response envelope** `{ "message", "code", "status", "data" }` where `status` is `success` or `failed`. This envelope is not the list request payload (FR-008). On success, `data` holds the payload. On error, `data` is null unless extra error data is explicitly required. `code` identifies the result or error condition. The backend MUST map `code` to user-facing `message` via i18n; `message` depends on the result. Do not invent a fixed success message. Ticket-domain error `code` values that MUST be used when they apply: VALIDATION_ERROR, TICKET_NOT_FOUND, PRODUCT_ACCESS_DENIED, INVALID_STATE_TRANSITION, TICKET_FROZEN, STALE_VERSION. Login and authentication failures MUST use `AUTHENTICATION_FAILED`. `AUTHENTICATION_FAILED` is not a ticket-domain code and MUST NOT be added to that list.
- **FR-036**: List pagination uses `page` and `size` in the list **request payload** (not offset). Backend `page` is zero-based: first page is `0`, second is `1`. The UI MUST display one-based page numbers (API page `0` shown as 1). Defaults: page 0, size 20. Page-size **options** are owned by the frontend UI (dropdown at the bottom-right of the listing). The API accepts the requested `size`. There is no separately specified backend maximum page size. Default order: createdAt descending. Empty results still use the **response envelope** with empty `content`.
- **FR-037**: Filter option catalogs for users (user/assignee/reporter pickers) and products MUST come from PostgreSQL-backed APIs. Status filter options are the six statuses in FR-017.
- **FR-038**: Frontend automated tests MUST cover: create-ticket validation; empty/whitespace title; comment validation; priority selection; product selection for one vs many memberships; illegal status transition blocked before API call; display of meaningful backend errors; stale-version handling; CLOSED/CANCELLED UI restrictions; list filters and search; successful create, field update, and status-change flows.

### Key Entities

- **Product**: Workspace that owns tickets. At least one exists. Users have one or many memberships.
- **User**: Person with username and password for login; memberships to products.
- **Membership**: User–product link with a role of PRODUCT OWNER, PRODUCT MANAGER, DEVELOPER, BA, or QA. Role does not restrict ticket assignment beyond product membership.
- **Ticket**: Support request under exactly one product; fields in FR-018; version integer starting at 1.
- **Comment**: Flat message on one ticket; no parent comment.
- **Activity entry**: Audit of a successful field update (one entry per successful save) or status change; `from`/`to` per-field JSON maps of changed fields only.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A signed-in user can create a ticket (correct product rules) and see it OPEN in the list in under 2 minutes.
- **SC-002**: All ten allowed transitions succeed and persist after restart in 100% of a scripted walkthrough.
- **SC-003**: Listed illegal transitions, at least one other unlisted pair, and at least one self-transition are rejected on the screen (no server call) and by the server, with unchanged status, in 100% of trials.
- **SC-004**: Combined multi-selects plus keyword search isolate a known ticket in a 20-ticket fixture on the first attempt in under 1 minute, without showing other products’ tickets.
- **SC-005**: Automated backend tests cover all allowed transitions, documented illegal examples, self-transitions, unknown enums, immutability, stale version, assignee default, blank title, empty comment, and product isolation before acceptance. Automated frontend tests cover FR-038 before acceptance.
- **SC-006**: Blank title, missing product, and empty comment are blocked on the screen (no call) and rejected by the server in 100% of tests, with plain-language errors in the standard envelope.
- **SC-007**: Field edits and comments on CLOSED/CANCELLED fail in 100% of tests; reopen to REOPEN still succeeds.
- **SC-008**: In a two-product setup, a single-product user never sees the other product’s tickets in 100% of list and detail attempts.
- **SC-009**: After a multi-field title+priority change and a legal status change, Activity shows one event for the field save and one for the status change, and Comments does not list them as comments, in 100% of trials.
- **SC-010**: Stale-version save fails, UI shows the backend message, then shows the latest ticket data, in 100% of scripted trials.
- **SC-011**: Over-max-length title (41+), description (1001+), and comment (201+) are rejected without persist in 100% of tests.

## Assumptions

- SSO is out of scope. Username/password login, BCrypt password hashes, and JWT `Authorization` (TTL 30 minutes) are in scope.
- Priority omitted on create → MEDIUM. Assignee omitted on create → reporter.
- Ticket technical `id` remains the server-generated ticket identifier already used by the API. Version is a per-ticket integer (create = 1, then +1). The string SVI-1425-1 is only an example of “ticket 1425 at revision 1,” not a literal version stored on every ticket.
- Product and membership administration may be seeded outside ticket screens.
- Multi-product users see the union of their products’ tickets unless filters narrow it.
- Tickets are never deleted; comments are never edited or deleted.
- Attachments, notifications, SLAs, moving a ticket to another product, real-time push, Elasticsearch, Redis, distributed transactions, extra databases, comment threads, and extra role-based permissions beyond membership + the listed roles-as-labels are out of scope.
- Ratified `spec/` still describes a conflicting older model. It is **not** merged here. This feature specification remains the source of truth until a human updates `spec/`.

## Out of scope

- Comment threads, mentions, reactions
- Moving a ticket between products after create
- Cross-product access for non-members
- SSO
- Additional statuses or transitions beyond FR-013
- Attachments, notifications, real-time updates
- Distributed transactions / two-phase commit
- Additional databases, Elasticsearch, Redis
- Role-based permissions beyond product membership (roles do not further restrict assignee)
- Ticket deletion

## State machine (normative)

```text
OPEN --> IN_PROGRESS
OPEN --> CANCELLED
IN_PROGRESS --> RESOLVED
IN_PROGRESS --> CANCELLED
RESOLVED --> CLOSED
RESOLVED --> REOPEN
CLOSED --> REOPEN
CANCELLED --> REOPEN
REOPEN --> IN_PROGRESS
REOPEN --> CANCELLED
```

All other pairs are illegal, including every self-transition.
