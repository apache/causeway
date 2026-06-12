# command-export-command-reordering Specification

## Purpose
TBD - created by archiving change add-move-commands-action. Update Purpose after archive.
## Requirements
### Requirement: Export manager moves selected commands after a target command
When command-log recording support is `ENABLED`, the export manager SHALL provide a `moveCommandsUp` action that moves one or more selected commands from the active commands collection upward by retimestamping them immediately after a target command.
When command-log recording support is `ENABLED`, the export manager SHALL provide a `moveCommandsDown` action that moves one or more selected commands from the active commands collection downward by retimestamping them immediately after a target command.
When command-log recording support is `DISABLED`, the export manager MUST disable command movement in both directions.
Both movement actions SHALL operate only on commands at or after the export manager baseline whose replay state is `UNDEFINED` or `EXPORTED`.
The `moveCommandsUp` target command choices SHALL include only commands from the active commands collection that are before the first selected command in export-manager ordering.
The `moveCommandsDown` target command choices SHALL include only commands from the active commands collection that are after the last selected command in export-manager ordering.
The target command choices for both movement actions MUST exclude every command selected for movement.
The target command choices for both movement actions MUST exclude commands whose replay state is `EXCLUDED`.
Both movement actions MUST reject execution when no commands are selected.
Both movement actions MUST reject execution when the target command is missing.
Both movement actions MUST reject execution when the target command is one of the selected commands.
Both movement actions MUST reject execution when any selected command or target command is outside the active commands collection.
The `moveCommandsUp` action MUST reject execution when the target command is not before the first selected command in export-manager ordering.
The `moveCommandsDown` action MUST reject execution when the target command is not after the last selected command in export-manager ordering.
Both movement actions MUST NOT exclude a selected command or target command merely because its replay state is `EXPORTED`.
The export manager MUST NOT expose the old generic `moveCommands` action once direction-specific movement actions are available.

#### Scenario: Up action target choices include only commands before the first selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are in the active commands collection after the baseline
- **AND** commands `B` and `C` are selected for upward movement
- **WHEN** the system provides target choices for the move-up action
- **THEN** command `A` is offered as a target choice
- **AND** commands `B` and `C` are not offered as target choices

#### Scenario: Down action target choices include only commands after the last selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are in the active commands collection after the baseline
- **AND** commands `A` and `B` are selected for downward movement
- **WHEN** the system provides target choices for the move-down action
- **THEN** command `C` is offered as a target choice
- **AND** commands `A` and `B` are not offered as target choices

#### Scenario: Target choices include exported commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` has replay state `EXPORTED`
- **AND** command `B` has replay state `UNDEFINED`
- **AND** commands `A` and `B` are in the active commands collection after the baseline
- **AND** command `B` is selected for upward movement
- **WHEN** the system provides target choices for the move-up action
- **THEN** command `A` is offered as a target choice

#### Scenario: Target choices exclude excluded commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` has replay state `UNDEFINED`
- **AND** command `B` has replay state `EXCLUDED`
- **AND** command `A` is in the active commands collection after the baseline
- **AND** command `B` is in the excluded commands collection after the baseline
- **AND** command `A` is selected for movement
- **WHEN** the system provides target choices for either movement action
- **THEN** command `B` is not offered as a target choice

#### Scenario: Cannot move without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes either movement action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move relative to a selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is selected for movement
- **WHEN** the user invokes either movement action using command `A` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move up relative to a target after the first selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before command `B` in the active commands collection
- **AND** command `A` is selected for upward movement
- **WHEN** the user invokes the move-up action using command `B` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move down relative to a target before the last selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before command `B` in the active commands collection
- **AND** command `B` is selected for downward movement
- **WHEN** the user invokes the move-down action using command `A` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move commands outside the baseline-bounded active set
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes either movement action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move excluded command by direct invocation
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` has replay state `EXCLUDED`
- **WHEN** a caller bypasses the UI and invokes either movement action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` keeps its original timestamp

#### Scenario: Movement actions are disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates either movement action
- **THEN** the movement action is disabled

#### Scenario: Generic move action is no longer exposed
- **WHEN** the user views the export manager commands collection
- **THEN** there is no collection action named `moveCommands`
- **AND** collection actions named `moveCommandsUp` and `moveCommandsDown` are available when command-log recording support is `ENABLED`

### Requirement: Moved commands preserve selected block timing
The export manager SHALL move selected commands as one contiguous block when moving commands up or down.
For upward movement, the first moved command SHALL receive a timestamp 10ms after the target command timestamp.
For downward movement, the first moved command SHALL receive a timestamp 10ms after the target command timestamp.
Each remaining moved command SHALL preserve the original elapsed time from the neighbouring selected command where that elapsed time is positive and fits the direction of movement.
When an original elapsed time is zero, negative, unavailable, or unusable for the requested direction, the system SHALL use a deterministic minimum increment of 10ms between moved command timestamps.
Both movement actions SHALL preserve the relative order of the selected commands from before the move.
Both movement actions MUST NOT change the target command timestamp.
Both movement actions MUST NOT change timestamps of commands that are neither selected nor the target command.

