## 1. Delete Commands Action

- [x] 1.1 Add a `CommandExportManager_deleteCommands` mixin associated with the `excludedCommands` collection.
- [x] 1.2 Annotate the action with `choicesFrom = "excludedCommands"`.
- [x] 1.3 Implement `choicesSelected()` to explicitly return `CommandExportManager#getExcludedCommands()`.
- [x] 1.4 Implement the action to accept one or more selected `ReplayableCommand` rows and delete their backing command log entries.
- [x] 1.5 Return the current `CommandExportManager` after deleting selected commands.

## 2. Validation and Disablement

- [x] 2.1 Disable `deleteCommands` when `excludedCommands` is empty.
- [x] 2.2 Validate that at least one command is selected.
- [x] 2.3 Validate that every selected command is currently present in the baseline-bounded `excludedCommands` collection before deleting.
- [x] 2.4 Ensure active commands from `commands` cannot be deleted by direct invocation.

## 3. Registration and UI Metadata

- [x] 3.1 Register the new mixin in `CausewayModuleExtCommandLogApplib` if explicit registration is required.
- [x] 3.2 Add action description text indicating that deletion permanently removes selected excluded command log entries.
- [x] 3.3 Confirm the action is associated with the `excludedCommands` collection.

## 4. Tests

- [x] 4.1 Add tests that deleting one or more excluded commands removes their backing command log entries.
- [x] 4.2 Add tests that the action rejects empty selections.
- [x] 4.3 Add tests that active commands or stale selections outside `excludedCommands` are rejected.
- [x] 4.4 Add tests that `deleteCommands` is disabled when `excludedCommands` is empty and enabled when not empty.
- [x] 4.5 Add tests that explicit selected-command choices include excluded commands and omit active commands.

## 5. Verification

- [x] 5.1 Run focused command-log applib tests for command export manager excluded-command deletion behavior.
- [x] 5.2 Run `openspec status --change delete-excluded-commands` and resolve any artifact or spec validation issues.
