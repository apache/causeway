# Persistent Command Replay Mapping Specification

## Purpose

Define durable replay-result bookmark mappings, their JPA persistence, conditional listener activation, and commandlog management behavior.

## Requirements

### Requirement: Persistent listener records replay-result mappings
The system SHALL provide a persistent `CommandReplayMappingListener` that stores replay results by recorded result bookmark.
The persisted mapping SHALL contain the recorded bookmark, actual bookmark, and optional originating command interaction id.
The listener SHALL persist both identity mappings and mappings whose recorded and actual bookmarks differ.
When no mapping exists for a recorded bookmark, the listener SHALL persist the first observed actual bookmark and interaction id.
When the same recorded bookmark and actual bookmark are observed again, the listener SHALL treat the notification as idempotent and MUST NOT replace the original interaction id.
When a recorded bookmark is observed with a different actual bookmark, the listener SHALL apply the configured conflict policy and MUST NOT replace the persisted actual bookmark or interaction id.

#### Scenario: First changed mapping is persisted
- **WHEN** replay reports recorded bookmark `demoInvoice:1`, actual bookmark `demoInvoice:2`, and interaction id `11111111-1111-1111-1111-111111111111`
- **THEN** the persistent listener stores that bookmark mapping and interaction id

#### Scenario: Identity mapping is persisted
- **WHEN** replay reports recorded and actual bookmark `demoInvoice:1`
- **THEN** the persistent listener stores the identity mapping

#### Scenario: Repeated mapping is idempotent
- **GIVEN** recorded bookmark `demoInvoice:1` is stored against actual bookmark `demoInvoice:2` and a first interaction id
- **WHEN** replay reports the same bookmarks with a later interaction id
- **THEN** only one mapping remains
- **AND** the first interaction id is retained

#### Scenario: Strict conflict rejects the replay
- **GIVEN** recorded bookmark `demoInvoice:1` is stored against actual bookmark `demoInvoice:2`
- **AND** conflict policy is `THROW_EXCEPTION`
- **WHEN** replay reports `demoInvoice:1` against `demoInvoice:3`
- **THEN** the listener throws an exception
- **AND** the stored actual bookmark remains `demoInvoice:2`

#### Scenario: Lenient conflict keeps the first mapping
- **GIVEN** recorded bookmark `demoInvoice:1` is stored against actual bookmark `demoInvoice:2`
- **AND** conflict policy is `LOG_AND_CONTINUE`
- **WHEN** replay reports `demoInvoice:1` against `demoInvoice:3`
- **THEN** the listener logs the conflict and returns normally
- **AND** the stored actual bookmark remains `demoInvoice:2`

### Requirement: Persistent listener remaps replay inputs from stored mappings
The persistent listener SHALL return the stored actual bookmark when the replay mapping SPI looks up a recorded bookmark with a persisted mapping.
The listener SHALL return no replacement when no mapping exists.
The listener MUST NOT resolve either bookmark to a live domain object while performing lookup.

#### Scenario: Stored mapping replaces replay input
- **GIVEN** recorded bookmark `demoInvoice:1` is stored against actual bookmark `demoInvoice:2`
- **WHEN** replay looks up `demoInvoice:1`
- **THEN** the listener returns `demoInvoice:2`

#### Scenario: Stored identity mapping is returned
- **GIVEN** recorded bookmark `demoInvoice:1` is stored against actual bookmark `demoInvoice:1`
- **WHEN** replay looks up `demoInvoice:1`
- **THEN** the listener returns `demoInvoice:1`

#### Scenario: Unknown bookmark is not replaced
- **WHEN** replay looks up a recorded bookmark with no persisted mapping
- **THEN** the listener returns no replacement

### Requirement: Applib defines persistence-neutral mapping contracts
The system SHALL define an abstract commandlog applib mapping type exposing recorded bookmark, actual bookmark, and optional command interaction id.
The system SHALL define an applib repository contract and reusable repository behavior for finding a mapping by recorded bookmark, finding mappings by actual bookmark, listing all mappings, listing mappings whose bookmarks differ, creating a mapping, deleting one mapping, and deleting all mappings.
The applib contracts SHALL use `Bookmark` and UUID values without depending on a persistence technology.

#### Scenario: Changed finder excludes identity mappings
- **GIVEN** stored mappings `demoInvoice:1` to `demoInvoice:1` and `demoInvoice:2` to `demoInvoice:3`
- **WHEN** the repository lists changed mappings
- **THEN** only the mapping from `demoInvoice:2` to `demoInvoice:3` is returned

#### Scenario: Reverse finder returns every recorded source
- **GIVEN** stored mappings `demoInvoice:1` to `demoInvoice:9` and `demoInvoice:2` to `demoInvoice:9`
- **WHEN** the repository finds mappings by actual bookmark `demoInvoice:9`
- **THEN** both mappings are returned

### Requirement: JPA module persists replay-result mappings
The Causeway 4 commandlog JPA module SHALL provide a concrete Jakarta Persistence mapping entity and repository implementing the applib contracts.
The mapping table SHALL enforce uniqueness of recorded bookmark and SHALL index recorded and actual bookmarks.
The JPA module SHALL register the entity, repository, and persistent-listener configuration and SHALL include mapping rows in commandlog teardown.
Creation SHALL flush the new mapping within the replay transaction so persistence failures propagate before replay commits.

#### Scenario: JPA module supplies persistent mapping services
- **WHEN** the commandlog JPA persistence module is active
- **THEN** a concrete replay-result mapping repository is available
- **AND** its mapping entity is included in JPA entity scanning

