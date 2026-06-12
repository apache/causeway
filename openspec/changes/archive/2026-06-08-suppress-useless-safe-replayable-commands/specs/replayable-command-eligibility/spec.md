## ADDED Requirements

### Requirement: Replayable commands are created only for replay-useful command log entries
The system SHALL define replayable command eligibility before wrapping a command log entry as a `ReplayableCommand`.
A state-changing command log entry SHALL remain eligible for `ReplayableCommand` wrapping according to existing replay state and collection filters.
A safe action command log entry SHALL be eligible for `ReplayableCommand` wrapping only when it stores a single returned object bookmark as its result.
A safe action command log entry that has no stored result bookmark MUST NOT be wrapped as a `ReplayableCommand`.
Suppressing `ReplayableCommand` wrapping MUST NOT delete, mutate, or prevent persistence of the underlying command log entry.

#### Scenario: State-changing command remains replayable
- **GIVEN** a persisted command log entry represents a state-changing action
- **WHEN** the system builds a replayable command collection
- **THEN** the entry remains eligible to be wrapped as a `ReplayableCommand`

#### Scenario: Safe action with single result is replayable
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry stores result bookmark `demoCustomer:1`
- **WHEN** the system builds a replayable command collection
- **THEN** the entry is eligible to be wrapped as a `ReplayableCommand`

#### Scenario: Safe action without result is not replayable
- **GIVEN** a persisted command log entry represents a safe action
- **AND** the entry stores no result bookmark
- **WHEN** the system builds a replayable command collection
- **THEN** the entry is not wrapped as a `ReplayableCommand`
- **AND** the command log entry remains persisted

#### Scenario: Safe action with multi-result list is not replayable
- **GIVEN** a persisted command log entry represents a safe action that returned a list with more than one element
- **AND** the entry stores no single result bookmark
- **WHEN** the system builds a replayable command collection
- **THEN** the entry is not wrapped as a `ReplayableCommand`
- **AND** the command log entry remains persisted
