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

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a support ticket (Priority: P1)

A user records a new request with title, reporter, product, optional description, priority from a dropdown, and optional assignee. Missing assignee becomes the reporter. Status starts OPEN. Product is mandatory: one membership uses that product; several memberships require a choice from the user’s list.

**Why this priority**: Without create, no other ticket work exists.

**Independent Test**: Create with title, reporter, and product rules; confirm OPEN, version, assignee default, and product stored.

**Acceptance Scenarios**:

1. **Given** a valid title, reporter, and assignee, **When** they submit, **Then** the ticket is stored with that assignee, status OPEN (not REOPEN), timestamps, and initial version.
2. **Given** omitted assignee, **When** they submit a valid title and reporter, **Then** stored assignee is the reporter.
3. **Given** default priority on the dropdown, **When** they submit, **Then** priority is MEDIUM. Priority is only LOW, MEDIUM, or HIGH from the dropdown.
4. **Given** empty or whitespace title in the UI, **When** they submit, **Then** the UI shows a validation error and MUST NOT call create. **Given** create is invoked with blank title, **When** the server receives it, **Then** the server rejects it.
5. **Given** a non-UI client sends an illegal priority, **When** they create, **Then** the server rejects it.
6. **Given** omitted reporter, **When** they create, **Then** the server rejects it and the UI shows a meaningful error if the screen allowed submit.
7. **Given** the user belongs to exactly one product, **When** they create, **Then** the ticket is stored under that product without a product picker.
8. **Given** the user belongs to more than one product, **When** they create, **Then** they must choose a product from their memberships; the UI MUST NOT submit without a product; the server MUST reject create without a product.
9. **Given** the user has no product membership, **When** they try to create, **Then** they see a meaningful explanation and cannot store a ticket.

---

### User Story 2 - Browse and find tickets (Priority: P1)

A user lists only tickets for products they belong to, searches by keyword, and combines multi-select filters: status, assignee, reporter, and user (involved as reporter or assignee).

**Why this priority**: Users cannot manage tickets they cannot find.

**Independent Test**: Two products and mixed tickets; confirm isolation, keyword search, and combined multi-selects.

**Acceptance Scenarios**:

1. **Given** tickets exist in the user’s products, **When** they open the list, **Then** they see title, status, priority, assignee, reporter, version, and product — and not tickets of products they do not belong to.
2. **Given** a user in products X and Y, **When** they open the list, **Then** they see the union of X and Y tickets only.
3. **Given** mixed statuses, **When** they multi-select two statuses, **Then** only those statuses appear.
4. **Given** mixed assignees and reporters, **When** they multi-select assignees and/or reporters, **Then** tickets match those selections (OR within a group).
5. **Given** the user filter, **When** they multi-select people, **Then** a ticket appears if reporter or assignee is in that set.
6. **Given** two filter groups both have selections, **When** they view the list, **Then** a ticket must satisfy every selected group (AND).
7. **Given** a keyword in title or description, **When** they search, **Then** matching tickets appear.
8. **Given** no matches, **When** they view the list, **Then** they see a clear empty result.

---

### User Story 3 - Inspect ticket details (Priority: P1)

A user opens a ticket they can access and sees all fields, comments, and Activity beside Comments.

**Why this priority**: Detail is required before informed updates.

**Independent Test**: Open a known ticket; confirm fields, comments, Activity, and isolation.

**Acceptance Scenarios**:

1. **Given** a persisted ticket in a product they belong to, **When** they open detail, **Then** they see id, title, description, priority, status, assignee, reporter, version, product, timestamps, comments, and Activity.
2. **Given** an unknown id or a ticket in a product they do not belong to, **When** they try to view, **Then** the system reports it as not available in a meaningful way.
3. **Given** Comments and Activity, **When** they look at detail, **Then** the two are adjacent tabs (or equivalent); comments are not listed as activity events.

---

### User Story 4 - Update ticket fields (Priority: P2)

A user changes title, description, priority, or assignee when allowed. Successful changes appear in Activity. Empty title is blocked on the screen and on the server.

**Why this priority**: Field edits are core management.

**Independent Test**: Edit OPEN/REOPEN; reject CLOSED/CANCELLED; confirm Activity and dual title validation.

**Acceptance Scenarios**:

1. **Given** OPEN, IN_PROGRESS, RESOLVED, or REOPEN, **When** they update allowed fields with the current version, **Then** changes persist, timestamps and version advance, and Activity records the change.
2. **Given** empty title in the UI, **When** they save, **Then** the UI shows an error and MUST NOT call update. **Given** the server receives blank title, **Then** it rejects and data is unchanged.
3. **Given** CLOSED or CANCELLED, **When** they change title, description, priority, or assignee, **Then** the UI MUST NOT submit that save; the server rejects if called.
4. **Given** a stale version, **When** they save, **Then** the server rejects with a meaningful conflict error.

