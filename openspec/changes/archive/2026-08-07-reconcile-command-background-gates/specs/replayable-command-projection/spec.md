## MODIFIED Requirements

### Requirement: Replay or retry uses the P2 replay-state boundary
The replay-or-retry action SHALL be enabled for a replayable command in state `PENDING`, `OK`, or `FAILED` when no background command is pending execution. It SHALL be disabled for state `UNDEFINED`, legacy `EXPORTED`, or `EXCLUDED`. It SHALL also be disabled while at least one persisted `ExecuteIn.BACKGROUND` command has not yet started, with a message instructing the replay user to wait until pending background commands have executed and committed. The action guard SHALL use a replay-specific predicate and MUST NOT broaden exclusion-action eligibility. Direct invocation MUST NOT replay a command when either the replay-state or background-completion guard disables it.

#### Scenario: Successful command can be replayed again
- **GIVEN** no background command is pending
- **WHEN** a replayable command is in state `OK`
- **THEN** replay-or-retry is enabled

#### Scenario: Historical exported command cannot be replayed
- **WHEN** a replayable command is in state `EXPORTED`
- **THEN** replay-or-retry is disabled

#### Scenario: Excluded command cannot be replayed
- **WHEN** a replayable command is in state `EXCLUDED`
- **THEN** replay-or-retry is disabled

#### Scenario: Pending background work disables replay
- **GIVEN** a replayable command is in `PENDING`, `OK`, or `FAILED`
- **AND** at least one background command is pending execution
- **WHEN** replay-or-retry disablement is evaluated
- **THEN** replay-or-retry is disabled with the pending-background wait message

#### Scenario: Direct invocation cannot bypass background completion
- **GIVEN** a replayable command is otherwise replayable
- **AND** at least one background command is pending execution
- **WHEN** replay-or-retry is invoked directly
- **THEN** the command is not replayed
