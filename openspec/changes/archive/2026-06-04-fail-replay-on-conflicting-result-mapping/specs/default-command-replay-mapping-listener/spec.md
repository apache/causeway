## MODIFIED Requirements

### Requirement: Default replay mapping listener records result mappings
The system SHALL provide a default command replay mapping listener that records each replay result mapping by recorded result bookmark.
When replay notifies the listener with a recorded result bookmark and a different actual result bookmark, the listener SHALL remember the actual bookmark for the recorded bookmark if no mapping already exists.
When replay notifies the listener with equal recorded and actual result bookmarks, the listener MUST NOT remember a mapping for that notification.
When the same recorded bookmark is notified more than once with the same actual result bookmark, the listener SHALL treat the notification as idempotent.
When the same recorded bookmark is notified more than once with different actual result bookmarks, the listener SHALL throw an exception and MUST NOT replace the remembered actual bookmark.

#### Scenario: Different result mapping is recorded
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the default listener records `demoInvoice:2` as the actual bookmark for `demoInvoice:1`

#### Scenario: Equal result mapping is not recorded
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:1`
- **THEN** the default listener records no mapping for `demoInvoice:1`

#### Scenario: Result mapping is repeated with the same actual bookmark
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the default listener keeps `demoInvoice:2` as the actual bookmark for `demoInvoice:1`
- **AND** no exception is thrown

#### Scenario: Result mapping conflict is rejected
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the default listener throws an exception
- **AND** the default listener keeps `demoInvoice:2` as the actual bookmark for `demoInvoice:1`
