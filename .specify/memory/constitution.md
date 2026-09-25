<!--
Sync Impact Report
- Version change: (unratified template placeholders) → 1.0.0
- Modified principles: template PRINCIPLE_1–5 → ten named SDD principles (I–X)
- Added sections: Additional Constraints; Specification-Driven Development Workflow
- Removed sections: none (template SECTION_2/SECTION_3 placeholders replaced)
- Follow-up TODOs: none
-->

# Jira-Lite Constitution

## Core Principles

### I. Specification First

No implementation of product behavior MAY begin until the relevant
specification, technical plan, and tasks have been reviewed by a human.

- Product behavior MUST be defined in `spec/` (requirements, data model,
  API contract, state machine, UI flow, test strategy) before code for
  that behavior is written.
- A technical plan and a task breakdown MUST exist and MUST be reviewed
  before implementation of those tasks starts.
- Skeleton, tooling, or documentation work that does not implement
  unspecified product behavior MAY proceed without a new feature spec.

**Rationale:** Specification-Driven Development fails if code becomes the
source of truth. Review gates prevent unscoped generation.

### II. Human Engineering Authority

AI-generated specifications, plans, designs, and code MUST be reviewed
by a human before they are accepted.

- A human MUST approve spec, plan, and task artifacts before they are
  treated as binding.
- A human MUST review implementation (pull request or equivalent) before
  merge to `main`.
- AI output is a draft until a human accepts it. Acceptance is recorded
  by merge, explicit approval, or an ADR in `docs/decisions.md`.

**Rationale:** The project is a learning system. Humans remain accountable
for correctness, scope, and security.

### III. Backend Business Rule Authority

Business rules and validation MUST be enforced by the backend. The
frontend MUST NOT be treated as the source of truth.

- Ticket invariants, Bean Validation, and authorization-equivalent checks
  (as specified) MUST execute in server-side services or domain components.
- The UI MAY hide illegal actions for usability. Hidden or client-only
  checks MUST NOT replace server rejection of illegal requests.
- API clients MUST display backend validation and error messages; they
  MUST NOT invent a second rule set that diverges from the spec.

**Rationale:** Any client can call the API. Rules that live only in the UI
are not rules.

### IV. State Machine Integrity

Ticket status transitions MUST be implemented through an explicit
state-machine or equivalent business-rule mechanism defined in
`spec/state-machine.md`.

- Direct status mutation that bypasses transition validation is
  prohibited (controllers, repositories, entity listeners, SQL updates,
  and admin shortcuts included).
- Illegal transitions MUST be rejected by the backend with the specified
  error contract (for example HTTP 409 and `ILLEGAL_TRANSITION`).
- Closed-ticket mutation rules in the spec MUST be enforced by the same
  mechanism, not by ad hoc controller conditionals scattered across layers.

**Rationale:** Lifecycle correctness is the core product invariant.

### V. Testability

Important business rules, especially state transitions, MUST have
automated tests.

- Every business rule introduced by the specification MUST have an
  appropriate automated test (`rules/testing.md`, `spec/test-strategy.md`).
- Tests MUST cover happy paths, validation failures, not-found cases,
  valid and invalid state transitions, persistence behavior, and API
  behavior.
- A change that adds or alters a business rule without a corresponding
  test is non-compliant.

**Rationale:** Unverified rules regress silently. Transitions are the
highest-risk logic.

### VI. Simple Architecture

Do not introduce infrastructure that is not required by the specification.

- New services, brokers, caches, auth products, or frameworks MUST be
  justified by a spec or ADR. Absent that justification they MUST NOT
  be added.
- Prefer the existing Controller → Service → Repository layout, Java 21,
  Spring Boot, PostgreSQL, and Next.js App Router unless the spec changes.
- YAGNI: unused abstraction, premature microservices, and speculative
  scaling layers are prohibited.

**Rationale:** Extra infrastructure increases cost and hides the learning
goals of the ticket domain.

### VII. Traceability

Requirements, specifications, plans, tasks, implementation, tests, and
important engineering decisions MUST be traceable.

