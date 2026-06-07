## MODIFIED Requirements

### Requirement: Framework synthesizes selector actions for parented collections
The system SHALL provide the `causeway.extensions.command-log.recording-support` configuration property that controls command-log recording support behavior.
The `recording-support` property SHALL be an enum with values `ENABLED` and `DISABLED`.
The system SHALL default `recording-support` to `DISABLED`.
When `recording-support` is `ENABLED`, the system SHALL synthesize a safe metamodel `ObjectAction` for each eligible parented collection association whose owning type does not implement the command recording suppression marker interface.
When `recording-support` is `DISABLED`, the system MUST NOT synthesize parented collection selector actions.
When a parented collection association's owning type implements the command recording suppression marker interface, the system MUST NOT synthesize a parented collection selector action for that association.
The system MUST NOT require or use a separate `causeway.extensions.command-log.parented-collection-selector-actions-enabled` boolean property to enable synthetic parented collection selector action creation.
An eligible parented collection association SHALL be allowed when its owning type is an entity or view model and its element type is an entity, view model, or abstract domain type.
The synthetic action SHALL represent navigation from the collection owner to one selected collection element.
The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
The synthetic action identifier SHALL use the reserved prefix `__causeway_select_from_` followed by the associated parented collection id.
The synthetic action SHALL be distinguishable from developer-authored actions by framework metadata.
The synthetic action SHALL be associated with the parented collection through layout metadata equivalent to `@ActionLayout(associateWith=...)`.
The synthetic action SHALL have the display name `Select` through name metadata equivalent to `@ActionLayout(named="Select")`.
The synthetic action SHALL have action layout CSS class metadata equivalent to `@ActionLayout(cssClass="btn-outline-secondary")`.
The synthetic action SHALL have action layout Font Awesome metadata equivalent to `@ActionLayout(cssClassFa="hand-point-left")`.

#### Scenario: Synthetic action is not available by default
- **GIVEN** a domain object type has a parented collection of child entities
- **AND** command-log recording support is not configured
- **WHEN** the framework fully introspects the domain object type
- **THEN** the metamodel does not include a synthetic selector action for that collection

#### Scenario: Synthetic action is available when recording support is enabled for an entity-owned parented collection
- **GIVEN** an entity type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **AND** the entity type does not implement the command recording suppression marker interface
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel includes a synthetic safe action for selecting one element from that collection
- **AND** the synthetic action is associated with the collection owner type

#### Scenario: Synthetic action is available when recording support is enabled for a view-model-owned parented collection
- **GIVEN** a view model type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **AND** the view model type does not implement the command recording suppression marker interface
- **WHEN** the framework fully introspects the view model type
- **THEN** the metamodel includes a synthetic safe action for selecting one element from that collection
- **AND** the synthetic action is associated with the collection owner type

#### Scenario: Synthetic action is not added for marked entity type
- **GIVEN** an entity type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **AND** the entity type implements the command recording suppression marker interface
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel does not include a synthetic selector action for that collection

#### Scenario: Synthetic action is not added for marked view model type
- **GIVEN** a view model type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **AND** the view model type implements the command recording suppression marker interface
- **WHEN** the framework fully introspects the view model type
- **THEN** the metamodel does not include a synthetic selector action for that collection

#### Scenario: Synthetic action id is deterministic
- **GIVEN** the framework introspects the same parented collection in two application runs
- **WHEN** the synthetic selector action is created in each run
- **THEN** both actions have the same action identifier
- **AND** the identifier uses the reserved prefix `__causeway_select_from_`
- **AND** the identifier is reserved so it does not conflict with application action ids

#### Scenario: Ordinary action lists can identify synthetic actions
- **WHEN** a viewer or metamodel exporter enumerates actions for a type
- **THEN** it can determine whether a listed action is a synthetic parented collection selector action

#### Scenario: Synthetic action is associated with its parented collection
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **AND** `Lease` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has layout association metadata for collection id `items`

#### Scenario: Synthetic action display name is Select
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **AND** `Lease` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action display name is `Select`

#### Scenario: Synthetic action uses secondary button styling
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **AND** `Lease` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has CSS class metadata of `btn-outline-secondary`

#### Scenario: Synthetic action uses select navigation icon styling
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **AND** `Lease` does not implement the command recording suppression marker interface
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has Font Awesome metadata of `hand-point-left`
