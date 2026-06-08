## MODIFIED Requirements

### Requirement: Logged safe action entries include command DTO and available result bookmark
When safe action command publishing is enabled, the system SHALL store a command DTO for each eligible safe action invocation.
When the safe action returns exactly one bookmarkable domain object and the framework captures a returned object bookmark, the system SHALL store that returned object's bookmark as the command log entry result.
When the safe action returns void, returns a non-bookmarkable result, or returns a list that does not produce exactly one returned object bookmark, the system SHALL still store the safe action command log entry without a result bookmark.
A safe action command log entry with a single stored result bookmark SHALL be suitable for command export and command replay.
A safe action command log entry without a stored result bookmark MUST NOT be exposed as a replayable command candidate merely because it was command logged.

#### Scenario: Safe action returns one bookmarkable object
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action with command target and parameters that returns bookmark `demoCustomer:1`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry result is bookmark `demoCustomer:1`
- **AND** the command log entry remains eligible for command export and command replay

#### Scenario: Safe action returns a result without a bookmark
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action whose result does not produce a returned object bookmark
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark
- **AND** the command log entry is not exposed as a replayable command candidate

#### Scenario: Safe action returns void
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action that returns void
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark
- **AND** the command log entry is not exposed as a replayable command candidate

#### Scenario: Safe action returns multiple objects
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action that returns a list with more than one bookmarkable object
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no single result bookmark
- **AND** the command log entry is not exposed as a replayable command candidate
