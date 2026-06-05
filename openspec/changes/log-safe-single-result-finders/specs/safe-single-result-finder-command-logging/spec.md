## ADDED Requirements

### Requirement: Safe action command publishing is configurable
The system SHALL provide a configuration property that enables command publishing for safe action invocations.
The system SHALL leave this property disabled by default.
When the property is disabled, safe action invocations MUST retain the existing behavior and MUST NOT be command logged solely because they are safe actions.
The system SHALL apply this behavior through the command publishing facet model used by normal command logging.

#### Scenario: Safe action command publishing is disabled by default
- **WHEN** an application does not configure safe action command publishing
- **AND** a user invokes a safe action
- **THEN** the system does not create a command log entry solely for that safe action invocation

#### Scenario: Safe action command publishing is enabled by property
- **GIVEN** safe action command publishing is configured as enabled
- **WHEN** a user invokes a safe action
- **THEN** the system creates a command log entry for that safe action invocation through the normal command publishing flow

### Requirement: Logged safe action entries include command DTO and available result bookmark
When safe action command publishing is enabled, the system SHALL store a command DTO for each eligible safe action invocation.
When the safe action returns a bookmarkable domain object and the framework captures a returned object bookmark, the system SHALL store that returned object's bookmark as the command log entry result.
When the safe action result does not produce a returned object bookmark, the system SHALL still store the safe action command log entry without a result bookmark.
The command log entry SHALL be suitable for command export and command replay.

#### Scenario: Safe action returns one bookmarkable object
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action with command target and parameters that returns bookmark `demoCustomer:1`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry result is bookmark `demoCustomer:1`

#### Scenario: Safe action returns a result without a bookmark
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes a safe action whose result does not produce a returned object bookmark
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry has no result bookmark

### Requirement: Safe action command publishing does not affect state-changing action policy
When safe action command publishing is enabled, the system MUST NOT use this property to determine whether idempotent or non-idempotent actions are command logged.
Idempotent and non-idempotent actions SHALL continue to use existing command publishing behavior.

#### Scenario: State-changing action is invoked
- **GIVEN** safe action command publishing is enabled
- **WHEN** a user invokes an idempotent or non-idempotent action
- **THEN** the safe action command publishing property does not determine whether the action is command logged
- **AND** the action continues to follow existing command publishing rules

### Requirement: Explicit command publishing is not duplicated
When an action invocation is already command logged through existing command publishing rules, the system MUST NOT create a duplicate command log entry for the same invocation through safe action command publishing.

#### Scenario: Safe action is explicitly command-published
- **GIVEN** safe action command publishing is enabled
- **AND** a safe action is explicitly configured for command publishing
- **WHEN** a user invokes that action
- **THEN** the system stores at most one command log entry for that invocation
