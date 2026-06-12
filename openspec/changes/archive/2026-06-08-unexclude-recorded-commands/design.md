## Context

`CommandExportManager` now separates active commands from excluded commands.
The `commands` collection contains `UNDEFINED` and `EXPORTED` commands, while `excludedCommands` contains commands whose replay state is `EXCLUDED`.

The exclusion workflow intentionally preserves command log entries rather than deleting them.
Because users can make mistakes when excluding commands, they need a reciprocal action that restores excluded commands to the active export-manager sequence.

The existing exclusion action is guarded by command-log recording support mode.
The restoration action should use the same guard because it changes replay/export workflow state.

## Goals / Non-Goals

**Goals:**

- Provide an export-manager action associated with `excludedCommands` that restores one or more selected commands.
- Change restored command log entries from replay state `EXCLUDED` to replay state `UNDEFINED`.
- Validate that selected commands come from the current baseline-bounded `excludedCommands` collection.
- Disable and guard restoration when command-log recording support mode is disabled.
- Return the current `CommandExportManager` so the refreshed view shows restored commands in `commands`.

**Non-Goals:**

- Do not restore commands to `EXPORTED`, even if they were exported before being excluded.
- Do not introduce automatic restoration based on exportability.
- Do not change import or replay-manager exclusion behavior for replay states such as `PENDING`, `OK`, or `FAILED`.
- Do not change the `CommandExportManager` memento format.

## Decisions

1. Restore by setting replay state to `UNDEFINED`.

`UNDEFINED` is the active not-yet-exported state for recorded commands in the export manager sequence.
Alternative considered: remember and restore the command's pre-exclusion state.
That alternative was rejected because the current model persists only the current replay state, and restoring to `UNDEFINED` gives the user a safe active command that can be exported again.

2. Add a `CommandExportManager_unexcludeCommands` mixin associated with `excludedCommands`.

The action will accept a multi-select list of `ReplayableCommand` rows from `excludedCommands`.
It will validate that each selected command is still in the current baseline-bounded excluded collection before changing state.
Alternative considered: add a per-row `ReplayableCommand` action.
That alternative was rejected because users need to restore one or more excluded commands efficiently from the collection.

3. Use the same recording-support guard pattern as exclusion.

The new action will inject `CausewayConfiguration` and disable/validate when command-log recording support is disabled.
Alternative considered: allow restoration when recording support is disabled because restoration only changes state.
That alternative was rejected because the export-manager exclusion and restoration workflow is part of replay-support mode.

## Risks / Trade-offs

- Restoring to `UNDEFINED` loses prior `EXPORTED` state semantics → Make this explicit in requirements and tests so restored commands re-enter the active sequence as not-yet-exported.
- Direct callers could submit stale selected rows → Validate selected interaction IDs against the current `excludedCommands` collection before mutating state.
- Users may expect restored commands to be exportable immediately → Restoration only changes state, while existing exportability rules continue to determine whether the command can be exported safely.
