## ADDED Requirements

### Requirement: Eager type-hierarchy candidate selection

V4 SHALL select mixin candidates from its existing eager index using exact classes, superclasses and ordinary implemented interfaces before per-target mixed-in member access.
Candidates without a usable mixin facet or whose mixee type is unrelated SHALL not be selected.

#### Scenario: Exact inherited and interface candidates

- **WHEN** a target extends a base type and implements an ordinary interface
- **THEN** mixins indexed for the target, base or interface are selected
- **AND** unrelated and missing-facet candidates are excluded

#### Scenario: No applicable candidates

- **WHEN** the registry is empty or all indexed mixee types are unrelated
- **THEN** selection returns no candidates

### Requirement: No per-target member work for rejected candidates

Both action and association discovery SHALL consume the selected candidate sequence without streaming rejected candidates' members or issuing additional loader requests for them.
This SHALL NOT prohibit the existing independent eager bootstrap of all registered mixins.
Existing domain-service Object-mixin action exclusion SHALL remain effective before member access.

#### Scenario: Unrelated action and association candidates

- **WHEN** discovery runs after the eager index is constructed
- **THEN** applicable candidates can be inspected for contributions
- **AND** unrelated candidates have no member-stream access or new loader request due to that target

#### Scenario: Domain-service Object mixin

- **WHEN** action discovery evaluates an Object mixin for a managed contributing service
- **THEN** the existing exclusion rejects it before streaming its members

### Requirement: Preserved real contributions and ordering

Existing applicable mixed-in actions, properties and collections SHALL retain their identifiers, inherited/interface applicability, element types and explicit member ordering.
Command recording SHALL retain synthetic navigation for applicable collections.

#### Scenario: Real inherited and interface contributions

- **WHEN** a real subtype is bootstrapped with base/interface mixins and unrelated counterparts
- **THEN** its applicable action/property/collection contributions retain their expected identities and element types
- **AND** unrelated counterparts do not contribute

#### Scenario: Explicit ordering and navigation

- **WHEN** command recording is enabled and explicit action sequence opposes mixin class-name order
- **THEN** user-facing action ordering follows the explicit sequence
- **AND** the applicable collection retains its synthetic navigation action
