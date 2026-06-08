## 1. Locate Existing Recording Support Checks

- [x] 1.1 Review `CommandExportManager_exportSelected` to confirm how it reads command-log recording support from `CausewayConfiguration`.
- [x] 1.2 Review `CommandExportManager_moveCommands` to identify current disable and validation flow.
- [x] 1.3 Review existing move command tests to identify where enabled recording support must be provided explicitly.

## 2. Guard Move Action by Recording Support

- [x] 2.1 Inject or otherwise make `CausewayConfiguration` available to `CommandExportManager_moveCommands`.
- [x] 2.2 Add an `isRecordingSupportEnabled()` helper consistent with export selected behavior.
- [x] 2.3 Update `disableAct()` so the move action is disabled when recording support is disabled.
- [x] 2.4 Keep recording-support handling out of validation because disablement is evaluated first and greys out the action with the prompt reason.

## 3. Update Tests

- [x] 3.1 Update existing move action tests so normal movement scenarios run with recording support enabled.
- [x] 3.2 Add a test showing `disableAct()` reports the move action as unavailable when recording support is disabled.
- [x] 3.3 Ensure existing selection, target-choice, and retimestamp tests still pass when recording support is enabled.

## 4. Verify

- [x] 4.1 Run focused command-log applib tests for `CommandExportManagerMoveCommandsTest`.
- [x] 4.2 Run the command-log applib module test suite.
- [x] 4.3 Run `openspec validate guard-move-commands-by-recording-support --strict` and fix any issues.
