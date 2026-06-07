## ADDED Requirements

### Requirement: Framework synthesizes navigate-to actions for scalar references
The system SHALL synthesize a safe metamodel `ObjectAction` for each eligible scalar reference association whose owning type does not implement the command recording suppression marker interface when `causeway.extensions.command-log.recording-support` is `ENABLED`.
When `recording-support` is `DISABLED`, the system MUST NOT synthesize scalar reference navigate-to actions.
When a scalar reference association's owning type implements the command recording suppression marker interface, the system MUST NOT synthesize a scalar reference navigate-to action for that association.
An eligible scalar reference association SHALL be allowed when its owning type is an entity or view model and its referenced type is an entity, view model, or abstract domain type.
The synthetic action SHALL represent navigation from the current object to the object referenced by the associated scalar reference association.
The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
The synthetic action identifier SHALL use the reserved prefix `__causeway_navigate_to_` followed by the associated scalar reference association id.
The synthetic action SHALL be distinguishable from developer-authored actions by framework metadata.
The synthetic action SHALL be associated with the scalar reference association through layout metadata equivalent to `@ActionLayout(associateWith=...)`.
The synthetic action SHALL have the display name `Navigate To` through name metadata equivalent to `@ActionLayout(named="Navigate To")`.
The synthetic action SHALL have action layout CSS class metadata equivalent to `@ActionLayout(cssClass="btn-outline-secondary")`.
The synthetic action SHALL have action layout Font Awesome metadata equivalent to `@ActionLayout(cssClassFa="hand-point-left")`.
The synthetic action MUST NOT define action parameters.

#### Scenario: Reference navigation action is not available by default
- **GIVEN** a domain object type has a scalar reference to another domain object
- **AND** command-log recording support is not configured
- **WHEN** the framework fully introspects the domain object type
- **THEN** the metamodel does not include a synthetic navigate-to action for that reference

#### Scenario: Reference navigation action is available when recording support is enabled for an entity-owned reference
- **GIVEN** an entity type has a scalar reference to another entity
- **AND** command-log recording support is `ENABLED`
- **AND** the entity type does not implement the command recording suppression marker interface
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel includes a synthetic safe action for navigating to the referenced object
- **AND** the synthetic action is associated with the reference owner type

#### Scenario: Reference navigation action is available when recording support is enabled for a view-model-owned reference
- **GIVEN** a view model type has a scalar reference to an entity
- **AND** command-log recording support is `ENABLED`
- **AND** the view model type does not implement the command recording suppression marker interface
- **WHEN** the framework fully introspects the view model type
- **THEN** the metamodel includes a synthetic safe action for navigating to the referenced object
- **AND** the synthetic action is associated with the reference owner type

#### Scenario: Reference navigation action is not added for marked owner type
- **GIVEN** an entity or view model type has a scalar reference to another domain object
- **AND** command-log recording support is `ENABLED`
- **AND** the owner type implements the command recording suppression marker interface
- **WHEN** the framework fully introspects the owner type
- **THEN** the metamodel does not include a synthetic navigate-to action for that reference

#### Scenario: Reference navigation action is not added for scalar value property
- **GIVEN** an entity type has a scalar value property
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel does not include a synthetic navigate-to action for that value property

#### Scenario: Reference navigation action id is deterministic
- **GIVEN** the framework introspects the same scalar reference in two application runs
- **WHEN** the synthetic navigate-to action is created in each run
- **THEN** both actions have the same action identifier
- **AND** the identifier uses the reserved prefix `__causeway_navigate_to_`
- **AND** the identifier is reserved so it does not conflict with application action ids

#### Scenario: Ordinary action lists can identify synthetic reference navigation actions
- **WHEN** a viewer or metamodel exporter enumerates actions for a type
- **THEN** it can determine whether a listed action is a synthetic scalar reference navigate-to action

#### Scenario: Reference navigation action is associated with its reference property
- **GIVEN** an entity type `LeaseItem` has a scalar reference `lease`
- **AND** command-log recording support is `ENABLED`
- **AND** `LeaseItem` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the navigate-to action for `lease`
- **THEN** the action has layout association metadata for reference property id `lease`

