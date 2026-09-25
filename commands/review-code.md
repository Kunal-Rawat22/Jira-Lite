# Command: review code

Use this prompt when reviewing `backend/` or `frontend/` against the spec and rules.

```
Review the current code changes for Jira-Lite (Support Ticket Management).

Read and apply:
- spec/requirements.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md (if frontend files changed)
- rules/java-springboot.md and rules/api-standards.md (backend)
- rules/frontend.md (frontend)
- rules/testing.md (if tests changed)
- docs/ai-review.md

Check:
1. Controllers have no business/state-machine logic.
2. Status transitions match spec/state-machine.md; illegal ones are 409.
3. JSON field names and enums match spec/api-contract.md.
4. Closed tickets are read-only except CLOSED → OPEN.
5. No secrets, no extra undocumented endpoints.
6. Tests cover create-open, illegal transition, closed comment.

Output: findings by severity (blocker / major / nit), file paths, and whether spec should change instead of code.
```
