# Command: review code changes

Use this prompt to review the **current git working tree** (staged and unstaged), not a GitHub PR.

```
Review the local code changes in this Jira-Lite repo.

Run:
- git status
- git diff
- git diff --staged

Focus on the diff, not the whole codebase.

Check:
1. Does the diff match the request and spec/rules that apply to the touched files?
2. Accidental secrets, debug leftovers, unrelated files
3. Broken layering (business logic in Spring controllers) if backend changed
4. Contract drift if API or ticket fields changed (then read spec/api-contract.md)
5. Missing tests only if behavior changed

Output: findings by severity (blocker / major / nit) with file paths. Do not commit.
```
