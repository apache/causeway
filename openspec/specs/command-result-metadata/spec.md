## Purpose

Defines bookmarkable command-result capture and the portable DTO, YAML, and copy foundations used by later command replay and export workflows.

## Requirements

### Requirement: Commands capture a bookmarkable direct action result
The system SHALL store the bookmark of a direct action result on the current command when the result is non-empty and bookmarkable.
The system SHALL capture bookmarkable entity and view-model results without requiring the result type to be persistable.
When a persistable result requires transaction synchronization before it has a bookmark, the system SHALL synchronize the transaction before re-evaluating its bookmark.
The system MUST leave the command result unset when the direct result has no bookmark.
The system MUST NOT replace a command result that was already captured.

#### Scenario: Action returns a bookmarkable entity
- **WHEN** a published action returns an entity with bookmark `demoCustomer:1`
- **THEN** the current command result is bookmark `demoCustomer:1`

#### Scenario: Action returns a bookmarkable view model
- **WHEN** a published action returns a view model with bookmark `demoReport:weekly`
- **THEN** the current command result is bookmark `demoReport:weekly`

#### Scenario: Persistable result obtains an identifier during synchronization
- **WHEN** a published action returns a persistable result that has no identifier before transaction synchronization
- **AND** transaction synchronization assigns bookmark `demoCustomer:1`
- **THEN** the current command result is bookmark `demoCustomer:1`

#### Scenario: Direct result is not bookmarkable
- **WHEN** a published action returns a non-empty result that has no bookmark
- **THEN** the current command has no result bookmark from that return value

#### Scenario: Earlier command result is preserved
- **GIVEN** the current command already has result bookmark `demoCustomer:1`
- **WHEN** a later nested or mixin invocation returns bookmark `demoCustomer:2`
- **THEN** the current command result remains bookmark `demoCustomer:1`

### Requirement: Commands normalize singleton result containers
The system SHALL inspect framework-supported packed result containers when capturing a command result.
When such a container contains exactly one non-empty bookmarkable object, the system SHALL store that object's bookmark as the command result.
The system MUST leave the command result unset for an empty container, a container with more than one element, or a singleton element without a bookmark.
The system MUST store only the contained object's bookmark and MUST NOT encode the original container shape in the command result.

#### Scenario: Singleton list contains one bookmarkable object
- **WHEN** a published action returns a list containing only bookmark `demoCustomer:1`
- **THEN** the current command result is bookmark `demoCustomer:1`
- **AND** the command result does not record that the action returned a list

#### Scenario: Result container is empty
- **WHEN** a published action returns an empty result container
- **THEN** the current command has no result bookmark from that container

#### Scenario: Result container has multiple elements
- **WHEN** a published action returns a result container containing bookmarks `demoCustomer:1` and `demoCustomer:2`
- **THEN** the current command has no result bookmark from that container

#### Scenario: Singleton element is not bookmarkable
- **WHEN** a published action returns a result container containing one non-bookmarkable object
- **THEN** the current command has no result bookmark from that container

### Requirement: Portable command exports carry optional result metadata
The system SHALL provide a public `CommandExportDto` transfer value containing an embedded `CommandDto` and optional result metadata.
The result metadata SHALL be represented by a public `BookmarkDto` with fields `type` and `id` derived from the bookmark logical type name and identifier.
The system SHALL convert `BookmarkDto` back to a bookmark without requiring the bookmark to resolve to a live domain object.
The system SHALL provide an `ImportedCommandDto` transfer value containing an embedded `CommandDto` and optional applib bookmark for later replay-import consumers.
The transfer factories SHALL preserve a null result as absent result metadata.
The `CommandExportDto` reader SHALL ignore unknown fields but MUST NOT treat the legacy `returnedObject` field as an alias for `result`.

#### Scenario: Create command export with result
- **WHEN** a command DTO is wrapped with result bookmark `demoCustomer:1`
- **THEN** the command export contains the original command DTO
- **AND** its result has type `demoCustomer` and id `1`

#### Scenario: Create command export without result
- **WHEN** a command DTO is wrapped without a result bookmark
- **THEN** the command export contains the original command DTO
- **AND** its result metadata is absent

#### Scenario: Convert unresolved bookmark metadata
- **WHEN** result metadata has type `remoteInvoice` and id `42`
- **AND** no corresponding local domain object exists
- **THEN** the metadata converts to bookmark `remoteInvoice:42` without resolving that object

#### Scenario: Unknown legacy result field is not an alias
- **WHEN** a command export document contains `returnedObject` but does not contain `result`
- **THEN** the command export has no result metadata

### Requirement: Result-bearing command exports use multi-document YAML
The system SHALL serialize command export values as a multi-document YAML stream.
Each YAML document SHALL contain the embedded command under field `command`.
When a command export has result metadata, the YAML document SHALL contain it under field `result` with fields `type` and `id`.
When a command export has no result metadata, the YAML document SHALL omit field `result`.
The YAML output MUST NOT contain the legacy field `returnedObject`.
Adding result-bearing YAML support MUST NOT change the existing plain `CommandDto` YAML APIs or their accepted formats.

#### Scenario: Serialize command export with result
- **WHEN** a command export has result bookmark `demoCustomer:1`
- **THEN** its YAML document contains an embedded `command`
- **AND** it contains `result` with type `demoCustomer` and id `1`
- **AND** it does not contain `returnedObject`

#### Scenario: Serialize command export without result
- **WHEN** a command export has no result bookmark
- **THEN** its YAML document contains an embedded `command`
- **AND** it omits `result`
- **AND** it does not contain `returnedObject`

#### Scenario: Serialize multiple command exports
- **WHEN** two command export values are serialized
- **THEN** the output contains two YAML documents in input order

#### Scenario: Legacy command YAML remains compatible
- **WHEN** callers use the existing plain `CommandDto` YAML methods
- **THEN** their list and multi-document behavior remains unchanged

### Requirement: Command DTOs can be copied independently
The system SHALL provide a deep-copy operation for `CommandDto`.
The copied command DTO SHALL preserve the schema content of the original command DTO.
The copied command DTO and its nested mutable values MUST be structurally independent from the original.
The copy operation SHALL return null when supplied null.

#### Scenario: Copy populated command DTO
- **WHEN** a populated command DTO is copied
- **THEN** the copy has the same serialized command schema content as the original
- **AND** the copy is a different object from the original

#### Scenario: Mutate copied command DTO
- **GIVEN** a command DTO has been copied
- **WHEN** a nested value in the copy is changed
- **THEN** the corresponding value in the original command DTO is unchanged

#### Scenario: Copy null command DTO
- **WHEN** the copy operation is supplied null
- **THEN** it returns null
