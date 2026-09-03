## MODIFIED Requirements

### Requirement: Optional router-led HTMX viewer
The project SHALL provide an explicitly enabled generic HTMX viewer whose primary responsibility is canonical application routing, bounded declarative-template binding, HTTP integration, and route lifecycle policy over the semantic web-component library.
The viewer SHALL NOT manufacture GraphQL-client or object-context elements around application content.

#### Scenario: Viewer is enabled
- **WHEN** an application includes and imports the HTMX viewer module
- **THEN** it serves the documented declarative shell and canonical object routes beneath the configured base path
- **AND** uses application-authored semantic components for domain, interaction, menu, context, and layout behavior

#### Scenario: Viewer is absent
- **WHEN** an application does not include and import the module
- **THEN** no HTMX viewer routes or assets are required
- **AND** the framework-neutral component library remains independently usable

### Requirement: Router-owned custom-page resolution
The HTMX route resolver SHALL select one trusted custom page registered for the exact public logical type before using the generic object page, and custom definitions MAY originate from a conventional private HTML resource or the compatible Java fragment-factory SPI.
Every selected definition SHALL own its route-page and semantic object-context markup, while the router binds only validated route values and lifecycle metadata.

#### Scenario: HTML page is registered
- **WHEN** the route contains a logical type with one qualifying classpath HTML page
- **THEN** the viewer renders that application-authored definition after binding its declared route context to canonical identity
- **AND** the page receives no metamodel, persistence, authorization, or GraphQL implementation internals

#### Scenario: Java fragment factory is registered
- **WHEN** the route contains a logical type with one application fragment factory and no conflicting HTML page
- **THEN** the viewer renders the complete trusted object-page definition returned for validated route identity
- **AND** the viewer does not wrap that definition in an injected object context

#### Scenario: Duplicate pages are registered
- **WHEN** two resource pages, two factories, or one of each claim the same exact logical type
- **THEN** viewer startup fails with a bounded configuration error
- **AND** bean, resource-enumeration, and classpath ordering do not select an arbitrary page

#### Scenario: No custom page is registered
- **WHEN** the route contains a logical type without an exact HTML resource or factory registration
- **THEN** the viewer selects the declarative generic object-page template containing `<cw-object editable>`
- **AND** binds its authored route context through the same contract as a custom page

#### Scenario: Generic component renders
- **WHEN** `<cw-object>` connects inside the selected generic page
- **THEN** it renders the effective or fallback object layout
- **AND** does not discover custom pages or inspect router state

### Requirement: Convention-registered private HTML pages
The HTMX viewer SHALL discover trusted `.html` object-page resources from one documented private classpath root and SHALL register each resource by the exact public logical type represented by its filename.
It SHALL support configured `CACHED` and `RELOAD` resource-page modes, SHALL default to `CACHED`, and MUST keep the registered page set immutable between application-context startups in both modes.
Each resource SHALL declare its route-page boundary, exactly one route-level `<cw-object-context>` with documented exact route-binding tokens, and its interaction controller.

#### Scenario: Application packages an exact logical-type page
- **WHEN** an application packages `META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html`
- **THEN** viewer startup registers the complete declarative page for exact logical type `petclinic.PetOwner`
- **AND** no application Java bean, annotation, manifest, template controller, or client-side page fetch is required

#### Scenario: Page is supplied by a dependency jar
- **WHEN** one application module or dependency jar contributes a qualifying page beneath the private root
- **THEN** classpath discovery includes that page in the same immutable registry
- **AND** the resource is not exposed by the ordinary static-resource handler

#### Scenario: Cached page content is loaded
- **WHEN** a qualifying resource is decoded during startup in the default `CACHED` mode
- **THEN** it is read as bounded non-empty UTF-8 declarative HTML and retained immutably for subsequent renders
- **AND** only the documented exact route-binding tokens are eligible for escaped substitution

#### Scenario: Reload page content is initially loaded
- **WHEN** a qualifying resource is decoded during startup in configured `RELOAD` mode
- **THEN** bounded UTF-8, NUL-content, required-token, and semantic-root validation runs before registration completes
- **AND** the definition retains authority for its exact logical type without retaining startup content as a stale fallback

