## 1. Public contracts and configuration

- [ ] 1.1 Add the Causeway 4 record-based `causeway.extensions.command-log.recording-support` configuration with `ENABLED` and `DISABLED` values, a disabled default, configuration documentation, and focused configuration tests.
- [ ] 1.2 Add the core applib `CommandRecordingSuppressed`, `PauseCommandLoggingEvent`, and `ResumeCommandLoggingEvent` public contracts with API documentation and focused contract tests where applicable.

## 2. Member execution suppression

- [ ] 2.1 Adapt `MemberExecutorServiceDefault` so marked interaction owners or targets bypass command preparation while member execution continues normally.
- [ ] 2.2 Add runtime-service tests covering marked owner, marked target, unmarked target, and unaffected member invocation behaviour.

## 3. Commandlog pause state

- [ ] 3.1 Add the application-wide atomic pause-depth service and application-event listener, with unit tests for nesting and unmatched resume events.
- [ ] 3.2 Make commandlog ready, started, and completed subscriber callbacks honour pause state, with focused subscriber or integration tests for creation and synchronization suppression.

## 4. Framework-owned pause scope

- [ ] 4.1 Publish paired pause and resume events around initial fixture-script installation using `try/finally` and without adding a commandlog extension dependency.
- [ ] 4.2 Add fixture-installer tests covering successful execution, failed execution, event ordering, and guaranteed resume publication.

## 5. Commandlog helper suppression

- [ ] 5.1 Mark the existing export manager, replay manager, replayable command, and applicable command log entry abstractions as recording-suppressed.
- [ ] 5.2 Add or adapt commandlog tests proving helper actions are not persisted through recording support while ordinary eligible interactions remain unaffected.

## 6. Verification

- [ ] 6.1 Run the focused applib, configuration, runtime-services, testing-fixtures, and commandlog test suites and resolve regressions caused by the new contracts.
- [ ] 6.2 Verify the external configuration property, Jakarta imports, public API documentation, and module dependency direction against the specification and programme ledger.
