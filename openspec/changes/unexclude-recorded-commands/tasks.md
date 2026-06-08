## 1. Restore Action Implementation

- [ ] 1.1 Add a `CommandExportManager_unexcludeCommands` mixin associated with the `excludedCommands` collection.
- [ ] 1.2 Implement the action to accept one or more selected `ReplayableCommand` rows and set their backing command log entries to replay state `UNDEFINED`.
- [ ] 1.3 Validate that at least one command is selected.
- [ ] 1.4 Validate that every selected command is currently present in the baseline-bounded `excludedCommands` collection before changing replay state.
- [ ] 1.5 Disable and guard the action when command-log recording support is disabled.
- [ ] 1.6 Return the current `CommandExportManager` after restoring selected commands.

## 2. Registration and UI Metadata

- [ ] 2.1 Register the new mixin in `CausewayModuleExtCommandLogApplib` if explicit registration is required.
- [ ] 2.2 Ensure the action is associated with `excludedCommands` and uses the excluded collection for choices.
- [ ] 2.3 Add action description text that explains restored commands re-enter the active command sequence as `UNDEFINED`.

## 3. Tests

- [ ] 3.1 Add tests that restoring one or more excluded commands changes their replay state to `UNDEFINED`.
- [ ] 3.2 Add tests that the restore action rejects empty selections.
- [ ] 3.3 Add tests that stale selections or active commands outside `excludedCommands` are rejected.
- [ ] 3.4 Add tests that restoration is disabled and direct invocation is guarded when command-log recording support is disabled.
- [ ] 3.5 Add tests or assertions that choices for selected commands come from `excludedCommands`.

## 4. Verification

- [ ] 4.1 Run focused command-log applib tests for command export manager exclusion and restoration behavior.
- [ ] 4.2 Run `openspec status --change unexclude-recorded-commands` and resolve any artifact or spec validation issues.
