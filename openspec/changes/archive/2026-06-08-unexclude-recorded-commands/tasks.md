## 1. Restore Action Implementation

- [x] 1.1 Add a `CommandExportManager_unexcludeCommands` mixin associated with the `excludedCommands` collection.
- [x] 1.2 Implement the action to accept one or more selected `ReplayableCommand` rows and set their backing command log entries to replay state `UNDEFINED`.
- [x] 1.3 Validate that at least one command is selected.
- [x] 1.4 Validate that every selected command is currently present in the baseline-bounded `excludedCommands` collection before changing replay state.
- [x] 1.5 Disable and guard the action when command-log recording support is disabled.
- [x] 1.6 Return the current `CommandExportManager` after restoring selected commands.

## 2. Registration and UI Metadata

- [x] 2.1 Register the new mixin in `CausewayModuleExtCommandLogApplib` if explicit registration is required.
- [x] 2.2 Ensure the action is associated with `excludedCommands` and uses the excluded collection for choices.
- [x] 2.3 Add action description text that explains restored commands re-enter the active command sequence as `UNDEFINED`.

## 3. Tests

- [x] 3.1 Add tests that restoring one or more excluded commands changes their replay state to `UNDEFINED`.
- [x] 3.2 Add tests that the restore action rejects empty selections.
- [x] 3.3 Add tests that stale selections or active commands outside `excludedCommands` are rejected.
- [x] 3.4 Add tests that restoration is disabled and direct invocation is guarded when command-log recording support is disabled.
- [x] 3.5 Add tests or assertions that choices for selected commands come from `excludedCommands`.

## 4. Verification

- [x] 4.1 Run focused command-log applib tests for command export manager exclusion and restoration behavior.
- [x] 4.2 Run `openspec status --change unexclude-recorded-commands` and resolve any artifact or spec validation issues.
