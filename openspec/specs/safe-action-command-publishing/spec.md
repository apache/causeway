# safe-action-command-publishing Specification

## Purpose
TBD - created by archiving change log-safe-single-result-finders. Update Purpose after archive.
## Requirements
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

### Requirement: Synthetic selector actions participate in safe action command publishing
When command-log recording support is `ENABLED`, the system SHALL treat synthesized parented collection selector actions as safe actions for command publishing purposes.
The system SHALL expose command publishing facet metadata on synthetic parented collection selector actions using the normal action command publishing facet model.
When command-log recording support is `DISABLED`, invoking a synthetic selector action MUST NOT create a command log entry solely because it is synthetic.
When command-log recording support is `ENABLED` and a synthetic selector action is invoked through the normal action invocation flow, the system SHALL create a command log entry through the normal command publishing flow.
The command log entry for a synthetic selector action SHALL include the command DTO for the selector action invocation.
When the synthetic selector action returns a bookmarkable child object and the framework captures a returned object bookmark, the command log entry SHALL store that returned object's bookmark as the result.
Types that implement the command recording suppression marker interface MUST NOT receive synthetic selector actions, so they have no synthetic selector command publishing metadata to expose.

#### Scenario: Selector action exposes command publishing metadata
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a parented collection owner type does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes a selector action for that owner type
- **THEN** the synthetic selector action exposes command publishing facet metadata through the normal action facet model

#### Scenario: Disabled recording support does not log selector action
- **GIVEN** command-log recording support is `DISABLED`
- **WHEN** a synthetic selector action is invoked
- **THEN** the system does not create a command log entry solely for that invocation

#### Scenario: Enabled recording support logs selector action
- **GIVEN** command-log recording support is `ENABLED`
- **AND** the synthetic selector action exists for an owner type that does not implement the command recording suppression marker interface
- **WHEN** a synthetic selector action is invoked
- **THEN** the system creates a command log entry for the selector action invocation through the normal command publishing flow
- **AND** the command log entry contains the selector action command DTO

#### Scenario: Logged selector action stores returned child bookmark
- **GIVEN** command-log recording support is `ENABLED`
- **AND** the synthetic selector action exists for an owner type that does not implement the command recording suppression marker interface
- **AND** a synthetic selector action returns bookmark `demoChild:1`
- **WHEN** the synthetic selector action is invoked through the normal action invocation flow
- **THEN** the system creates a command log entry for the selector action invocation
- **AND** the command log entry result is bookmark `demoChild:1`

#### Scenario: Marked owner type has no selector command publishing metadata
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a parented collection owner type implements the command recording suppression marker interface
- **WHEN** the framework fully introspects the owner type
- **THEN** the owner type has no synthetic selector action for that collection
- **AND** the system exposes no selector command publishing metadata for that collection

### Requirement: Logged navigation actions establish known export targets
When command-log recording support is `ENABLED`, logged safe actions that return bookmarkable domain objects SHALL make those result bookmarks available as known targets for later command export validation.
Synthetic parented collection navigate-to actions SHALL make their returned child bookmark available as a known export target when their command log entry stores that bookmark as the result.
Synthetic scalar reference navigate-to actions SHALL make their returned referenced-object bookmark available as a known export target when their command log entry stores that bookmark as the result.
Safe actions that do not store a result bookmark MUST NOT establish a known target for later exported actions.

#### Scenario: Finder safe action establishes a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a logged safe finder action returns bookmark `demoCustomer:1`
- **WHEN** a later selected export action targets bookmark `demoCustomer:1`
- **THEN** the later target is known from the safe action result if the safe action is within the export manager baseline

#### Scenario: Parented collection navigate-to action establishes a child export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a synthetic parented collection navigate-to command returns bookmark `demoLine:1`
- **WHEN** a later selected export action targets bookmark `demoLine:1`
- **THEN** the later target is known from the synthetic navigate-to result if the navigate-to command is within the export manager baseline

#### Scenario: Scalar reference navigate-to action establishes a referenced export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a synthetic scalar reference navigate-to command returns bookmark `demoLease:1`
- **WHEN** a later selected export action targets bookmark `demoLease:1`
- **THEN** the later target is known from the synthetic navigate-to result if the navigate-to command is within the export manager baseline

#### Scenario: Safe action without result does not establish a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a logged safe action has no result bookmark
- **WHEN** a later selected export action targets bookmark `demoCustomer:1`
- **THEN** the safe action without a result does not make bookmark `demoCustomer:1` known
