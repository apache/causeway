# command-export-command-reordering Specification

## Purpose
TBD - created by archiving change add-move-commands-action. Update Purpose after archive.
## Requirements
### Requirement: Export manager moves selected commands after a target command
When command-log recording support is `ENABLED`, the export manager SHALL provide an action to move one or more selected exportable commands after a target command.
When command-log recording support is `DISABLED`, the export manager MUST disable command movement.
The action SHALL operate only on commands at or after the export manager baseline.
The target command choices SHALL include commands at or after the export manager baseline.
The target command choices MUST exclude every command selected for movement.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when the target command is missing.
The action MUST reject execution when the target command is one of the selected commands.
The action MUST reject execution when any selected command or target command is outside the baseline-bounded exportable command set.

#### Scenario: Target choices exclude selected commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are exportable after the baseline
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the system provides target choices for the move action
- **THEN** command `C` is offered as a target choice
- **AND** commands `A` and `B` are not offered as target choices

#### Scenario: Cannot move without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the move action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move after a selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is selected for movement
- **WHEN** the user invokes the move action using command `A` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move commands outside the baseline-bounded set
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes the move action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Move action is disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the move action
- **THEN** the move action is disabled

### Requirement: Moved commands preserve selected block timing
The export manager SHALL move selected commands as one contiguous block after the target command.
The first moved command SHALL receive a timestamp 10ms after the target command timestamp.
Each remaining moved command SHALL preserve the original elapsed time from the previous selected command where that elapsed time is positive.
When an original elapsed time is zero, negative, or unavailable, the system SHALL use a deterministic minimum increment of 10ms from the previous moved command timestamp.
The action SHALL preserve the relative order of the selected commands from before the move.
The action MUST NOT change the target command timestamp.
The action MUST NOT change timestamps of commands that are neither selected nor the target command.

#### Scenario: Single command moves after target with ten millisecond offset
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:05.000`
- **AND** command `A` is selected for movement
- **WHEN** the user moves the selected command after target command `B`
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` keeps timestamp `10:00:05.000`

#### Scenario: Multiple commands preserve original internal gaps
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:00.250`
- **AND** command `C` has timestamp `10:00:05.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C`
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` has timestamp `10:00:05.260`
- **AND** command `C` keeps timestamp `10:00:05.000`

#### Scenario: Multiple commands keep selected order when original gap is not positive
- **GIVEN** commands `A` and `B` are selected for movement
- **AND** command `A` does not have a timestamp before command `B`
- **WHEN** the user moves the selected commands after a target command
- **THEN** command `A` is timestamped before command `B`
- **AND** command `B` is timestamped at least 10ms after command `A`

#### Scenario: Unselected commands are not retimestamped
- **GIVEN** commands `A`, `B`, and `C` are after the export manager baseline
- **AND** command `A` is selected for movement
- **AND** command `B` is not selected
- **WHEN** the user moves command `A` after target command `C`
- **THEN** command `B` keeps its original timestamp

