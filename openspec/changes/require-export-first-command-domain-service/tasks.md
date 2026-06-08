## 1. Validation Behavior

- [ ] 1.1 Review current command export known-target validation to confirm first-command semantics are handled by an empty known-participant set.
- [ ] 1.2 Adjust validation logic if the first selected command can currently be accepted with an ordinary domain-object target.
- [ ] 1.3 Ensure disabled command-log recording support continues to bypass dotted-path known-target export validation.

## 2. Exportability Indicator

- [ ] 2.1 Review `ReplayableCommand#getExportable()` and `CommandExportManager#knownParticipantsAsOf(...)` for first-command behavior.
- [ ] 2.2 Adjust exportability logic if the first replayable command can currently report `true` for an ordinary domain-object target.
- [ ] 2.3 Ensure a first replayable command targeting a domain service reports `true` when recording support is enabled.

## 3. Tests

- [ ] 3.1 Add or update export action validation tests for a first selected command targeting a domain service.
- [ ] 3.2 Add or update export action validation tests for a first selected command targeting an ordinary domain object.
- [ ] 3.3 Add or update exportability indicator tests for first-command domain-service and ordinary-domain-object targets.
- [ ] 3.4 Add regression coverage showing a later command is exportable when an earlier finder result establishes its target.

## 4. Verification

- [ ] 4.1 Run the focused commandlog applib command export tests.
- [ ] 4.2 Run OpenSpec validation for `require-export-first-command-domain-service`.
