## 1. Remove Open Action Mixins

- [x] 1.1 Delete the `ReplayableCommand_openTarget` mixin class.
- [x] 1.2 Delete the `ReplayableCommand_openArgument` mixin class.
- [x] 1.3 Delete the `ReplayableCommand_openResult` mixin class.
- [x] 1.4 Remove the deleted mixins from command log applib module imports and registrations.

## 2. Tests

- [x] 2.1 Remove focused tests for `openTarget`, `openArgument`, and `openResult` action metadata and behaviour.
- [x] 2.2 Ensure participant object resolution tests still cover target, argument, and result object availability through participant rows.

## 3. Validation

- [x] 3.1 Run the command log applib focused test suite for replayable command mapping.
- [x] 3.2 Run OpenSpec validation for the change.
