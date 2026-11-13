# Command Replay Background Completion Specification

## Purpose

Define global pending-background replay gates, bounded replay pausing and continuation, and compatibility across replay entry points.
## Requirements
### Requirement: Bounded replay pauses when background commands become pending

When replaying multiple commands from the unified manager, the system SHALL stop replaying additional commands
after a replayed command creates at least one pending background command. A background command SHALL be considered
pending when it is persisted with `ExecuteIn.BACKGROUND` and has not yet started. The system MUST leave commands
replayed before the pause in their resulting replay states, and MUST allow the replay user to continue replaying
remaining eligible commands after the pending background commands have executed and committed.

A command whose replay **fails in a handled way** (an advisor veto, a hidden/disabled target, or a
replay-result-mapping conflict) SHALL be recorded as `FAILED` and SHALL NOT stop the batch; bounded replay
continues to the next eligible command. Bounded replay stops only when a replayed command creates pending
background work, when the requested bound is reached, or when the collection is exhausted.

#### Scenario: Bounded replay stops after command creates pending background work

- **GIVEN** the unified manager has multiple pending or failed commands
- **AND** an earlier command schedules a background command during replay
- **WHEN** the scheduled background command remains pending after that command is replayed
- **THEN** the bounded replay loop stops before replaying later commands
- **AND** the replay user is instructed to wait for pending background commands to complete before continuing

#### Scenario: Bounded replay continues when no background work is pending

- **GIVEN** the unified manager has multiple pending or failed commands
- **WHEN** each replayed command completes without leaving pending background commands
- **THEN** replay continues in manager order until its requested bound is reached or the collection is exhausted

#### Scenario: Bounded replay can continue after background work completes

- **GIVEN** replay paused because a replayed command created a pending background command
- **AND** that background command has executed and committed
- **WHEN** the replay user starts replay for the remaining pending or failed commands
- **THEN** the system allows replay to continue

#### Scenario: Existing pending background work prevents bounded replay from starting

- **GIVEN** at least one background command remains pending execution
- **WHEN** bounded replay is invoked directly
- **THEN** no pending or failed command is replayed

#### Scenario: Replay failure is recorded and bounded replay continues

- **GIVEN** no background command is pending
- **WHEN** a command in bounded replay fails in a handled way
- **THEN** that command is recorded as `FAILED`
- **AND** bounded replay continues to the next eligible command
- **AND** replay states committed before the failure remain unchanged

### Requirement: Replay entry points are disabled while background commands are pending
Unified-manager, legacy-manager, and replayable-command replay entry points SHALL be disabled while at least one background command remains pending execution. The disablement message MUST tell the replay user to wait until pending background commands have executed and committed before continuing replay. Direct invocation MUST NOT replay a command while the condition holds. When no background commands are pending, established replay-state, collection, and known-participant disablement rules SHALL continue to apply.

#### Scenario: Unified replay next is disabled while background work is pending
- **GIVEN** at least one background command remains pending execution
- **WHEN** the framework evaluates the unified manager action that replays the next eligible command
- **THEN** the action is disabled
- **AND** its message instructs the replay user to wait for pending background commands to complete

#### Scenario: Legacy replay manager cannot bypass the gate
- **GIVEN** at least one background command remains pending execution
- **WHEN** a retained legacy replay-manager next or selected-replay entry point is evaluated or directly invoked
- **THEN** it does not replay a command
- **AND** UI disablement uses the shared pending-background wait message

#### Scenario: Replay entry point resumes its established rules after completion
- **GIVEN** previously pending background commands have executed and committed
- **WHEN** the framework evaluates a replay entry point
- **THEN** it is not disabled with the pending-background wait message
- **AND** any remaining disablement comes from its established replay rules

### Requirement: Unified manager offers bounded replay controls
The unified manager SHALL offer a replay-next action and a bounded replay action associated with `pendingOrFailed`. Replay-next SHALL act on the oldest pending-or-failed command and SHALL require that command to be present in `commandsInSequence` with known participants. Bounded replay SHALL process pending-or-failed commands in established manager order with bounds of 5, 10, 20, 40, 80, 160, 320, or all and SHALL default to 10. Bounded replay SHALL NOT require each command to report known participants. Both actions SHALL be prototyping-only and SHALL disable command and execution publishing.

#### Scenario: Replay next executes the oldest known command
- **GIVEN** no background command is pending
- **AND** the oldest pending-or-failed command is in the current sequence with known participants
- **WHEN** replay-next is invoked
- **THEN** that command is replayed or retried
- **AND** later commands are untouched

#### Scenario: Replay next rejects unknown participants
- **GIVEN** no background command is pending
- **AND** the oldest pending-or-failed command does not have known participants in the current sequence
- **WHEN** replay-next disablement is evaluated
- **THEN** replay-next is disabled without replaying a command

#### Scenario: Bounded replay defaults to ten
- **WHEN** the framework requests the default bounded-replay limit
- **THEN** the default is 10 commands

#### Scenario: Bounded replay deliberately relies on prior review
- **GIVEN** no background command is pending
- **WHEN** bounded replay processes pending-or-failed commands
- **THEN** it delegates each row to its replay-or-retry guard in manager order
- **AND** the manager action does not add a separate known-participants requirement

