## ADDED Requirements

### Requirement: Optional router-led Angular viewer
The project SHALL provide an explicitly installed generic Angular viewer that owns application routing and shell lifecycle while using application-authored semantic Causeway custom elements for domain behavior.

#### Scenario: Angular viewer is installed
- **WHEN** a standalone Angular application installs the viewer providers and route integration
- **THEN** canonical Causeway routes and the documented viewer shell become available
- **AND** domain state remains semantic-component-owned

#### Scenario: Angular viewer is absent
- **WHEN** an application does not install the package
- **THEN** Angular and Angular Router are not runtime requirements of the component library

### Requirement: Angular Router bookmark routes
The viewer SHALL resolve canonical public logical-type and identifier routes through Angular Router with configurable mounting beneath an application base path.

#### Scenario: Bookmark route loads
- **WHEN** a valid authorized bookmark route is opened, refreshed, or revisited through history
- **THEN** Angular Router resolves the corresponding route component
- **AND** route identity remains compatible with the other generic viewers

#### Scenario: Bookmark route fails
- **WHEN** the bookmark is malformed, absent, stale, or unauthorized
- **THEN** the route component presents a bounded accessible outcome
- **AND** reveals no domain state or authorization rule

### Requirement: Angular custom-page precedence
The Angular route resolver SHALL choose an exact-logical-type registered standalone component before the generic object page.
Application-authored custom and generic Angular templates SHALL declare their own route-level `<cw-object-context>`, and the router SHALL bind canonical identity without manufacturing that element.

#### Scenario: Angular page is registered
- **WHEN** a route resolves a logical type with a registered standalone component or lazy `loadComponent` loader
- **THEN** that page's declared object context is bound to canonical route identity

#### Scenario: No Angular page is registered
- **WHEN** no exact registration exists
- **THEN** the route renders a declarative fallback containing one object context and `<cw-object>`

#### Scenario: Generic component connects
- **WHEN** `<cw-object>` renders the route object
- **THEN** it uses effective or fallback grid behavior
- **AND** does not inspect Angular Router, dependency injection, or custom-page registrations

### Requirement: Stable Angular application shell
The application-authored Angular shell SHALL declare one stable `<cw-graphql-client>` containing `<cw-menubars>` and global viewer state outside the changing `router-outlet` region.
The viewer SHALL bind configured client properties without manufacturing the provider element.

#### Scenario: Angular route changes
- **WHEN** `router-outlet` activates a different object route
- **THEN** the declared client and menu coordination remain stable
- **AND** obsolete declared object contexts disconnect deterministically

#### Scenario: Route page has an invalid context boundary
- **WHEN** a custom or generic Angular page omits or duplicates its route-level object context
- **THEN** the viewer presents a bounded diagnostic
- **AND** does not silently create or select a context

### Requirement: Native Angular custom-element integration
The Angular viewer SHALL preserve Causeway custom-element attributes, properties, native custom events, upgrade behavior, and lifecycle without duplicating domain state in Angular services, forms, or signals.

#### Scenario: Causeway tags are compiled
- **WHEN** a viewer shell or route template contains Causeway custom elements
- **THEN** documented `CUSTOM_ELEMENTS_SCHEMA` configuration accepts those tags
- **AND** does not require an Angular wrapper component for each semantic element

#### Scenario: Semantic event is published
- **WHEN** a Causeway component emits navigation, interaction, or result semantics
- **THEN** the Angular adapter forwards it to replaceable viewer policy
- **AND** does not alter the component's GraphQL operation

### Requirement: Angular home and result policy
The viewer SHALL provide replaceable Angular policy for configured home entries and scalar, object, collection, and void results.

#### Scenario: Object result uses default policy
- **WHEN** an interaction produces an object result
- **THEN** default policy navigates to its canonical Angular Router route

#### Scenario: Application replaces policy
- **WHEN** an application supplies its own home or result handler
- **THEN** the handler receives semantic data without replacing component interaction behavior

### Requirement: Angular route lifecycle accessibility
The viewer SHALL present accessible loading, ready, not-found, access-denied, partial-error, and terminal-error states and manage focus after route navigation.

#### Scenario: Lazy custom page is superseded
- **WHEN** navigation changes before a lazy route component resolves
- **THEN** the obsolete page is not activated
- **AND** focus and announcements belong to the current route

### Requirement: Initial client-rendered Angular scope
The first generic Angular viewer SHALL document client rendering as its supported lifecycle and SHALL NOT imply unverified Angular SSR or hydration compatibility.

#### Scenario: SSR support is requested
- **WHEN** an application requires Angular SSR, hydration, or streaming
- **THEN** the viewer reports that capability as separate compatibility work
- **AND** does not silently provide inconsistent custom-element hydration
