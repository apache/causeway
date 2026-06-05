## ADDED Requirements

### Requirement: Exported command YAML includes logged safe action returned object metadata
The system SHALL export safe action command log entries using the existing command export YAML shape when safe action command publishing created those entries.
When a logged safe action entry has a non-null returned object bookmark, the exported YAML SHALL include returned object metadata containing the bookmark logical type name and identifier.
The system SHALL preserve compatibility with existing command replay import for exported safe action entries.

#### Scenario: Export logged safe action command with returned object
- **GIVEN** safe action command publishing is enabled
- **AND** a safe action command log entry has result bookmark `demoCustomer:1`
- **WHEN** the command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the safe action invocation
- **AND** the generated YAML contains returned object metadata with logical type name `demoCustomer` and id `1`

#### Scenario: Export command stream containing finder and state-changing commands
- **GIVEN** a command export selection contains a logged safe finder command followed by a state-changing command that uses the found object
- **WHEN** the commands are exported
- **THEN** the generated YAML preserves both command entries in replay order
- **AND** the safe finder entry includes its returned object metadata when available
