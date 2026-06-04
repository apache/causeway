## MODIFIED Requirements

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

## ADDED Requirements

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
