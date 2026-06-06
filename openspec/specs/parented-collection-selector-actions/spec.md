# parented-collection-selector-actions Specification

## Purpose
TBD - created by archiving change synthesize-parented-collection-actions. Update Purpose after archive.
## Requirements
### Requirement: Framework synthesizes selector actions for parented collections
The system SHALL provide the `causeway.extensions.command-log.recording-support` configuration property that controls command-log recording support behavior.
The `recording-support` property SHALL be an enum with values `ENABLED` and `DISABLED`.
The system SHALL default `recording-support` to `DISABLED`.
When `recording-support` is `ENABLED`, the system SHALL synthesize a safe metamodel `ObjectAction` for each eligible parented collection association.
When `recording-support` is `DISABLED`, the system MUST NOT synthesize parented collection selector actions.
The system MUST NOT require or use a separate `causeway.extensions.command-log.parented-collection-selector-actions-enabled` boolean property to enable synthetic parented collection selector action creation.
The synthetic action SHALL represent navigation from the collection owner to one selected collection element.
The synthetic action SHALL have a deterministic identifier that does not collide with developer-authored actions.
The synthetic action identifier SHALL use the reserved prefix `__causeway_select_from_` followed by the associated parented collection id.
The synthetic action SHALL be distinguishable from developer-authored actions by framework metadata.
The synthetic action SHALL be associated with the parented collection through layout metadata equivalent to `@ActionLayout(associateWith=...)`.
The synthetic action SHALL have the display name `Select` through name metadata equivalent to `@ActionLayout(named="Select")`.

#### Scenario: Synthetic action is not available by default
- **GIVEN** an entity type has a parented collection of child entities
- **AND** command-log recording support is not configured
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel does not include a synthetic selector action for that collection

#### Scenario: Synthetic action is available when recording support is enabled for a parented collection
- **GIVEN** an entity type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework fully introspects the entity type
- **THEN** the metamodel includes a synthetic safe action for selecting one element from that collection
- **AND** the synthetic action is associated with the collection owner type

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
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action has layout association metadata for collection id `items`

#### Scenario: Synthetic action display name is Select
- **GIVEN** an entity type `Lease` has a parented collection `items`
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework synthesizes the selector action for `items`
- **THEN** the action display name is `Select`

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

### Requirement: Selector action invocation returns exactly one child object
The synthetic selector action SHALL access the collection from the supplied parent object.
The synthetic selector action SHALL filter collection elements using the supplied scalar parameter values.
When a supplied scalar parameter value is a string, the synthetic selector action SHALL match child string property values that contain the supplied string value.
When a supplied scalar parameter value is not a string, the synthetic selector action SHALL match child property values by exact equality.
The synthetic selector action SHALL be valid only when the supplied parent and scalar parameter values identify exactly one matching child object.
The synthetic selector action MUST prevent invocation with a clear no-match validation reason when no child matches.
The synthetic selector action MUST prevent invocation with a clear ambiguous-match validation reason when multiple children match.
The synthetic selector action SHALL return the single matching child object when exactly one child matches.
The synthetic selector action MUST fail clearly if invocation is attempted without prior successful validation and no child matches or multiple children match.

#### Scenario: Single child is valid and selected
- **GIVEN** a `Lease` has several `LeaseItem` children
- **AND** the selector action parameters identify exactly one `LeaseItem`
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation allows invocation
- **WHEN** the synthetic selector action is invoked
- **THEN** the action returns that `LeaseItem`

#### Scenario: Partial string filter identifies a single child
- **GIVEN** a `Lease` has several `LeaseItem` children
- **AND** exactly one child has a string property value containing the supplied string filter value
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation allows invocation
- **WHEN** the synthetic selector action is invoked
- **THEN** the action returns that child

#### Scenario: Non-string scalar filters still use exact equality
- **GIVEN** a `Lease` has several `LeaseItem` children
- **AND** a supplied non-string scalar filter value only partially resembles a child property value
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation does not treat the non-string value as a partial match

#### Scenario: No child match prevents invocation
- **GIVEN** a `Lease` has no `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation rejects the action with a clear no-match reason
- **AND** the action is not invoked

#### Scenario: Multiple child matches prevent invocation
- **GIVEN** a `Lease` has more than one `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation rejects the action with a clear ambiguous-match reason
- **AND** the action is not invoked

#### Scenario: Partial string filter with multiple matches is ambiguous
- **GIVEN** a `Lease` has more than one `LeaseItem` with string property values containing the supplied string filter value
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation rejects the action with a clear ambiguous-match reason
- **AND** the action is not invoked

#### Scenario: Direct invocation still fails for no child match
- **GIVEN** a caller bypasses validation
- **AND** a `Lease` has no `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked directly
- **THEN** the invocation fails with a clear no-match error

#### Scenario: Direct invocation still fails for multiple child matches
- **GIVEN** a caller bypasses validation
- **AND** a `Lease` has more than one `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked directly
- **THEN** the invocation fails with a clear ambiguous-match error

### Requirement: Disabled selector action creation preserves ordinary navigation
The system SHALL preserve existing collection rendering and direct row selection behavior when synthetic selector action creation is disabled.
The system SHALL avoid adding synthetic selector actions to ordinary metamodel action lists unless the configuration property is enabled.

#### Scenario: Ordinary collection browsing is unchanged by default
- **GIVEN** a user views a parent object page containing a collection
- **AND** synthetic parented collection selector action creation is disabled
- **WHEN** the viewer renders the collection
- **THEN** the user can still navigate by selecting a rendered collection row
- **AND** the metamodel does not include a synthetic selector action for the collection

#### Scenario: Command recording can include synthetic actions after opt-in
- **GIVEN** a command recording surface supports synthetic selector actions
- **AND** synthetic parented collection selector action creation is enabled
- **WHEN** the recorder enumerates actions for navigating a parented collection
- **THEN** the synthetic selector action is available for recording the parent-to-child navigation step

