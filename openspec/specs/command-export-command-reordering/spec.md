# command-export-command-reordering Specification

## Purpose
Define how the command export manager reorders commands by retimestamping selected commands relative to another command in the current sequence.

## Requirements
### Requirement: Export manager moves selected commands after a target command
When command-log recording support is `ENABLED`, the export manager SHALL provide a `moveCommands` action for the `commandsInSequence` collection.
The action SHALL move one or more selected commands by retimestamping them immediately after a target command.
The same action SHALL support moving commands earlier or later in the sequence.
When command-log recording support is `DISABLED`, the export manager MUST disable command movement.
The movement action SHALL operate only on commands in the current baseline-bounded `commandsInSequence` collection.
The target command choices SHALL include commands from the current `commandsInSequence` collection except those selected for movement.
The target command choices MUST exclude commands whose replay state is `EXCLUDED`, because excluded commands are not in `commandsInSequence`.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when the target command is missing.
The action MUST reject execution when the target command is one of the selected commands.
The action MUST reject execution when any selected command or target command is outside the current `commandsInSequence` collection.
The action MUST NOT reject a movement merely because the target is after the selected commands or before the selected commands.
The export manager MUST NOT expose separate direction-specific `moveCommandsUp` or `moveCommandsDown` actions.

#### Scenario: Target choices exclude selected commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are in the `commandsInSequence` collection after the baseline
- **AND** commands `B` and `C` are selected for movement
- **WHEN** the system provides target choices for the move action
- **THEN** command `A` is offered as a target choice
- **AND** commands `B` and `C` are not offered as target choices

#### Scenario: Target choices can support moving commands later
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are in the `commandsInSequence` collection after the baseline
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the system provides target choices for the move action
- **THEN** command `C` is offered as a target choice
- **AND** commands `A` and `B` are not offered as target choices

#### Scenario: Target choices exclude excluded commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` has replay state `UNDEFINED`
- **AND** command `B` has replay state `EXCLUDED`
- **AND** command `A` is in the `commandsInSequence` collection after the baseline
- **AND** command `B` is in the `excluded` collection after the baseline
- **AND** command `A` is selected for movement
- **WHEN** the system provides target choices for the move action
- **THEN** command `B` is not offered as a target choice

#### Scenario: Cannot move without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the movement action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move relative to a selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is selected for movement
- **WHEN** the user invokes the movement action using command `A` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Movement can target a later command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before command `B` in `commandsInSequence`
- **AND** command `A` is selected for movement
- **WHEN** the user invokes `moveCommands` using command `B` as the target command
- **THEN** the system accepts the invocation
- **AND** command `A` is retimestamped after command `B`

#### Scenario: Movement can target an earlier command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before command `B` in `commandsInSequence`
- **AND** command `B` is selected for movement
- **WHEN** the user invokes `moveCommands` using command `A` as the target command
- **THEN** the system accepts the invocation
- **AND** command `B` is retimestamped after command `A`

#### Scenario: Cannot move commands outside the baseline-bounded sequence
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes the movement action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move excluded command by direct invocation
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` has replay state `EXCLUDED`
- **WHEN** a caller bypasses the UI and invokes the movement action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` keeps its original timestamp

#### Scenario: Movement action is disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the movement action
- **THEN** the movement action is disabled

#### Scenario: Direction-specific move actions are not exposed
- **WHEN** the user views the export manager `commandsInSequence` collection
- **THEN** there is a collection action named `moveCommands`
- **AND** there are no collection actions named `moveCommandsUp` or `moveCommandsDown`

### Requirement: Moved commands preserve selected block timing with a one-second minimum gap
The export manager SHALL move selected commands as one ordered block after the target command.
The first moved command SHALL receive a timestamp 1 second after the target command timestamp.
Each remaining moved command SHALL preserve the original elapsed time from the preceding selected command when that elapsed time is positive and at least 1 second.
When an original elapsed time is zero, negative, unavailable, or less than 1 second, the system SHALL use a deterministic minimum increment of 1 second between moved command timestamps.
The movement action SHALL preserve the relative order of the selected commands from before the move.
The movement action MUST NOT change the target command timestamp.
The movement action MUST NOT change timestamps of commands that are neither selected nor the target command.
The movement action SHALL update the timestamp stored in each moved command's DTO as well as the command log entry timestamp.

