## Purpose

Define command export YAML and replay import compatibility for command DTOs and returned object metadata.
## Requirements
### Requirement: Exported command YAML includes returned object metadata
The system SHALL include returned object metadata for each exported command whose underlying `CommandLogEntry` has a non-null returned object bookmark.
The returned object metadata SHALL contain `logicalTypeName` and `id` fields derived from the returned object's bookmark.

#### Scenario: Export command with returned object
- **WHEN** a command selected for export has a non-null `CommandLogEntry#getResult()` bookmark
- **THEN** the generated YAML contains returned object metadata for that command with `logicalTypeName` set to the bookmark logical type name and `id` set to the bookmark identifier

#### Scenario: Export command without returned object
- **WHEN** a command selected for export has a null `CommandLogEntry#getResult()` bookmark
- **THEN** the generated YAML does not include returned object metadata for that command

### Requirement: Exported command YAML remains replay compatible
The system SHALL preserve the command DTO data required by command replay import when adding returned object metadata to exported YAML.
Command replay import SHALL first attempt to read the input as a multi-document stream of `CommandExportDto` values.
If that fails, command replay import SHALL fall back to reading the input as a multi-document stream of `CommandDto` values.
Command replay import MUST NOT support a single YAML list of `CommandDto` values.

#### Scenario: Import legacy multi-document command DTO YAML
- **WHEN** command replay import receives a YAML file using the legacy multi-document `CommandDto` export shape
- **THEN** the system imports the command DTOs as before

#### Scenario: Import YAML with returned object metadata
- **WHEN** command replay import receives a YAML file generated with returned object metadata
- **THEN** the system imports the command DTOs needed for replay and stores the returned object metadata as the corresponding command log result when present

#### Scenario: Reject command DTO list YAML
- **WHEN** command replay import receives a YAML file containing a single list of `CommandDto` values
- **THEN** the system rejects the import instead of importing the list entries

### Requirement: Imported command export YAML stores returned object result
The system SHALL store returned object metadata from each imported `CommandExportDto` as the result bookmark of the corresponding replay `CommandLogEntry`.
The system MUST NOT require the returned object bookmark to resolve to an existing domain object during import.

#### Scenario: Import command export with returned object
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` whose `returnedObject` has `logicalTypeName` and `id`
- **THEN** the system persists the embedded command DTO for replay and sets the created `CommandLogEntry` result bookmark from that returned object metadata

#### Scenario: Import command export without returned object
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` without `returnedObject`
- **THEN** the system persists the embedded command DTO for replay and leaves the created `CommandLogEntry` result bookmark unset

### Requirement: Exported command YAML includes logged safe action returned object metadata
The system SHALL export safe action command log entries using the existing command export YAML shape when safe action command publishing created those entries.
The system SHALL export synthetic parented collection selector action command log entries using the same command export YAML shape as other logged safe action entries.
When a logged safe action entry has a non-null returned object bookmark, the exported YAML SHALL include returned object metadata containing the bookmark logical type name and identifier.
The system SHALL preserve compatibility with existing command replay import for exported safe action entries.

#### Scenario: Export logged safe action command with returned object
- **GIVEN** safe action command publishing is enabled
- **AND** a safe action command log entry has result bookmark `demoCustomer:1`
- **WHEN** the command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the safe action invocation
- **AND** the generated YAML contains returned object metadata with logical type name `demoCustomer` and id `1`

#### Scenario: Export logged selector action command with returned object
- **GIVEN** synthetic parented collection selector action creation is enabled
- **AND** safe action command publishing is enabled
- **AND** a synthetic selector action command log entry has result bookmark `demoChild:1`
- **WHEN** the selector action command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the selector action invocation
- **AND** the generated YAML contains returned object metadata with logical type name `demoChild` and id `1`

#### Scenario: Export command stream containing safe and state-changing commands
- **GIVEN** a command export selection contains a logged safe action command followed by a state-changing command that uses the found object
- **WHEN** the commands are exported
- **THEN** the generated YAML preserves both command entries in replay order
- **AND** the safe action entry includes its returned object metadata when available

