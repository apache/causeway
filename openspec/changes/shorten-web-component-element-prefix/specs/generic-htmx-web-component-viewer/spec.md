## MODIFIED Requirements

### Requirement: Router-owned custom-page resolution
The HTMX route resolver SHALL select one trusted custom page registered for the exact public logical type before using the generic object page, and custom definitions MAY originate from a conventional private HTML resource or the compatible Java fragment-factory SPI.

#### Scenario: HTML page is registered
- **WHEN** the route contains a logical type with one qualifying classpath HTML page
- **THEN** the viewer renders that literal application definition beneath the route object context
- **AND** the page receives no route interpolation, metamodel, persistence, authorization, or GraphQL implementation internals

#### Scenario: Java fragment factory is registered
- **WHEN** the route contains a logical type with one application fragment factory and no conflicting HTML page
- **THEN** the viewer renders that trusted application definition beneath the route object context
- **AND** the factory receives validated route identity rather than metamodel, persistence, authorization, or GraphQL internals

#### Scenario: Duplicate pages are registered
- **WHEN** two resource pages, two factories, or one of each claim the same exact logical type
- **THEN** viewer startup fails with a bounded configuration error
- **AND** bean, resource-enumeration, and classpath ordering do not select an arbitrary page

#### Scenario: No custom page is registered
- **WHEN** the route contains a logical type without an exact HTML resource or factory registration
- **THEN** the viewer renders `<cw-object editable>` beneath the same route object context
- **AND** absence is treated as ordinary generic fallback rather than a configuration failure

#### Scenario: Generic component renders
- **WHEN** `<cw-object>` connects
- **THEN** it renders the effective or fallback object layout
- **AND** does not discover custom pages or inspect router state

### Requirement: Trusted resource-page boundary
HTML page resources SHALL remain trusted packaged application content inside the viewer-owned shell and route context while strict response CSP and public semantic component contracts remain unchanged.

#### Scenario: Resource page renders semantic content
- **WHEN** a route selects a qualifying HTML page
- **THEN** its ordinary HTML and `<cw-*>` elements render beneath the route's existing `<cw-object-context>`
- **AND** the viewer retains ownership of canonical identity, the GraphQL client, interaction controller, shell, history, announcements, and result policy

#### Scenario: Page needs application styling
- **WHEN** an authored page uses application-specific presentation
- **THEN** it uses ordinary classes, the configured same-origin application stylesheet, documented selectors, and `--causeway-*` variables
- **AND** it does not require inline event handlers, inline style attributes, new CSP relaxations, or application-facing raw `<vaadin-*>` elements

#### Scenario: Route fragment is replaced
- **WHEN** HTMX replaces a resource-authored route fragment
- **THEN** its semantic consumers disconnect with the one route context
- **AND** cancellation, stale-result protection, focus, announcements, and canonical-history behavior remain equivalent to generic and Java-factory pages

#### Scenario: Production page is inspected
- **WHEN** diagnostics are not introduced by a separate capability
- **THEN** page selection adds no complete-member introspection, authoring assistant, remote telemetry, or page-content disclosure
- **AND** safe internal source classification remains available for later prototype-only tooling

### Requirement: One disposable route object context
Every custom or generic object fragment SHALL contain exactly one route-level `<cw-object-context>` for the canonical logical route identity.

#### Scenario: Route fragment is replaced
- **WHEN** HTMX installs a newer object fragment
- **THEN** the prior route context disconnects and releases obsolete requirements
- **AND** stale GraphQL or structural-resource responses cannot render into the new route

#### Scenario: Custom page composes semantic members
- **WHEN** a custom page uses properties, actions, collections, ordinary HTML, or `<cw-object>`
- **THEN** those elements consume the same nearest route context
- **AND** the custom page does not create a parallel domain-state channel

### Requirement: Stable semantic application shell
The viewer SHALL keep application branding, menu bars, announcements, loading state, result presentation, and other global shell state outside replaceable route fragments.

#### Scenario: Full page is requested
- **WHEN** an ordinary browser request loads the viewer root or an object route
- **THEN** the server returns one complete document containing the GraphQL client, `<cw-menubars>`, and route region
- **AND** the requested landing or object fragment appears inside that region

#### Scenario: HTMX fragment is requested
- **WHEN** a valid request carries `HX-Request: true`
- **THEN** the server returns only the route fragment and canonical history instruction
- **AND** does not duplicate the stable shell

#### Scenario: Object route changes
- **WHEN** HTMX replaces the route-content fragment
- **THEN** `<cw-menubars>` remains coordinated in the stable shell
- **AND** menu state is invalidated only by its documented application-entry context

### Requirement: Executable Petclinic reference application
The project SHALL include a deterministic current-Causeway Petclinic application ported from the documented pinned Apache source, exposing HTMX and Wicket viewers over the same domain model and using private HTML resources for all application-specific HTMX object-page composition.

#### Scenario: Petclinic sample starts
- **WHEN** the documented Maven profile launches the sample
- **THEN** Pet Owners, Pets, Visits, object home, service actions, object actions, choices, defaults, validation, effective menus, retained effective grids, and fixture data are available through GraphQL and the HTMX viewer
- **AND** the Wicket viewer is available at its documented comparison path over the same state

#### Scenario: Copied source is reviewed
- **WHEN** a maintainer inspects the Petclinic sample
- **THEN** provenance identifies repository commit `16a10608129ca9ce8ae04d21df1462f4d69ac018`, copied concepts, license, omissions, and current-API porting changes
- **AND** obsolete starter, security, operational, and deployment infrastructure is not represented as current viewer behavior

#### Scenario: Petclinic HTML page is present
- **WHEN** a route addresses `petclinic.HomePage`, `petclinic.PetOwner`, `petclinic.Pet`, or `petclinic.Visit`
- **THEN** the exact convention-registered HTML resource composes ordinary HTML and semantic components beneath one route context
- **AND** no Petclinic Java page renderer or custom-page knowledge inside `<cw-object>` is required

#### Scenario: Petclinic HTML page is absent
- **WHEN** a Petclinic logical type is run without its corresponding packaged HTML resource
- **THEN** the HTMX router uses the generic `<cw-object editable>` page
- **AND** the retained effective grid and collection-column resources remain available for fallback composition

#### Scenario: Petclinic page resources are packaged
- **WHEN** the ordinary sample artifact is built and inspected
- **THEN** all four HTML pages and all retained layout fallback resources are present at their documented private locations
- **AND** no frontend package installation, JavaScript bundling, CDN retrieval, or executable Spring Boot repackaging is required for ordinary reactor packaging

### Requirement: Cross-viewer route compatibility
Canonical route meaning, custom-page precedence, and generic `<cw-object>` fallback SHALL remain semantically compatible with the generic Vue and Svelte viewers.

#### Scenario: Viewer implementation changes
- **WHEN** the same authorized bookmark is opened in another generic viewer
- **THEN** logical route identity and custom-versus-generic resolution have the same semantic outcome
- **AND** framework-specific lifecycle mechanics remain internal to that viewer
