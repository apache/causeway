## MODIFIED Requirements

### Requirement: Safe action command publishing is configurable
The system SHALL provide the `causeway.extensions.command-log.recording-support` configuration property that controls recording support behavior.
The `recording-support` property SHALL be an enum with values `ENABLED` and `DISABLED`.
The system SHALL default `recording-support` to `DISABLED`.
When `recording-support` is `DISABLED`, safe action invocations MUST retain the existing behavior and MUST NOT be command logged solely because they are safe actions.
When `recording-support` is `ENABLED`, the system SHALL enable command publishing for safe action invocations.
The system SHALL apply this behavior through the command publishing facet model used by normal command logging.
The system MUST NOT require or use a separate `causeway.extensions.command-log.safe-action-command-publishing` boolean property to enable safe action command publishing.

#### Scenario: Safe action command publishing is disabled by default
- **WHEN** an application does not configure command-log recording support
- **AND** a user invokes a safe action
- **THEN** the system does not create a command log entry solely for that safe action invocation

#### Scenario: Safe action command publishing is enabled by recording support
- **GIVEN** command-log recording support is configured as `ENABLED`
- **WHEN** a user invokes a safe action
- **THEN** the system creates a command log entry for that safe action invocation through the normal command publishing flow

### Requirement: Synthetic selector actions participate in safe action command publishing
When command-log recording support is `ENABLED`, the system SHALL treat synthetic parented collection selector actions as safe actions for command publishing purposes.
The system SHALL expose command publishing facet metadata on synthetic parented collection selector actions using the normal action command publishing facet model.
When command-log recording support is `DISABLED`, invoking a synthetic selector action MUST NOT create a command log entry solely because it is synthetic.
When command-log recording support is `ENABLED` and a synthetic selector action is invoked through the normal action invocation flow, the system SHALL create a command log entry through the normal command publishing flow.
The command log entry for a synthetic selector action SHALL include the command DTO for the selector action invocation.
When the synthetic selector action returns a bookmarkable child object and the framework captures a returned object bookmark, the command log entry SHALL store that returned object's bookmark as the result.

#### Scenario: Selector action exposes command publishing metadata
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes a selector action
- **THEN** the synthetic selector action exposes command publishing facet metadata through the normal action facet model

#### Scenario: Disabled recording support does not log selector action
- **GIVEN** command-log recording support is `DISABLED`
- **WHEN** a synthetic selector action is invoked
- **THEN** the system does not create a command log entry solely for that invocation

#### Scenario: Enabled recording support logs selector action
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** a synthetic selector action is invoked
- **THEN** the system creates a command log entry for the selector action invocation through the normal command publishing flow
- **AND** the command log entry contains the selector action command DTO

#### Scenario: Logged selector action stores returned child bookmark
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a synthetic selector action returns bookmark `demoChild:1`
- **WHEN** the synthetic selector action is invoked through the normal action invocation flow
- **THEN** the system creates a command log entry for the selector action invocation
- **AND** the command log entry result is bookmark `demoChild:1`
