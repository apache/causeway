## MODIFIED Requirements

### Requirement: Selector actions expose parent and scalar child parameters
The synthetic selector action SHALL define one mandatory parent object parameter.
The parent parameter SHALL be typed as the collection owner type.
The parent parameter SHALL default to the current action target.
The parent parameter SHALL be disabled so it cannot be changed by the user.
The synthetic selector action SHALL define optional parameters only for eligible scalar properties of the collection element type that appear as columns of the associated parented collection.
The synthetic selector action SHALL order child filter parameters according to the associated parented collection column order.
The synthetic selector action MUST NOT automatically create selector parameters for child collections or reference properties.
The synthetic selector action MUST NOT create selector parameters for blob or clob child properties.
The synthetic selector action MUST NOT create selector parameters for child properties whose ids are `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, or `datanucleusVersionTimestamp`.

#### Scenario: Parent parameter is mandatory
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has a mandatory parameter typed as `Lease`

#### Scenario: Parent parameter defaults to action target
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework evaluates defaults for the selector action on a `Lease` target
- **THEN** the parent parameter default is that `Lease` target

#### Scenario: Parent parameter is disabled
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the framework evaluates parameter usability for the selector action
- **THEN** the parent parameter is disabled

#### Scenario: Collection column scalar child properties become optional filters
- **GIVEN** `Lease.items` renders eligible scalar child properties as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has optional parameters corresponding to those column-backed scalar properties

#### Scenario: Scalar child properties that are not collection columns are excluded from automatic filters
- **GIVEN** `LeaseItem` has an eligible scalar property that is not rendered as a column of `Lease.items`
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create an automatic parameter for that non-column scalar property

#### Scenario: Child filter parameters follow collection column order
- **GIVEN** `Lease.items` renders eligible scalar child properties as columns in a configured order
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action lists child filter parameters in the same relative order as the collection columns

#### Scenario: Non-scalar child members are excluded from automatic filters
- **GIVEN** `LeaseItem` has a child collection and a reference property that appear as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for the child collection or reference property

#### Scenario: Blob and clob child properties are excluded from automatic filters
- **GIVEN** `LeaseItem` has blob and clob properties that appear as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for the blob or clob properties

#### Scenario: Technical metadata child properties are excluded from automatic filters
- **GIVEN** `LeaseItem` has scalar properties named `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` that appear as collection columns
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action does not create automatic parameters for `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, or `datanucleusVersionTimestamp`