#### Scenario: Single command moves up after target with ten millisecond offset
- **GIVEN** command `A` has timestamp `10:00:05.000`
- **AND** command `B` has timestamp `10:00:00.000`
- **AND** command `A` is selected for upward movement
- **WHEN** the user moves the selected command up after target command `B`
- **THEN** command `A` has timestamp `10:00:00.010`
- **AND** command `B` keeps timestamp `10:00:00.000`

#### Scenario: Single command moves down after target with ten millisecond offset
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:05.000`
- **AND** command `A` is selected for downward movement
- **WHEN** the user moves the selected command down after target command `B`
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` keeps timestamp `10:00:05.000`

#### Scenario: Multiple commands preserve original internal gaps when moved up
- **GIVEN** command `A` has timestamp `10:00:05.000`
- **AND** command `B` has timestamp `10:00:05.250`
- **AND** command `C` has timestamp `10:00:00.000`
- **AND** commands `A` and `B` are selected for upward movement
- **WHEN** the user moves the selected commands up after target command `C`
- **THEN** command `A` has timestamp `10:00:00.010`
- **AND** command `B` has timestamp `10:00:00.260`
- **AND** command `C` keeps timestamp `10:00:00.000`

#### Scenario: Multiple commands preserve original internal gaps when moved down
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:00.250`
- **AND** command `C` has timestamp `10:00:05.000`
- **AND** commands `A` and `B` are selected for downward movement
- **WHEN** the user moves the selected commands down after target command `C`
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` has timestamp `10:00:05.260`
- **AND** command `C` keeps timestamp `10:00:05.000`

#### Scenario: Multiple commands keep selected order when original gap is not positive
- **GIVEN** commands `A` and `B` are selected for movement
- **AND** command `A` does not have a timestamp before command `B`
- **WHEN** the user moves the selected commands using either movement action
- **THEN** command `A` is timestamped before command `B`
- **AND** command `B` is timestamped at least 10ms after command `A`

#### Scenario: Unselected commands are not retimestamped
- **GIVEN** commands `A`, `B`, and `C` are after the export manager baseline
- **AND** command `A` is selected for movement
- **AND** command `B` is not selected
- **WHEN** the user moves command `A` relative to target command `C`
- **THEN** command `B` keeps its original timestamp

### Requirement: Moved commands can squash selected block timing
Both export manager movement actions SHALL provide a checkbox parameter that controls whether selected command timings are squashed during the move.
When timing squash is not selected, each action SHALL preserve the existing selected-block timing behavior for its direction.
For upward movement with timing squash selected, the first moved command SHALL receive a timestamp 1 second after the target command timestamp.
For downward movement with timing squash selected, the first moved command SHALL receive a timestamp 1 second after the target command timestamp.
When timing squash is selected, each remaining moved command SHALL receive a timestamp exactly 1 second from the neighbouring moved command in the direction needed to preserve selected order.
When timing squash is selected, each action SHALL preserve the relative order of the selected commands from before the move.
When timing squash is selected, each action MUST discard the original elapsed times between selected commands.
Both movement actions MUST NOT change the target command timestamp.
Both movement actions MUST NOT change timestamps of commands that are neither selected nor the target command.

#### Scenario: Multiple commands squash original internal gaps when moved up
- **GIVEN** command `A` has timestamp `10:00:10.000`
- **AND** command `B` has timestamp `10:00:15.000`
- **AND** command `C` has timestamp `10:00:00.000`
- **AND** commands `A` and `B` are selected for upward movement
- **WHEN** the user moves the selected commands up after target command `C` with timing squash selected
- **THEN** command `A` has timestamp `10:00:01.000`
- **AND** command `B` has timestamp `10:00:02.000`
- **AND** command `C` keeps timestamp `10:00:00.000`

#### Scenario: Multiple commands squash original internal gaps when moved down
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:05.000`
- **AND** command `C` has timestamp `10:00:20.000`
- **AND** commands `A` and `B` are selected for downward movement
- **WHEN** the user moves the selected commands down after target command `C` with timing squash selected
- **THEN** command `A` has timestamp `10:00:21.000`
- **AND** command `B` has timestamp `10:00:22.000`
- **AND** command `C` keeps timestamp `10:00:20.000`

#### Scenario: Timing gaps are preserved when squash is not selected
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:00.250`
- **AND** command `C` has timestamp `10:00:05.000`
- **AND** commands `A` and `B` are selected for downward movement
- **WHEN** the user moves the selected commands down after target command `C` with timing squash not selected
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` has timestamp `10:00:05.260`
- **AND** command `C` keeps timestamp `10:00:05.000`

#### Scenario: Squash timing keeps selected order independent of original gaps
- **GIVEN** commands `A`, `B`, and `C` are selected for movement
- **AND** the original timestamp gaps between the selected commands are larger than 1 second
- **WHEN** the user moves the selected commands with timing squash selected using either movement action
- **THEN** command `A` remains timestamped before command `B`
- **AND** command `B` remains timestamped before command `C`
- **AND** adjacent moved commands are separated by exactly 1 second

