## ADDED Requirements

### Requirement: Replay or retry action is enabled only for replay states
The system SHALL expose a replay-or-retry action for replayable commands.
The replay-or-retry action SHALL be enabled when the replay state is `PENDING`.
The replay-or-retry action SHALL be enabled when the replay state is `OK`.
The replay-or-retry action SHALL be enabled when the replay state is `FAILED`.
The replay-or-retry action MUST be disabled when the replay state is `UNDEFINED`.
The replay-or-retry action MUST be disabled when the replay state is `EXPORTED`.
The replay-or-retry action MUST be disabled when the replay state is `EXCLUDED`.
If invocation is attempted while the replay-or-retry action is disabled, the system MUST NOT replay the command.

#### Scenario: Pending command can be replayed
- **GIVEN** a replayable command has replay state `PENDING`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Successful command can be replayed again
- **GIVEN** a replayable command has replay state `OK`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Failed command can be retried
- **GIVEN** a replayable command has replay state `FAILED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Newly recorded command cannot be replayed
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled

#### Scenario: Exported command cannot be replayed from the command view
- **GIVEN** a replayable command has replay state `EXPORTED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled

#### Scenario: Excluded command cannot be replayed
- **GIVEN** a replayable command has replay state `EXCLUDED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled

#### Scenario: Direct invocation is guarded for non-replay state
- **GIVEN** a caller bypasses UI disablement
- **AND** a replayable command has replay state `UNDEFINED`
- **WHEN** the caller invokes replay-or-retry directly
- **THEN** the system does not replay the command
