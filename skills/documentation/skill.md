---
name: documentation
description: Keep Jira-Lite spec, decisions, and prompt history in sync when product or API behavior changes. Use when editing spec/, rules/, docs/, or when implementation would drift from the contract.
---

# Documentation sync

## When to apply

Any change to ticket fields, statuses, endpoints, UI screens, or coding rules.

## Source of truth order

1. `spec/requirements.md` — what the product does
2. `spec/data-model.md` + `spec/state-machine.md` — data and transitions
3. `spec/api-contract.md` — HTTP
4. `spec/ui-flow.md` — screens
5. `spec/architecture.md`, `spec/test-strategy.md`, `spec/implementation-plan.md`
6. `rules/` — how to implement
7. `docs/decisions.md` — why we chose something
8. Code last (`backend/`, `frontend/`)

## Rules

- If you change a status name, enum, or path, update **every** spec file that mentions it in the same change.
- Do not add API routes in code before `spec/api-contract.md`.
- Append `docs/prompt-history.md` when a prompt produced a structural or product decision.
- Add an ADR-style entry in `docs/decisions.md` when layout, stack, or MVP scope changes.
- Never put secrets, tokens, or real passwords in spec or docs.

## Output

After a spec edit, list which files were aligned (status lists, JSON examples, UI copy).
