## ADDED Requirements

### Requirement: Moved commands can squash selected block timing
The export manager move action SHALL provide a checkbox parameter that controls whether selected command timings are squashed during the move.
When timing squash is not selected, the action SHALL preserve the existing selected-block timing behavior.
When timing squash is selected, the first moved command SHALL receive a timestamp 1 second after the target command timestamp.
When timing squash is selected, each remaining moved command SHALL receive a timestamp exactly 1 second after the preceding moved command timestamp.
When timing squash is selected, the action SHALL preserve the relative order of the selected commands from before the move.
When timing squash is selected, the action MUST discard the original elapsed times between selected commands.
The action MUST NOT change the target command timestamp.
The action MUST NOT change timestamps of commands that are neither selected nor the target command.

#### Scenario: Multiple commands squash original internal gaps
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:05.000`
- **AND** command `C` has timestamp `10:00:10.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C` with timing squash selected
- **THEN** command `A` has timestamp `10:00:11.000`
- **AND** command `B` has timestamp `10:00:12.000`
- **AND** command `C` keeps timestamp `10:00:10.000`

#### Scenario: Timing gaps are preserved when squash is not selected
- **GIVEN** command `A` has timestamp `10:00:00.000`
- **AND** command `B` has timestamp `10:00:00.250`
- **AND** command `C` has timestamp `10:00:05.000`
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the user moves the selected commands after target command `C` with timing squash not selected
- **THEN** command `A` has timestamp `10:00:05.010`
- **AND** command `B` has timestamp `10:00:05.260`
- **AND** command `C` keeps timestamp `10:00:05.000`

#### Scenario: Squash timing keeps selected order independent of original gaps
- **GIVEN** commands `A`, `B`, and `C` are selected for movement
- **AND** the original timestamp gaps between the selected commands are larger than 1 second
- **WHEN** the user moves the selected commands after a target command with timing squash selected
- **THEN** command `A` is timestamped 1 second after the target command
- **AND** command `B` is timestamped 1 second after command `A`
- **AND** command `C` is timestamped 1 second after command `B`
