## ADDED Requirements

### Requirement: Safe single-result finder command logging is configurable
The system SHALL provide a configuration property that enables command logging for safe action invocations that return a single domain object.
The system SHALL leave this property disabled by default.
When the property is disabled, safe action invocations MUST retain the existing behavior and MUST NOT be command logged solely because they return a single domain object.

#### Scenario: Safe finder logging is disabled by default
- **WHEN** an application does not configure safe single-result finder command logging
- **AND** a user invokes a safe action that returns one domain object
- **THEN** the system does not create a command log entry solely for that safe action invocation

#### Scenario: Safe finder logging is enabled by property
- **GIVEN** safe single-result finder command logging is configured as enabled
- **WHEN** a user invokes a safe action that returns one domain object
- **THEN** the system creates a command log entry for that safe action invocation

### Requirement: Logged safe finder entries include command DTO and result bookmark
When safe single-result finder command logging is enabled, the system SHALL store a command DTO for each eligible safe finder invocation.
When the eligible safe finder returns a bookmarkable domain object, the system SHALL store that returned object's bookmark as the command log entry result.
The command log entry SHALL be suitable for command export and command replay.

#### Scenario: Safe finder returns one bookmarkable object
- **GIVEN** safe single-result finder command logging is enabled
- **WHEN** a user invokes a safe action with command target and parameters that returns bookmark `demoCustomer:1`
- **THEN** the system creates a command log entry containing a command DTO for that invocation
- **AND** the command log entry result is bookmark `demoCustomer:1`

#### Scenario: Safe finder returns null
- **GIVEN** safe single-result finder command logging is enabled
- **WHEN** a user invokes a safe action that returns null
- **THEN** the system creates no command log entry for that invocation under the safe single-result finder logging feature

### Requirement: Safe finder logging excludes non-single-domain-object results
When safe single-result finder command logging is enabled, the system MUST NOT create safe finder command log entries for safe actions whose result is a list, collection, array, void, scalar value, or otherwise not representable as exactly one returned object bookmark.
The system MUST NOT log unsafe, idempotent, or non-idempotent actions through this safe finder logging path because those actions continue to use existing command publishing behavior.

#### Scenario: Safe action returns a list
- **GIVEN** safe single-result finder command logging is enabled
- **WHEN** a user invokes a safe action that returns a list of domain objects
- **THEN** the system creates no command log entry for that invocation under the safe single-result finder logging feature

#### Scenario: Safe action returns a scalar value
- **GIVEN** safe single-result finder command logging is enabled
- **WHEN** a user invokes a safe action that returns a scalar value
- **THEN** the system creates no command log entry for that invocation under the safe single-result finder logging feature

#### Scenario: State-changing action is invoked
- **GIVEN** safe single-result finder command logging is enabled
- **WHEN** a user invokes an idempotent or non-idempotent action
- **THEN** the safe finder logging path does not determine whether the action is command logged
- **AND** the action continues to follow existing command publishing rules

### Requirement: Explicit command publishing is not duplicated
When an action invocation is already command logged through existing command publishing rules, the system MUST NOT create a duplicate command log entry for the same invocation through safe single-result finder command logging.

#### Scenario: Safe action is explicitly command-published
- **GIVEN** safe single-result finder command logging is enabled
- **AND** a safe single-result action is explicitly configured for command publishing
- **WHEN** a user invokes that action
- **THEN** the system stores at most one command log entry for that invocation
