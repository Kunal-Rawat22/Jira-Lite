# Jira-Lite — Support Ticket Management

A spec-driven learning project for a Jira-like support ticket system. This pass is **documentation and AI workflow only**: `backend/` and `frontend/` are empty placeholders. Application code comes later, following `spec/` and `rules/`.

## Folder map

| Path | Purpose |
| --- | --- |
| `spec/` | Product and engineering source of truth |
| `rules/` | Coding constraints for humans and AI |
| `skills/` | Cursor skill for keeping docs in sync |
| `commands/` | Reusable prompts (review code, review spec, generate tests) |
| `docs/` | Process, prompt history, and architecture decisions |
| `backend/` | Future Spring Boot API (empty) |
| `frontend/` | Future React + TypeScript SPA (empty) |
| `.specstory/history/` | Local conversation history placeholder |

## How to work

1. Change product behavior in `spec/` first (especially `requirements.md`, `data-model.md`, `api-contract.md`, `state-machine.md`).
2. Keep `rules/` in mind when implementing.
3. Use `commands/` prompts for reviews and test generation.
4. Record notable choices in `docs/decisions.md` and prompts in `docs/prompt-history.md`.

See `docs/ai-context-strategy.md` for which files to load for a given task.

## Postgres (later)

When the API exists, start a local database:

```bash
# Put POSTGRES_PASSWORD in a gitignored .env — do not commit secrets.
docker compose up -d
```

Default database name and user: `tickets`. Connection details for the app will live in local env files, not in this README.

## Tests

There is no application code yet, so there is nothing to lint or test. After Spring Boot and React are scaffolded, follow `spec/test-strategy.md` and `rules/testing.md`.
