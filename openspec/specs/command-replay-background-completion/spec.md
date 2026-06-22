# command-replay-background-completion Specification

## Purpose
Ensure command replay cannot advance a replay sequence while background commands from earlier replayed work remain pending execution.

## Requirements
### Requirement: Selected replay pauses when background commands become pending
When replaying multiple selected commands, the system SHALL stop replaying additional selected commands after a replayed command creates at least one pending background command.
A background command SHALL be considered pending when it is persisted with `ExecuteIn.BACKGROUND` and has not yet started.
The system MUST leave commands replayed before the pause in their resulting replay states.
The system MUST allow the replay user to continue replaying remaining eligible commands after the pending background commands have executed and committed.

#### Scenario: Selected replay stops after command creates pending background work
- **GIVEN** a replay user selected multiple replayable commands
- **AND** an earlier selected command schedules a background command during replay
- **WHEN** the scheduled background command remains pending execution after that selected command is replayed
- **THEN** the selected replay loop stops before replaying later selected commands
- **AND** the replay user is told to wait for pending background commands to complete before continuing replay

#### Scenario: Selected replay continues when no background work is pending
- **GIVEN** a replay user selected multiple replayable commands
- **WHEN** each replayed command completes without leaving pending background commands
- **THEN** the selected replay loop continues to the next selected command until the selection is exhausted or a command replay fails

#### Scenario: Selected replay can continue after background work completes
- **GIVEN** selected replay paused because a replayed command created a pending background command
- **AND** that background command has executed and committed
- **WHEN** the replay user starts replay for remaining eligible commands
- **THEN** the system allows replay to continue

### Requirement: Replay manager single replay is disabled while background commands are pending
The replay manager SHALL disable single-command replay entry points while at least one background command remains pending execution.
The disablement message MUST tell the replay user to wait until pending background commands have executed and committed before continuing replay.
When no background commands are pending, existing replay manager disablement rules SHALL continue to apply.

#### Scenario: Replay next is disabled while background work is pending
- **GIVEN** at least one background command remains pending execution
- **WHEN** the framework evaluates a replay manager action that replays the next eligible command
- **THEN** the action is disabled
- **AND** the disablement message instructs the replay user to wait for pending background commands to complete before continuing replay

#### Scenario: Replay next no longer reports background wait after background work completes
- **GIVEN** previously pending background commands have executed and committed
- **AND** there is at least one pending or failed command available for replay review
- **WHEN** the framework evaluates a replay manager action that replays the next eligible command
- **THEN** the action is not disabled with the pending-background-commands message
- **AND** any remaining disablement comes from the existing replay eligibility rules
