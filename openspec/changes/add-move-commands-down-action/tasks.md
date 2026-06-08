## 1. Movement Action Structure

- [x] 1.1 Review `CommandExportManager_moveCommands`, module registration, layouts, and tests that refer to the current `moveCommands` action.
- [x] 1.2 Introduce shared movement support through a helper class or superclass for validation, target choices, command extraction, timestamp updates, and gap constants.
- [x] 1.3 Rename the existing manager movement action to `CommandExportManager_moveCommandsUp` and expose it as `moveCommandsUp`.
- [x] 1.4 Add `CommandExportManager_moveCommandsDown` and expose it as `moveCommandsDown`.
- [x] 1.5 Remove the old generic `CommandExportManager_moveCommands` registration and source references.

## 2. Movement Semantics

- [x] 2.1 Preserve existing upward movement behavior, including command-log recording support guards, active-set validation, target choices, selected-order preservation, and squash timing.
- [x] 2.2 Implement downward movement that places selected commands immediately after the target command.
- [x] 2.3 Ensure downward movement preserves selected order, supports squash timing, updates command DTO timestamps, and leaves target and unrelated commands unchanged.
- [x] 2.4 Ensure both movement actions reject empty selections, missing targets, selected targets, excluded commands, and commands outside the baseline-bounded active set.

## 3. Tests and Metadata

- [x] 3.1 Update existing move-command tests to use `moveCommandsUp` for the existing upward behavior.
- [x] 3.2 Add focused tests for `moveCommandsDown` choices, validation, timestamp placement, selected-order preservation, squash timing, and unrelated command preservation.
- [x] 3.3 Update module registration tests, generated metadata expectations, or layout references from `moveCommands` to the direction-specific actions.
- [x] 3.4 Verify active command list and export validation tests still pass after the action rename.

## 4. Verification

- [x] 4.1 Run focused command-log applib tests for command export manager movement and affected registration or metadata tests.
- [x] 4.2 Run OpenSpec validation for `add-move-commands-down-action`.
