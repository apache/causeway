## 1. Movement Action Structure

- [ ] 1.1 Review `CommandExportManager_moveCommands`, module registration, layouts, and tests that refer to the current `moveCommands` action.
- [ ] 1.2 Introduce shared movement support through a helper class or superclass for validation, target choices, command extraction, timestamp updates, and gap constants.
- [ ] 1.3 Rename the existing manager movement action to `CommandExportManager_moveCommandsUp` and expose it as `moveCommandsUp`.
- [ ] 1.4 Add `CommandExportManager_moveCommandsDown` and expose it as `moveCommandsDown`.
- [ ] 1.5 Remove the old generic `CommandExportManager_moveCommands` registration and source references.

## 2. Movement Semantics

- [ ] 2.1 Preserve existing upward movement behavior, including command-log recording support guards, active-set validation, target choices, selected-order preservation, and squash timing.
- [ ] 2.2 Implement downward movement that places selected commands immediately before the target command.
- [ ] 2.3 Ensure downward movement preserves selected order, supports squash timing, updates command DTO timestamps, and leaves target and unrelated commands unchanged.
- [ ] 2.4 Ensure both movement actions reject empty selections, missing targets, selected targets, excluded commands, and commands outside the baseline-bounded active set.

## 3. Tests and Metadata

- [ ] 3.1 Update existing move-command tests to use `moveCommandsUp` for the existing upward behavior.
- [ ] 3.2 Add focused tests for `moveCommandsDown` choices, validation, timestamp placement, selected-order preservation, squash timing, and unrelated command preservation.
- [ ] 3.3 Update module registration tests, generated metadata expectations, or layout references from `moveCommands` to the direction-specific actions.
- [ ] 3.4 Verify active command list and export validation tests still pass after the action rename.

## 4. Verification

- [ ] 4.1 Run focused command-log applib tests for command export manager movement and affected registration or metadata tests.
- [ ] 4.2 Run OpenSpec validation for `add-move-commands-down-action`.
