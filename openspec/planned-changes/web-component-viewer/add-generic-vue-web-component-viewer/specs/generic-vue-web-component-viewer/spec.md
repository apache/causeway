## ADDED Requirements

### Requirement: Optional router-led Vue viewer
The project SHALL provide an explicitly installed generic Vue viewer that owns application routing and shell lifecycle while using semantic Causeway custom elements for domain behavior.

#### Scenario: Vue viewer is installed
- **WHEN** a Vue application installs the viewer package and router integration
- **THEN** canonical Causeway routes and the documented viewer shell become available
- **AND** domain state remains component-owned

#### Scenario: Vue viewer is absent
- **WHEN** an application does not install the package
- **THEN** Vue and Vue Router are not runtime requirements of the component library

### Requirement: Vue Router bookmark routes
The viewer SHALL resolve canonical public logical-type and identifier routes through Vue Router with configurable mounting beneath an application base path.

#### Scenario: Bookmark route loads
- **WHEN** a valid authorized bookmark route is opened, refreshed, or revisited through history
- **THEN** Vue Router resolves the corresponding route page
- **AND** route identity remains compatible with the other generic viewers

#### Scenario: Bookmark route fails
- **WHEN** the bookmark is malformed, absent, stale, or unauthorized
- **THEN** the route page presents a bounded accessible outcome
- **AND** reveals no domain state or authorization rule

### Requirement: Vue custom-page precedence
The Vue route resolver SHALL choose an exact-logical-type registered Vue page before the generic object page.

#### Scenario: Vue page is registered
- **WHEN** a route resolves a logical type with a registered Vue component, async component, or accepted factory
- **THEN** that page is rendered beneath the route-level object context

#### Scenario: No Vue page is registered
- **WHEN** no exact registration exists
- **THEN** the route renders `<causeway-object>` beneath the same kind of route-level object context

#### Scenario: Generic component connects
- **WHEN** `<causeway-object>` renders the route object
- **THEN** it uses effective or fallback grid behavior
- **AND** does not inspect Vue Router or custom-page registrations

### Requirement: Stable Vue application shell
The Vue viewer SHALL keep `<causeway-menubars>` and global viewer state outside the changing route-page region.

#### Scenario: Vue route changes
- **WHEN** `RouterView` or the equivalent page region changes object routes
- **THEN** menu coordination remains stable
- **AND** obsolete object contexts disconnect deterministically

### Requirement: Native custom-element integration
The Vue viewer SHALL preserve Causeway custom-element attributes, properties, slots, native custom events, upgrade behavior, and lifecycle without duplicating their domain state in Vue.

#### Scenario: Semantic event is published
- **WHEN** a Causeway component emits navigation, interaction, or result semantics
- **THEN** the Vue viewer adapter forwards it to replaceable viewer policy
- **AND** does not alter the component's GraphQL operation

### Requirement: Vue home and result policy
The viewer SHALL provide replaceable Vue policy for configured home entries and scalar, object, collection, and void results.

#### Scenario: Object result uses default policy
- **WHEN** an interaction produces an object result
- **THEN** default policy may navigate to its canonical Vue Router route

#### Scenario: Application replaces policy
- **WHEN** an application registers its own home or result handler
- **THEN** the handler receives semantic data without replacing component interaction behavior

### Requirement: Vue route lifecycle accessibility
The viewer SHALL present accessible loading, ready, not-found, access-denied, partial-error, and terminal-error states and manage focus after route navigation.

#### Scenario: Async custom page is superseded
- **WHEN** navigation changes before an async page resolves
- **THEN** the obsolete page is not rendered
- **AND** focus and announcements belong to the current route

### Requirement: Initial client-rendered scope
The first generic Vue viewer SHALL document client rendering as its supported lifecycle and SHALL NOT imply unverified Nuxt or SSR compatibility.

#### Scenario: SSR support is requested
- **WHEN** an application requires Nuxt, SSR, or streaming hydration
- **THEN** the viewer reports that capability as separate compatibility work
- **AND** does not silently provide inconsistent custom-element hydration
