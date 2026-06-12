## MODIFIED Requirements

### Requirement: Replay result mapping SPI is notified after successful replay
The system SHALL provide a command replay mapping SPI that applications can implement to receive replay result bookmark mappings and provide replay input remappings.
After a command replay succeeds, the system SHALL notify the SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL include the recorded returned object bookmark and the actual replay result bookmark.
The system SHALL notify the SPI in the same transaction as replay command execution.
The system SHALL propagate result-mapping notification exceptions so the replayed command execution fails and rolls back.

#### Scenario: Successful replay produces a different result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`

#### Scenario: Successful replay produces the same result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:1`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

#### Scenario: Result mapping listener rejects the replay result
- **WHEN** command replay execution succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **AND** a command replay mapping SPI throws while handling the result mapping notification
- **THEN** the replayed command execution fails
- **AND** the replayed command transaction rolls back
