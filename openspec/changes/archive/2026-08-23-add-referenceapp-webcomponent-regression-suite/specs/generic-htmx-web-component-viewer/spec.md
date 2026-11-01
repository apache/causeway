## ADDED Requirements

### Requirement: Executable Reference Application regression qualification
The generic HTMX viewer SHALL be qualified against the pinned broad Reference Application regression corpus in addition to the focused Petclinic acceptance application.
Qualification MUST preserve the public GraphQL data plane, semantic Causeway components, canonical routes, strict security boundaries, route disposal, and viewer-owned presentation policy.

#### Scenario: Reference Application HTMX runtime starts
- **WHEN** the dedicated regression launcher boots with its deterministic JPA fixture
- **THEN** rich GraphQL, HTMX, and Wicket comparison routes share one effective metamodel, security context, and persistence state
- **AND** no copied application code becomes a production dependency of the generic viewer

#### Scenario: Capability inventory is generated
- **WHEN** the HTMX viewer consumes the pinned corpus through public GraphQL introspection and operations
- **THEN** every in-scope member and value family receives a reviewed support or gap classification
- **AND** unsupported or viewer-specific features remain explicit rather than being silently omitted

#### Scenario: Reference Application browser profile runs
- **WHEN** maintainers activate the documented headless browser profile
- **THEN** representative menus, layouts, values, properties, actions, references, collections, navigation, security, accessibility, and lifecycle journeys pass their accepted classifications
- **AND** unexpected GraphQL failures, browser errors, CSP violations, external requests, stale state, focus loss, overlay leaks, or overflow fail the suite
