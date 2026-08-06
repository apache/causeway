## ADDED Requirements

### Requirement: Unified manager replays pending work with background completion gates
The unified manager SHALL provide prototyping `replayOrRetryNext` and `replayOrRetryMultiple` actions associated with `pendingOrFailed`, with command and execution publishing disabled. Replay-next SHALL execute only the oldest pending-or-failed command when it is in the current sequence with known participants. Replay-multiple SHALL process current pending-or-failed commands in manager order up to a selected bound, defaulting to 10, and SHALL stop after a replay failure or newly pending background work. Both actions MUST reject direct invocation while background commands are already pending and MUST return the same manager state.

#### Scenario: Replay next retains manager state
- **WHEN** replay-next successfully executes the oldest known pending command
- **THEN** it returns a manager with the same baseline, limit, and memento

#### Scenario: Replay multiple stops at its selected bound
- **GIVEN** more pending-or-failed commands than the selected replay bound
- **WHEN** replay-multiple succeeds without creating background work
- **THEN** it processes only the bounded prefix in manager order
- **AND** it returns a manager with unchanged state

#### Scenario: Pending background work guards direct manager invocation
- **GIVEN** at least one background command remains pending execution
- **WHEN** either unified replay action is invoked directly
- **THEN** no command is replayed
- **AND** manager state remains unchanged

## MODIFIED Requirements

### Requirement: Fallback presentation exposes P2 review metadata only
The manager fallback layout SHALL expose baseline and limit with their state controls and SHALL present all four collections as sequence and replay review surfaces. Manager collection tables SHALL identify commands using interaction id, timestamp, member, replay state, result presence, and known-participants status, with known participants immediately after result presence. The layout SHALL expose E1 sequence-export and replay-import actions. It SHALL expose W1 exclusion and movement actions with `commandsInSequence` and W1 restoration and deletion actions with `excluded`. It SHALL expose B2 replay-next and replay-multiple actions with `pendingOrFailed` while retaining import on that collection.

#### Scenario: Unified manager layout includes completed reconciliation workflows
- **WHEN** the manager is rendered from fallback layout metadata
- **THEN** baseline, limit, all four collections, P1 identification columns, R2 known-participants status, E1 export/import actions, W1 exclusion, restoration, deletion, and movement actions, and B2 replay controls are visible
- **AND** replay-next and replay-multiple are associated with `pendingOrFailed`
