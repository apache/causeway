## MODIFIED Requirements

### Requirement: Synthetic selector actions participate in safe action command publishing
When synthetic parented collection selector action creation is enabled, the system SHALL treat synthetic parented collection selector actions as safe actions for command publishing purposes.
The system SHALL expose command publishing facet metadata on synthetic parented collection selector actions using the normal action command publishing facet model.
When safe action command publishing is disabled, invoking a synthetic selector action MUST NOT create a command log entry solely because it is synthetic.
When safe action command publishing is enabled and a synthetic selector action is invoked through the normal action invocation flow, the system SHALL create a command log entry through the normal command publishing flow.
The command log entry for a synthetic selector action SHALL include the command DTO for the selector action invocation.
When the synthetic selector action returns a bookmarkable child object and the framework captures a returned object bookmark, the command log entry SHALL store that returned object's bookmark as the result.

#### Scenario: Selector action exposes command publishing metadata
- **GIVEN** synthetic parented collection selector action creation is enabled
- **WHEN** the framework synthesizes a selector action
- **THEN** the synthetic selector action exposes command publishing facet metadata through the normal action facet model

#### Scenario: Disabled safe action publishing does not log selector action
- **GIVEN** synthetic parented collection selector action creation is enabled
- **AND** safe action command publishing is disabled
- **WHEN** a synthetic selector action is invoked
- **THEN** the system does not create a command log entry solely for that invocation

#### Scenario: Enabled safe action publishing logs selector action
- **GIVEN** synthetic parented collection selector action creation is enabled
- **AND** safe action command publishing is enabled
- **WHEN** a synthetic selector action is invoked
- **THEN** the system creates a command log entry for the selector action invocation through the normal command publishing flow
- **AND** the command log entry contains the selector action command DTO

#### Scenario: Logged selector action stores returned child bookmark
- **GIVEN** synthetic parented collection selector action creation is enabled
- **AND** safe action command publishing is enabled
- **AND** a synthetic selector action returns bookmark `demoChild:1`
- **WHEN** the synthetic selector action is invoked through the normal action invocation flow
- **THEN** the system creates a command log entry for the selector action invocation
- **AND** the command log entry result is bookmark `demoChild:1`
