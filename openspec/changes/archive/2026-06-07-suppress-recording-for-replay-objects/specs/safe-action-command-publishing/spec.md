## MODIFIED Requirements

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
