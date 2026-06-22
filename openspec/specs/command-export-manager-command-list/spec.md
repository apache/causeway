# command-export-manager-command-list Specification

## Purpose
Define the command manager collections used to review recorded commands, export command DTOs, and replay imported commands.

## Requirements
### Requirement: Export manager shows a baseline-bounded command sequence
The export manager SHALL expose a collection named `commandsInSequence`.
The `commandsInSequence` collection SHALL contain replayable foreground command log entries at or after the export manager baseline except entries whose replay state is `EXCLUDED`.
The `commandsInSequence` collection MAY include entries whose replay state is `UNDEFINED`, `PENDING`, `OK`, or `FAILED`.
The `commandsInSequence` collection MUST exclude command log entries whose replay state is `EXCLUDED`.
The `commandsInSequence` collection MUST exclude safe action command log entries that do not have a stored result bookmark.
The `commandsInSequence` collection SHALL be ordered by the command ordering used for command export, replay review, and command movement.
The export manager MUST NOT expose separate mode-filtered collections for not-yet-exported commands and exported commands.

#### Scenario: Command sequence includes non-excluded replay states
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `PENDING`
- **AND** command `C` at or after the baseline has replay state `OK`
- **AND** command `D` at or after the baseline has replay state `FAILED`
- **WHEN** the user views the export manager command sequence
- **THEN** command `A` is included
- **AND** command `B` is included
- **AND** command `C` is included
- **AND** command `D` is included

#### Scenario: Excluded command is removed from command sequence
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager command sequence
- **THEN** command `A` is not included

#### Scenario: Commands before baseline are excluded
- **GIVEN** an export manager baseline is set
- **AND** command `A` is before the baseline
- **AND** command `B` is at or after the baseline
- **WHEN** the user views the export manager command sequence
- **THEN** command `A` is not included
- **AND** command `B` is included

#### Scenario: Safe action without result is omitted from command sequence
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline represents a logged safe action with no result bookmark
- **WHEN** the user views the export manager command sequence
- **THEN** command `A` is not included
- **AND** the underlying command log entry for command `A` remains persisted

#### Scenario: Safe action with a result remains in command sequence
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline represents a logged safe action with result bookmark `demoCustomer:1`
- **WHEN** the user views the export manager command sequence
- **THEN** command `A` is included

### Requirement: Export manager removes command-list mode switching
The export manager SHALL use a single baseline-bounded command sequence for export-related actions.
The export manager MUST NOT expose a mode property that switches between exported and not-yet-exported commands.
The export manager MUST NOT expose a toggle action for switching between exported and not-yet-exported command lists.
The export manager view-model memento SHALL store the baseline and page limit needed for the command sequence.
The export manager view-model memento MUST NOT store a command-list mode.

#### Scenario: Export manager page has no toggle mode action
- **WHEN** the user views the export manager
- **THEN** there is no action to toggle between exported and not-yet-exported commands
- **AND** the `commandsInSequence` collection remains visible as the command sequence

#### Scenario: Memento stores only unified list state
- **GIVEN** an export manager has a baseline and page limit
- **WHEN** the export manager creates its view-model memento
- **THEN** the memento contains the baseline
- **AND** the memento contains the page limit
- **AND** the memento does not contain a command-list mode

### Requirement: Export sequence creates YAML for commands with known participants without changing replay state
The export manager SHALL provide an `exportSequence` action associated with the `commandsInSequence` collection.
The `exportSequence` action SHALL export all commands in `commandsInSequence` whose known-participants property is `true`.
The `exportSequence` action SHALL include each exported command DTO and recorded result bookmark in the YAML export.
The `exportSequence` action SHALL be disabled when no command in `commandsInSequence` has known participants.
Exporting the sequence MUST NOT change exported command log entries' replay states.
Exporting the sequence MUST NOT remove those commands from `commandsInSequence` merely because they were exported.

#### Scenario: Export sequence creates YAML and leaves replay state unchanged
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in `commandsInSequence` with replay state `UNDEFINED`
- **AND** command `A` has known participants
- **WHEN** the user exports the sequence
- **THEN** the YAML export contains command `A`'s DTO
- **AND** command `A` still has replay state `UNDEFINED`
- **AND** command `A` remains eligible for `commandsInSequence`

#### Scenario: Replay state column remains visible
- **GIVEN** an export manager baseline is set
- **AND** command `A` in `commandsInSequence` has replay state `UNDEFINED`
- **AND** command `B` in `commandsInSequence` has replay state `OK`
- **WHEN** the user views the command sequence
- **THEN** the replay state for command `A` is visible
- **AND** the replay state for command `B` is visible

