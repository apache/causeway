# command-export-manager-command-list Specification

## Purpose
TBD - created by archiving change simplify-command-export-manager-commands. Update Purpose after archive.
## Requirements
### Requirement: Export manager shows one baseline-bounded command collection
The export manager SHALL expose a collection named `commands`.
The `commands` collection SHALL contain foreground command log entries at or after the export manager baseline.
The `commands` collection MUST include command log entries regardless of whether their replay state is `UNDEFINED`, `EXPORTED`, or another replay state.
The `commands` collection SHALL be ordered by the command ordering used for command export and command movement.
The export manager MUST NOT expose separate mode-filtered collections for not-yet-exported commands and exported commands.

#### Scenario: Unified command list includes mixed replay states
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `EXPORTED`
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is included
- **AND** command `B` is included

#### Scenario: Commands before baseline are excluded
- **GIVEN** an export manager baseline is set
- **AND** command `A` is before the baseline
- **AND** command `B` is at or after the baseline
- **WHEN** the user views the export manager commands collection
- **THEN** command `A` is not included
- **AND** command `B` is included

### Requirement: Export manager removes mode switching
The export manager SHALL use a single command-list mode.
The export manager MUST NOT expose a mode property that switches between exported and not-yet-exported commands.
The export manager MUST NOT expose a toggle action for switching between exported and not-yet-exported command lists.
The export manager view-model memento SHALL store the baseline and page limit needed for the unified command list.
The export manager view-model memento MUST NOT store a command-list mode.

#### Scenario: Export manager page has no toggle mode action
- **WHEN** the user views the export manager
- **THEN** there is no action to toggle between exported and not-yet-exported commands
- **AND** the commands collection remains visible as the single command list

#### Scenario: Memento stores only unified list state
- **GIVEN** an export manager has a baseline and page limit
- **WHEN** the export manager creates its view-model memento
- **THEN** the memento contains the baseline
- **AND** the memento contains the page limit
- **AND** the memento does not contain a command-list mode

### Requirement: Exported replay state remains visible and updated
The export manager commands collection SHALL display each command's replay state.
When selected commands are exported successfully, the export manager SHALL set those command log entries to replay state `EXPORTED`.
A command whose replay state is `EXPORTED` MUST remain eligible to appear in the commands collection.
Exporting selected commands MUST NOT remove those commands from the commands collection merely because their replay state changed.

#### Scenario: Export updates replay state without removing command
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in the commands collection with replay state `UNDEFINED`
- **WHEN** the user exports command `A`
- **THEN** command `A` has replay state `EXPORTED`
- **AND** command `A` remains eligible for the commands collection

#### Scenario: Replay state column distinguishes already exported commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` in the commands collection has replay state `UNDEFINED`
- **AND** command `B` in the commands collection has replay state `EXPORTED`
- **WHEN** the user views the commands collection
- **THEN** the replay state for command `A` is visible
- **AND** the replay state for command `B` is visible

