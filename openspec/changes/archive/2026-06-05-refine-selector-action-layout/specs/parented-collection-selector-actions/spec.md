## MODIFIED Requirements

### Requirement: Framework synthesizes selector actions for parented collections
The system SHALL provide the disabled-by-default `causeway.extensions.command-log.parented-collection-selector-actions-enabled` configuration property that enables synthetic parented collection selector action creation.
When synthetic parented collection selector action creation is enabled, the system SHALL synthesize a safe metamodel `ObjectAction` for each eligible parented collection association.
When synthetic parented collection selector action creation is disabled, the system MUST NOT synthesize parented collection selector actions.
The synthetic action SHALL represent navigation from the collection owner to one selected collection element.
The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
The synthetic action SHALL be distinguishable from developer-authored actions by framework metadata.
The synthetic action SHALL be associated with the parented collection through layout metadata equivalent to `@ActionLayout(associateWith=...)`.
The synthetic action SHALL have the display name `Select` through name metadata equivalent to `@ActionLayout(named="Select")`.

#### Scenario: Synthetic action is not available by default
- **GIVEN** an entity type has a parented collection of child entities
- **AND** synthetic parented collection selector action creation is disabled
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel does not include a synthetic selector action for that collection

#### Scenario: Synthetic action is available when enabled for a parented collection
- **GIVEN** an entity type has a parented collection of child entities
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel includes a synthetic safe action for selecting one element from that collection
- **AND** the synthetic action is associated with the collection owner type

#### Scenario: Synthetic action id is deterministic
- **GIVEN** the framework introspects the same parented collection in two application runs
- **WHEN** the synthetic selector action is created in each run
- **THEN** both actions have the same action identifier
- **AND** the identifier is reserved so it does not conflict with application action ids

#### Scenario: Ordinary action lists can identify synthetic actions
- **WHEN** a viewer or metamodel exporter enumerates actions for a type
- **THEN** it can determine whether a listed action is a synthetic parented collection selector action

#### Scenario: Synthetic action is associated with its parented collection
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has layout association metadata for collection id `items`

#### Scenario: Synthetic action display name is Select
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action display name is `Select`
