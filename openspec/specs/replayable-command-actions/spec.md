# replayable-command-actions Specification

## Purpose
TBD - created by archiving change disable-undefined-replay-or-retry. Update Purpose after archive.
## Requirements
### Requirement: Replay or retry action is enabled only for replay states
The system SHALL expose a replay-or-retry action for replayable commands.
The replay-or-retry action SHALL be enabled when the replay state is `PENDING` and no background commands are pending execution.
The replay-or-retry action SHALL be enabled when the replay state is `OK` and no background commands are pending execution.
The replay-or-retry action SHALL be enabled when the replay state is `FAILED` and no background commands are pending execution.
The replay-or-retry action MUST be disabled when the replay state is `UNDEFINED`.
The replay-or-retry action MUST be disabled when the replay state is `EXCLUDED`.
The replay-or-retry action MUST be disabled when at least one background command remains pending execution.
If invocation is attempted while the replay-or-retry action is disabled, the system MUST NOT replay the command.

#### Scenario: Pending command can be replayed
- **GIVEN** a replayable command has replay state `PENDING`
- **AND** no background commands are pending execution
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Successful command can be replayed again
- **GIVEN** a replayable command has replay state `OK`
- **AND** no background commands are pending execution
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Failed command can be retried
- **GIVEN** a replayable command has replay state `FAILED`
- **AND** no background commands are pending execution
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is enabled

#### Scenario: Newly recorded command cannot be replayed
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled

#### Scenario: Excluded command cannot be replayed
- **GIVEN** a replayable command has replay state `EXCLUDED`
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled

#### Scenario: Replay is disabled while background work is pending
- **GIVEN** a replayable command has replay state `PENDING`
- **AND** at least one background command remains pending execution
- **WHEN** the framework evaluates the replay-or-retry action
- **THEN** the replay-or-retry action is disabled
- **AND** the disablement message instructs the replay user to wait for pending background commands to complete before continuing replay

#### Scenario: Direct invocation is guarded for non-replay state
- **GIVEN** a caller bypasses UI disablement
- **AND** a replayable command has replay state `UNDEFINED`
- **WHEN** the caller invokes replay-or-retry directly
- **THEN** the system does not replay the command

#### Scenario: Direct invocation is guarded for pending background work
- **GIVEN** a caller bypasses UI disablement
- **AND** a replayable command has replay state `PENDING`
- **AND** at least one background command remains pending execution
- **WHEN** the caller invokes replay-or-retry directly
- **THEN** the system does not replay the command

