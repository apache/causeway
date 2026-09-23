## ADDED Requirements

### Requirement: Type-level applicability prefilter

The framework SHALL determine whether a registered mixin applies to a target specification after type introspection and before requesting full member introspection of that mixin.
The prefilter SHALL use the mixin's authoritative `MixinFacet` semantics.

#### Scenario: Candidate mixin does not apply

- **WHEN** the framework evaluates a registered mixin whose `MixinFacet` does not apply to the target type
- **THEN** the framework does not request `FULLY_INTROSPECTED` for that candidate as part of mixed-in member discovery
- **THEN** the candidate contributes no member to the target specification

#### Scenario: Candidate mixin applies

- **WHEN** the framework evaluates a registered mixin whose `MixinFacet` applies to the target type
- **THEN** the framework advances the cached mixin specification to `FULLY_INTROSPECTED` before streaming its members
- **THEN** the applicable members are eligible for contribution to the target specification

#### Scenario: Candidate has no mixin facet

- **WHEN** a registered candidate has no usable `MixinFacet` after type introspection
- **THEN** the framework does not request full introspection merely to evaluate its applicability
- **THEN** the candidate contributes no member to the target specification

### Requirement: Consistent action and association prefiltering

The framework SHALL apply the same type-level applicability prefilter to mixed-in action discovery and mixed-in association discovery.

#### Scenario: Inapplicable action mixin

- **WHEN** an action mixin does not apply to the target type
- **THEN** mixed-in action discovery rejects it without full member introspection

#### Scenario: Inapplicable property or collection mixin

- **WHEN** a property or collection mixin does not apply to the target type
- **THEN** mixed-in association discovery rejects it without full member introspection

### Requirement: Preserved applicable-mixin behavior

Applicable mixins SHALL retain their existing member identity, facets, applicability, explicit ordering, and navigation-action synthesis behavior after prefiltering.

#### Scenario: Applicable mixed-in members are created

- **WHEN** applicable action, property, and collection mixins are discovered
- **THEN** the target specification contains the same mixed-in members as before prefiltering
- **THEN** each contributed member retains its existing identifier and facets

#### Scenario: Explicit member order conflicts with traversal order

- **WHEN** applicable mixins declare explicit member ordering that differs from deterministic class-name traversal order
- **THEN** the resulting members follow the explicit member ordering

#### Scenario: Parented mixed-in collection is applicable

- **WHEN** command recording is enabled and an applicable mixin contributes a parented collection
- **THEN** navigation-action synthesis still creates the corresponding synthetic navigation action

### Requirement: Avoidable nesting reduction

Mixed-in member discovery SHALL not create nested full-introspection transitions for inapplicable candidates.

#### Scenario: Target is evaluated against many inapplicable mixins

- **WHEN** a target specification is evaluated against a large deterministic sequence of inapplicable mixins
- **THEN** diagnostics show type-level applicability checks for those candidates without corresponding full-introspection requests from mixed-in action or association discovery
- **THEN** applicable candidates later in the sequence are still fully introspected and contributed