### Requirement: Export manager shows excluded commands separately
The export manager SHALL expose a collection named `excluded` below the `commandsInSequence` collection.
The `excluded` collection SHALL contain foreground command log entries at or after the export manager baseline whose replay state is `EXCLUDED`.
The `excluded` collection MUST exclude command log entries whose replay state is not `EXCLUDED`.
The `excluded` collection SHALL be ordered by the same command ordering used for `commandsInSequence`.
Excluded commands MUST NOT appear in both `commandsInSequence` and `excluded` at the same time.

#### Scenario: Excluded command appears in excluded collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager excluded collection
- **THEN** command `A` is included
- **AND** command `A` is not included in `commandsInSequence`

#### Scenario: Non-excluded commands do not appear in excluded collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `OK`
- **WHEN** the user views the export manager excluded collection
- **THEN** command `A` is not included
- **AND** command `B` is not included

#### Scenario: Excluded commands before baseline are omitted
- **GIVEN** an export manager baseline is set
- **AND** command `A` before the baseline has replay state `EXCLUDED`
- **AND** command `B` at or after the baseline has replay state `EXCLUDED`
- **WHEN** the user views the export manager excluded collection
- **THEN** command `A` is not included
- **AND** command `B` is included

### Requirement: Export manager shows replay work separately
The export manager SHALL expose a collection named `pendingOrFailed` for imported commands that can be replayed or retried.
The `pendingOrFailed` collection SHALL contain foreground command log entries at or after the baseline whose replay state is `PENDING` or `FAILED`.
The `pendingOrFailed` collection SHALL use the repository's pending-or-failed replay-state query directly.
The `pendingOrFailed` collection MAY include safe action command log entries even when they do not have a result bookmark.
The export manager SHALL expose a collection named `recordedOrReplayed` for foreground command log entries whose replay state is `UNDEFINED` or `OK`.
The `recordedOrReplayed` collection MUST exclude safe action command log entries that do not have a stored result bookmark.

#### Scenario: Pending and failed commands appear in pending-or-failed collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `PENDING`
- **AND** command `B` at or after the baseline has replay state `FAILED`
- **WHEN** the user views the `pendingOrFailed` collection
- **THEN** command `A` is included
- **AND** command `B` is included

#### Scenario: Safe action without result can remain pending replay work
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline represents an imported safe action with replay state `PENDING`
- **AND** command `A` has no result bookmark
- **WHEN** the user views the `pendingOrFailed` collection
- **THEN** command `A` is included

#### Scenario: Recorded and replayed commands appear in recorded-or-replayed collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` at or after the baseline has replay state `UNDEFINED`
- **AND** command `B` at or after the baseline has replay state `OK`
- **WHEN** the user views the `recordedOrReplayed` collection
- **THEN** command `A` is included
- **AND** command `B` is included

### Requirement: Export sequence uses known participants as its implicit selection
The export manager SHALL treat `commandsInSequence` entries whose known-participants property is `true` as the implicit export sequence.
The export manager MUST NOT export commands whose known-participants property is `false` from `exportSequence`.
Computing or exporting the implicit sequence MUST NOT change command replay state.

#### Scenario: Export sequence uses commands with known participants
- **GIVEN** an export manager baseline is set
- **AND** command `A` in `commandsInSequence` has known participants
- **AND** command `B` in `commandsInSequence` has unknown participants
- **WHEN** the user exports the sequence
- **THEN** command `A` is included in the export
- **AND** command `B` is not included in the export

#### Scenario: Implicit selection does not change replay state
- **GIVEN** an export manager baseline is set
- **AND** command `A` in `commandsInSequence` has replay state `UNDEFINED`
- **AND** command `A` has known participants
- **WHEN** the system computes the implicit export sequence
- **THEN** command `A` still has replay state `UNDEFINED`

### Requirement: Export manager omits obsolete make-selected-exportable action
The export manager MUST NOT expose a `makeSelectedExportable` collection action for `commandsInSequence`.
The export manager SHALL rely on current export, exclusion, restoration, movement, and known-participants behavior instead of the obsolete make-selected-exportable workflow.
Removing the obsolete collection action MUST NOT remove `commandsInSequence` or each replayable command's known-participants state.

#### Scenario: Export manager page has no make selected exportable action
- **WHEN** the user views the export manager command sequence
- **THEN** there is no collection action named `makeSelectedExportable`
- **AND** the `commandsInSequence` collection remains visible
- **AND** known-participants state remains available on each replayable command when export-manager context supports it