#### Scenario: Existing reload page is edited
- **WHEN** the content of an already-registered page resource changes on the running classpath in `RELOAD` mode
- **AND** a subsequent route render selects that page
- **THEN** the viewer opens and validates the resource again and binds its current declarative HTML
- **AND** no Spring application-context restart, classpath rescan, public page fetch, or Java fragment-factory invocation is required

#### Scenario: Reloaded content is defective
- **WHEN** an already-registered page in `RELOAD` mode becomes unreadable, oversized, empty, malformed UTF-8, contains forbidden NUL content, or violates the declarative page contract
- **THEN** the affected render fails with a bounded safe diagnostic
- **AND** the viewer does not serve cached stale content, expose an absolute resource path, add missing semantic wrappers, or silently use generic fallback

#### Scenario: Page registration changes while running
- **WHEN** a page resource is added, deleted, renamed, or changed to claim another logical type after viewer startup
- **THEN** the immutable registry does not add, remove, or rename that registration in either mode
- **AND** an application-context restart is required to re-run bounded discovery and conflict validation

#### Scenario: Invalid resource-page mode is configured
- **WHEN** application configuration supplies a resource-page mode other than `CACHED` or `RELOAD`
- **THEN** viewer startup fails through bounded configuration binding
- **AND** it does not infer behavior from the classpath launch mechanism

#### Scenario: Page registration is defective
- **WHEN** a discovered page has an invalid logical-type filename, exceeds a documented bound, is empty, contains malformed UTF-8 or forbidden NUL content, cannot be read, conflicts with another definition, omits required bindings, or duplicates its route context
- **THEN** viewer startup fails with a bounded safe diagnostic
- **AND** the viewer does not silently repair or use generic layout fallback for that defective registration

#### Scenario: Registry discovery is bounded
- **WHEN** classpath discovery reaches the documented finite page-count ceiling
- **THEN** startup rejects additional registrations deterministically
- **AND** does not allocate an unbounded page registry

### Requirement: Trusted resource-page boundary
HTML page resources SHALL remain trusted packaged application content and SHALL own their public semantic object-page hierarchy inside an application-authored stable shell, while strict response CSP and public semantic component contracts remain unchanged.

#### Scenario: Resource page renders semantic content
- **WHEN** a route selects a qualifying HTML page
- **THEN** its route page, `<cw-object-context>`, interaction controller, ordinary HTML, and other `<cw-*>` elements come from the selected declarative definition
- **AND** the viewer retains ownership only of canonical route-value binding, HTTP policy, history, announcements, and result policy

#### Scenario: Page needs application styling
- **WHEN** an authored page uses application-specific presentation
- **THEN** it uses ordinary classes, the configured same-origin application stylesheet, documented selectors, and `--causeway-*` variables
- **AND** it does not require inline event handlers, inline style attributes, new CSP relaxations, or application-facing raw `<vaadin-*>` elements

#### Scenario: Route fragment is replaced
- **WHEN** HTMX replaces a resource-authored route fragment
- **THEN** its authored route context and semantic consumers disconnect together
- **AND** cancellation, stale-result protection, focus, announcements, and canonical-history behavior remain equivalent to generic and Java-factory pages

#### Scenario: Production page is inspected
- **WHEN** diagnostics are not introduced by a separate capability
- **THEN** page selection adds no complete-member introspection, authoring assistant, remote telemetry, or page-content disclosure
- **AND** safe internal source classification remains available for later prototype-only tooling

### Requirement: Source-visible Petclinic page customization
Petclinic SHALL demonstrate application-owned HTMX presentation through private `.html` files containing complete route-level context composition, ordinary HTML, and public semantic Causeway components without application-specific page-rendering Java code.

#### Scenario: Maintainer inspects Petclinic source
- **WHEN** a maintainer opens the Petclinic application resources
- **THEN** `petclinic.HomePage.html`, `petclinic.PetOwner.html`, `petclinic.Pet.html`, and `petclinic.Visit.html` visibly contain their route page, route object context, interaction controller, and sample-specific composition
- **AND** no Petclinic `HtmxPageFragmentFactory` or equivalent Java renderer supplies or wraps page markup

