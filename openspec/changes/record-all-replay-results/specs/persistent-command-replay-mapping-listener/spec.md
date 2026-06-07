## MODIFIED Requirements

### Requirement: Persistent replay mapping listener records result mappings
The system SHALL provide a persistent command replay mapping listener that records replay results by recorded result bookmark.
The persisted mapping SHALL store the recorded bookmark and actual bookmark as bookmark value type properties.
When replay notifies the persistent listener with equal recorded and actual result bookmarks, the listener SHALL persist a mapping for that notification if no mapping already exists.
When no mapping exists for a recorded result bookmark, the persistent listener SHALL create a mapping to the actual result bookmark.
When the same recorded bookmark is notified more than once with the same actual bookmark, the persistent listener SHALL treat the notification as idempotent.
When the same recorded bookmark is notified more than once with a different actual bookmark, the persistent listener SHALL apply the configured conflict policy and MUST NOT replace the persisted actual bookmark.

#### Scenario: Persistent mapping is recorded
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the system persists a mapping with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`

#### Scenario: Equal result mapping is persisted
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:1`
- **THEN** the system persists a mapping with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

#### Scenario: Repeated persistent mapping is idempotent
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the system keeps one persisted mapping from `demoInvoice:1` to `demoInvoice:2`
- **AND** no exception is thrown

#### Scenario: Persistent mapping conflict is rejected when configured to throw
- **GIVEN** the replay mapping conflict policy is configured to throw an exception
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the persistent listener throws an exception
- **AND** the persisted mapping remains from `demoInvoice:1` to `demoInvoice:2`

#### Scenario: Persistent mapping conflict is logged when configured to continue
- **GIVEN** the replay mapping conflict policy is configured to log and continue
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the persistent listener logs the conflict
- **AND** no exception is thrown
- **AND** the persisted mapping remains from `demoInvoice:1` to `demoInvoice:2`

### Requirement: Persistent replay mapping listener remaps from persisted mappings
The persistent command replay mapping listener SHALL return a persisted actual bookmark when replay asks it to look up a recorded bookmark that has a stored mapping.
When no persisted mapping exists for the recorded bookmark, the listener SHALL return no replacement.
The persistent listener SHALL perform lookup without resolving recorded or actual bookmarks to live domain objects.

#### Scenario: Recorded bookmark is remapped from persisted mapping
- **WHEN** the persistent listener has stored that `demoInvoice:1` mapped to actual bookmark `demoInvoice:2`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the persistent listener returns replacement bookmark `demoInvoice:2`

#### Scenario: Identity recorded bookmark is looked up from persisted mapping
- **WHEN** the persistent listener has stored that `demoInvoice:1` mapped to actual bookmark `demoInvoice:1`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the persistent listener returns replacement bookmark `demoInvoice:1`

#### Scenario: Unmapped recorded bookmark is not replaced
- **WHEN** the persistent listener has no persisted mapping for bookmark `demoInvoice:1`
- **AND** command replay asks the listener to look up recorded bookmark `demoInvoice:1`
- **THEN** the persistent listener returns no replacement

### Requirement: Persistent replay mappings are listable from the command log menu
The system SHALL provide a command log menu action that lists persisted replay result mappings.
The system SHALL provide fallback layout metadata for the persisted replay result mapping entity.
The list action SHALL return persisted mappings using the abstract applib repository.
The listed replay result mappings SHALL include identity replay results where the recorded and actual bookmarks are equal.

#### Scenario: User lists persisted replay mappings
- **WHEN** a user invokes the command log menu action for replay result mappings
- **THEN** the system returns the persisted replay result mappings
- **AND** each mapping displays its recorded and actual bookmarks

#### Scenario: User lists persisted identity replay mappings
- **WHEN** a persisted replay result mapping has recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`
- **AND** a user invokes the command log menu action for replay result mappings
- **THEN** the system returns the persisted identity replay result mapping
- **AND** the mapping displays recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`
