## ADDED Requirements

### Requirement: Export uses recorded singleton-list results as result metadata
The system SHALL treat a command log result captured from a one-element list result the same as a scalar captured result for command export purposes.
When an exported command's underlying `CommandLogEntry#getResult()` bookmark was captured from a singleton list, the exported YAML SHALL include result metadata under field `result` with the bookmark logical type name and identifier.
When a command returned an empty list, a multi-object list, or a non-bookmarkable list result and therefore has no command log result bookmark, exported YAML SHALL omit result metadata for that command.
The system MUST NOT change the exported YAML result field shape to represent the original container shape.

#### Scenario: Export command with singleton-list result
- **WHEN** a command selected for export has result bookmark `demoCustomer:1` that was captured from a singleton list result
- **THEN** the generated YAML contains result metadata for that command under field `result` with type `demoCustomer` and id `1`
- **AND** the generated YAML does not record that the original action result was a list

#### Scenario: Export command with multi-object list result
- **WHEN** a command selected for export returned a list containing bookmark `demoCustomer:1` and bookmark `demoCustomer:2`
- **AND** the command log entry has no result bookmark
- **THEN** the generated YAML does not include result metadata for that command

### Requirement: Singleton-list results establish command export path knowledge
The system SHALL allow a recorded singleton-list result bookmark to establish a known target for later command export validation.
The system SHALL apply the same baseline-bounded export ordering rules to singleton-list result bookmarks as it applies to scalar result bookmarks.
A singleton-list result bookmark MUST make only the contained bookmark known, not the list or container itself.

#### Scenario: Singleton-list finder establishes a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected finder command returns a singleton list containing bookmark `demoCustomer:1`
- **AND** the command log entry result is bookmark `demoCustomer:1`
- **WHEN** a selected later action command targets bookmark `demoCustomer:1`
- **THEN** the export manager accepts the later command target as known

#### Scenario: Multi-object finder does not establish a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected finder command returns multiple bookmarkable objects
- **AND** the command log entry has no result bookmark
- **WHEN** a selected later action command targets one of those returned object bookmarks
- **THEN** the finder command does not make that later target known by itself
