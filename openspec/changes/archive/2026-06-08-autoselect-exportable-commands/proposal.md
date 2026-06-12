## Why

Exporting commands currently requires users to manually select the commands that are safe to export, even though the command list already shows which commands are exportable.
Defaulting the export action to exportable commands reduces repetitive UI work and helps users avoid accidentally exporting commands that validation would reject.

## What Changes

- Update `CommandExportManager_exportSelected` so its `selected` parameter defaults to the commands in the export manager `commands` collection whose exportability is `true`.
- Keep the selectable choices for `selected` as the full active `commands` collection so users can override the default selection if needed.
- Keep existing export validation and replay-state updates unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-export-manager-command-list`: The export manager export action defaults selected commands to the exportable rows in the active command list.

## Impact

- Affects `CommandExportManager_exportSelected` and focused command-log applib tests for default selected commands.
- No API, persistence, or dependency changes are expected.