#### Scenario: Petclinic page loads
- **WHEN** a canonical route selects one of the four Petclinic logical types
- **THEN** the exact HTML resource is bound and rendered with one authored route context
- **AND** properties, references, actions, collections, columns, editing, validation, invocation, results, and navigation continue to use authoritative GraphQL semantics

#### Scenario: Petclinic runs with Vaadin-default policy
- **WHEN** an authored page contains eligible reference, basic, numeric, or local-temporal editors
- **THEN** the corresponding internal toolkit family remains independently route-lazy
- **AND** unsupported shapes and failed families retain native or explicit unsupported behavior

#### Scenario: Petclinic runs with native rollback
- **WHEN** the common editor toolkit policy is explicitly native
- **THEN** the same authored HTML pages and public Causeway elements remain functional
- **AND** no Vaadin hash or asset request is introduced

### Requirement: One disposable route object context
Every custom or generic object-page definition SHALL author exactly one route-level `<cw-object-context>` for binding to canonical logical route identity.
The HTMX renderer MUST NOT create, infer, or silently repair that element.

#### Scenario: Route fragment is replaced
- **WHEN** HTMX installs a newer object fragment
- **THEN** the prior authored route context disconnects and releases obsolete requirements
- **AND** stale GraphQL or structural-resource responses cannot render into the new route

#### Scenario: Custom page composes semantic members
- **WHEN** a custom page uses properties, actions, collections, ordinary HTML, or `<cw-object>`
- **THEN** those elements consume the same nearest authored route context
- **AND** the custom page does not create a parallel domain-state channel

#### Scenario: Page omits or duplicates the route context
- **WHEN** a resource or factory page contains zero or multiple route-level object contexts
- **THEN** validation fails with a bounded diagnostic
- **AND** the viewer does not add or choose a context

#### Scenario: Route identity is bound
- **WHEN** a valid resource page is rendered for a canonical object route
- **THEN** the viewer replaces only its exact documented logical-type and object-id tokens with escaped canonical values
- **AND** no general expression, metamodel value, persistence value, or GraphQL result is interpolated

### Requirement: Stable semantic application shell
The viewer SHALL render application-authored bounded shell-template markup that keeps branding, a shared `<cw-graphql-client>`, menu bars, announcements, loading state, result presentation, and other global shell state outside replaceable route fragments.
The renderer SHALL bind documented shell configuration and structural slots without manufacturing semantic provider elements.

#### Scenario: Full page is requested
- **WHEN** an ordinary browser request loads the viewer root or an object route
- **THEN** the server binds one validated declarative shell containing the GraphQL client, `<cw-menubars>`, and route region
- **AND** installs the requested landing or object fragment through the shell's exact route-content slot

#### Scenario: HTMX fragment is requested
- **WHEN** a valid request carries `HX-Request: true`
- **THEN** the server returns only the complete authored route fragment and canonical history instruction
- **AND** does not duplicate the stable shell

#### Scenario: Object route changes
- **WHEN** HTMX replaces the route-content fragment
- **THEN** the authored `<cw-menubars>` and `<cw-graphql-client>` remain coordinated in the stable shell
- **AND** menu state is invalidated only by its documented application-entry context

#### Scenario: Shell contract is defective
- **WHEN** a configured shell omits or duplicates the shared GraphQL client, route-content slot, menu boundary, or stable result boundary
- **THEN** viewer startup fails with a bounded safe diagnostic
- **AND** no partial shell or backend-created replacement element is served

### Requirement: HTMX-independent component data plane
The HTMX viewer SHALL NOT construct GraphQL domain operations, translate GraphQL response data, parse Causeway grid or menu resources, directly access Causeway metamodel and persistence internals, or manufacture GraphQL-client and object-context elements.

#### Scenario: Page requires domain state
- **WHEN** a custom or generic authored page connects beneath its declared route context and shared shell client
- **THEN** semantic components obtain domain state through GraphQL context contracts
- **AND** HTMX handles only routing, bounded value binding, HTTP integration, history, announcements, and fragment lifecycle

#### Scenario: HTMX JavaScript is unavailable
- **WHEN** a user follows a canonical route link without HTMX enhancement
- **THEN** the server returns a complete navigable declarative shell for that route
- **AND** no domain link depends solely on a scripted click handler
