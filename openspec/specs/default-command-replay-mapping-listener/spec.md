## Purpose

Provide an out-of-the-box replay mapping listener that remembers non-identity replay result mappings and remaps later replay inputs from those mappings.
## Requirements
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

### Requirement: Default replay mapping listener remaps from recorded results
The default command replay mapping listener SHALL return a remembered actual bookmark when replay asks it to remap a recorded bookmark that was previously stored from a replay result mapping.
When no mapping is remembered for the recorded bookmark, the listener SHALL return no replacement.
The listener SHALL perform remapping without resolving recorded or actual bookmarks to live domain objects.

#### Scenario: Recorded bookmark is remapped from stored mapping
- **WHEN** the default listener has recorded that `demoInvoice:1` mapped to actual bookmark `demoInvoice:2`
- **AND** command replay asks the listener to remap recorded bookmark `demoInvoice:1`
- **THEN** the default listener returns replacement bookmark `demoInvoice:2`

#### Scenario: Unmapped recorded bookmark is not replaced
- **WHEN** the default listener has no recorded mapping for bookmark `demoInvoice:1`
- **AND** command replay asks the listener to remap recorded bookmark `demoInvoice:1`
- **THEN** the default listener returns no replacement

### Requirement: Default replay mapping listener uses in-memory state
The default command replay mapping listener SHALL store mappings in memory only.
The system SHALL NOT require the default listener to persist mappings across JVM restarts, application nodes, or Spring bean lifecycle recreation.

#### Scenario: Mapping state is scoped to listener instance
- **WHEN** the default listener records that `demoInvoice:1` mapped to actual bookmark `demoInvoice:2`
- **AND** a new default listener instance is created
- **THEN** the new listener instance has no required remembered mapping for `demoInvoice:1`

