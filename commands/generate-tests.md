# Command: generate tests

Use this prompt after backend or frontend behavior exists. Do not generate empty test suites while `backend/` and `frontend/` are placeholders.

```
Generate tests for Jira-Lite from the spec. Do not invent extra product behavior.

Read:
- spec/test-strategy.md
- spec/api-contract.md
- spec/state-machine.md
- spec/data-model.md
- rules/testing.md
- rules/java-springboot.md (backend) or rules/frontend.md (frontend)

Implement the must-cover cases in spec/test-strategy.md:
- Create → OPEN, null assignee, TCK-n key
- List filters
- Allowed and illegal status transitions
- CLOSED field lock and reopen
- Comment allowed vs CLOSED → 409
- 404 unknown ticket, 400 validation, assignee role check
- Pagination size cap

Place tests next to existing project test layout once the app is scaffolded.
Use Testcontainers for DB-backed API tests. No mocked state machine if the test claims to prove transitions.
```
