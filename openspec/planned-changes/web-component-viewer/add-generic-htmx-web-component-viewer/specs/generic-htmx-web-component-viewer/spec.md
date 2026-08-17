## ADDED Requirements

### Requirement: Optional router-led HTMX viewer
The project SHALL provide an explicitly enabled generic HTMX viewer whose primary responsibility is application routing, shell composition, and page-fragment lifecycle over the semantic web-component library.

#### Scenario: Viewer is enabled
- **WHEN** an application includes and configures the HTMX viewer module
- **THEN** it serves the documented shell and canonical object routes
- **AND** uses semantic components for domain and layout behavior

#### Scenario: Viewer is absent
- **WHEN** an application does not include the module
- **THEN** no HTMX viewer routes or assets are required
- **AND** the framework-neutral component library remains independently usable

### Requirement: Canonical bookmark routing
The viewer SHALL map public logical type and object identifier to one documented round-trippable route grammar.

#### Scenario: Direct object route is requested
- **WHEN** a valid authorized bookmark route is loaded or refreshed
- **THEN** the router resolves the object page for that bookmark
- **AND** browser history represents the same canonical route

#### Scenario: Route is invalid or unavailable
- **WHEN** route values are malformed, absent, stale, or unauthorized
- **THEN** the viewer presents the documented bounded route state
- **AND** does not disclose object state or authorization rules

### Requirement: Router-owned custom-page resolution
The HTMX route resolver SHALL select a custom page registered for the exact logical type before using the generic object page.

#### Scenario: Custom page is registered
- **WHEN** the route resolves an object whose logical type has an application fragment or factory
- **THEN** the viewer renders that custom definition beneath the route object context

#### Scenario: No custom page is registered
- **WHEN** the route resolves an object without an exact registration
- **THEN** the viewer renders `<causeway-object>` beneath the same route object context

#### Scenario: Generic component renders
- **WHEN** `<causeway-object>` connects
- **THEN** it renders the effective or fallback object layout
- **AND** does not discover custom pages or inspect router state

### Requirement: Stable semantic application shell
The viewer SHALL keep application menu bars and global shell state outside replaceable object fragments.

#### Scenario: Object route changes
- **WHEN** HTMX replaces the route-content fragment
- **THEN** `<causeway-menubars>` remains coordinated in the stable shell
- **AND** menu state is invalidated only by its documented application-entry context

### Requirement: HTMX-independent component data plane
The HTMX viewer SHALL NOT construct GraphQL domain operations, translate GraphQL response data, or parse Causeway grid or menu resources.

#### Scenario: Page requires domain state
- **WHEN** a custom or generic page connects beneath its route context
- **THEN** semantic components obtain domain state through GraphQL context contracts
- **AND** HTMX handles only routing, shell, history, and fragment lifecycle

### Requirement: Semantic navigation and result policy
The viewer SHALL translate semantic navigation and result events through replaceable viewer policy rather than changing component contracts.

#### Scenario: Object navigation is requested
- **WHEN** a component publishes a semantic object navigation event
- **THEN** default policy requests the canonical HTMX object route

#### Scenario: Application overrides result handling
- **WHEN** an application registers a scoped handler for scalar, object, collection, or void results
- **THEN** that handler receives the semantic result without replacing component interaction behavior

#### Scenario: Home entry is available
- **WHEN** application-entry metadata resolves a home-page object or service action
- **THEN** viewer policy decides whether and when to route, invoke, or present it

### Requirement: HTMX route lifecycle accessibility
The viewer SHALL provide accessible route-level loading, ready, not-found, access-denied, partial-error, and terminal-error states.

#### Scenario: HTMX navigation completes
- **WHEN** a new route fragment replaces the active page
- **THEN** focus and announcements follow documented accessible navigation behavior
- **AND** obsolete object-context responses cannot render into the new route

### Requirement: Cross-viewer route compatibility
Canonical route meaning, custom-page precedence, and generic `<causeway-object>` fallback SHALL remain semantically compatible with the generic Vue and Svelte viewers.

#### Scenario: Viewer implementation changes
- **WHEN** the same authorized bookmark is opened in another generic viewer
- **THEN** logical route identity and custom-versus-generic resolution have the same semantic outcome
- **AND** framework-specific lifecycle mechanics remain internal to that viewer
