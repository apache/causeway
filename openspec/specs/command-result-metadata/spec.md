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

### Requirement: Replay import decodes canonical and legacy multi-document YAML
The system SHALL provide a replay-import decoder that first attempts a multi-document stream of `CommandExportDto` values and, when that shape is not valid, falls back to a multi-document stream of plain `CommandDto` values. A wrapped stream SHALL be accepted only when at least one document contains an embedded command. The replay-import decoder MUST reject a YAML list root and MUST surface a failure when neither supported multi-document shape can be decoded. The existing plain `CommandDto` YAML APIs and their accepted formats MUST remain unchanged.

#### Scenario: Result-bearing export stream is decoded
- **WHEN** replay import receives multi-document YAML whose documents contain embedded commands and optional `result` metadata
- **THEN** it returns one imported-command carrier per embedded command in document order
- **AND** each carrier retains its optional result bookmark

#### Scenario: Legacy command stream falls back successfully
- **WHEN** replay import receives legacy multi-document `CommandDto` YAML
- **THEN** it returns the commands in document order with no result bookmark metadata

#### Scenario: YAML list root is rejected for replay
- **WHEN** replay import receives a single YAML list of commands
- **THEN** it rejects the input instead of importing list entries

#### Scenario: General YAML compatibility remains unchanged
- **WHEN** an existing caller uses the plain `CommandDto` YAML APIs
- **THEN** those APIs retain their previously accepted list and multi-document behavior

### Requirement: Replay import preserves portable result identity
For each imported `CommandExportDto`, the system SHALL persist the embedded command through the replay repository and SHALL store `result.type` and `result.id` as the corresponding command-log result bookmark when present. Import MUST NOT require that bookmark to resolve to a local domain object. An absent `result` SHALL leave the imported entry's result unset, and the legacy field `returnedObject` MUST NOT be interpreted as a result alias.

#### Scenario: Imported envelope stores its result bookmark
- **WHEN** an imported envelope contains result type `demo.Invoice` and id `1`
- **THEN** its replay command-log entry records bookmark `demo.Invoice:1`
- **AND** no domain object is resolved during import

#### Scenario: Imported envelope without result remains unset
- **WHEN** an imported envelope has an embedded command and no `result`
- **THEN** its replay command-log entry has no imported result bookmark

#### Scenario: Legacy returned-object field is ignored
- **WHEN** an imported envelope contains `returnedObject` but no `result`
- **THEN** the replay command-log entry does not derive a result bookmark from `returnedObject`

### Requirement: Replay import is idempotent for an already-present interaction id

When the system persists an imported command for replay, it SHALL be idempotent with respect to the command's
interaction id. If a command-log entry already exists for that interaction id, the system SHALL return the
existing entry without creating a new one and without failing persistence. If no entry exists, the system SHALL
create, initialise, and persist a new replay entry as before. Idempotency SHALL apply regardless of whether the
import arrives through the unified importer or the retained legacy importer.

#### Scenario: Re-importing a command reuses the existing entry

- **GIVEN** a command with a given interaction id has already been imported for replay
- **WHEN** the same command is imported again
- **THEN** the existing replay command-log entry is returned
- **AND** no duplicate entry is created
- **AND** persistence does not fail

#### Scenario: First import creates the entry

- **WHEN** a command whose interaction id is not yet present is imported for replay
- **THEN** a new replay command-log entry is created and persisted for that interaction id

#### Scenario: Idempotency applies to the legacy import path

- **GIVEN** a command with a given interaction id has already been imported for replay
- **WHEN** the same command is imported again through the legacy import path
- **THEN** the existing entry is returned and no duplicate is created

### Requirement: Every replay import entry point uses the strict decoder

The system SHALL decode YAML at every replay import entry point — the unified manager importer and the retained legacy replay-manager importer — using the strict replay-import decoder. The strict decoder accepts the wrapped `CommandExportDto` multi-document form and the plain `CommandDto` multi-document form, rejects a YAML list root, and surfaces a failure when the input cannot be decoded. A malformed replay YAML upload MUST therefore be reported as a failure and MUST NOT be silently treated as an empty import at any replay import entry point. The general-purpose plain-`CommandDto` YAML API used by non-replay callers is unaffected by this requirement.

#### Scenario: Legacy importer rejects malformed YAML

- **GIVEN** a malformed YAML file uploaded through the legacy replay-manager import action
- **WHEN** the import is performed
- **THEN** the import fails with an error
- **AND** no commands are imported
- **AND** the failure is reported rather than a silent successful empty import

#### Scenario: Legacy importer accepts a canonical result-bearing stream

- **GIVEN** a valid wrapped `CommandExportDto` multi-document stream uploaded through the legacy importer
- **WHEN** the import is performed
- **THEN** each embedded command is persisted for replay
- **AND** any present result bookmark is retained

#### Scenario: Legacy importer accepts a legacy multi-document stream

- **GIVEN** a valid plain `CommandDto` multi-document stream uploaded through the legacy importer
- **WHEN** the import is performed
- **THEN** each command is persisted for replay in document order

#### Scenario: Non-replay YAML API is unchanged

- **WHEN** a non-replay caller uses the general-purpose plain-`CommandDto` YAML API
- **THEN** that API retains its previously accepted list and multi-document behavior

