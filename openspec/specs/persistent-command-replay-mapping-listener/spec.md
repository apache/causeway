# persistent-command-replay-mapping-listener Specification

## Purpose
TBD - created by archiving change persistent-command-replay-mapping-listener. Update Purpose after archive.
## Requirements
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

### Requirement: Persistent replay mappings are represented by applib and persistence-specific types
The system SHALL define an abstract applib replay result mapping entity that exposes recorded and actual bookmark properties.
The system SHALL define an abstract applib repository for finding, listing, and creating replay result mappings.
The abstract applib repository SHALL support listing all replay result mappings.
The abstract applib repository SHALL support listing changed replay result mappings where the recorded bookmark and actual bookmark differ.
The abstract applib repository SHALL support finding a replay result mapping by recorded bookmark.
The abstract applib repository SHALL support finding replay result mappings by actual bookmark.
The JDO command log persistence module SHALL provide a concrete replay result mapping entity and repository.
The JPA command log persistence module SHALL provide a concrete replay result mapping entity and repository.

#### Scenario: JDO module provides persistent mapping types
- **WHEN** the command log JDO persistence module is active
- **THEN** the system provides a concrete JDO replay result mapping entity and repository implementing the applib contracts

#### Scenario: JPA module provides persistent mapping types
- **WHEN** the command log JPA persistence module is active
- **THEN** the system provides a concrete JPA replay result mapping entity and repository implementing the applib contracts

#### Scenario: Repository lists changed replay mappings
- **WHEN** persisted replay mappings include `demoInvoice:1` to `demoInvoice:1` and `demoInvoice:2` to `demoInvoice:3`
- **THEN** the repository changed-mappings finder returns the mapping from `demoInvoice:2` to `demoInvoice:3`
- **AND** the repository changed-mappings finder does not return the mapping from `demoInvoice:1` to `demoInvoice:1`

#### Scenario: Repository finds replay mappings by actual bookmark
- **WHEN** persisted replay mappings include recorded bookmark `demoInvoice:1` with actual bookmark `demoInvoice:9`
- **AND** persisted replay mappings include recorded bookmark `demoInvoice:2` with actual bookmark `demoInvoice:9`
- **THEN** the repository actual-bookmark finder for `demoInvoice:9` returns both persisted replay mappings

### Requirement: Persistent replay mapping listener is conditionally enabled
The system SHALL provide a configuration property that selects the built-in replay mapping listener storage strategy.
When the storage strategy is `PERSISTENT` and no application-defined `CommandReplayMappingListener` bean is present, the command log autoconfiguration SHALL provide the persistent replay mapping listener if a replay result mapping repository is available.
When an application-defined `CommandReplayMappingListener` bean is present, the command log autoconfiguration MUST NOT provide the persistent replay mapping listener.
When the storage strategy is not `PERSISTENT`, the command log autoconfiguration MUST NOT provide the persistent replay mapping listener.

#### Scenario: Persistent listener is autoconfigured when selected
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `PERSISTENT`
- **AND** the application context contains no custom `CommandReplayMappingListener` bean
- **AND** a replay result mapping repository bean is available
- **THEN** the command log autoconfiguration provides the persistent replay mapping listener

#### Scenario: Persistent listener backs off for custom listener
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `PERSISTENT`
- **AND** the application context contains a custom `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration does not provide the persistent replay mapping listener

#### Scenario: Persistent listener is not created for in-memory storage
- **GIVEN** the built-in replay mapping listener storage strategy is configured as `IN_MEMORY`
- **THEN** the command log autoconfiguration does not provide the persistent replay mapping listener

### Requirement: Persistent replay mappings are listable from the command log menu
The system SHALL provide a command log menu action that lists all persisted replay result mappings.
The system SHALL provide a command log menu action that lists changed persisted replay result mappings where the recorded bookmark and actual bookmark differ.
The system SHALL provide a command log menu action that finds a persisted replay result mapping by recorded bookmark.
The system SHALL provide a command log menu action that finds persisted replay result mappings by actual bookmark.
The system SHALL provide fallback layout metadata for the persisted replay result mapping entity.
The list actions SHALL return persisted mappings using the abstract applib repository.
The listed replay result mappings SHALL include identity replay results where the recorded and actual bookmarks are equal when the user asks for all mappings.
The changed-mappings action MUST NOT return identity replay result mappings where the recorded and actual bookmarks are equal.
The replay mapping menu actions MUST be hidden when no persistent replay result mapping repository is available.

#### Scenario: User lists persisted replay mappings
- **WHEN** a user invokes the command log menu action for all replay result mappings
- **THEN** the system returns the persisted replay result mappings
- **AND** each mapping displays its recorded and actual bookmarks

#### Scenario: User lists persisted identity replay mappings
- **WHEN** a persisted replay result mapping has recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`
- **AND** a user invokes the command log menu action for all replay result mappings
- **THEN** the system returns the persisted identity replay result mapping
- **AND** the mapping displays recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

#### Scenario: User lists changed replay mappings
- **WHEN** persisted replay mappings include `demoInvoice:1` to `demoInvoice:1` and `demoInvoice:2` to `demoInvoice:3`
- **AND** a user invokes the command log menu action for changed replay result mappings
- **THEN** the system returns the mapping from `demoInvoice:2` to `demoInvoice:3`
- **AND** the system does not return the mapping from `demoInvoice:1` to `demoInvoice:1`

#### Scenario: User finds replay mapping by recorded bookmark
- **WHEN** a persisted replay result mapping has recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`
- **AND** a user invokes the command log menu action to find by recorded bookmark `demoInvoice:1`
- **THEN** the system returns the persisted replay result mapping with recorded bookmark `demoInvoice:1`

#### Scenario: User finds no replay mapping by recorded bookmark
- **WHEN** no persisted replay result mapping has recorded bookmark `demoInvoice:99`
- **AND** a user invokes the command log menu action to find by recorded bookmark `demoInvoice:99`
- **THEN** the system returns an empty result set

#### Scenario: User finds replay mappings by actual bookmark
- **WHEN** persisted replay result mappings include recorded bookmark `demoInvoice:1` with actual bookmark `demoInvoice:9`
- **AND** persisted replay result mappings include recorded bookmark `demoInvoice:2` with actual bookmark `demoInvoice:9`
- **AND** a user invokes the command log menu action to find by actual bookmark `demoInvoice:9`
- **THEN** the system returns both persisted replay result mappings

