## MODIFIED Requirements

### Requirement: Export manager shows one baseline-bounded command collection
The export manager SHALL expose a collection named `commands`.
The `commands` collection SHALL contain replayable foreground command log entries at or after the export manager baseline whose replay state is `UNDEFINED` or `EXPORTED`.
The `commands` collection MUST exclude command log entries whose replay state is `EXCLUDED`.
The `commands` collection MUST exclude command log entries whose replay state is a replay execution state such as `PENDING`, `OK`, or `FAILED`.
The `commands` collection MUST exclude safe action command log entries that do not have a single stored result bookmark.
The `commands` collection SHALL be ordered by the command ordering used for command export and command movement.
The export manager MUST NOT expose separate mode-filtered collections for not-yet-exported commands and exported commands.

#### Scenario: Active command list includes undefined and exported states
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `EXPORTED`
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is included
- **AND** command `B` is included

#### Scenario: Excluded command is removed from active command list
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is not included

#### Scenario: Commands before baseline are excluded
- **GIVEN** an export manager baseline is set
- **AND** command `A` is before the baseline
- **AND** command `B` is at or after the baseline
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is not included
- **AND** command `B` is included

#### Scenario: Safe action without result is omitted from export manager commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline represents a logged safe action with no result bookmark
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is not included
- **AND** the underlying command log entry for command `A` remains persisted

#### Scenario: Safe action with single result remains in export manager commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline represents a logged safe action with result bookmark `demoCustomer:1`
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is included
