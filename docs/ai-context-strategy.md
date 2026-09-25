# AI context strategy

Load the smallest set that still prevents drift.

## Always

- `README.md` — current phase (docs-only vs apps)
- `docs/decisions.md` — standing ADRs
- When logging prompts: `docs/prompt-history.md` (include **Mode**)

## By task

| Task | Load |
| --- | --- |
| Change product behavior | All of `spec/`, then `rules/api-standards.md` |
| New/changed HTTP endpoint | `spec/api-contract.md`, `spec/data-model.md`, `spec/state-machine.md`, `rules/api-standards.md`, `rules/java-springboot.md` |
| Status/transition | `spec/state-machine.md`, `spec/api-contract.md`, `spec/test-strategy.md` |
| UI screen | `spec/ui-flow.md`, `spec/api-contract.md`, `rules/frontend.md` |
| Tests | `spec/test-strategy.md`, `commands/generate-tests.md`, `rules/testing.md`, plus the spec files for the feature |
| Code review | `commands/review-code.md`, `docs/ai-review.md`, plus spec/rules for the touched layer |
| Spec review | `commands/review-spec.md`, entire `spec/`, `rules/api-standards.md` |
| Keep docs in sync | `skills/documentation/skill.md` |

## Do not load by default

- `.specstory/history/` (noisy, not a spec)
- `node_modules/`, `target/`, build output
- Unrelated markdown when the task is a single endpoint

## Commands vs skills

- **Commands** (`commands/*.md`): paste or attach as the user prompt for a one-shot job.
- **Skill** (`skills/documentation/skill.md`): standing rule when editing spec/docs.
