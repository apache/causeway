## ADDED Requirements

### Requirement: Exported command YAML includes synthetic selector returned object metadata
The system SHALL export logged synthetic parented collection selector action entries using the existing command export YAML shape.
When a logged synthetic selector action entry has a non-null returned object bookmark, the exported YAML SHALL include returned object metadata containing the bookmark logical type name and identifier.
The system SHALL preserve replay compatibility for command streams that contain synthetic selector action entries.

#### Scenario: Export logged synthetic selector action with returned object
- **GIVEN** safe action command publishing is enabled
- **AND** a synthetic parented collection selector action command log entry has a returned object bookmark
- **WHEN** the command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the synthetic selector action invocation
- **AND** the generated YAML contains returned object metadata for the selected child object

#### Scenario: Export command stream containing selector and state-changing commands
- **GIVEN** a command export selection contains a logged synthetic selector action followed by a state-changing command on the selected child object
- **WHEN** the commands are exported
- **THEN** the generated YAML preserves both command entries in replay order
- **AND** the synthetic selector entry includes its returned object metadata when available