- Spec changes MUST update the related contract, state machine, data
  model, and test strategy files in the same change set when they are
  affected.
- Implementation tasks MUST map to spec items (IDs, headings, or
  ticket keys such as `JRL-n`).
- Standing technical choices MUST be recorded in `docs/decisions.md`.
- Structural prompts MUST be logged in `docs/prompt-history.md` with Mode.

**Rationale:** Without a trail, SDD cannot be audited or taught.

### VIII. AI Context Discipline

AI agents MUST receive only the relevant context required for the current
task rather than the entire repository whenever possible.

- Follow `docs/ai-context-strategy.md`: load spec and rules for the
  touched layer; do not load `.specstory/history/`, `node_modules/`,
  build output, or unrelated markdown by default.
- Agents MUST prefer `rules/`, `spec/`, and `.cursor/rules/` over dumping
  the whole tree into context.

**Rationale:** Excess context causes drift, cost, and contradictory
instructions.

### IX. Security

Secrets, credentials, API keys, and passwords MUST NOT be committed to
source control.

- `.env`, private keys, tokens, and production credentials MUST stay out
  of git. Use environment variables and documented non-secret examples.
- Responses MUST NOT include stack traces or secrets. Logging MUST NOT
  print credentials.
- `eval`, unsafe deserialization, disabled TLS, and auth or validation
  bypasses are prohibited.

**Rationale:** A learning repo is still a real repository with history
that cannot be fully erased.

### X. Incremental Implementation

Implementation MUST be performed in small, independently reviewable
tasks rather than generating the entire application at once.

- Each task MUST be mergeable and reviewable on its own (API slice,
  persistence slice, one UI screen, or equivalent).
- Generating the full ticket product in a single unreviewed drop is
  prohibited.
- Default delivery is a feature branch and pull request into `main`
  (`rules/git.md`).

**Rationale:** Small diffs match human review capacity and keep the
state machine and contract aligned.

## Additional Constraints

- This constitution governs Jira-Lite, a Support Ticket Management
  System developed with Specification-Driven Development.
- Runtime coding constraints in `rules/` (Java/Spring Boot, API
  standards, frontend, testing, git) MUST be followed; they MUST NOT
  contradict this constitution. If they conflict, amend the constitution
  first, then the rules.
- JPA entities MUST NOT be exposed as API JSON. Use DTOs and Bean
  Validation. Controllers MUST stay thin.
- Pagination is used only where the spec requires it. Search and filter
  use query parameters.

## Specification-Driven Development Workflow

1. Specify: update `spec/` for the behavior.
2. Plan: produce a technical plan that cites the spec.
3. Task: break the plan into independently reviewable tasks.
4. Human review of spec, plan, and tasks (Principles I and II).
5. Implement one task at a time with tests for new rules (Principles V
   and X).
6. Review code against spec, this constitution, and `rules/`.
7. Record decisions and prompts when the change is structural.

Quality gates for merge: spec alignment, constitution compliance,
automated tests for touched business rules, no secrets in the diff.

## Governance

This constitution supersedes informal practice and conflicting AI
defaults. `spec/` remains the product source of truth for behavior;
this document is the source of truth for how work is done.

**Amendments:** A human MUST propose the change, update this file,
bump the version, set Last Amended to the amendment date, and record
the reason in `docs/decisions.md` or `docs/prompt-history.md`. AI MAY
draft amendments; a human MUST accept them before they bind.

**Versioning:**

- MAJOR: remove or incompatibly redefine a principle.
- MINOR: add a principle or materially expand guidance.
- PATCH: clarification, wording, or non-semantic refinement.

**Compliance:** Reviews and pull requests MUST check constitution
compliance. Unjustified complexity, bypassed state transitions, missing
tests for new rules, or secret commits are merge blockers. Guidance for
day-to-day coding is `rules/` and `.cursor/rules/`.

**Version**: 1.0.0 | **Ratified**: 2026-09-25 | **Last Amended**: 2026-09-25
