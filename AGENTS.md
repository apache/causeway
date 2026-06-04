# Repository agent instructions

## Git commit messages

When committing changes in this repository, prefix the commit message with the current git branch name.
The branch name typically reflects the Jira ticket, for example `CAUSEWAY-4010`.
Use the form `CAUSEWAY-4010: adds foo to bar`.

Write the commit subject so it completes the phrase "this commit, if applied, ...".
Prefer present-tense verbs such as `adds`, `updates`, `removes`, `fixes`, or `refactors`.
Avoid past-tense verbs such as `added`, `updated`, `removed`, or `fixed`.

## OpenSpec archive policy

When running `/opsx-archive` without an explicit change name, if there is exactly one active change then select it automatically and do not ask for confirmation.
When archiving a change with delta specs, sync the delta specs to the main specs automatically because syncing is the recommended path.
After archiving and syncing, create a git commit for the archive and spec-sync changes.
Use the repository git commit message rules above for that commit.

## OpenSpec apply policy

When running `/opsx-apply`, first check whether there are uncommitted OpenSpec proposal artifacts for the active change.
If proposal artifacts are uncommitted, create a git commit for those proposal docs before starting implementation.
Use the repository git commit message rules above for that commit.

## Session handoff

If `SESSION_HANDOFF.md` exists at the repository root, read it at the start of a new session for recent context.
