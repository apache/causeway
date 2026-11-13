## Why

The unified command manager's replay-multiple action currently has a minimum selectable bound of five commands, so an operator cannot deliberately use that reviewed bulk-replay path for exactly one pending or failed command.
A bound of one provides precise operational control while retaining the action's existing ordering, failure, and background-work safeguards.

## What Changes

- Add one command as a selectable bound for replay-multiple.
- Preserve the existing default bound of ten and all larger bounds.
- Preserve current manager ordering, participant-review assumptions, failure handling, and pending-background-work gates.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `unified-command-manager`: Allow replay-multiple to execute a bounded prefix of exactly one pending-or-failed command when the operator selects that bound.

## Impact

- Affects the replay bound choices exposed by `CommandManager_replayOrRetryMultiple` in the commandlog applib module.
- Does not change the replay algorithm, default selection, persistence model, or public service contracts.
