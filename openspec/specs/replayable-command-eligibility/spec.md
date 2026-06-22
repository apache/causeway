# replayable-command-eligibility Specification

## Purpose
Define when command log entries are wrapped as replayable command rows in command manager collections.

## Requirements
### Requirement: Command sequence wraps replay-useful command log entries
The system SHALL apply replayable command eligibility before wrapping a command log entry for command-sequence style collections.
A state-changing command log entry SHALL remain eligible for `ReplayableCommand` wrapping according to existing replay state and collection filters.
A safe action command log entry SHALL be eligible for `ReplayableCommand` wrapping in command-sequence style collections only when it stores a returned object bookmark as its result.
A safe action command log entry that has no stored result bookmark MUST NOT be wrapped as a `ReplayableCommand` in `commandsInSequence`, `excluded`, `recordedOrReplayed`, or adjacent-command navigation.
Suppressing `ReplayableCommand` wrapping MUST NOT delete, mutate, or prevent persistence of the underlying command log entry.

#### Scenario: State-changing command remains replayable
- **GIVEN** a persisted command log entry represents a state-changing action
- **WHEN** the system builds a command-sequence style replayable command collection
- **THEN** the entry remains eligible to be wrapped as a `ReplayableCommand`

#### Scenario: Safe action with result is replayable in command sequence
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry stores result bookmark `demoCustomer:1`
- **WHEN** the system builds `commandsInSequence`
- **THEN** the entry is eligible to be wrapped as a `ReplayableCommand`

#### Scenario: Safe action without result is omitted from command sequence
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry stores no result bookmark
- **WHEN** the system builds `commandsInSequence`
- **THEN** the entry is not wrapped as a `ReplayableCommand`
- **AND** the command log entry remains persisted

#### Scenario: Safe action without result is omitted from recorded-or-replayed review
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry stores no result bookmark
- **WHEN** the system builds `recordedOrReplayed`
- **THEN** the entry is not wrapped as a `ReplayableCommand`
- **AND** the command log entry remains persisted

### Requirement: Pending replay collection preserves imported pending-or-failed entries
The `pendingOrFailed` collection SHALL wrap the command log entries returned by the repository pending-or-failed query.
The `pendingOrFailed` collection MAY wrap a safe action command log entry even when that entry has no stored result bookmark.
This exception allows imported replay work to remain visible for replay or retry review.

#### Scenario: Safe action without result remains visible as pending replay work
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry has replay state `PENDING`
- **AND** the entry stores no result bookmark
- **WHEN** the system builds `pendingOrFailed`
- **THEN** the entry is wrapped as a `ReplayableCommand`

#### Scenario: State-changing pending command remains visible
- **GIVEN** a persisted command log entry represents a state-changing action
- **AND** the entry has replay state `PENDING`
- **WHEN** the system builds `pendingOrFailed`
- **THEN** the entry is wrapped as a `ReplayableCommand`
