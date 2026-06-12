## Why

Users can mark accidentally recorded commands as `EXCLUDED` from the Command Export Manager, but may later discover that one or more excluded commands are actually needed in the export sequence.
They need a safe way to restore excluded commands without manually editing command log state or recreating the recording.

## What Changes

- Add an export-manager action associated with the `excludedCommands` collection that marks one or more selected excluded commands as active again.
- The action will set selected command log entries from replay state `EXCLUDED` back to `UNDEFINED`.
- The action will support multi-selection from `excludedCommands`.
- The action will be enabled only when command-log recording support mode is enabled.
- Direct invocation will validate that selected commands are currently in the baseline-bounded `excludedCommands` collection.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-command-exclusion`: Adds restoration of excluded commands back into the active export-manager command sequence.

## Impact

- Affects `CommandExportManager` and a new action mixin under `extensions/core/commandlog/applib`.
- Affects command export manager layout/action metadata for the `excludedCommands` collection.
- Requires focused tests for un-excluding commands, multi-selection validation, and recording-support disablement.
