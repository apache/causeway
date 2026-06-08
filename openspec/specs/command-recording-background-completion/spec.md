# command-recording-background-completion Specification

## Purpose
Ensure command-log recording support cannot advance a replayable command sequence while background commands from earlier recorded work remain pending execution.

## Requirements
### Requirement: Recording rejects subsequent commands while background commands are pending
When command-log recording support is `ENABLED`, the system SHALL allow a recorded foreground command that schedules background commands to complete normally.
The system SHALL reject a subsequent recorded foreground command attempt if at least one earlier background command remains pending execution.
A background command SHALL be considered pending execution when it is persisted with `ExecuteIn.BACKGROUND` and has not yet started.
The rejection MUST raise an exception with a message that tells the end-user to wait until pending background commands have been executed and committed before continuing the recording.

#### Scenario: Scheduling command completes during recording
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a foreground action invocation is being recorded
- **WHEN** that invocation schedules at least one background command
- **THEN** the system accepts completion of that foreground invocation
- **AND** the scheduled background command remains available for background execution

#### Scenario: Subsequent command is rejected while background work is pending
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an earlier recorded foreground action scheduled a background command
- **AND** that background command remains pending execution
- **WHEN** the end-user attempts to execute a subsequent recorded foreground command
- **THEN** the system raises an exception instead of accepting the subsequent command
- **AND** the exception message instructs the end-user to wait for pending background commands to complete before continuing

#### Scenario: Subsequent command is accepted after background work completes
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an earlier recorded foreground action scheduled a background command
- **AND** that background command has executed and committed
- **WHEN** the end-user attempts to execute a subsequent recorded foreground command
- **THEN** the system accepts the subsequent command

#### Scenario: Foreground command completes with no pending background work during recording
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a foreground action invocation is being recorded
- **WHEN** there are no pending background commands from earlier work
- **THEN** the system accepts the foreground invocation

#### Scenario: Pending background work is allowed outside recording support
- **GIVEN** command-log recording support is `DISABLED`
- **AND** command-log persistence is enabled
- **AND** an earlier foreground action scheduled a background command that remains pending execution
- **WHEN** the end-user attempts to execute a subsequent foreground command
- **THEN** the system does not reject the subsequent invocation solely because the background command is pending
