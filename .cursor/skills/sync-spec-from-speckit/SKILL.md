---
name: sync-spec-from-speckit
description: After GitHub Spec Kit / Specify commands, keep specs/ feature artifacts and merge product-behavior deltas into spec/*.md. Use when running speckit-specify, speckit-plan, speckit-tasks, speckit-clarify, speckit-analyze, speckit-checklist, /speckit-*, Spec Kit, Specify, or when specs/ markdown changes product behavior and spec/ must stay aligned.
---

# Sync spec/ from Spec Kit

Companion to vendor `.cursor/skills/speckit-*`. Do not replace Spec Kit. Do not edit those vendor skill files.

`specs/<NNN-short-name>/` holds feature working papers. `spec/` (singular) is the product source of truth.

## When

After `/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-clarify`, `/speckit-analyze`, `/speckit-checklist`, or any Specify/Spec Kit run that changes feature markdown.

Skip `spec/` when output is non-product: constitution-only, checklist fluff, GitHub issues, or implementation with no spec delta.

## Workflow

1. Finish the Spec Kit command first. Feature files still go under `specs/<NNN-short-name>/`.
2. Before reporting done, read **new or changed** files in that feature dir and the current `spec/` files that could be affected.
3. Merge **product-behavior** deltas into `spec/`. Edit only files that actually changed.
4. Follow [skills/documentation/skill.md](../../../skills/documentation/skill.md): same change set for renamed statuses/enums/paths; no secrets; append `docs/prompt-history.md` only if the run was a structural/product decision (include **Mode**).
5. Report which `spec/` files were updated, or `no spec/ changes`.

## Mapping

| Spec Kit artifact | `spec/` target |
| --- | --- |
| `spec.md` user stories, FRs, out-of-scope | `spec/requirements.md` |
| Status names, allowed transitions, closed-ticket rules | `spec/state-machine.md` |
| Entities, fields, keys | `spec/data-model.md` |
| HTTP paths, JSON, errors, query params (`contracts/` too) | `spec/api-contract.md` |
| Screens and UX rules | `spec/ui-flow.md` |
| Stack, layers, deployment notes | `spec/architecture.md` |
| Acceptance/test criteria for new rules | `spec/test-strategy.md` |
| Phased work from `plan.md` / `tasks.md` | `spec/implementation-plan.md` |

Prefer amending existing tables/sections. Do not maintain two conflicting status lists.

## Conflicts

If Spec Kit contradicts ratified `spec/` or `.specify/memory/constitution.md`, do **not** silently overwrite. List the conflict and stop for human review (Human Engineering Authority). A dated subsection is allowed when extending, not when inventing a second lifecycle.
