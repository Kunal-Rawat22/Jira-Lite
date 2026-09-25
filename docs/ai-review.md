# AI review

How to review this repo with an assistant. Prefer the prompts in `commands/`.

## Two review types

### Spec review

Run when `spec/` or `rules/` change, and before Phase 1 implementation.

- Use [commands/review-spec.md](../commands/review-spec.md).
- Goal: one status set, one API, no UI that the API cannot serve.

### Code review

Run when `backend/` or `frontend/` have diffs.

- Use [commands/review-code.md](../commands/review-code.md).
- Goal: code implements spec; if product should change, edit spec first, then code.

## Severity

| Level | Meaning |
| --- | --- |
| Blocker | Wrong status machine, wrong contract, data loss, secrets |
| Major | Missing must-cover test, layering violation, undocumented endpoint |
| Nit | Naming, extra comments, style |

Do not “fix” product disagreements only in code.

## Context

Load files listed in [ai-context-strategy.md](ai-context-strategy.md). Do not dump the whole repo when reviewing a single DTO.
