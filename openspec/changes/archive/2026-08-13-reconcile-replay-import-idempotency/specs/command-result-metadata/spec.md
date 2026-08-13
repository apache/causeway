## ADDED Requirements

### Requirement: Replay import is idempotent for an already-present interaction id

When the system persists an imported command for replay, it SHALL be idempotent with respect to the command's
interaction id. If a command-log entry already exists for that interaction id, the system SHALL return the
existing entry without creating a new one and without failing persistence. If no entry exists, the system SHALL
create, initialise, and persist a new replay entry as before. Idempotency SHALL apply regardless of whether the
import arrives through the unified importer or the retained legacy importer.

#### Scenario: Re-importing a command reuses the existing entry

- **GIVEN** a command with a given interaction id has already been imported for replay
- **WHEN** the same command is imported again
- **THEN** the existing replay command-log entry is returned
- **AND** no duplicate entry is created
- **AND** persistence does not fail

#### Scenario: First import creates the entry

- **WHEN** a command whose interaction id is not yet present is imported for replay
- **THEN** a new replay command-log entry is created and persisted for that interaction id

#### Scenario: Idempotency applies to the legacy import path

- **GIVEN** a command with a given interaction id has already been imported for replay
- **WHEN** the same command is imported again through the legacy import path
- **THEN** the existing entry is returned and no duplicate is created
