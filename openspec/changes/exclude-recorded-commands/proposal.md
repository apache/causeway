## Why

End-users can accidentally record commands that are not part of the intended replay/export scenario.
They need a safe way to remove those commands from the active export sequence without deleting command log history.

## What Changes

- Add an export-manager action that marks one or more selected commands as `EXCLUDED`.
- Auto-select active commands that are not exportable when presenting the exclusion action, so accidental/non-exportable recordings can be excluded quickly.
- Show only `UNDEFINED` and `EXPORTED` commands in the main `commands` collection.
- Add an `excludedCommands` collection below `commands` to show commands whose replay state is `EXCLUDED`.
- Ensure excluded commands are not considered for command export, exportability, or command movement choices.
- Enable exclusion functionality only when command-log recording support mode is enabled.

## Capabilities

### New Capabilities
- `command-export-command-exclusion`: Covers excluding baseline-bounded recorded commands from the export manager command sequence.

### Modified Capabilities
- `command-export-manager-command-list`: The export manager command collections now separate active commands from excluded commands.
- `command-export-command-reordering`: Command movement now operates only on active commands and must not consider excluded commands.

## Impact

- Affects `CommandExportManager`, its collection layout, and replay/export mixins under `extensions/core/commandlog/applib`.
- Affects command export and move command choices because they are associated with the active `commands` collection.
- Requires tests for collection filtering, exclusion state changes, default selection of non-exportable commands, and recording-support disablement.
