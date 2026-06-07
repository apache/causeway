## ADDED Requirements

### Requirement: Replay result mapping observes command interaction id
The system SHALL make the replayed command interaction id available when a replay result mapping observation is handled.
The built-in replay mapping listener implementations SHALL capture the interaction id from the `CommandLogEntry` supplied to `onReplayResult(...)` when they create a new mapping.
When the supplied command log entry has no interaction id, the system SHALL still record or retain the replay result mapping without an interaction id.
The system MUST NOT require a `CommandReplayMappingListener` SPI signature change to expose this interaction id.

#### Scenario: Replay result observation includes command interaction id
- **WHEN** command replay succeeds for an imported command whose interaction id is `11111111-1111-1111-1111-111111111111`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the built-in replay mapping listener records a replay result mapping associated with command interaction id `11111111-1111-1111-1111-111111111111`

#### Scenario: Replay result observation has no command interaction id
- **WHEN** command replay notifies a built-in replay mapping listener with recorded result bookmark `demoInvoice:1` and actual result bookmark `demoInvoice:2`
- **AND** the supplied command log entry has no interaction id
- **THEN** the built-in replay mapping listener records or retains the replay result mapping without a command interaction id

#### Scenario: Existing SPI signature is preserved
- **WHEN** an application implements `CommandReplayMappingListener#onReplayResult(Bookmark, Bookmark, CommandLogEntry)`
- **THEN** the implementation remains source-compatible with the replay result mapping SPI
