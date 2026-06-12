## MODIFIED Requirements

### Requirement: Selector actions expose parent and scalar child parameters
The synthetic selector action MUST NOT define a parameter for the current action target.
The synthetic selector action SHALL use the current action target as the parent object for collection access.
The synthetic selector action SHALL define optional parameters for eligible scalar properties of the collection element type that appear as columns of the associated parented collection.
The synthetic selector action SHALL also define optional parameters for eligible reference properties of the collection element type that appear as columns of the associated parented collection and have a bounded, choices, or autocomplete facet installed.
A reference property with a bounded referenced type SHALL be considered selectable when the bounded semantics install a non-fallback choices facet for that referenced type.
A reference property with property choices SHALL be considered selectable when a non-fallback property choices facet is installed on the child property.
A reference property with property autocomplete SHALL be considered selectable when a non-fallback property autocomplete facet is installed on the child property.
A reference property with a domain-object autocomplete referenced type SHALL be considered selectable when domain-object autocomplete semantics install a non-fallback autocomplete facet for that referenced type.
The synthetic selector action SHALL order child filter parameters according to the associated parented collection column order.
The synthetic selector action MUST NOT automatically create selector parameters for child collections.
The synthetic selector action MUST NOT automatically create selector parameters for reference properties that do not have a bounded, choices, or autocomplete facet installed.
The synthetic selector action MUST NOT create selector parameters for blob or clob child properties.
The synthetic selector action MUST NOT create selector parameters for child properties whose ids are `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, or `datanucleusVersionTimestamp`.

#### Scenario: Current target is not exposed as a parameter
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has no mandatory disabled parameter typed as `Lease`

#### Scenario: Current target supplies the parent collection owner
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework validates or invokes the selector action on a `Lease` target
- **THEN** the action accesses `items` from that `Lease` target

#### Scenario: Collection column scalar child properties become optional filters
- **GIVEN** `Lease.items` renders eligible scalar child properties as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has optional parameters corresponding to those column-backed scalar properties

#### Scenario: Collection column bounded reference child properties become optional filters
- **GIVEN** `Lease.items` renders an eligible child reference property as a collection column
- **AND** the referenced type has bounded semantics that install a choices facet
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has an optional parameter corresponding to that column-backed reference property

#### Scenario: Collection column choices reference child properties become optional filters
- **GIVEN** `Lease.items` renders an eligible child reference property as a collection column
- **AND** the child reference property has a choices facet
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has an optional parameter corresponding to that column-backed reference property

#### Scenario: Collection column autocomplete reference child properties become optional filters
- **GIVEN** `Lease.items` renders an eligible child reference property as a collection column
- **AND** the child reference property has an autocomplete facet
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has an optional parameter corresponding to that column-backed reference property

#### Scenario: Collection column domain-object autocomplete reference child properties become optional filters
- **GIVEN** `Lease.items` renders an eligible child reference property as a collection column
- **AND** the referenced type has domain-object autocomplete semantics that install an autocomplete facet
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has an optional parameter corresponding to that column-backed reference property

#### Scenario: Scalar child properties that are not collection columns are excluded from automatic filters
- **GIVEN** `LeaseItem` has an eligible scalar property that is not rendered as a column of `Lease.items`
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create an automatic parameter for that non-column scalar property

#### Scenario: Selectable reference child properties that are not collection columns are excluded from automatic filters
- **GIVEN** `LeaseItem` has a selectable reference property that is not rendered as a column of `Lease.items`
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create an automatic parameter for that non-column reference property

#### Scenario: Child filter parameters follow collection column order
- **GIVEN** `Lease.items` renders eligible scalar and selectable reference child properties as columns in a configured order
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action lists child filter parameters in the same relative order as the collection columns

#### Scenario: Non-scalar child collections are excluded from automatic filters
- **GIVEN** `LeaseItem` has a child collection that appears as a collection column
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create an automatic parameter for the child collection

#### Scenario: Unconstrained reference child properties are excluded from automatic filters
- **GIVEN** `LeaseItem` has a reference property that appears as a collection column
- **AND** the reference property has no bounded, choices, or autocomplete facet installed
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create an automatic parameter for the reference property

#### Scenario: Blob and clob child properties are excluded from automatic filters
- **GIVEN** `LeaseItem` has blob and clob properties that appear as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for the blob or clob properties

#### Scenario: Technical metadata child properties are excluded from automatic filters
- **GIVEN** `LeaseItem` has scalar or reference properties named `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` that appear as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, or `datanucleusVersionTimestamp`
