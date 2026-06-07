## MODIFIED Requirements

### Requirement: Default replay mapping listener records result mappings
The system SHALL provide a default command replay mapping listener that records each replay result by recorded result bookmark.
When replay notifies the listener with a recorded result bookmark and a different actual result bookmark, the listener SHALL remember the actual bookmark for the recorded bookmark if no mapping already exists.
When replay notifies the listener with equal recorded and actual result bookmarks, the listener SHALL remember the actual bookmark for the recorded bookmark if no mapping already exists.
When the same recorded bookmark is notified more than once with the same actual result bookmark, the listener SHALL treat the notification as idempotent.
When the same recorded bookmark is notified more than once with different actual result bookmarks, the listener SHALL apply its configured conflict policy and MUST NOT replace the remembered actual bookmark.
The system SHALL default the conflict policy to throwing an exception.
The system SHALL provide a configuration property that can change the conflict policy to log the conflict and continue.

#### Scenario: Different result mapping is recorded
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the default listener records `demoInvoice:2` as the actual bookmark for `demoInvoice:1`

#### Scenario: Equal result mapping is recorded
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:1`
- **THEN** the default listener records `demoInvoice:1` as the actual bookmark for `demoInvoice:1`

#### Scenario: Result mapping is repeated with the same actual bookmark
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the default listener keeps `demoInvoice:2` as the actual bookmark for `demoInvoice:1`
- **AND** no exception is thrown

#### Scenario: Result mapping conflict is rejected by default
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the default listener throws an exception
- **AND** the default listener keeps `demoInvoice:2` as the actual bookmark for `demoInvoice:1`

#### Scenario: Result mapping conflict is logged and ignored when configured
- **GIVEN** the default listener conflict policy is configured to log and continue
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the default listener logs the conflict
- **AND** no exception is thrown
- **AND** the default listener keeps `demoInvoice:2` as the actual bookmark for `demoInvoice:1`

### Requirement: Default replay mapping listener remaps from recorded results
The default command replay mapping listener SHALL return a remembered actual bookmark when replay asks it to look up a recorded bookmark that was previously stored from a replay result notification.
When no mapping is remembered for the recorded bookmark, the listener SHALL return no replacement.
The listener SHALL perform lookup without resolving recorded or actual bookmarks to live domain objects.

#### Scenario: Recorded bookmark is remapped from stored mapping
- **WHEN** the default listener has recorded that `demoInvoice:1` mapped to actual bookmark `demoInvoice:2`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the default listener returns replacement bookmark `demoInvoice:2`

#### Scenario: Identity recorded bookmark is looked up from stored mapping
- **WHEN** the default listener has recorded that `demoInvoice:1` mapped to actual bookmark `demoInvoice:1`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the default listener returns replacement bookmark `demoInvoice:1`

#### Scenario: Unmapped recorded bookmark is not replaced
- **WHEN** the default listener has no recorded mapping for bookmark `demoInvoice:1`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the default listener returns no replacement
