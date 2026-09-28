# Command: preview, test, and merge PR

Use this prompt to preview a GitHub pull request, run tests on its head, merge it into its destination branch, and update the local destination. Follow [rules/git.md](../rules/git.md). Review-only (no merge) stays in [review-pr.md](review-pr.md).

```
Preview, test, merge, and pull a GitHub PR for this Jira-Lite repo.

1. Resolve the PR:
   - If the user passed a URL or number, use that.
   - Else the PR for the current branch (gh pr view).
   - Else if exactly one open PR, use it.
   - Else gh pr list and stop to ask which PR.

2. Preview:
   gh pr view --json title,body,baseRefName,headRefName,url,mergeable
   gh pr diff
   Apply the same review checks as commands/review-pr.md:
   - PR title matches JRL-n: imperative summary
   - Diff matches the description
   - Spec vs code if ticket/API/UI changed
   - No secrets
   - Tests for behavior changes
   If there is a blocker finding: report it and do not merge.

3. Test the PR head:
   gh pr checkout <n>
   cd backend && ./gradlew test
   cd frontend && npm run build
   (Use a frontend test script when one exists.)
   If either command fails: report and do not merge.

4. Merge into the PR base (baseRefName, usually main):
   Only if mergeable is MERGEABLE (or equivalent) and there were no blockers or test failures.
   gh pr merge <n> --merge
   Matches existing history (Merge pull request #…).
   No --admin. No --force. No squash/rebase unless the user explicitly asks.

5. Update local destination:
   git checkout <baseRefName>
   git pull origin <baseRefName>

Return the PR URL and merge result. Do not merge if GitHub says not mergeable.
```
