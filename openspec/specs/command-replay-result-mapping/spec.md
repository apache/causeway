## Purpose

Define the replay result mapping SPI that lets applications observe mappings from recorded returned object bookmarks to actual replay result bookmarks.

## Requirements

### Requirement: Replay result mapping SPI is notified after successful replay
The system SHALL provide an SPI that applications can implement to receive replay result bookmark mappings.
After a command replay succeeds, the system SHALL notify the SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL include the recorded returned object bookmark and the actual replay result bookmark.

#### Scenario: Successful replay produces a different result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the system notifies the replay result mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`

#### Scenario: Successful replay produces the same result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:1`
- **THEN** the system notifies the replay result mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

### Requirement: Replay result mapping SPI is not notified when mapping data is unavailable
The system MUST NOT notify the replay result mapping SPI unless replay succeeds and both recorded and actual result bookmarks are available.

#### Scenario: Replay fails
- **WHEN** command replay fails for an imported command whose recorded result bookmark is available
- **THEN** the system does not notify the replay result mapping SPI

#### Scenario: Recorded result is absent
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is absent
- **THEN** the system does not notify the replay result mapping SPI

#### Scenario: Actual result is absent
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is available
- **AND** replay execution does not return an actual result bookmark
- **THEN** the system does not notify the replay result mapping SPI

### Requirement: Replay result mapping SPI uses bookmarks without resolving objects
The system SHALL pass recorded and actual result bookmarks to the replay result mapping SPI without requiring those bookmarks to resolve to live domain objects.

#### Scenario: Recorded object is not resolvable
- **WHEN** command replay succeeds and the recorded result bookmark does not resolve to a local domain object
- **THEN** the system can still notify the replay result mapping SPI with the recorded bookmark and actual bookmark
