## ADDED Requirements

### Requirement: Exported command YAML includes logged safe finder returned object metadata
The system SHALL export safe single-result finder command log entries using the existing command export YAML shape when safe finder logging created those entries.
When a logged safe finder entry has a non-null returned object bookmark, the exported YAML SHALL include returned object metadata containing the bookmark logical type name and identifier.
The system SHALL preserve compatibility with existing command replay import for exported safe finder entries.

#### Scenario: Export logged safe finder command with returned object
- **GIVEN** safe single-result finder command logging is enabled
- **AND** a safe finder command log entry has result bookmark `demoCustomer:1`
- **WHEN** the command is selected for export
- **THEN** the generated YAML contains the embedded command DTO for the safe finder invocation
- **AND** the generated YAML contains returned object metadata with logical type name `demoCustomer` and id `1`

#### Scenario: Export command stream containing finder and state-changing commands
- **GIVEN** a command export selection contains a logged safe finder command followed by a state-changing command that uses the found object
- **WHEN** the commands are exported
- **THEN** the generated YAML preserves both command entries in replay order
- **AND** the safe finder entry includes its returned object metadata when available
