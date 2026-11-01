## MODIFIED Requirements

### Requirement: Unified manager replays pending work with background completion gates
The unified manager SHALL provide prototyping `replayOrRetryNext` and `replayOrRetryMultiple` actions associated with `pendingOrFailed`, with command and execution publishing disabled.
Replay-next SHALL execute only the oldest pending-or-failed command when it is in the current sequence with known participants.
Replay-multiple SHALL process current pending-or-failed commands in manager order up to a selected bound, SHALL offer one as a selectable bound, SHALL default to 10, and SHALL stop after a replay failure or newly pending background work.
Both actions MUST reject direct invocation while background commands are already pending and MUST return the same manager state.

#### Scenario: Replay next retains manager state
- **WHEN** replay-next successfully executes the oldest known pending command
- **THEN** it returns a manager with the same baseline, limit, and memento

#### Scenario: Replay multiple stops at its selected bound
- **GIVEN** more pending-or-failed commands than the selected replay bound
- **WHEN** replay-multiple succeeds without creating background work
- **THEN** it processes only the bounded prefix in manager order
- **AND** it returns a manager with unchanged state

#### Scenario: Replay multiple executes exactly one command
- **GIVEN** more than one pending-or-failed command
- **WHEN** replay-multiple is invoked with the one-command bound and the first replay succeeds without creating background work
- **THEN** it processes only the first command in manager order
- **AND** it returns a manager with unchanged state

#### Scenario: Pending background work guards direct manager invocation
- **GIVEN** at least one background command remains pending execution
- **WHEN** either unified replay action is invoked directly
- **THEN** no command is replayed
- **AND** manager state remains unchanged
