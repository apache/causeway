## ADDED Requirements

### Requirement: Synthetic selector actions participate in safe action command publishing
The system SHALL treat synthetic parented collection selector actions as safe actions for command publishing purposes.
When safe action command publishing is disabled, invoking a synthetic selector action MUST NOT create a command log entry solely because it is synthetic.
When safe action command publishing is enabled and a synthetic selector action is invoked through the normal action invocation flow, the system SHALL create a command log entry through the normal command publishing flow.

#### Scenario: Disabled safe action publishing does not log selector action
- **GIVEN** safe action command publishing is disabled
- **WHEN** a synthetic parented collection selector action is invoked
- **THEN** the system does not create a command log entry solely for that invocation

#### Scenario: Enabled safe action publishing logs selector action
- **GIVEN** safe action command publishing is enabled
- **WHEN** a synthetic parented collection selector action is invoked
- **THEN** the system creates a command log entry for the selector action invocation through the normal command publishing flow

#### Scenario: Selector action result bookmark is captured when available
- **GIVEN** safe action command publishing is enabled
- **AND** a synthetic selector action returns a bookmarkable child object
- **WHEN** the invocation is command logged
- **THEN** the command log entry includes the returned object bookmark when the normal result handling can capture it
