## ADDED Requirements

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
Existing YAML exports that contain only command DTO data MUST remain importable.

#### Scenario: Import legacy command DTO YAML
- **WHEN** command replay import receives a YAML file using the legacy command DTO-only export shape
- **THEN** the system imports the command DTOs as before

#### Scenario: Import YAML with returned object metadata
- **WHEN** command replay import receives a YAML file generated with returned object metadata
- **THEN** the system imports the command DTOs needed for replay and does not require the returned object metadata to resolve to an object
