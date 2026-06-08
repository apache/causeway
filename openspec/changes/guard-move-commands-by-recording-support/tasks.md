## 1. Locate Existing Recording Support Checks

- [ ] 1.1 Review `CommandExportManager_exportSelected` to confirm how it reads command-log recording support from `CausewayConfiguration`.
- [ ] 1.2 Review `CommandExportManager_moveCommands` to identify current disable and validation flow.
- [ ] 1.3 Review existing move command tests to identify where enabled recording support must be provided explicitly.

## 2. Guard Move Action by Recording Support

- [ ] 2.1 Inject or otherwise make `CausewayConfiguration` available to `CommandExportManager_moveCommands`.
- [ ] 2.2 Add an `isRecordingSupportEnabled()` helper consistent with export selected behavior.
- [ ] 2.3 Update `disableAct()` so the move action is disabled when recording support is disabled.
- [ ] 2.4 Update validation so direct invocation is rejected before selection-specific checks when recording support is disabled.
- [ ] 2.5 Ensure the action does not retimestamp selected or target commands when recording support is disabled.

## 3. Update Tests

- [ ] 3.1 Update existing move action tests so normal movement scenarios run with recording support enabled.
- [ ] 3.2 Add a test showing `disableAct()` reports the move action as unavailable when recording support is disabled.
- [ ] 3.3 Add a test showing direct invocation with recording support disabled is rejected.
- [ ] 3.4 Add assertions that disabled direct invocation leaves selected and target timestamps unchanged.
- [ ] 3.5 Ensure existing selection, target-choice, and retimestamp tests still pass when recording support is enabled.

## 4. Verify

- [ ] 4.1 Run focused command-log applib tests for `CommandExportManagerMoveCommandsTest`.
- [ ] 4.2 Run the command-log applib module test suite.
- [ ] 4.3 Run `openspec validate guard-move-commands-by-recording-support --strict` and fix any issues.
