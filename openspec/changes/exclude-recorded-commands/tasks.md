## 1. Collection Filtering

- [ ] 1.1 Update `CommandExportManager#getCommands()` so it returns only baseline-bounded foreground commands whose replay state is `UNDEFINED` or `EXPORTED`.
- [ ] 1.2 Add `CommandExportManager#getExcludedCommands()` for baseline-bounded foreground commands whose replay state is `EXCLUDED`.
- [ ] 1.3 Place `excludedCommands` below `commands` in the export manager layout metadata.
- [ ] 1.4 Ensure exportability and known-participant traversal use the active command sequence so excluded commands are not considered part of the export sequence.

## 2. Exclude Commands Action

- [ ] 2.1 Add a `CommandExportManager_excludeCommands` mixin associated with the `commands` collection.
- [ ] 2.2 Implement the action to accept one or more selected active `ReplayableCommand` rows and set their backing command log entries to replay state `EXCLUDED`.
- [ ] 2.3 Add a default for the selected commands parameter that auto-selects active commands whose exportability is explicitly `false`.
- [ ] 2.4 Ensure selected-command defaults omit commands whose exportability is `true` or `null`.
- [ ] 2.5 Validate that at least one command is selected.
- [ ] 2.6 Validate that every selected command is currently present in the active `commands` collection before changing replay state.
- [ ] 2.7 Disable and guard the action when command-log recording support is disabled.
- [ ] 2.8 Register the new mixin in `CausewayModuleExtCommandLogApplib` if explicit registration is required.

## 3. Existing Actions and Choices

- [ ] 3.1 Confirm `CommandExportManager_exportSelected` choices come only from the filtered active `commands` collection.
- [ ] 3.2 Confirm `CommandExportManager_moveCommands` choices and validation reject excluded commands because they are outside the active `commands` collection.
- [ ] 3.3 Update action descriptions or labels if needed so users understand excluded commands are omitted from export and movement.

## 4. Tests

- [ ] 4.1 Update `CommandExportManagerCommandsTest` to verify `commands` includes `UNDEFINED` and `EXPORTED` commands and omits `EXCLUDED` commands.
- [ ] 4.2 Add tests that `excludedCommands` includes only baseline-bounded `EXCLUDED` commands.
- [ ] 4.3 Add tests for `CommandExportManager_excludeCommands` successful state transitions, empty selection validation, and stale/outside-selection validation.
- [ ] 4.4 Add tests that selected-command defaults include non-exportable active commands and omit exportable or unknown-exportability commands.
- [ ] 4.5 Add tests that exclusion is disabled and direct invocation is guarded when command-log recording support is disabled.
- [ ] 4.6 Update `CommandExportManagerMoveCommandsTest` if needed to assert excluded commands are not offered as targets and cannot be moved by direct invocation.

## 5. Verification

- [ ] 5.1 Run focused command-log applib tests for command export manager collections, exclusion, export selected, and move commands.
- [ ] 5.2 Run `openspec status --change exclude-recorded-commands` and resolve any artifact or spec validation issues.
