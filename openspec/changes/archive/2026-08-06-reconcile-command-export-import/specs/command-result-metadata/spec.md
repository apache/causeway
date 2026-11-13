## ADDED Requirements

### Requirement: Replay import decodes canonical and legacy multi-document YAML
The system SHALL provide a replay-import decoder that first attempts a multi-document stream of `CommandExportDto` values and, when that shape is not valid, falls back to a multi-document stream of plain `CommandDto` values. A wrapped stream SHALL be accepted only when at least one document contains an embedded command. The replay-import decoder MUST reject a YAML list root and MUST surface a failure when neither supported multi-document shape can be decoded. The existing plain `CommandDto` YAML APIs and their accepted formats MUST remain unchanged.

#### Scenario: Result-bearing export stream is decoded
- **WHEN** replay import receives multi-document YAML whose documents contain embedded commands and optional `result` metadata
- **THEN** it returns one imported-command carrier per embedded command in document order
- **AND** each carrier retains its optional result bookmark

#### Scenario: Legacy command stream falls back successfully
- **WHEN** replay import receives legacy multi-document `CommandDto` YAML
- **THEN** it returns the commands in document order with no result bookmark metadata

#### Scenario: YAML list root is rejected for replay
- **WHEN** replay import receives a single YAML list of commands
- **THEN** it rejects the input instead of importing list entries

#### Scenario: General YAML compatibility remains unchanged
- **WHEN** an existing caller uses the plain `CommandDto` YAML APIs
- **THEN** those APIs retain their previously accepted list and multi-document behavior

### Requirement: Replay import preserves portable result identity
For each imported `CommandExportDto`, the system SHALL persist the embedded command through the replay repository and SHALL store `result.type` and `result.id` as the corresponding command-log result bookmark when present. Import MUST NOT require that bookmark to resolve to a local domain object. An absent `result` SHALL leave the imported entry's result unset, and the legacy field `returnedObject` MUST NOT be interpreted as a result alias.

#### Scenario: Imported envelope stores its result bookmark
- **WHEN** an imported envelope contains result type `demo.Invoice` and id `1`
- **THEN** its replay command-log entry records bookmark `demo.Invoice:1`
- **AND** no domain object is resolved during import

#### Scenario: Imported envelope without result remains unset
- **WHEN** an imported envelope has an embedded command and no `result`
- **THEN** its replay command-log entry has no imported result bookmark

#### Scenario: Legacy returned-object field is ignored
- **WHEN** an imported envelope contains `returnedObject` but no `result`
- **THEN** the replay command-log entry does not derive a result bookmark from `returnedObject`
