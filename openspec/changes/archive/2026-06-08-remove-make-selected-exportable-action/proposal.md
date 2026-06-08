## Why

`CommandExportManager_makeSelectedExportable` is a leftover from an earlier export workflow and no longer fits the current command export manager UI.
Removing the obsolete collection action simplifies the manager and reduces confusion now that exportability is computed and exclusion/reordering actions cover the current workflow.

## What Changes

- Remove the `CommandExportManager_makeSelectedExportable` collection action from the command export manager.
- Remove module registration and any layout or metadata references for that action.
- Keep per-command exportability indicators and any still-supported per-command remediation actions unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-export-manager-command-list`: The export manager no longer exposes the obsolete `makeSelectedExportable` collection action.

## Impact

- Affects `CommandExportManager_makeSelectedExportable`, `CausewayModuleExtCommandLogApplib`, and command-log applib metadata or tests that expect the action.
- No persistence, YAML format, replay validation, or dependency changes are expected.
