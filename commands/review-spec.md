# Command: review spec

Use this prompt when `spec/` or `rules/` change, or before implementation.

```
Review the Jira-Lite spec set for consistency and implementability.

Read all of:
- spec/requirements.md
- spec/architecture.md
- spec/data-model.md
- spec/api-contract.md
- spec/state-machine.md
- spec/ui-flow.md
- spec/test-strategy.md
- spec/implementation-plan.md
- rules/api-standards.md
- docs/ai-review.md

Check:
1. TicketStatus and TicketPriority sets are identical everywhere.
2. Allowed transitions match the mermaid diagram and the 409 rules.
3. Every UI action maps to an API method; no UI-only fields.
4. Comment rules (append-only, none on CLOSED) match API and requirements.
5. MVP out-of-scope items are not required by the API or UI flow.
6. Error codes and pagination match rules/api-standards.md.

Output: contradictions, missing cases, and a short pass/fail on “ready to implement Phase 1”.
```
