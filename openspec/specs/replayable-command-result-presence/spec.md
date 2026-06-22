# replayable-command-result-presence Specification

## Purpose
TBD - created by archiving change improve-replayable-command-navigation. Update Purpose after archive.
## Requirements
### Requirement: Replayable command reports result presence
A replayable command SHALL expose a non-persisted Boolean property named `hasResult`.
The `hasResult` property SHALL be `true` when the wrapped command log entry stores a non-null result bookmark.
The `hasResult` property SHALL be `false` when the wrapped command log entry has no stored result bookmark.
The `hasResult` property MUST NOT perform bookmark lookup to determine whether the result object currently resolves locally.
The `hasResult` property MUST NOT change the command replay state.

#### Scenario: Command with stored result reports true
- **GIVEN** a replayable command wraps a command log entry with result bookmark `demoCustomer:1`
- **WHEN** the system reads the `hasResult` property
- **THEN** the property is `true`

#### Scenario: Command without stored result reports false
- **GIVEN** a replayable command wraps a command log entry with no result bookmark
- **WHEN** the system reads the `hasResult` property
- **THEN** the property is `false`

#### Scenario: Result presence does not require local result object
- **GIVEN** a replayable command wraps a command log entry with result bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the `hasResult` property
- **THEN** the property is `true`

### Requirement: Result presence is visible before known-participants in tables
The replayable command table layout SHALL display `hasResult` before the known-participants property.
The `hasResult` table placement SHALL apply when the known-participants property is available.
The `hasResult` table placement SHALL apply when the known-participants property is hidden.

#### Scenario: Result presence precedes known-participants in command manager table
- **GIVEN** a replayable command is rendered in the command manager sequence table
- **WHEN** the table columns are ordered
- **THEN** the `hasResult` column appears before the known-participants column

#### Scenario: Result presence remains visible outside export manager context
- **GIVEN** a replayable command is rendered outside command manager context
- **WHEN** the table columns are ordered
- **THEN** the `hasResult` column remains eligible for display

