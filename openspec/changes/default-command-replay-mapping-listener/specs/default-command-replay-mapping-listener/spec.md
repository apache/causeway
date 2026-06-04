## ADDED Requirements

### Requirement: Default replay mapping listener records result mappings
The system SHALL provide a default command replay mapping listener that records each replay result mapping by recorded result bookmark.
When replay notifies the listener with a recorded result bookmark and an actual result bookmark, the listener SHALL remember the actual bookmark for the recorded bookmark.
When the same recorded bookmark is notified more than once, the listener SHALL use the most recent actual bookmark for later remapping.

#### Scenario: Result mapping is recorded
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **THEN** the default listener records `demoInvoice:2` as the actual bookmark for `demoInvoice:1`

#### Scenario: Result mapping is replaced
- **WHEN** command replay notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** command replay later notifies the default listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **THEN** the default listener records `demoInvoice:3` as the actual bookmark for `demoInvoice:1`

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
