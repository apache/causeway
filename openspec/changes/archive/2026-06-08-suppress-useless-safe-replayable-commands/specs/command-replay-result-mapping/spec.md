## MODIFIED Requirements

### Requirement: Replay result mapping includes logged safe action results
The system SHALL replay imported safe action command log entries as replayable commands only when they are eligible replayable command candidates with a single recorded result bookmark.
After an eligible logged safe action replay succeeds, the system SHALL notify the command replay mapping SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL use the existing replay result mapping SPI contract and SHALL include the recorded result bookmark and the actual replay result bookmark.
The system MUST NOT notify the SPI for a logged safe action replay when either the recorded or actual result bookmark is unavailable.
The system MUST NOT replay or notify replay result mapping for logged safe action entries that have no recorded result bookmark.

#### Scenario: Replayed safe action maps recorded result to actual result
- **GIVEN** an imported safe action command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe action and obtains actual result bookmark `demoCustomer:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoCustomer:1` and actual bookmark `demoCustomer:2`

#### Scenario: Replayed safe action result is unavailable
- **GIVEN** an imported safe action command log entry has no recorded result bookmark
- **WHEN** command replay evaluates whether the entry is replayable
- **THEN** the system does not expose the entry as a replayable command
- **AND** the system does not notify the command replay mapping SPI for that action result

#### Scenario: Imported safe action with multiple results is not replayed
- **GIVEN** an imported safe action command log entry represents a safe action that returned multiple objects
- **AND** the imported command log entry has no single recorded result bookmark
- **WHEN** command replay evaluates whether the entry is replayable
- **THEN** the system does not expose the entry as a replayable command
- **AND** the system does not notify the command replay mapping SPI for that action result
