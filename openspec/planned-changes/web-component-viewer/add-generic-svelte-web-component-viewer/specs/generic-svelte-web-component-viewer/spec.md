## ADDED Requirements

### Requirement: Optional router-led Svelte viewer
The project SHALL provide an explicitly installed generic SvelteKit viewer that owns application routing and shell lifecycle while using semantic Causeway custom elements for domain behavior.

#### Scenario: Svelte viewer is installed
- **WHEN** a SvelteKit application mounts the viewer routes and layout
- **THEN** canonical Causeway routes and the documented viewer shell become available
- **AND** domain state remains component-owned

#### Scenario: Svelte viewer is absent
- **WHEN** an application does not install the package
- **THEN** Svelte and SvelteKit are not runtime requirements of the component library

### Requirement: SvelteKit bookmark routes
The viewer SHALL resolve canonical public logical-type and identifier routes through thin SvelteKit route helpers with configurable application mounting.

#### Scenario: Bookmark route loads
- **WHEN** a valid authorized bookmark route is opened, refreshed, or revisited through history
- **THEN** SvelteKit resolves the corresponding route page
- **AND** route identity remains compatible with the other generic viewers

#### Scenario: Bookmark route fails
- **WHEN** the bookmark is malformed, absent, stale, or unauthorized
- **THEN** the route page presents a bounded accessible outcome
- **AND** reveals no domain state or authorization rule

### Requirement: Svelte custom-page precedence
The Svelte route resolver SHALL choose an exact-logical-type registered Svelte page before the generic object page.
Application-authored custom and generic Svelte templates SHALL declare their own route-level `<cw-object-context>`, and the router SHALL bind canonical identity without manufacturing that element.

#### Scenario: Svelte page is registered
- **WHEN** a route resolves a logical type with a registered Svelte component or lazy loader
- **THEN** that page's declared object context is bound to canonical route identity

#### Scenario: No Svelte page is registered
- **WHEN** no exact registration exists
- **THEN** the route renders a declarative fallback containing one object context and `<cw-object>`

#### Scenario: Generic component connects
- **WHEN** `<cw-object>` renders the route object
- **THEN** it uses effective or fallback grid behavior
- **AND** does not inspect SvelteKit routing or custom-page registrations

### Requirement: Stable Svelte application layout
The application-authored Svelte layout SHALL declare one stable `<cw-graphql-client>` containing `<cw-menubars>` and global viewer state outside the changing route-page slot.
The viewer SHALL bind configured client properties without manufacturing the provider element.

#### Scenario: SvelteKit route changes
- **WHEN** the active page slot changes object routes
- **THEN** the declared client and menu coordination remain stable
- **AND** obsolete declared object contexts and lazy page resolutions disconnect deterministically

#### Scenario: Route page has an invalid context boundary
- **WHEN** a custom or generic Svelte page omits or duplicates its route-level object context
- **THEN** the viewer presents a bounded diagnostic
- **AND** does not silently create or select a context

### Requirement: Deterministic custom-element upgrade
The Svelte viewer SHALL define browser registration, upgrade, readiness, cleanup, and supported server-rendering behavior for Causeway custom elements.

#### Scenario: Viewer page reaches the browser
- **WHEN** a route contains Causeway custom elements
- **THEN** interactive domain loading begins only under the documented browser registration lifecycle
- **AND** server output does not create a hydration contract the viewer cannot preserve

#### Scenario: Route is superseded during upgrade
- **WHEN** navigation changes before custom elements or a lazy page become ready
- **THEN** obsolete page state is discarded
- **AND** only the current route becomes interactive

### Requirement: Svelte semantic-event integration
The viewer SHALL forward semantic custom-element navigation and result events into replaceable SvelteKit viewer policy without reconstructing their GraphQL operations.

#### Scenario: Object navigation is published
- **WHEN** a component requests semantic object navigation
- **THEN** default policy navigates to its canonical SvelteKit route

#### Scenario: Application replaces policy
- **WHEN** an application registers its own home or result handler
- **THEN** the handler receives semantic data without replacing component interaction behavior

### Requirement: Svelte route lifecycle accessibility
The viewer SHALL present accessible loading, ready, not-found, access-denied, partial-error, and terminal-error states and manage focus after client navigation.

#### Scenario: Route navigation completes
- **WHEN** SvelteKit activates a new viewer page
- **THEN** focus and announcements follow documented accessible behavior
- **AND** obsolete route responses cannot render into the new page

### Requirement: Explicit SvelteKit scope
The first generic Svelte viewer SHALL define its supported SvelteKit client and server lifecycle and SHALL NOT imply support for arbitrary standalone Svelte routers.

#### Scenario: Standalone Svelte application requests routing
- **WHEN** an application does not use SvelteKit
- **THEN** it may still consume the framework-neutral components
- **AND** router integration remains application-owned until a separately validated adapter exists
