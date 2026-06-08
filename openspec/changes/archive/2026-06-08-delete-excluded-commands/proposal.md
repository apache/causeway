## Why

After commands have been excluded from the Command Export Manager, users may decide those commands are no longer useful and should be removed from the command log entirely.
Providing deletion from `excludedCommands` keeps destructive cleanup limited to commands the user has already removed from the active export sequence.

## What Changes

- Add a `deleteCommands` action associated with the `excludedCommands` collection.
- Allow one or more excluded commands to be selected and deleted in one invocation.
- Disable the action when the `excludedCommands` collection is empty.
- Use `choicesFrom = "excludedCommands"` and also provide explicit choices for the selected command parameter.
- Validate direct invocation so only commands currently in the baseline-bounded `excludedCommands` collection can be deleted.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-command-exclusion`: Adds deletion of baseline-bounded excluded commands from the export manager.

## Impact

- Affects `CommandExportManager` and a new action mixin under `extensions/core/commandlog/applib`.
- Deletes backing `CommandLogEntry` entities for selected excluded commands.
- Requires tests for multi-select deletion, empty-collection disablement, explicit choices, and validation against non-excluded or stale selections.