---

### User Story 5 - Comment on a ticket (Priority: P2)

A user adds a flat comment on one ticket. No replies. Empty comments are blocked on the screen and on the server. CLOSED and CANCELLED do not accept comments.

**Why this priority**: Collaboration needs a stored history without threads.

**Independent Test**: Two comments on one ticket; none on another; no reply control; empty comment does not call the server.

**Acceptance Scenarios**:

1. **Given** OPEN, IN_PROGRESS, RESOLVED, or REOPEN, **When** they submit a non-empty comment, **Then** it is stored on that ticket only, with author and timestamp, in a flat list.
2. **Given** the comments area, **When** they inspect it, **Then** there is no reply-to-comment action.
3. **Given** empty or whitespace comment in the UI, **When** they submit, **Then** they see a meaningful error and the server is not called. **Given** the server receives an empty body, **Then** it rejects and stores nothing.
4. **Given** CLOSED or CANCELLED, **When** they try to comment, **Then** the UI MUST NOT submit; the server rejects if called.

---

### User Story 6 - Move a ticket through allowed statuses (Priority: P1)

Status changes only along the published list. Illegal moves are blocked on the screen (no server call) and rejected by the server. Allowed moves are recorded in Activity. New tickets start OPEN.

**Why this priority**: Lifecycle integrity is a non-negotiable business rule.

**Independent Test**: All allowed edges; illegal CLOSED → OPEN from UI (no call) and from a direct request (reject).

**Acceptance Scenarios**:

1. **Given** a newly created ticket, **When** it is stored, **Then** status is OPEN (never REOPEN on create).
2. **Given** each allowed edge (OPEN → IN_PROGRESS or CANCELLED; IN_PROGRESS → RESOLVED or CANCELLED; RESOLVED → CLOSED or REOPEN; CLOSED → REOPEN; CANCELLED → REOPEN; REOPEN → IN_PROGRESS or CANCELLED), **When** the user applies it, **Then** status updates and Activity records it.
3. **Given** CLOSED, RESOLVED, or CANCELLED, **When** the user attempts OPEN in the UI, **Then** they see a meaningful error and the update is not sent. **Given** the same request to the server, **Then** it rejects and status is unchanged.
4. **Given** IN_PROGRESS, **When** they attempt CLOSED, **Then** UI and server reject as above.
5. **Given** any pair not listed as allowed, **When** attempted, **Then** UI and server reject the same way.

---

### Edge Cases

