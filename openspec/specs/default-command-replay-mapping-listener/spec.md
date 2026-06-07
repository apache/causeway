## Purpose

Provide an out-of-the-box replay mapping listener that remembers non-identity replay result mappings and remaps later replay inputs from those mappings.
## Requirements
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

### Requirement: Default replay mapping listener uses in-memory state
When the built-in replay mapping listener storage strategy is `IN_MEMORY`, the default command replay mapping listener SHALL store mappings in memory only.
The system SHALL NOT require the in-memory listener to persist mappings across JVM restarts, application nodes, or Spring bean lifecycle recreation.
The system SHALL use `IN_MEMORY` as the default built-in replay mapping listener storage strategy.

#### Scenario: Mapping state is scoped to listener instance
- **WHEN** the in-memory listener records that `demoInvoice:1` mapped to actual bookmark `demoInvoice:2`
- **AND** a new in-memory listener instance is created
- **THEN** the new listener instance has no required remembered mapping for `demoInvoice:1`

#### Scenario: In-memory listener is the default storage strategy
- **WHEN** the application does not configure a built-in replay mapping listener storage strategy
- **THEN** the system uses the in-memory listener as the built-in replay mapping listener

### Requirement: Built-in in-memory replay mapping listener is conditionally enabled
When the built-in replay mapping listener storage strategy is `IN_MEMORY` and no application-defined `CommandReplayMappingListener` bean is present, the command log autoconfiguration SHALL provide the in-memory replay mapping listener.
When an application-defined `CommandReplayMappingListener` bean is present, the command log autoconfiguration MUST NOT provide the in-memory replay mapping listener.
When the storage strategy is not `IN_MEMORY`, the command log autoconfiguration MUST NOT provide the in-memory replay mapping listener.

#### Scenario: In-memory listener is autoconfigured when selected
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `IN_MEMORY`
- **AND** the application context contains no custom `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration provides the in-memory replay mapping listener

#### Scenario: In-memory listener backs off for custom listener
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `IN_MEMORY`
- **AND** the application context contains a custom `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration does not provide the in-memory replay mapping listener

#### Scenario: In-memory listener is not created for persistent storage
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `PERSISTENT`
- **THEN** the command log autoconfiguration does not provide the in-memory replay mapping listener

