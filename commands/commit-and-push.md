# Command: commit and push

Use this prompt to create a git commit and push. Follow [rules/git.md](../rules/git.md).

```
Commit and push Jira-Lite changes.

1. git status, git diff, git diff --staged, git log -8 --oneline
2. Do not commit secrets, .env, node_modules, .next, build/, .gradle
3. Message format: JRL-<n>: <imperative summary>
   - n is the ticket number, or next after the latest JRL- in git log
   - lowercase after the colon, no trailing period
   - HEREDOC for the message; optional one-sentence body for why
4. Feature branch if currently on main: cursor/jrl-<n>-<short-slug>
5. git add the intended files, git commit, git push -u origin HEAD
6. Do not --no-verify, do not force-push main, do not amend unless the user asked and the amend rules allow it
7. If the user asked for a PR: gh pr create against main, return the URL
8. Append docs/prompt-history.md if this was a structural prompt (Mode + template)

Push only because this command was invoked (user asked to push).
```