- Ticket not found, or ticket in another product, on view, update, comment, or status change.
- Invalid priority or status values.
- Empty title, missing reporter, missing product, empty comment.
- Keyword or filters with no matches; empty filter group means no extra restriction for that group.
- Persistence across restart (tickets/comments/activity in the document store; users/products in the structured store).
- Combined field+status update: illegal status fails the whole change. CLOSED/CANCELLED reject field edits unless the only change is reopen.
- Stale version: reject; no partial apply.
- Failed updates do not create Activity entries.
- User with zero memberships cannot list or create tickets.
- Create product list shows only memberships.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to create a ticket with title, reporter, product, optional description, priority from LOW/MEDIUM/HIGH, and optional assignee.
- **FR-002**: New tickets MUST start in status OPEN. Create MUST NOT store status REOPEN.
- **FR-003**: Users MUST be able to view a list of tickets they are allowed to see.
- **FR-004**: Users MUST be able to view full details of a single allowed ticket, including comments and Activity.
- **FR-005**: Users MUST be able to update title, description, priority, and assignee when status is OPEN, IN_PROGRESS, RESOLVED, or REOPEN.
- **FR-006**: Users MUST be able to add comments when status is OPEN, IN_PROGRESS, RESOLVED, or REOPEN. Comments MUST belong to exactly one ticket. Replies and threads MUST NOT exist.
- **FR-007**: Users MUST be able to search tickets by keyword against title and description (within visible products).
- **FR-008**: The list MUST support combined multi-select filters: user (reporter or assignee), status, assignee, and reporter. Groups AND; values inside a group OR.
- **FR-009**: Ticket, comment, and activity data MUST persist across sessions.
- **FR-010**: The server MUST reject invalid input (blank title, missing reporter, missing product, empty comment, illegal priority/status).
- **FR-011**: The screen MUST show meaningful errors. The server remains authoritative; screen checks MUST NOT replace server rejection.
- **FR-012**: Ticket status MUST only change through the explicit transitions below.
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
- **FR-014**: Any transition not listed in FR-013 MUST be rejected by the server, including CLOSED → OPEN, RESOLVED → OPEN, CANCELLED → OPEN, and IN_PROGRESS → CLOSED.
- **FR-015**: Illegal transitions MUST be blocked on the screen without calling the server, and rejected by the server if called, with a meaningful error; status MUST stay unchanged.
- **FR-016**: Priority MUST be one of LOW, MEDIUM, HIGH.
- **FR-017**: Status MUST be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED, REOPEN. REOPEN uses the same working rules as OPEN but is not a first-time ticket.
- **FR-018**: Each ticket MUST have id, title, description, priority, status, assignee, reporter, product, version, created timestamp, and updated timestamp.
- **FR-019**: Automated tests MUST cover the state machine, CLOSED/CANCELLED immutability, stale version, assignee default, blank title, empty comments, product isolation, and missing product on create.
- **FR-020**: Successful create or update MUST refresh updated timestamp; version MUST advance on successful updates.
- **FR-021**: REOPEN MUST allow the same next statuses and edit/comment rules as OPEN.
- **FR-022**: While CLOSED or CANCELLED, the only permitted mutation is status → REOPEN. Field edits and new comments MUST be rejected (screen and server).
- **FR-023**: Stale version MUST be rejected; stored data MUST not change.
- **FR-024**: Reporter is required at create and MUST NOT change later.
- **FR-025**: If assignee is absent on create, the server MUST set assignee to the reporter. Stored assignee MUST NOT be empty after successful create.
- **FR-026**: Empty title on create or update MUST be blocked on the screen without a server call, and rejected by the server if called.
- **FR-027**: Create priority MUST be a dropdown of LOW, MEDIUM, HIGH (default MEDIUM). The server MUST reject other values.
- **FR-028**: The system MUST have at least one product while tickets are used. Every ticket MUST belong to exactly one product.
- **FR-029**: A user MUST only see and access tickets whose product they belong to.
- **FR-030**: One product membership → create uses that product. Several → user MUST choose from memberships. Zero → cannot create.
- **FR-031**: Successful field or status changes MUST be recorded in an audit trail. Ticket detail MUST show Activity beside Comments; comments MUST NOT appear as activity events.
- **FR-032**: Empty comments MUST be blocked on the screen without a server call, and rejected by the server if called.
- **FR-033**: Tickets, comments, and ticket activity MUST be stored in MongoDB. Users, products, and memberships MUST be stored in PostgreSQL.

### Key Entities

- **Product**: Workspace that owns tickets. At least one exists. Users have one or many memberships.
- **User**: Person; memberships to products.
- **Ticket**: Support request under exactly one product; fields in FR-018.
- **Comment**: Flat message on one ticket; no parent comment.
- **Activity entry**: Audit of a successful field or status change (who, when, what).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create a ticket (correct product rules) and see it OPEN in the list in under 2 minutes.
- **SC-002**: All ten allowed transitions succeed and persist after restart in 100% of a scripted walkthrough.
- **SC-003**: Listed illegal transitions (and at least one other unlisted pair) are rejected on the screen (no server call) and by the server, with unchanged status, in 100% of trials.
- **SC-004**: Combined multi-selects plus keyword search isolate a known ticket in a 20-ticket fixture on the first attempt in under 1 minute, without showing other products’ tickets.
- **SC-005**: Automated tests cover all allowed transitions, documented illegal examples, immutability, stale version, assignee default, blank title, empty comment, and product isolation before acceptance.
- **SC-006**: Blank title, missing product, and empty comment are blocked on the screen (no call) and rejected by the server in 100% of tests, with plain-language errors.
- **SC-007**: Field edits and comments on CLOSED/CANCELLED fail in 100% of tests; reopen to REOPEN still succeeds.
- **SC-008**: In a two-product setup, a single-product user never sees the other product’s tickets in 100% of list and detail attempts.
- **SC-009**: After a title change and a legal status change, Activity shows both events and Comments does not list them as comments, in 100% of trials.

## Assumptions

- Login/SSO is out of scope; the app acts as a chosen user identity. Product membership still limits which tickets that identity can see.
- Priority omitted on create → MEDIUM. Assignee omitted → reporter.
- Keyword search is case-insensitive on title and description.
- Product and membership administration may be seeded outside ticket screens.
- Multi-product users see the union of their products’ tickets unless filters narrow it.
- Tickets are never deleted; comments are never edited or deleted.
- Attachments, notifications, SLAs, moving a ticket to another product, and real-time push are out of scope.
- Ratified `spec/` still describes a Postgres-only, four-status, no-product model until a human replaces it.

## Out of scope

- Comment threads, mentions, reactions
- Moving a ticket between products after create
- Cross-product access for non-members

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

All other pairs are illegal.
