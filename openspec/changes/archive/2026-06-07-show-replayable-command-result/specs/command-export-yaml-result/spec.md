## ADDED Requirements

### Requirement: Replayable command displays exported result metadata
The system SHALL expose recorded result metadata on ReplayableCommand when the underlying command log entry has a non-null result bookmark.
The displayed result metadata SHALL use the same `result` element shape as exported command YAML, with `type` and `id` fields derived from the result bookmark.
The system SHALL leave ReplayableCommand result metadata absent when the underlying command log entry has no result bookmark.
The system MUST NOT display the legacy `returnedObject` field name for ReplayableCommand result metadata.
The system MUST NOT require the result bookmark to resolve to a live domain object before displaying the metadata.

#### Scenario: Replayable command with recorded result
- **WHEN** a ReplayableCommand wraps a command log entry whose result bookmark is `demoCustomer:1`
- **THEN** ReplayableCommand displays result metadata using field `result` with type `demoCustomer` and id `1`
- **AND** ReplayableCommand does not display field `returnedObject`

#### Scenario: Replayable command without recorded result
- **WHEN** a ReplayableCommand wraps a command log entry whose result bookmark is null
- **THEN** ReplayableCommand does not display result metadata
- **AND** ReplayableCommand does not display field `returnedObject`

#### Scenario: Replayable command result is unresolved locally
- **WHEN** a ReplayableCommand wraps an imported command log entry whose result bookmark does not resolve to a local domain object
- **THEN** ReplayableCommand still displays the bookmark type and id as result metadata
