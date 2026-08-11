## ADDED Requirements

### Requirement: Handled replay failures are recorded and mapped to a successful outcome

The system SHALL record a handled replay failure durably and then present the replay outcome as successful, so that callers and bounded, multiple, or next replay batches treat the failure as handled and continue to the next command. A handled replay failure is an advisor veto, a hidden or disabled target, or a replay-result-mapping conflict.

The recorded failure SHALL set the command's replay state to `FAILED`, SHALL store a failure reason, and SHALL store the underlying exception. The failure reason SHALL be classified with a typed prefix that distinguishes a hidden or disabled target (`Disabled:`) from an invalid input (`Invalid:`). Recording the failure MUST be committed in a `REQUIRES_NEW` transaction, independently of the outer replay transaction, so the failure record survives even though the outer outcome is success.

#### Scenario: A handled failure is recorded but the outcome is success

- **GIVEN** a command whose replay fails a pre-requisite / advisor check
- **WHEN** the command is replayed
- **THEN** its replay state is recorded as `FAILED` with a reason and the exception
- **AND** the replay outcome returned to the caller is a success

#### Scenario: The failure record is committed in its own transaction

- **GIVEN** a command replay that fails in a handled way
- **WHEN** the failure is recorded
- **THEN** the `FAILED` state, reason, and exception are committed in a separate transaction
- **AND** are visible even though the outer replay outcome is success

#### Scenario: The failure reason carries a typed classification prefix

- **GIVEN** a replay failure caused by a hidden or disabled target
- **WHEN** the failure reason is recorded
- **THEN** the reason is prefixed `Disabled:`

#### Scenario: An invalid-input failure is classified distinctly

- **GIVEN** a replay failure caused by invalid input
- **WHEN** the failure reason is recorded
- **THEN** the reason is prefixed `Invalid:`