#### Scenario: Single command moves after target with one-second offset
- **GIVEN** command `A` has timestamp `10:00:05.000`
- **AND** command `B` has timestamp `10:00:00.000`
- **AND** command `A` is selected for movement
- **WHEN** the user moves the selected command after target command `B`
- **THEN** command `A` has timestamp `10:00:01.000`
- **AND** command `B` keeps timestamp `10:00:00.000`
- **AND** command `A`'s DTO timestamp is updated to `10:00:01.000`

#### Scenario: Multiple commands preserve original internal gaps when moved
- **GIVEN** command `A` has timestamp `10:00:05.000`
- **AND** command `B` has timestamp `10:00:07.500`
- **AND** command `C` has timestamp `10:00:00.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C`
- **THEN** command `A` has timestamp `10:00:01.000`
- **AND** command `B` has timestamp `10:00:03.500`
- **AND** command `C` keeps timestamp `10:00:00.000`

#### Scenario: Multiple commands use minimum gap when original gap is too small
- **GIVEN** commands `A` and `B` are selected for movement
- **AND** command `A` does not have a timestamp at least 1 second before command `B`
- **WHEN** the user moves the selected commands after a target command
- **THEN** command `A` is timestamped before command `B`
- **AND** command `B` is timestamped at least 1 second after command `A`

#### Scenario: Unselected commands are not retimestamped
- **GIVEN** commands `A`, `B`, and `C` are after the export manager baseline
- **AND** command `A` is selected for movement
- **AND** command `B` is not selected
- **WHEN** the user moves command `A` after target command `C`
- **THEN** command `B` keeps its original timestamp

### Requirement: Moved commands can squash selected block timing
The export manager movement action SHALL provide a checkbox parameter that controls whether selected command timings are squashed during the move.
When timing squash is not selected, the action SHALL preserve selected-block timing subject to the one-second minimum gap.
When timing squash is selected, the first moved command SHALL receive a timestamp 1 second after the target command timestamp.
When timing squash is selected, each remaining moved command SHALL receive a timestamp exactly 1 second after the preceding moved command.
When timing squash is selected, the action SHALL preserve the relative order of the selected commands from before the move.
When timing squash is selected, the action MUST discard the original elapsed times between selected commands.
The default value for timing squash SHALL be `false`.
The movement action MUST NOT change the target command timestamp.
The movement action MUST NOT change timestamps of commands that are neither selected nor the target command.

#### Scenario: Multiple commands squash original internal gaps
- **GIVEN** command `A` has timestamp `10:00:10.000`
- **AND** command `B` has timestamp `10:00:15.000`
- **AND** command `C` has timestamp `10:00:00.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C` with timing squash selected
- **THEN** command `A` has timestamp `10:00:01.000`
- **AND** command `B` has timestamp `10:00:02.000`
- **AND** command `C` keeps timestamp `10:00:00.000`

#### Scenario: Timing gaps are preserved when squash is not selected
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:02.500`
- **AND** command `C` has timestamp `10:00:05.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C` with timing squash not selected
- **THEN** command `A` has timestamp `10:00:06.000`
- **AND** command `B` has timestamp `10:00:08.500`
- **AND** command `C` keeps timestamp `10:00:05.000`

#### Scenario: Squash timing keeps selected order independent of original gaps
- **GIVEN** commands `A`, `B`, and `C` are selected for movement
- **AND** the original timestamp gaps between the selected commands are larger than 1 second
- **WHEN** the user moves the selected commands with timing squash selected
- **THEN** command `A` remains timestamped before command `B`
- **AND** command `B` remains timestamped before command `C`
- **AND** adjacent moved commands are separated by exactly 1 second
