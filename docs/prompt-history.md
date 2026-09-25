# Prompt history

Log prompts that changed structure, product scope, or workflow. Do not paste secrets.

## 2026-09-25 — Project structure scaffold

**Prompt:** Create the complete project structure (`backend/`, `frontend/`, `spec/`, `rules/`, `skills/`, `commands/`, `docs/`, `.specstory/history/`, `docker-compose.yml`, `.gitignore`, `README.md`).

**Decisions captured:**

- Place the tree at the **repo root** (no nested `support-ticket-management/` folder).
- **Docs-first:** empty `backend/` and `frontend/` except `.gitkeep`; full spec/rules/skills/commands/docs.
- Postgres 16 via Compose for later API work; no app services yet.

See [decisions.md](decisions.md).
