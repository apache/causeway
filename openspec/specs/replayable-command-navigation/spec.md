# replayable-command-navigation Specification

## Purpose
TBD - created by archiving change improve-replayable-command-navigation. Update Purpose after archive.
## Requirements
### Requirement: Replayable command exposes adjacent navigation actions
A replayable command SHALL expose a `previous` action that returns the adjacent earlier command log entry as a `ReplayableCommand`.
A replayable command SHALL expose a `next` action that returns the adjacent later command log entry as a `ReplayableCommand`.
The adjacent earlier and later commands SHALL be determined by the command log ordering used for replayable command lists.
The navigation actions MUST NOT mutate command log entries.
The navigation actions MUST preserve the replay context used by the current replayable command.

#### Scenario: Navigate to previous command
- **GIVEN** command `A` appears immediately before command `B` in command log ordering
- **AND** the user is viewing replayable command `B`
- **WHEN** the user invokes `previous`
- **THEN** the system returns replayable command `A`

#### Scenario: Navigate to next command
- **GIVEN** command `B` appears immediately before command `C` in command log ordering
- **AND** the user is viewing replayable command `B`
- **WHEN** the user invokes `next`
- **THEN** the system returns replayable command `C`

#### Scenario: Navigation preserves replay context
- **GIVEN** the user is viewing a replayable command with a replay context
- **AND** an adjacent command exists
- **WHEN** the user invokes `next`
- **THEN** the returned replayable command uses the same replay context

### Requirement: Replayable command disables navigation at list boundaries
The `previous` action MUST be disabled when no earlier command exists in command log ordering.
The `next` action MUST be disabled when no later command exists in command log ordering.
Boundary disablement MUST be calculated without mutating command log entries.

#### Scenario: First command has no previous action
- **GIVEN** command `A` is the first command in command log ordering
- **WHEN** the framework evaluates the `previous` action for replayable command `A`
- **THEN** the `previous` action is disabled

#### Scenario: Last command has no next action
- **GIVEN** command `Z` is the last command in command log ordering
- **WHEN** the framework evaluates the `next` action for replayable command `Z`
- **THEN** the `next` action is disabled

#### Scenario: Middle command can navigate both directions
- **GIVEN** command `B` has an earlier command and a later command in command log ordering
- **WHEN** the framework evaluates navigation actions for replayable command `B`
- **THEN** the `previous` action is enabled
- **AND** the `next` action is enabled

