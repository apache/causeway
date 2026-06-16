## ADDED Requirements

### Requirement: Selector action boolean filters are optional tri-valued predicates
The synthetic selector action SHALL define each generated boolean child-column filter parameter as optional even when the underlying child property is mandatory.
The generated boolean filter parameter SHALL support three caller states: unselected, explicitly `true`, and explicitly `false`.
The synthetic selector action MUST NOT apply a boolean predicate when the generated boolean filter parameter is unselected, null, unspecified, or empty.
The synthetic selector action SHALL apply an exact boolean predicate when the generated boolean filter parameter is explicitly `true` or explicitly `false`.
The synthetic selector action SHALL keep non-boolean scalar and selectable reference filter semantics unchanged.

#### Scenario: Mandatory checkbox column becomes optional generated filter
- **GIVEN** `Lease.items` renders a mandatory boolean child property as a collection column
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the action has an optional filter parameter corresponding to the mandatory boolean column
- **AND** the parameter can be left unselected by the caller

#### Scenario: Unselected boolean filter is not applied
- **GIVEN** a parented collection contains child objects with both `true` and `false` values for a mandatory boolean column
- **WHEN** the synthetic selector action validates or invokes with the generated boolean filter unselected
- **THEN** the boolean column does not constrain the matching child objects

#### Scenario: Explicit false boolean filter is applied
- **GIVEN** a parented collection contains child objects with both `true` and `false` values for a mandatory boolean column
- **WHEN** the synthetic selector action validates or invokes with the generated boolean filter explicitly set to `false`
- **THEN** only child objects whose boolean column value is `false` match that filter

#### Scenario: Explicit true boolean filter is applied
- **GIVEN** a parented collection contains child objects with both `true` and `false` values for a mandatory boolean column
- **WHEN** the synthetic selector action validates or invokes with the generated boolean filter explicitly set to `true`
- **THEN** only child objects whose boolean column value is `true` match that filter

### Requirement: Selector action parameter prompts follow parented collection column order
The synthetic selector action SHALL order generated child filter parameters by the associated parented collection column order before omitting ineligible columns.
The synthetic selector action SHALL preserve the relative order of eligible generated parameters when ineligible columns appear between them.
The synthetic selector action MUST NOT fall back to child property declaration order when explicit parented collection column order metadata is available.

#### Scenario: Eligible parameters follow configured collection column order
- **GIVEN** `Lease.items` renders eligible child columns in the order `checkbox`, `name`, and `sequence`
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the generated filter parameters appear in the order `checkbox`, `name`, and `sequence`

#### Scenario: Ineligible columns are skipped without reordering eligible parameters
- **GIVEN** `Lease.items` renders child columns in the order `name`, `attachment`, `checkbox`, and `sequence`
- **AND** `attachment` is not eligible as a generated selector filter parameter
- **WHEN** the framework synthesizes the selector action for `Lease.items`
- **THEN** the generated filter parameters appear in the order `name`, `checkbox`, and `sequence`
