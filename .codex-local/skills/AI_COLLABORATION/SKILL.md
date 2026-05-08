---
name: AI_COLLABORATION
description: Use for this ChessBoard project when applying the user's collaboration workflow preferences. Covers review-first behavior, consultation before fixes, and how to handle multi-agent implementation and review without making unsanctioned code changes.
---

# AI Collaboration

Follow these collaboration rules when working with the user on this project.

## Review Before Fix

- If a review finds issues, do not fix them automatically.
- Present findings to the user first.
- Wait for the user's decision before making code changes based on review findings.

## Consult Before Changes

- If new issues, regressions, or follow-up improvements are discovered during implementation, do not automatically apply extra fixes outside the agreed scope.
- Explain what was found and ask the user how to proceed when the next step is not already explicitly agreed.
- Prefer narrow, approved changes over opportunistic cleanup.

## Multi-Agent Workflow

- If one agent implements code and another agent reviews it, the review agent should report findings only.
- Do not automatically apply review feedback after the review step.
- Summarize the findings for the user and wait for approval before changing code.

## Practical Rule

- The default behavior after review is: report first, change later.
- Only skip the extra approval step when the user has already explicitly asked for automatic fixes in advance.
- Treat default no-op callbacks such as `= {}` as a code smell in this project; report or avoid them instead of introducing them casually.

## Read-Only Git Escalation

- The user pre-approves running safe read-only git commands with elevated permissions when sandbox restrictions block normal execution.
- This permission is limited to viewing repository state and history.
- Use it without asking again for:
  - `git status`
  - `git branch`
  - `git branch -r`
  - `git branch -a`
  - `git remote -v`
  - `git log`
  - `git show`
  - `git diff`
  - `git diff --stat`
  - `git rev-parse`
  - `git symbolic-ref`
  - `git tag`
  - `git reflog`
  - `git stash list`
  - `git ls-files`
  - `git grep`
- Do not treat any git command that changes the working tree, index, local refs, remote refs, or network state as pre-approved.
- Commands that are still not auto-approved include:
  - `git fetch`
  - `git pull`
  - `git push`
  - `git switch`
  - `git checkout`
  - `git merge`
  - `git rebase`
  - `git cherry-pick`
  - `git commit`
  - `git branch -d`
  - `git branch -D`
  - `git reset`
  - `git clean`
  - `git stash push`
  - `git stash pop`
