## ADDED Requirements

### Requirement: Replay result mapping includes logged safe action results
The system SHALL replay imported safe action command log entries as replayable commands when they are present in the imported command stream.
After a logged safe action replay succeeds, the system SHALL notify the command replay mapping SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL use the existing replay result mapping SPI contract and SHALL include the recorded result bookmark and the actual replay result bookmark.
The system MUST NOT notify the SPI for a logged safe action replay when either the recorded or actual result bookmark is unavailable.

#### Scenario: Replayed safe action maps recorded result to actual result
- **GIVEN** an imported safe action command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe action and obtains actual result bookmark `demoCustomer:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoCustomer:1` and actual bookmark `demoCustomer:2`

#### Scenario: Replayed safe action result is unavailable
- **GIVEN** an imported safe action command log entry has no recorded result bookmark
- **WHEN** command replay executes that safe action
- **THEN** the system does not notify the command replay mapping SPI for that action result

### Requirement: Safe action result mappings can remap later replay inputs
The system SHALL make replay result mappings produced by logged safe action commands available to the same replay input remapping flow used by other replayed commands.
When a later replayed command target or reference parameter uses a recorded bookmark that was mapped by an earlier safe action replay, the system SHALL allow the command replay mapping SPI to replace that recorded bookmark with the actual replay bookmark.

#### Scenario: Later replay command target is remapped from safe action result
- **GIVEN** replay of a safe action command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command against target bookmark `demoCustomer:2`

#### Scenario: Later replay command reference parameter is remapped from safe action result
- **GIVEN** replay of a safe action command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose reference parameter bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command with reference parameter bookmark `demoCustomer:2`
