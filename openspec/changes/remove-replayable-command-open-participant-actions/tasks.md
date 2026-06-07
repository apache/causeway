## 1. Remove Open Action Mixins

- [ ] 1.1 Delete the `ReplayableCommand_openTarget` mixin class.
- [ ] 1.2 Delete the `ReplayableCommand_openArgument` mixin class.
- [ ] 1.3 Delete the `ReplayableCommand_openResult` mixin class.
- [ ] 1.4 Remove the deleted mixins from command log applib module imports and registrations.

## 2. Tests

- [ ] 2.1 Remove focused tests for `openTarget`, `openArgument`, and `openResult` action metadata and behaviour.
- [ ] 2.2 Ensure participant object resolution tests still cover target, argument, and result object availability through participant rows.

## 3. Validation

- [ ] 3.1 Run the command log applib focused test suite for replayable command mapping.
- [ ] 3.2 Run OpenSpec validation for the change.
