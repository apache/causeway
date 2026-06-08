## MODIFIED Requirements

### Requirement: Logged safe action entries include command DTO and available result bookmark
When safe action command publishing is enabled, the system SHALL store a command DTO for each eligible safe action invocation.
When the safe action returns a bookmarkable domain object and the framework captures a returned object bookmark, the system SHALL store that returned object's bookmark as the command log entry result.
When the safe action returns a list, collection, array, or other framework-supported result container containing exactly one bookmarkable domain object, the system SHALL store that contained object's bookmark as the command log entry result.
When the safe action result is empty, contains more than one object, or does not produce a returned object bookmark, the system SHALL still store the safe action command log entry without a result bookmark.
The command log entry SHALL be suitable for command export and command replay.

#### Scenario: Safe action returns one bookmarkable object
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action with command target and parameters that returns bookmark `demoCustomer:1`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry result is bookmark `demoCustomer:1`

#### Scenario: Safe action returns a singleton list containing one bookmarkable object
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action with command target and parameters that returns a list containing only object bookmark `demoCustomer:1`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry result is bookmark `demoCustomer:1`

#### Scenario: Safe action returns an empty list
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action whose result is an empty list
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark

#### Scenario: Safe action returns multiple bookmarkable objects
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action whose result contains bookmark `demoCustomer:1` and bookmark `demoCustomer:2`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark

#### Scenario: Safe action returns a result without a bookmark
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action whose result does not produce a returned object bookmark
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark
