## ADDED Requirements

### Requirement: Framework synthesizes selector actions for parented collections
The system SHALL synthesize a safe metamodel `ObjectAction` for each eligible parented collection association.
The synthetic action SHALL represent navigation from the collection owner to one selected collection element.
The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
The synthetic action SHALL be distinguishable from developer-authored actions by framework metadata.

#### Scenario: Synthetic action is available for a parented collection
- **GIVEN** an entity type has a parented collection of child entities
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

### Requirement: Selector actions expose parent and scalar child parameters
The synthetic selector action SHALL define one mandatory parent object parameter.
The parent parameter SHALL be typed as the collection owner type.
The synthetic selector action SHALL define optional parameters for eligible scalar properties of the collection element type.
The synthetic selector action MUST NOT automatically create selector parameters for child collections or reference properties.

#### Scenario: Parent parameter is mandatory
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has a mandatory parameter typed as `Lease`

#### Scenario: Scalar child properties become optional filters
- **GIVEN** `LeaseItem` has scalar properties suitable for equality matching
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has optional parameters corresponding to those scalar properties

#### Scenario: Non-scalar child members are excluded from automatic filters
- **GIVEN** `LeaseItem` has a child collection and a reference property
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for the child collection or reference property

### Requirement: Selector action invocation returns exactly one child object
The synthetic selector action SHALL access the collection from the supplied parent object.
The synthetic selector action SHALL filter collection elements using the supplied scalar parameter values.
The synthetic selector action SHALL return the single matching child object when exactly one child matches.
The synthetic selector action MUST fail clearly when no child matches or when multiple children match.

#### Scenario: Single child is selected
- **GIVEN** a `Lease` has several `LeaseItem` children
- **AND** the selector action parameters identify exactly one `LeaseItem`
- **WHEN** the synthetic selector action is invoked
- **THEN** the action returns that `LeaseItem`

#### Scenario: No child matches
- **GIVEN** a `Lease` has no `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked
- **THEN** the invocation fails with a clear no-match error

#### Scenario: Multiple children match
- **GIVEN** a `Lease` has more than one `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked
- **THEN** the invocation fails with a clear ambiguous-match error

### Requirement: Synthetic selector actions are hidden from ordinary navigation
The system SHALL preserve existing collection rendering and direct row selection behavior.
The system SHALL hide synthetic selector actions from ordinary application pages unless a command recording or metamodel tooling surface explicitly includes them.

#### Scenario: Ordinary collection browsing is unchanged
- **GIVEN** a user views a parent object page containing a collection
- **WHEN** the viewer renders the collection
- **THEN** the user can still navigate by selecting a rendered collection row
- **AND** the synthetic selector action is not shown as an ordinary domain action by default

#### Scenario: Command recording can include synthetic actions
- **GIVEN** a command recording surface supports synthetic selector actions
- **WHEN** the recorder enumerates actions for navigating a parented collection
- **THEN** the synthetic selector action is available for recording the parent-to-child navigation step
