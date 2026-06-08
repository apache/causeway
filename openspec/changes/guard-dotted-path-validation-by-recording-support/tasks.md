## 1. Current Behavior Verification

- [ ] 1.1 Confirm `CommandExportManager_exportSelected` invokes `CommandExportKnownTargetValidator` for both `act` and `validateSelected` without checking command-log recording support.
- [ ] 1.2 Confirm existing metamodel synthetic navigation action creation remains separately gated by recording support and does not need changes.

## 2. Core Implementation

- [ ] 2.1 Add access to `CausewayConfiguration` or an equivalent recording-support policy at the command export action boundary.
- [ ] 2.2 Bypass `CommandExportKnownTargetValidator` from `act` when command-log recording support is disabled.
- [ ] 2.3 Bypass `CommandExportKnownTargetValidator` from `validateSelected` when command-log recording support is disabled.
- [ ] 2.4 Preserve the existing validator behavior and failure messages when command-log recording support is enabled.

## 3. Test Coverage

- [ ] 3.1 Add or update focused export validation tests showing an unknown selected action target is accepted when recording support is disabled.
- [ ] 3.2 Add or update focused export validation tests showing an unknown reference action parameter is accepted when recording support is disabled.
- [ ] 3.3 Keep or add tests showing unknown selected action targets and reference parameters are rejected when recording support is enabled.
- [ ] 3.4 Run the commandlog applib replay/export test set that covers the changed behavior.

## 4. Validation

- [ ] 4.1 Run `openspec validate guard-dotted-path-validation-by-recording-support --strict`.
- [ ] 4.2 Review the final diff for unintended API, YAML, replay, or metamodel changes.
