## ADDED Requirements

### Requirement: Declarative context-boundary ownership
Applications SHALL be able to declare `<cw-graphql-client>` and `<cw-object-context>` directly in ordinary HTML or framework-native templates, while a host router supplies endpoint and canonical object-identity values without constructing GraphQL operations or duplicating domain state.
The component library SHALL NOT require HTMX, Vue, Svelte, Angular, or another host framework to manufacture those semantic elements imperatively.

#### Scenario: Plain HTML composition is authored
- **WHEN** application markup declares one GraphQL client containing an object context and semantic descendants
- **THEN** the descendants obtain client and object services through the established bubbling context protocols
- **AND** no host-specific wrapper is required

#### Scenario: Framework route values are bound
- **WHEN** a host router binds a GraphQL endpoint, logical type, and object identifier to already-declared context elements
- **THEN** the elements load the same authoritative object state as equivalent static attributes
- **AND** the framework does not need to mirror GraphQL object, member, validation, or interaction state

#### Scenario: Declared object context awaits identity
- **WHEN** an authored object context connects without a complete logical type and object identifier while its host prepares route bindings
- **THEN** it issues no object query
- **AND** it becomes operational when complete valid identity is supplied

#### Scenario: Route template disconnects
- **WHEN** a framework router removes an authored route template
- **THEN** its object context releases subscriptions, requirements, cancellation state, and obsolete responses according to the normal disconnect contract
- **AND** cleanup does not depend on which host router removed it
