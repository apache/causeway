## ADDED Requirements

### Requirement: Optional HTMX object viewer
The system SHALL provide an opt-in generic domain-object viewer that uses HTMX for application-shell navigation, history, page-region loading, and fragment transitions while using semantic web components for domain behavior.

#### Scenario: Viewer is not enabled
- **WHEN** an application does not include the generic HTMX viewer module
- **THEN** existing GraphQL and other Causeway viewers continue to operate without the HTMX viewer routes or assets

#### Scenario: Viewer is enabled
- **WHEN** an application includes the generic HTMX viewer module
- **THEN** its object routes, shell, assets, and default theme are available
- **AND** the underlying semantic components retain their framework-neutral public contracts

### Requirement: Canonical bookmark object routes
The viewer SHALL provide canonical object routes derived from a Causeway logical type name and object identifier.

#### Scenario: Direct object navigation
- **WHEN** a user opens a valid canonical object route directly
- **THEN** the viewer creates a page fragment containing the corresponding semantic object context
- **AND** resolves its page definition

#### Scenario: Semantic navigation event
- **WHEN** an object link publishes a semantic navigation event
- **THEN** the viewer maps the target bookmark to the canonical route
- **AND** loads it through HTMX with browser history enabled

#### Scenario: Browser back and forward
- **WHEN** a user navigates through object pages and then uses browser back or forward
- **THEN** the viewer restores the object route and corresponding page definition consistently

### Requirement: GraphQL-only member discovery
The generic viewer SHALL discover domain object members from the semantic schema description produced by standard GraphQL introspection and SHALL NOT require a duplicate member-list endpoint or direct metamodel access.

#### Scenario: Generic page composition
- **WHEN** no custom page is registered for a logical type
- **THEN** the generic composer enumerates that type's introspected property, action, and collection fields
- **AND** creates standard components identified by semantic member IDs

#### Scenario: Route fragment generation
- **WHEN** the server returns an object-page shell or fragment
- **THEN** it supplies route identity and component composition infrastructure
- **AND** does not enumerate members through Causeway metamodel services

### Requirement: Per-logical-type page customization
The viewer SHALL provide a page-definition resolver that selects an application page registered for the exact logical type and otherwise selects generic composition.

#### Scenario: Custom page exists
- **WHEN** an application has registered a page definition for the routed logical type
- **THEN** the resolver renders that definition beneath the route's object context

#### Scenario: No custom page exists
- **WHEN** no exact page registration exists
- **THEN** the resolver renders the generic schema-driven page

#### Scenario: Custom page uses standard components
- **WHEN** a custom page composes semantic properties, actions, and collections
- **THEN** those components share the route's object context and standard GraphQL semantics

### Requirement: Shared route-level object context
Each rendered object route SHALL provide one authoritative object context shared by its generic or custom page definition.

#### Scenario: Page requirements are composed
- **WHEN** generic or custom page components connect beneath the route context
- **THEN** their semantic requirements contribute to that context's coordinated read projection

#### Scenario: Navigation supersedes an old page
- **WHEN** HTMX navigates the page region to a different bookmark while old GraphQL requests remain in flight
- **THEN** responses belonging to the superseded context do not render into the new route

### Requirement: Causeway layout interpretation
The generic viewer SHALL use an available and usable Causeway grid resource to order and group recognized object members and SHALL provide a deterministic fallback when it cannot do so.

#### Scenario: Grid resource is available
- **WHEN** object metadata provides an accessible supported grid resource
- **THEN** the generic composer maps recognized rows, columns, groups, properties, actions, and collections into page regions

#### Scenario: Grid resource is absent or forbidden
- **WHEN** no grid resource is exposed for the object
- **THEN** the viewer renders its conventional deterministic object layout

#### Scenario: Grid contains an unsupported instruction
- **WHEN** the layout contains an instruction the viewer cannot interpret
- **THEN** the viewer records a diagnostic
- **AND** falls back for the affected region without discarding unrelated recognized layout content

### Requirement: Conventional generic object layout
The fallback generic page SHALL compose object header, action, property, and collection regions using deterministic schema-derived ordering and the standard semantic components.

#### Scenario: Generic page has all member kinds
- **WHEN** an object type exposes properties, actions, and collections and no usable grid is available
- **THEN** the page renders a header followed by deterministic action, property, and collection regions
- **AND** each member component retains its own dynamic hidden and disabled behavior

#### Scenario: Member is dynamically hidden
- **WHEN** a generically composed member is hidden for the current object and user
- **THEN** its standard component omits its visible content without requiring the page composer to rebuild the schema-derived member list

### Requirement: HTMX-independent component data plane
The generic viewer SHALL leave schema introspection, object reads, validation, choices, autocomplete, mutations, and action invocation to the GraphQL client, object contexts, and semantic domain components.

#### Scenario: Object fragment loads
- **WHEN** HTMX loads an object-page fragment
- **THEN** the fragment's semantic components obtain domain state through the GraphQL context contracts
- **AND** HTMX does not translate GraphQL JSON or construct domain operations

### Requirement: Viewer interaction-result policy
The viewer SHALL provide replaceable default handling for semantic object, collection, scalar, and void interaction results.

#### Scenario: Action returns an object
- **WHEN** the default result policy receives a semantic object result
- **THEN** it may navigate to that object's canonical route according to documented policy

#### Scenario: Application overrides result handling
- **WHEN** an application registers a result policy for a scope or result kind
- **THEN** that policy receives the semantic result without replacing the underlying action component or GraphQL command implementation

### Requirement: Object-page lifecycle states
The viewer SHALL provide accessible route-level loading, not-found, partial-error, terminal-error, and ready presentations while preserving member-local state where possible.

#### Scenario: Object lookup is pending
- **WHEN** a routed object context is loading its schema or object snapshot
- **THEN** the viewer presents an accessible page-level loading state

#### Scenario: Object does not resolve
- **WHEN** GraphQL cannot resolve the routed bookmark
- **THEN** the viewer presents its not-found or access-denied outcome without rendering a stale previous object

#### Scenario: One member has a partial error
- **WHEN** the object context is ready with successful data and a member-path error
- **THEN** the overall page remains usable
- **AND** the standard member component presents the local error

### Requirement: Viewer theming and accessibility
The viewer SHALL ship a default theme and accessible page structure while permitting applications to customize semantic component and page-region styling.

#### Scenario: Default viewer page
- **WHEN** an application enables the viewer without a custom theme
- **THEN** object pages have usable responsive structure, visible focus, labelled regions, and keyboard-operable navigation and interactions

#### Scenario: Application theme
- **WHEN** an application supplies viewer and semantic-component style overrides
- **THEN** the light-DOM component and page contracts permit those styles without modifying viewer behavior
