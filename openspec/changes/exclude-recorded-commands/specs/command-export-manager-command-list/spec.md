## MODIFIED Requirements

### Requirement: Export manager shows one baseline-bounded active command collection
The export manager SHALL expose a collection named `commands`.
The `commands` collection SHALL contain foreground command log entries at or after the export manager baseline whose replay state is `UNDEFINED` or `EXPORTED`.
The `commands` collection MUST exclude command log entries whose replay state is `EXCLUDED`.
The `commands` collection MUST exclude command log entries whose replay state is a replay execution state such as `PENDING`, `OK`, or `FAILED`.
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

## ADDED Requirements

### Requirement: Export manager shows excluded commands separately
The export manager SHALL expose a collection named `excludedCommands` below the `commands` collection.
The `excludedCommands` collection SHALL contain foreground command log entries at or after the export manager baseline whose replay state is `EXCLUDED`.
The `excludedCommands` collection MUST exclude command log entries whose replay state is not `EXCLUDED`.
The `excludedCommands` collection SHALL be ordered by the same command ordering used for the active `commands` collection.
Excluded commands MUST NOT appear in both `commands` and `excludedCommands` at the same time.

#### Scenario: Excluded command appears in excluded commands collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager excluded commands collection
- **THEN** command `A` is included
- **AND** command `A` is not included in the active commands collection

#### Scenario: Active commands do not appear in excluded commands collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `EXPORTED`
- **WHEN** the user views the export manager excluded commands collection
- **THEN** command `A` is not included
- **AND** command `B` is not included

#### Scenario: Excluded commands before baseline are omitted
- **GIVEN** an export manager baseline is set
- **AND** command `A` before the baseline has replay state `EXCLUDED`
- **AND** command `B` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager excluded commands collection
- **THEN** command `A` is not included
- **AND** command `B` is included
