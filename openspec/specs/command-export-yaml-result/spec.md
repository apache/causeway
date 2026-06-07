## Purpose

Define command export YAML and replay import compatibility for command DTOs and returned object metadata.
## Requirements
### Requirement: Exported command YAML includes returned object metadata
The system SHALL include result metadata for each exported command whose underlying `CommandLogEntry` has a non-null result bookmark.
The result metadata SHALL be emitted under a YAML field named `result`.
The result metadata SHALL contain `type` and `id` fields derived from the result bookmark.
The system MUST NOT emit the old `returnedObject` field for result metadata.

#### Scenario: Export command with result
- **WHEN** a command selected for export has a non-null `CommandLogEntry#getResult()` bookmark
- **THEN** the generated YAML contains result metadata for that command under field `result` with `type` set to the bookmark logical type name and `id` set to the bookmark identifier
- **AND** the generated YAML does not contain field `returnedObject`

#### Scenario: Export command without result
- **WHEN** a command selected for export has a null `CommandLogEntry#getResult()` bookmark
- **THEN** the generated YAML does not include result metadata for that command
- **AND** the generated YAML does not contain field `returnedObject`

### Requirement: Exported command YAML remains replay compatible
The system SHALL preserve the command DTO data required by command replay import when adding result metadata to exported YAML.
Command replay import SHALL first attempt to read the input as a multi-document stream of `CommandExportDto` values.
If that fails, command replay import SHALL fall back to reading the input as a multi-document stream of `CommandDto` values.
Command replay import MUST NOT support a single YAML list of `CommandDto` values.
Command replay import MUST NOT support the old `returnedObject` field as a compatibility alias for `result`.

#### Scenario: Import legacy multi-document command DTO YAML
- **WHEN** command replay import receives a YAML file using the legacy multi-document `CommandDto` export shape
- **THEN** the system imports the command DTOs as before

#### Scenario: Import YAML with result metadata
- **WHEN** command replay import receives a YAML file generated with result metadata
- **THEN** the system imports the command DTOs needed for replay and stores the result metadata as the corresponding command log result when present

#### Scenario: Reject command DTO list YAML
- **WHEN** command replay import receives a YAML file containing a single list of `CommandDto` values
- **THEN** the system rejects the import instead of importing the list entries

### Requirement: Imported command export YAML stores returned object result
The system SHALL store result metadata from each imported `CommandExportDto` as the result bookmark of the corresponding replay `CommandLogEntry`.
The system SHALL read imported result metadata from a `CommandExportDto` field named `result`.
The system MUST NOT read imported result metadata from the old `returnedObject` field.
The system MUST NOT require the result bookmark to resolve to an existing domain object during import.

#### Scenario: Import command export with result
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` whose `result` has `type` and `id`
- **THEN** the system persists the embedded command DTO for replay and sets the created `CommandLogEntry` result bookmark from that result metadata

#### Scenario: Import command export without result
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` without `result`
- **THEN** the system persists the embedded command DTO for replay and leaves the created `CommandLogEntry` result bookmark unset

#### Scenario: Import command export with old returnedObject field does not set result
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` with old field `returnedObject` and without field `result`
- **THEN** the system does not set the created `CommandLogEntry` result bookmark from `returnedObject`

### Requirement: Exported command YAML includes logged safe action returned object metadata
The system SHALL export safe action command log entries using the existing command export YAML shape when safe action command publishing created those entries.
The system SHALL export synthetic parented collection selector action command log entries using the same command export YAML shape as other logged safe action entries.
When a logged safe action entry has a non-null result bookmark, the exported YAML SHALL include result metadata under field `result` containing the bookmark logical type name and identifier.
The system SHALL preserve compatibility with existing command replay import for exported safe action entries that use the `result` field.
The system MUST NOT emit the old `returnedObject` field for logged safe action result metadata.

#### Scenario: Export logged safe action command with result
- **GIVEN** safe action command publishing is enabled
- **AND** a safe action command log entry has result bookmark `demoCustomer:1`
- **WHEN** the command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the safe action invocation
- **AND** the generated YAML contains result metadata under field `result` with type `demoCustomer` and id `1`
- **AND** the generated YAML does not contain field `returnedObject`

#### Scenario: Export logged selector action command with result
- **GIVEN** synthetic parented collection selector action creation is enabled
- **AND** safe action command publishing is enabled
- **AND** a synthetic selector action command log entry has result bookmark `demoChild:1`
- **WHEN** the selector action command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the selector action invocation
- **AND** the generated YAML contains result metadata under field `result` with type `demoChild` and id `1`
- **AND** the generated YAML does not contain field `returnedObject`

#### Scenario: Export command stream containing safe and state-changing commands
- **GIVEN** a command export selection contains a logged safe action command followed by a state-changing command that uses the found object
- **WHEN** the commands are exported
- **THEN** the generated YAML preserves both command entries in replay order
- **AND** the safe action entry includes its result metadata when available

### Requirement: Replayable command displays exported result metadata
The system SHALL expose recorded result metadata on ReplayableCommand when the underlying command log entry has a non-null result bookmark.
The displayed result metadata SHALL use the same `result` element shape as exported command YAML, with `type` and `id` fields derived from the result bookmark.
The system SHALL leave ReplayableCommand result metadata absent when the underlying command log entry has no result bookmark.
The system MUST NOT display the legacy `returnedObject` field name for ReplayableCommand result metadata.
The system MUST NOT require the result bookmark to resolve to a live domain object before displaying the metadata.

#### Scenario: Replayable command with recorded result
- **WHEN** a ReplayableCommand wraps a command log entry whose result bookmark is `demoCustomer:1`
- **THEN** ReplayableCommand displays result metadata using field `result` with type `demoCustomer` and id `1`
- **AND** ReplayableCommand does not display field `returnedObject`

#### Scenario: Replayable command without recorded result
- **WHEN** a ReplayableCommand wraps a command log entry whose result bookmark is null
- **THEN** ReplayableCommand does not display result metadata
- **AND** ReplayableCommand does not display field `returnedObject`

#### Scenario: Replayable command result is unresolved locally
- **WHEN** a ReplayableCommand wraps an imported command log entry whose result bookmark does not resolve to a local domain object
- **THEN** ReplayableCommand still displays the bookmark type and id as result metadata

