## ADDED Requirements

### Requirement: A single replayable command can be exported to YAML

The system SHALL provide an action that exports one replayable command to a result-bearing YAML document, both from the command's object form and from its table row. The exported document SHALL contain the recorded command and, when present, its recorded result envelope and result bookmark, using the same export-DTO form as the bulk sequence export. The export action SHALL support optional result remapping and SHALL NOT mutate the command's replay state. The exported content SHALL be delivered as a downloadable YAML value with a file name derived from the command.

#### Scenario: Export one command from its object form

- **GIVEN** a replayable command whose recorded result bookmark is `demo.Invoice:1`
- **WHEN** the per-command export action is invoked from the object form
- **THEN** a YAML document is produced containing that command and the recorded result bookmark
- **AND** the command's replay state is unchanged

#### Scenario: Export one command from its table row

- **WHEN** the per-command export action is invoked from a command's table row
- **THEN** the same result-bearing YAML document is produced for that command

#### Scenario: Export with result remapping

- **GIVEN** a replayable command whose recorded result maps to an actual result bookmark
- **WHEN** the per-command export action is invoked with result remapping enabled
- **THEN** the exported document carries the remapped result bookmark

#### Scenario: Export without a recorded result

- **GIVEN** a replayable command with no recorded result
- **WHEN** the per-command export action is invoked
- **THEN** the exported document contains the command and no result envelope
