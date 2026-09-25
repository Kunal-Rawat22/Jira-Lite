# Command: review PR

Use this prompt to review an open GitHub pull request for Jira-Lite.

```
Review the GitHub pull request for this Jira-Lite repo.

Use gh (or GitHub MCP):
- gh pr view --json title,body,baseRefName,headRefName,url
- gh pr diff
- gh pr checks (if any)

If the user passed a PR URL or number, use that; otherwise the PR for the current branch.

Read and apply when the diff touches those areas:
- spec/ and rules/ for product or API changes
- rules/java-springboot.md, rules/api-standards.md, rules/frontend.md, rules/testing.md, rules/git.md
- docs/ai-review.md

Check:
1. PR title matches JRL-n: imperative summary
2. Diff is complete vs the PR description
3. Spec vs code if ticket/API/UI changed
4. No secrets
5. Tests for behavior changes

Output: findings by severity (blocker / major / nit). Optionally leave a summary comment with gh pr comment. Do not merge unless asked.
```
