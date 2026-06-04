## ADDED Requirements

### Requirement: Imported command export YAML stores returned object result
The system SHALL store returned object metadata from each imported `CommandExportDto` as the result bookmark of the corresponding replay `CommandLogEntry`.
The system MUST NOT require the returned object bookmark to resolve to an existing domain object during import.

#### Scenario: Import command export with returned object
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` whose `returnedObject` has `logicalTypeName` and `id`
- **THEN** the system persists the embedded command DTO for replay and sets the created `CommandLogEntry` result bookmark from that returned object metadata

#### Scenario: Import command export without returned object
- **WHEN** command replay import receives a multi-document YAML file containing a `CommandExportDto` without `returnedObject`
- **THEN** the system persists the embedded command DTO for replay and leaves the created `CommandLogEntry` result bookmark unset

## MODIFIED Requirements

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
