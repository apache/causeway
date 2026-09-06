## ADDED Requirements

### Requirement: Vue unreferenced member composition parity

Application-authored Vue pages SHALL be able to use `<cw-unreferenced-properties>`, `<cw-unreferenced-collections>`, and `<cw-unreferenced-actions>` directly as native custom elements beneath a valid object-context boundary.
Vue MUST NOT wrap the elements, mirror their allocation in reactive state, derive member identity, generate controls, or own their lifecycle.

#### Scenario: Vue template declares catch-all elements

- **WHEN** a registered exact Vue page contains the three unreferenced member elements beneath its route context
- **THEN** Vue treats them as native custom elements and preserves their bounded attributes
- **AND** the existing application-owned GraphQL client, context, and interaction controller serve generated ordinary member descendants

#### Scenario: Equivalent Vue and HTMX contexts resolve

- **WHEN** equivalent Vue and HTMX pages expose the same authorized inventory, explicit claims, pending producers, and catch-all destinations
- **THEN** foundation generates equivalent property order, collection tabs, action order, empty states, and diagnostics
- **AND** host framework choice does not affect member allocation

#### Scenario: Vue route generation changes

- **WHEN** Vue changes or retires its keyed route object context while allocation or a composite claim producer is pending
- **THEN** foundation retires obsolete sinks, claims, generated descendants, and semantic requirements
- **AND** stale output cannot enter the newer route generation or result outlet

#### Scenario: Vue-hosted generated controls are operated

- **WHEN** a user edits an opted-in remaining property, changes a generated collection tab, expands a preview, or invokes a generated action
- **THEN** ordinary foundation components preserve validation, authorization, focus, invocation, navigation, and result semantics
- **AND** Vue owns no parallel member or interaction state

#### Scenario: Vue host has no remaining members

- **WHEN** all members of a catch-all kind are explicitly claimed
- **THEN** the catch-all remains inspectably empty without an empty visible container
- **AND** Vue supplies no placeholder or fallback member
