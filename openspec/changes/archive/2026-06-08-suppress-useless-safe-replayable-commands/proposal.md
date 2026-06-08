## Why

Command-log recording support deliberately persists safe action command log entries, but safe actions that return nothing or return multiple results cannot establish a unique replay target mapping.
Wrapping those entries as `ReplayableCommand` rows adds noise and invites replay/export of commands that are not useful for replay.

## What Changes

- Continue to persist command log entries for safe actions when command-log recording support is enabled.
- Treat safe action entries with no single returned object bookmark as non-replayable.
- Do not expose non-replayable safe action entries as `ReplayableCommand` instances in command replay or command export flows.
- Preserve replayability for safe actions that return exactly one bookmarkable result.
- Preserve replayability for state-changing command log entries.

## Capabilities

### New Capabilities

- `replayable-command-eligibility`: Defines which command log entries are eligible to be represented as replayable commands.

### Modified Capabilities

- `safe-action-command-publishing`: Logged safe actions without a single bookmarkable result are still persisted but are not replayable command candidates.
- `command-export-manager-command-list`: Export manager command collections exclude command log entries that are not replayable command candidates.
- `command-replay-result-mapping`: Safe action replay and result mapping apply only to replayable safe action entries with a single recorded result bookmark.

## Impact

- Affects replay/export list construction for `ReplayableCommand` view models.
- Requires identifying safe action command log entries whose result is absent because the action returned void or a multi-result list.
- Requires tests that command log entries are still persisted while non-useful safe action entries are omitted from replay/export command collections.
