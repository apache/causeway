## Context

`CommandExportManager` currently exposes a single `commands` collection containing foreground commands at or after the export baseline regardless of replay state.
Export and move actions are associated with that collection, so every visible row remains part of the active sequence for export repair and export selection.

Users can accidentally record administrative, exploratory, or otherwise unwanted commands while preparing a replayable scenario.
The system already has a replay state value of `EXCLUDED`, but the active export manager page does not provide a current way to move selected recorded commands into that state or display them separately.

Command movement and known-target exportability are meaningful only when command-log recording support is enabled.
The exclusion action follows the same guard because it is a replay-support workflow action rather than a general command-log maintenance operation.

## Goals / Non-Goals

**Goals:**

- Provide an export-manager action that changes selected active commands to replay state `EXCLUDED`.
- Default the action selection to active commands whose `ReplayableCommand#getExportable()` value is `false`.
- Keep the active `commands` collection limited to `UNDEFINED` and `EXPORTED` commands.
- Add an `excludedCommands` collection immediately below `commands` for baseline-bounded commands in replay state `EXCLUDED`.
- Ensure export and movement continue to work from the active command collection and therefore ignore excluded commands.
- Disable the exclusion action when command-log recording support is disabled.

**Non-Goals:**

- Do not delete command log entries.
- Do not introduce an undo or restore action for excluded commands in this change.
- Do not change import or replay execution semantics for commands already imported into replay states such as `PENDING`, `OK`, or `FAILED`.
- Do not change view-model memento structure unless layout metadata requires no persistent state.

## Decisions

1. Represent exclusion by updating `CommandLogEntry.replayState` to `EXCLUDED`.

This reuses the existing replay state model and preserves full command log history.
Alternative considered: maintain a separate export-manager exclusion list in the view-model memento.
That alternative was rejected because exclusions need to persist independently of a particular manager instance and because `EXCLUDED` already expresses the intended domain state.

2. Filter active commands at the collection query boundary.

`CommandExportManager#getCommands()` will continue to obtain baseline-bounded foreground entries, but it will include only entries whose replay state is `UNDEFINED` or `EXPORTED` before constructing `ReplayableCommand` rows.
The new `getExcludedCommands()` collection will use the same baseline and limit semantics but include only `EXCLUDED` entries.
Alternative considered: return all commands and hide excluded rows in the UI layout.
That alternative was rejected because export and movement choices derive from the collection and must not include excluded commands.

3. Add a `CommandExportManager_excludeCommands` mixin associated with `commands`.

The action will accept one or more selected `ReplayableCommand` rows from the active collection, validate that each selection is baseline-bounded and currently active, then set the backing command log entries to `EXCLUDED`.
The action will provide a default selection containing active commands whose exportability value is explicitly `false`.
Commands whose exportability is `true` or `null` will not be auto-selected, though users can still manually select active commands to exclude.
The action returns the same `CommandExportManager` so the page refresh shows those commands under `excludedCommands`.
Alternative considered: add the action to each `ReplayableCommand` row.
That alternative was rejected because the user requirement is to exclude one or more commands from the export-manager sequence in one operation.

4. Gate exclusion by command-log recording support.

The exclusion action will inject `CausewayConfiguration` and use the same recording-support enabled check pattern as command movement.
When recording support is disabled, the action is disabled and direct invocation validation still rejects the operation.
Alternative considered: allow exclusion regardless of recording support because it only changes state.
That alternative was rejected because exclusion changes replay/export behavior and should remain part of replay-support mode.

## Risks / Trade-offs

- Existing tests may assume `commands` includes all replay states → Update expectations to include only `UNDEFINED` and `EXPORTED`, and add focused tests for `EXCLUDED` rows in `excludedCommands`.
- The page limit could make active and excluded collections independently bounded → Use the existing manager limit consistently and document/test baseline filtering rather than changing pagination in this change.
- Direct callers could pass stale selected commands after a state change → Validate selected interaction IDs against the current active collection before mutating replay state.
- Auto-selection might surprise users if exportability is stale or unavailable → Only auto-select commands whose exportability is explicitly `false`, and leave `true` or `null` commands unselected by default.
- Filtering `commands` can affect exportability known-participant calculations → Ensure known-participant and exportability traversal uses the same active export sequence so excluded commands are not considered part of the command sequence.
