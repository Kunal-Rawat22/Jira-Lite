# Command: commit and push

Use this prompt to create a git commit, push a feature branch, and open a GitHub pull request. Follow [rules/git.md](../rules/git.md).

```
Commit, push, and open a GitHub PR for Jira-Lite changes.

Required sequence (never commit on main; never branch from a stale or unrelated feature branch):

1. Inspect: git status, git diff, git diff --staged, git log -8 --oneline.
   Ticket n is the next unused JRL- number from latest JRL- on origin/main (git log origin/main), not an old feature branch.
2. If the working tree is dirty and you are not already on the new feature branch created in this run:
   git stash push -u -m "jrl-n-wip"
   Never later commit secrets, .env, node_modules, .next, backend/bin/, build/, .gradle from that stash.
3. git checkout main
   git pull origin main
   Fast-forward only; do not create a merge commit on local main.
4. git checkout -b cursor/jrl-<n>-<short-slug>
   Skip this step only if that branch was already created from updated main in this same run.
5. git stash pop if a stash was created. Resolve conflicts if any.
6. git add the intended files. Do not add secrets, .env, node_modules, frontend/.next, backend/build, backend/.gradle, backend/bin.
7. git commit with HEREDOC. Message format: JRL-<n>: <imperative summary>
   - lowercase after the colon, no trailing period
   - optional one-sentence body for why
8. git push -u origin HEAD
   No --force to main. No --no-verify. Do not amend unless the user asked and amend rules allow it.
9. Always gh pr create with base main.
   Title = commit subject. Body: Summary + Test plan (HEREDOC). Print the PR URL.
10. git checkout main
    Local main still matches origin/main until the PR is merged.

If already on main with a clean tree, skip stash.

Push and PR only because this command was invoked.
```
