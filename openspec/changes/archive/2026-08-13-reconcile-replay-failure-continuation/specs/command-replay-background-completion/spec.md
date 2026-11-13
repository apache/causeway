<!--
DRAFT DELTA — re-validate on promotion.
MODIFIES the existing requirement "Bounded replay pauses when background commands become pending":
a handled command-replay FAILURE no longer stops the batch (it is recorded and replay continues);
the background-pending stop/pause/continue behaviour is unchanged. The full requirement is restated
below with the failure-related statement/scenario changed.
-->

## MODIFIED Requirements

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