#### Scenario: Reference navigation action display name is Navigate To
- **GIVEN** an entity type `LeaseItem` has a scalar reference `lease`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the navigate-to action for `lease`
- **THEN** the action display name is `Navigate To`

#### Scenario: Reference navigation action uses secondary button styling
- **GIVEN** an entity type `LeaseItem` has a scalar reference `lease`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the navigate-to action for `lease`
- **THEN** the action has CSS class metadata of `btn-outline-secondary`

#### Scenario: Reference navigation action uses select navigation icon styling
- **GIVEN** an entity type `LeaseItem` has a scalar reference `lease`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the navigate-to action for `lease`
- **THEN** the action has Font Awesome metadata of `hand-point-left`

#### Scenario: Reference navigation action has no parameters
- **GIVEN** an entity type `LeaseItem` has a scalar reference `lease`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the navigate-to action for `lease`
- **THEN** the action has no parameters

### Requirement: Reference navigation action usability follows null reference state
The synthetic scalar reference navigate-to action SHALL be disabled for a current object when the associated reference property value is `null`.
The synthetic scalar reference navigate-to action SHALL remain visible in the metamodel when disabled because the associated reference property value is `null`.
The synthetic scalar reference navigate-to action SHALL be enabled for a current object when the associated reference property value is non-null.
The disabled reason SHALL clearly indicate that there is no referenced object to navigate to.

#### Scenario: Null reference disables reference navigation action
- **GIVEN** an object has an eligible scalar reference whose value is `null`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework evaluates usability for the synthetic navigate-to action on that object
- **THEN** the navigate-to action is disabled with a clear reason

#### Scenario: Non-null reference keeps reference navigation action enabled
- **GIVEN** an object has an eligible scalar reference whose value is another domain object
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework evaluates usability for the synthetic navigate-to action on that object
- **THEN** the navigate-to action is enabled

### Requirement: Reference navigation action invocation returns referenced object
The synthetic scalar reference navigate-to action SHALL access the associated reference property from the current action target.
The synthetic scalar reference navigate-to action SHALL return the referenced object when the associated reference property value is non-null.
The synthetic scalar reference navigate-to action MUST fail clearly if invocation is attempted without prior successful usability checking and the associated reference property value is `null`.

#### Scenario: Non-null reference is returned
- **GIVEN** an object has an eligible scalar reference whose value is another domain object
- **AND** command-log recording support is `ENABLED`
- **WHEN** the synthetic navigate-to action is invoked on that object
- **THEN** the action returns the referenced object

#### Scenario: Direct invocation fails for null reference
- **GIVEN** a caller bypasses usability checking
- **AND** an object has an eligible scalar reference whose value is `null`
- **WHEN** the synthetic navigate-to action is invoked directly
- **THEN** the invocation fails with a clear no-reference error

### Requirement: Disabled reference navigation action creation preserves ordinary reference traversal
The system SHALL preserve existing reference rendering and direct link traversal behavior when synthetic scalar reference navigate-to action creation is disabled.
The system SHALL avoid adding synthetic scalar reference navigate-to actions to ordinary metamodel action lists unless the configuration property is enabled.

#### Scenario: Ordinary reference browsing is unchanged by default
- **GIVEN** a user views an object page containing a scalar reference to another object
- **AND** synthetic reference navigate-to action creation is disabled
- **WHEN** the viewer renders the reference
- **THEN** the user can still navigate by selecting the rendered reference link
- **AND** the metamodel does not include a synthetic navigate-to action for the reference

#### Scenario: Command recording can include synthetic reference navigation actions after opt-in
- **GIVEN** a command recording surface supports synthetic navigate-to actions
- **AND** synthetic reference navigate-to action creation is enabled
- **WHEN** the recorder enumerates actions for navigating a scalar reference
- **THEN** the synthetic navigate-to action is available for recording the reference traversal step
