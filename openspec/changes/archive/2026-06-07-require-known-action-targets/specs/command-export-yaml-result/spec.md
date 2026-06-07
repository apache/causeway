## ADDED Requirements

### Requirement: Exported command sequences contain only reachable action targets
The system SHALL validate selected command export sequences before emitting YAML.
The system SHALL rely on recorded result metadata at or after the export manager baseline to prove that later exported action targets are reachable from earlier commands in the same exportable range.
An exported action command whose target is not a menu service root SHALL have a target bookmark that was recorded as the result of an earlier command in the baseline-bounded exportable range.
Command export MUST NOT silently compensate for an unknown action target by fabricating result metadata or by inventing a path that was not recorded.
If selected command log data contains an action with an unknown target, export SHALL reject the sequence with a clear validation message rather than emitting unreplayable YAML.
The validation message SHALL identify the command that failed validation.

#### Scenario: Export sequence uses earlier result as later target
- **GIVEN** an export manager baseline is set
- **AND** an exportable command at or after the baseline has result bookmark `demoCustomer:1`
- **AND** a later selected action command targets bookmark `demoCustomer:1`
- **WHEN** the commands are exported
- **THEN** the exported YAML preserves both commands in replay order
- **AND** the earlier command includes result metadata that makes the later target reachable

#### Scenario: Export rejects unknown target in selected data
- **GIVEN** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** the target is not a menu service root
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the commands are exported
- **THEN** the system rejects the export sequence as unreplayable
- **AND** the validation message identifies the selected command that failed
- **AND** the system does not emit YAML for that invalid sequence

#### Scenario: Export does not fabricate target path metadata
- **GIVEN** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the commands are exported
- **THEN** the system does not add synthetic result metadata solely to make the sequence appear reachable
