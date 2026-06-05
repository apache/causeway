## ADDED Requirements

### Requirement: Replay result mapping includes logged safe finder results
The system SHALL replay imported safe single-result finder command log entries as replayable commands when they are present in the imported command stream.
After a logged safe finder replay succeeds, the system SHALL notify the command replay mapping SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL use the existing replay result mapping SPI contract and SHALL include the recorded finder result bookmark and the actual replay finder result bookmark.
The system MUST NOT notify the SPI for a logged safe finder replay when either the recorded or actual result bookmark is unavailable.

#### Scenario: Replayed safe finder maps recorded result to actual result
- **GIVEN** an imported safe finder command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe finder and obtains actual result bookmark `demoCustomer:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoCustomer:1` and actual bookmark `demoCustomer:2`

#### Scenario: Replayed safe finder result is unavailable
- **GIVEN** an imported safe finder command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe finder and obtains no actual result bookmark
- **THEN** the system does not notify the command replay mapping SPI for that finder result

### Requirement: Safe finder result mappings can remap later replay inputs
The system SHALL make replay result mappings produced by logged safe finder commands available to the same replay input remapping flow used by other replayed commands.
When a later replayed command target or reference parameter uses a recorded bookmark that was mapped by an earlier safe finder replay, the system SHALL allow the command replay mapping SPI to replace that recorded bookmark with the actual replay bookmark.

#### Scenario: Later replay command target is remapped from finder result
- **GIVEN** replay of a safe finder command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command against target bookmark `demoCustomer:2`

#### Scenario: Later replay command reference parameter is remapped from finder result
- **GIVEN** replay of a safe finder command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose reference parameter bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command with reference parameter bookmark `demoCustomer:2`
