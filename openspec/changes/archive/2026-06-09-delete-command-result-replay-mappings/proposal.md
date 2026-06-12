## Why

Replay result mappings are persisted to support later command replay remapping, but operators currently have no dedicated menu action to clear stale mappings when resetting or repeating replay experiments.
A guarded delete-all action makes prototype replay maintenance safer and more discoverable while preserving the existing finder actions for inspection.

## What Changes

- Add a new command-log menu action that deletes all persisted `CommandReplayResultMapping` records.
- Mark the new action with `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE` so users must explicitly confirm the destructive operation.
- Hide the action when no `CommandReplayResultMappingRepository` is available, matching the existing replay mapping finder actions.
- Report the number of deleted mappings to the user using `MessageService`.
- Reorder command export and replay menu actions with `@ActionLayout(sequence)` so `exportManager` appears first, `replayManager` second, replay mapping finders after those managers, and the new delete action last among the replay mapping actions.
- Keep the change within the command log extension and do not introduce new dependencies.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-replay-result-mapping`: Add operator maintenance behavior for deleting all replay result mappings and define the menu action ordering around export, replay, finder, and delete actions.

## Impact

- Affects `CommandLogMenu` in the command log applib extension.
- Requires repository support for deleting all `CommandReplayResultMapping` entities across the existing applib abstraction and persistence-specific implementations.
- Requires or updates tests around command log menu behavior and repository deletion support.
- No external APIs, database schema, or dependency changes are expected.