#### Scenario: Recorded bookmark is unique
- **GIVEN** a JPA mapping already exists for recorded bookmark `demoInvoice:1`
- **WHEN** another row is inserted for recorded bookmark `demoInvoice:1`
- **THEN** datastore uniqueness prevents two authoritative mappings

#### Scenario: Commandlog teardown removes mappings
- **GIVEN** command log entries and replay-result mappings exist
- **WHEN** the commandlog JPA teardown fixture runs
- **THEN** the replay-result mappings are removed along with the commandlog test data

### Requirement: Persistent listener is conditionally enabled
When `causeway.extensions.command-log.replay-result-mapping.storage-strategy` is `PERSISTENT`, a replay-result mapping repository is available, and no application-defined `CommandReplayMappingListener` bean exists, the system SHALL provide the persistent listener.
When an application-defined listener exists, the persistent listener MUST back off.
When storage strategy is not `PERSISTENT` or no mapping repository is available, the system MUST NOT create the persistent listener.
The in-memory and persistent built-in listeners MUST NOT be active together.

#### Scenario: Persistent listener is selected with JPA repository
- **GIVEN** storage strategy is `PERSISTENT`
- **AND** a JPA replay-result mapping repository is available
- **AND** no custom replay-mapping listener exists
- **THEN** the persistent replay-mapping listener is provided
- **AND** the in-memory listener is absent

#### Scenario: Custom listener remains authoritative
- **GIVEN** storage strategy is `PERSISTENT`
- **AND** an application-defined replay-mapping listener exists
- **THEN** the built-in persistent listener is absent

#### Scenario: Persistent strategy without repository creates no built-in
- **GIVEN** storage strategy is `PERSISTENT`
- **AND** no replay-result mapping repository is available
- **THEN** the persistent listener is absent

#### Scenario: In-memory strategy excludes persistent listener
- **GIVEN** storage strategy is `IN_MEMORY`
- **THEN** the persistent listener is absent

### Requirement: Persisted mappings are queryable from the commandlog menu
The commandlog menu SHALL provide actions to list all persisted mappings, list changed mappings, find a mapping by recorded bookmark, and find mappings by actual bookmark.
The actions SHALL delegate through the applib repository and SHALL be hidden when no replay-result mapping repository is available.
The all-mappings action SHALL include identity mappings, while the changed-mappings action MUST exclude them.

#### Scenario: User lists all mappings including identity
- **GIVEN** identity and changed replay-result mappings are persisted
- **WHEN** the user invokes the all-mappings action
- **THEN** both identity and changed mappings are returned

#### Scenario: User finds mapping by recorded bookmark
- **GIVEN** `demoInvoice:1` is mapped to `demoInvoice:2`
- **WHEN** the user searches for recorded bookmark `demoInvoice:1`
- **THEN** that mapping is returned

#### Scenario: User finds no recorded mapping
- **WHEN** the user searches for a recorded bookmark with no persisted mapping
- **THEN** an empty result is returned

#### Scenario: Mapping finders are hidden without repository
- **GIVEN** no replay-result mapping repository is available
- **WHEN** the commandlog menu is rendered
- **THEN** every persistent-mapping finder action is hidden

### Requirement: Persisted mapping layout exposes audit data
The system SHALL provide fallback layout metadata for persisted replay-result mappings.
The layout SHALL display recorded bookmark, actual bookmark, and optional originating command interaction id.
The mapping title SHALL distinguish the recorded and actual bookmarks.

#### Scenario: User inspects mapping audit data
- **GIVEN** a persisted mapping has recorded bookmark `demoInvoice:1`, actual bookmark `demoInvoice:2`, and an interaction id
- **WHEN** the user views the mapping
- **THEN** the layout displays both bookmarks and the interaction id

### Requirement: Persisted mappings can be deleted without deleting commands
The system SHALL contribute an idempotent are-you-sure action that deletes one persisted replay-result mapping.
The commandlog menu SHALL provide an idempotent are-you-sure action that deletes all persisted replay-result mappings and reports the number deleted.
The bulk action SHALL be hidden when no replay-result mapping repository is available.
Neither deletion action SHALL delete command log entries or imported replay commands.

#### Scenario: User deletes one mapping
- **GIVEN** a persisted replay-result mapping exists
- **WHEN** the user confirms its delete action
- **THEN** that mapping is removed
- **AND** command log entries remain unchanged

#### Scenario: User deletes all mappings
- **GIVEN** two replay-result mappings and command log entries exist
- **WHEN** the user confirms the delete-all action
- **THEN** all replay-result mappings are removed
- **AND** the user is informed that two mappings were deleted
- **AND** command log entries remain unchanged

#### Scenario: Empty bulk deletion is idempotent
- **GIVEN** no replay-result mappings exist
- **WHEN** the user confirms the delete-all action
- **THEN** the action completes without error and reports zero deletions

### Requirement: Mapping menu actions follow replay workflow order
The commandlog menu SHALL order existing export and replay actions before persistent-mapping finder actions.
The menu SHALL order the delete-all mapping action after the mapping finder actions.
The ordering SHALL be expressed through action layout sequence metadata.

#### Scenario: Mapping management follows replay actions
- **WHEN** commandlog menu actions are ordered by layout sequence
- **THEN** export and replay actions appear before mapping finder actions
- **AND** the delete-all mapping action appears after the mapping finder actions
