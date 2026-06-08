## 1. Delete Commands Action

- [ ] 1.1 Add a `CommandExportManager_deleteCommands` mixin associated with the `excludedCommands` collection.
- [ ] 1.2 Annotate the action with `choicesFrom = "excludedCommands"`.
- [ ] 1.3 Implement `choicesSelected()` to explicitly return `CommandExportManager#getExcludedCommands()`.
- [ ] 1.4 Implement the action to accept one or more selected `ReplayableCommand` rows and delete their backing command log entries.
- [ ] 1.5 Return the current `CommandExportManager` after deleting selected commands.

## 2. Validation and Disablement

- [ ] 2.1 Disable `deleteCommands` when `excludedCommands` is empty.
- [ ] 2.2 Validate that at least one command is selected.
- [ ] 2.3 Validate that every selected command is currently present in the baseline-bounded `excludedCommands` collection before deleting.
- [ ] 2.4 Ensure active commands from `commands` cannot be deleted by direct invocation.

## 3. Registration and UI Metadata

- [ ] 3.1 Register the new mixin in `CausewayModuleExtCommandLogApplib` if explicit registration is required.
- [ ] 3.2 Add action description text indicating that deletion permanently removes selected excluded command log entries.
- [ ] 3.3 Confirm the action is associated with the `excludedCommands` collection.

## 4. Tests

- [ ] 4.1 Add tests that deleting one or more excluded commands removes their backing command log entries.
- [ ] 4.2 Add tests that the action rejects empty selections.
- [ ] 4.3 Add tests that active commands or stale selections outside `excludedCommands` are rejected.
- [ ] 4.4 Add tests that `deleteCommands` is disabled when `excludedCommands` is empty and enabled when not empty.
- [ ] 4.5 Add tests that explicit selected-command choices include excluded commands and omit active commands.

## 5. Verification

- [ ] 5.1 Run focused command-log applib tests for command export manager excluded-command deletion behavior.
- [ ] 5.2 Run `openspec status --change delete-excluded-commands` and resolve any artifact or spec validation issues.
