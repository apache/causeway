## ADDED Requirements

### Requirement: Optional HTMX application viewer
The system SHALL provide an opt-in generic Causeway viewer that uses HTMX for application-shell navigation, history, page-region loading, and fragment transitions while using semantic web components for application and domain behavior.

#### Scenario: Viewer is not enabled
- **WHEN** an application does not include the generic HTMX viewer module
- **THEN** existing GraphQL and other Causeway viewers continue without the HTMX viewer routes or assets

#### Scenario: Viewer is enabled
- **WHEN** an application includes the generic HTMX viewer module
- **THEN** its shell, menu region, object routes, assets, and default theme are available
- **AND** underlying semantic components retain framework-neutral public contracts

### Requirement: Semantic menu-bar shell
The viewer SHALL place `<causeway-menubars>` in its stable application shell and SHALL delegate menu structure and service-action interaction to that component vocabulary.

#### Scenario: Shell loads
- **WHEN** the viewer initializes with application-entry capability
- **THEN** the stable shell renders semantic primary, secondary, and tertiary menu bars as available
- **AND** object-fragment navigation does not reconstruct menu semantics in HTMX

#### Scenario: Menu action returns an object
- **WHEN** a service action publishes a semantic object result
- **THEN** viewer policy may map its bookmark to a canonical object route
- **AND** the menu component itself remains route-independent

### Requirement: Canonical bookmark object routes
The viewer SHALL provide canonical object routes derived from Causeway logical type and object identifier.

#### Scenario: Direct object navigation
- **WHEN** a user opens a valid canonical object route
- **THEN** the viewer creates a fragment containing the corresponding semantic object context
- **AND** resolves its page definition

#### Scenario: Semantic navigation event
- **WHEN** a semantic component publishes an object navigation event
- **THEN** the viewer maps the target bookmark to the canonical route
- **AND** loads it through HTMX with browser history enabled

#### Scenario: Browser back and forward
- **WHEN** a user navigates object pages and uses browser back or forward
- **THEN** the viewer restores the route and corresponding page definition consistently

### Requirement: Default composite object page
The default object-page definition SHALL render `<causeway-object>` beneath one route-level object context and SHALL NOT enumerate members or parse grid resources itself.

#### Scenario: No custom page exists
- **WHEN** no exact page definition is registered for the routed logical type
- **THEN** the viewer renders `<causeway-object>` beneath the route context
- **AND** delegates member discovery, layout interpretation, fallback composition, and member runtime semantics to the component library

#### Scenario: Route fragment is generated
- **WHEN** the server returns an object fragment
- **THEN** it supplies route identity, one object context, and page-resolution infrastructure
- **AND** does not enumerate members through metamodel services

### Requirement: Per-logical-type page customization
The viewer SHALL select an application page registered for the exact logical type and otherwise select the default composite object page.

#### Scenario: Custom page exists
- **WHEN** an application registers a page definition for the routed logical type
- **THEN** the resolver renders that definition beneath the route object context

#### Scenario: Custom page uses standard components
- **WHEN** a custom page composes high-level or low-level semantic components
- **THEN** they share the route context and established GraphQL semantics

### Requirement: Shared route-level object context
Each object route SHALL provide one authoritative object context shared by its default or custom page definition.

#### Scenario: Page requirements are composed
- **WHEN** page components connect beneath the route context
- **THEN** their requirements contribute to that context's coordinated projection

#### Scenario: Navigation supersedes an old page
- **WHEN** HTMX navigates to another bookmark while old GraphQL requests remain in flight
- **THEN** superseded responses do not render into the new route

### Requirement: HTMX-independent component data plane
The viewer SHALL leave application-entry reads, schema introspection, object reads, validation, choices, autocomplete, mutations, action invocation, grid interpretation, and menu interpretation to GraphQL contexts and semantic components.

#### Scenario: Object fragment loads
- **WHEN** HTMX loads an object fragment
- **THEN** semantic components obtain domain state through GraphQL context contracts
- **AND** HTMX does not translate GraphQL JSON, construct domain operations, or parse Causeway layouts

### Requirement: Viewer interaction-result and home policy
The viewer SHALL provide replaceable handling for semantic object, collection, scalar, and void results and for the discovered home-page object or service action.

#### Scenario: Object result uses default policy
- **WHEN** default result policy receives a semantic object result
- **THEN** it may navigate to that object's canonical route according to documented policy

#### Scenario: Application overrides result handling
- **WHEN** an application registers a scoped result policy
- **THEN** it receives the semantic result without replacing action or menu components

#### Scenario: Home-page object is available
- **WHEN** application-entry metadata identifies and resolves a home-page object
- **THEN** configured viewer policy decides whether and when to route to or present it

#### Scenario: Home-page service action is available
- **WHEN** application-entry metadata identifies a home-page service action
- **THEN** configured viewer policy decides whether and when to invoke it and present its result

### Requirement: Object-page lifecycle states
The viewer SHALL provide accessible route-level loading, not-found, partial-error, terminal-error, and ready presentations while preserving component-local state where possible.

#### Scenario: Object lookup is pending
- **WHEN** a routed context is loading schema or object state
- **THEN** the viewer presents an accessible page-level loading state

#### Scenario: Object does not resolve
- **WHEN** GraphQL cannot resolve the routed bookmark
- **THEN** the viewer presents not-found or access-denied outcome without stale object content

#### Scenario: One member has a partial error
- **WHEN** the object context is ready with successful data and a member-path error
- **THEN** the overall page remains usable
- **AND** the semantic child presents its local error

### Requirement: Viewer theming and accessibility
The viewer SHALL ship a default responsive theme and accessible shell structure while permitting applications to customize semantic components and page regions.

#### Scenario: Default viewer shell
- **WHEN** an application enables the viewer without a custom theme
- **THEN** menus and object pages have labelled landmarks, visible focus, responsive structure, and keyboard operation

#### Scenario: Application theme
- **WHEN** an application supplies viewer and semantic-component style overrides
- **THEN** documented light-DOM contracts permit those styles without modifying behavior
