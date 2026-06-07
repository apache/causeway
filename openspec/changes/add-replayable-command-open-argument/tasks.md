## 1. Open Argument Mixin

- [ ] 1.1 Add a `ReplayableCommand_openArgument` mixin associated with the `participants` collection.
- [ ] 1.2 Configure the `openArgument` action with layout sequence `2`.
- [ ] 1.3 Register the new mixin with the command log applib module.

## 2. Action Behaviour

- [ ] 2.1 Disable `openArgument` when no participants have role `PARAMETER`.
- [ ] 2.2 Add a `parameterName` action parameter with choices from parameter participant names.
- [ ] 2.3 Default `parameterName` when exactly one parameter participant is available and leave it empty for multiple participants.
- [ ] 2.4 Validate the selected `parameterName` when the matching parameter participant has no actual bookmark.
- [ ] 2.5 Open the selected parameter participant's actual argument object when available.

## 3. Tests and Validation

- [ ] 3.1 Add focused tests for action association, sequence, disabled state, choices, defaults, validation, and opening the selected argument.
- [ ] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [ ] 3.3 Run OpenSpec validation for the change.
