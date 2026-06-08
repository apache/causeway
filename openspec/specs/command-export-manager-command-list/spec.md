# command-export-manager-command-list Specification

## Purpose
TBD - created by archiving change simplify-command-export-manager-commands. Update Purpose after archive.
## Requirements
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

### Requirement: Export selected defaults to exportable commands
The export manager SHALL default the `exportSelected` action's selected commands to active commands whose exportability property is `true`.
The export manager MUST NOT default commands whose exportability property is `false` or `null` into the `exportSelected` selected commands.
The `exportSelected` action choices SHALL remain the active `commands` collection so users can override the default selection.
Default selection MUST NOT change command replay state.

#### Scenario: Export action selects exportable active commands by default
- **GIVEN** an export manager baseline is set
- **AND** command `A` in the active commands collection has exportability `true`
- **AND** command `B` in the active commands collection has exportability `false`
- **WHEN** the system provides defaults for the `exportSelected` selected commands
- **THEN** command `A` is selected by default
- **AND** command `B` is not selected by default

#### Scenario: Export action choices still include active commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in the active commands collection
- **AND** command `B` is in the active commands collection
- **WHEN** the system provides choices for the `exportSelected` selected commands
- **THEN** command `A` is available as a choice
- **AND** command `B` is available as a choice

#### Scenario: Default selection does not mark commands exported
- **GIVEN** an export manager baseline is set
- **AND** command `A` in the active commands collection has replay state `UNDEFINED`
- **AND** command `A` has exportability `true`
- **WHEN** the system provides defaults for the `exportSelected` selected commands
- **THEN** command `A` still has replay state `UNDEFINED`

### Requirement: Export manager omits obsolete make-selected-exportable action
The export manager MUST NOT expose a `makeSelectedExportable` collection action for the active `commands` collection.
The export manager SHALL rely on current export, exclusion, unexclusion, movement, and row-level exportability behavior instead of the obsolete make-selected-exportable workflow.
Removing the obsolete collection action MUST NOT remove the active `commands` collection or its row-level exportability state.

#### Scenario: Export manager page has no make selected exportable action
- **WHEN** the user views the export manager commands collection
- **THEN** there is no collection action named `makeSelectedExportable`
- **AND** the active `commands` collection remains visible
- **AND** command exportability state remains available on each replayable command when export-manager context supports it

