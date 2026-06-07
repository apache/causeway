## 1. Open Argument Mixin

- [x] 1.1 Add a `ReplayableCommand_openArgument` mixin associated with the `participants` collection.
- [x] 1.2 Configure the `openArgument` action with layout sequence `2`.
- [x] 1.3 Register the new mixin with the command log applib module.

## 2. Action Behaviour

- [x] 2.1 Disable `openArgument` when no participants have role `PARAMETER`.
- [x] 2.2 Add a `parameterName` action parameter with choices from parameter participant names.
- [x] 2.3 Default `parameterName` when exactly one parameter participant is available and leave it empty for multiple participants.
- [x] 2.4 Validate the selected `parameterName` when the matching parameter participant has no actual bookmark.
- [x] 2.5 Open the selected parameter participant's actual argument object when available.

## 3. Tests and Validation

- [x] 3.1 Add focused tests for action association, sequence, disabled state, choices, defaults, validation, and opening the selected argument.
- [x] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [x] 3.3 Run OpenSpec validation for the change.
