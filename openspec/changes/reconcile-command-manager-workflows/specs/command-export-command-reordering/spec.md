## ADDED Requirements

### Requirement: Unified manager moves selected commands after an active target
When command-log recording support is `ENABLED`, the unified manager SHALL provide one `moveCommands` action associated with `commandsInSequence`. It SHALL move one or more selected commands earlier or later by retimestamping them after an unselected target command. Selected and target choices SHALL come from the current baseline-and-limit-bounded sequence, with selected commands removed from target choices. Direct invocation MUST reject disabled recording support, an empty selection, a missing target, a selected target, or any selected or target interaction id outside the fresh current sequence before changing any timestamp. Excluded commands MUST NOT be selectable or targetable, and separate `moveCommandsUp` and `moveCommandsDown` actions MUST NOT be exposed.

#### Scenario: Target choices exclude the selected block
- **GIVEN** active sequence commands `A`, `B`, and `C` and selected commands `B` and `C`
- **WHEN** movement target choices are requested
- **THEN** `A` is offered
- **AND** `B` and `C` are not offered

#### Scenario: One action moves earlier or later
- **GIVEN** active sequence order `A`, `B`, `C`
- **WHEN** `C` is moved after `A` or `A` is moved after `C`
- **THEN** each invocation is accepted and retimestamps the selected command after its target
- **AND** no direction-specific movement action is required

#### Scenario: Invalid movement changes nothing
- **GIVEN** selected command `A` is active and command `B` is excluded or outside the current bounded sequence
- **WHEN** direct invocation uses `B` as target or includes it in the selected block
- **THEN** movement is rejected
- **AND** no command-log or DTO timestamp changes

### Requirement: Movement preserves selected order with a one-second minimum gap
The action SHALL derive selected order from the fresh manager sequence before mutation and SHALL place the first selected command exactly one second after the target timestamp. For each later selected command, it SHALL preserve the original elapsed time from its preceding selected command only when that gap is positive and at least one second; otherwise it SHALL use a one-second increment. The action SHALL update each selected entry's command-log timestamp and embedded `CommandDto` timestamp to the same value. It MUST NOT change the target timestamp, any unselected entry timestamp, replay states, results, repository membership, manager baseline, or manager limit.

#### Scenario: Selected timing gaps are preserved
- **GIVEN** selected commands `A` and `B` are 2.5 seconds apart and target `C` has timestamp `10:00:00`
- **WHEN** the block is moved after `C` without timing squash
- **THEN** `A` has timestamp `10:00:01`
- **AND** `B` has timestamp `10:00:03.5`
- **AND** both embedded DTO timestamps match their entry timestamps

#### Scenario: Invalid or short gaps use deterministic minimum
- **GIVEN** two selected commands have equal, reversed, unavailable, or sub-second relative timing
- **WHEN** they are moved after a target without timing squash
- **THEN** their new timestamps retain selected manager order
- **AND** adjacent selected commands are separated by at least one second

#### Scenario: Movement is observational outside selected timestamps
- **WHEN** a selected block is moved after a target
- **THEN** target and unselected timestamps remain unchanged
- **AND** replay states, results, repository membership, baseline, and limit remain unchanged

### Requirement: Movement can squash selected timing gaps
The movement action SHALL provide a `squashTimings` boolean parameter defaulting to false. When false, it SHALL apply the preserve-gap policy with the one-second minimum. When true, it SHALL discard every original selected-command gap, place the first selected command one second after the target, and place each later selected command exactly one second after its predecessor while retaining selected manager order.

#### Scenario: Timing squash produces one-second spacing
- **GIVEN** selected commands `A`, `B`, and `C` have arbitrary original gaps
- **WHEN** they are moved with timing squash enabled
- **THEN** `A` is one second after the target
- **AND** `B` is one second after `A`
- **AND** `C` is one second after `B`

#### Scenario: Timing preservation remains the default
- **WHEN** movement parameter defaults are requested
- **THEN** `squashTimings` is false
- **AND** positive original gaps of at least one second are preserved unless the user opts in

### Requirement: JPA persists movement without a new ordering schema
Causeway 4 SHALL persist moved command-log and DTO timestamps through the existing managed `CommandLogEntry` mutation contract and Jakarta Persistence adapter. W1 MUST NOT add a persistent order field, datastore migration, JDO-specific implementation, or restored commandlog JDO adapter. Reloading moved entries SHALL retain matching timestamps and established timestamp-based queries SHALL observe the new order.

#### Scenario: Reload observes moved timestamps and order
- **WHEN** selected commands are moved and the JPA transaction commits
- **THEN** reloaded entry timestamps match their embedded DTO timestamps
- **AND** foreground timestamp ordering reflects the movement

#### Scenario: Persistence model remains compatible
- **WHEN** W1 is deployed
- **THEN** the existing command-log schema remains usable without migration
- **AND** no commandlog JDO adapter is introduced
