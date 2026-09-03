# generic-htmx-web-component-viewer Specification

## Purpose
TBD - created by archiving change add-generic-htmx-web-component-viewer. Update Purpose after archive.
## Requirements
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

### Requirement: Canonical bookmark routing
The viewer SHALL map a public logical type and opaque object identifier to one documented round-trippable route grammar beneath a configurable base path.
Each browser-generated and server-parsed segment MUST use the same canonical UTF-8 percent encoding and MUST remain within the documented encoded-segment bound without interpreting or altering identifier content.

#### Scenario: Direct object route is requested
- **WHEN** `<base-path>/object/<logical-type>/<identifier>` contains independently canonical percent-encoded route segments
- **THEN** the router renders the object page for that exact logical route identity
- **AND** browser history represents the same canonical route

#### Scenario: Semantic navigation is requested
- **WHEN** a component publishes an object navigation or object-result event
- **THEN** default route policy constructs the same canonical route from the exact advertised identity
- **AND** HTMX replaces only the route region while pushing that route into browser history

#### Scenario: Long authoritative identifier remains within the route bound
- **WHEN** GraphQL returns a valid opaque identifier longer than the former decoded-character limit whose canonical encoded segment remains within the documented bound
- **THEN** browser and server codecs preserve and round-trip the identifier exactly
- **AND** direct load, HTMX replacement, history restoration, back, and forward reconstruct the same object context

#### Scenario: Route encoding is invalid
- **WHEN** a route contains malformed escapes, empty values, encoded separators, control characters, dot segments, malformed Unicode, overlong encoded segments, or a non-canonical encoding
- **THEN** the viewer presents a bounded invalid-route state
- **AND** does not disclose object state, authorization rules, submitted route content, or raw decoder exceptions

#### Scenario: Viewer is mounted elsewhere
- **WHEN** an application configures a valid non-root base path
- **THEN** shell, object, asset, home, and history URLs consistently use that path
- **AND** no route assumes deployment at the origin root

### Requirement: Void action route recovery
The viewer SHALL refresh the current object route after a successful void action while the object remains available, and SHALL return to its configured home route when that post-action refresh establishes that the object no longer exists.
The fallback MUST be limited to the current post-action refresh and MUST NOT convert unrelated missing-object navigation into a home redirect.

#### Scenario: Void action retains its target
- **WHEN** a successful void action completes and the current object remains available
- **THEN** the viewer refreshes the current object route
- **AND** presents the updated object state on the same canonical route

#### Scenario: Void action deletes its target
- **WHEN** a successful void action completes and the refreshed current object reports `NOT_FOUND`
- **THEN** the viewer navigates to the configured home route
- **AND** does not leave the deleted object page in a terminal or component-error state

#### Scenario: Missing object is requested independently
- **WHEN** an object route reports `NOT_FOUND` without a current successful void-action refresh
- **THEN** the viewer retains its bounded missing-object state
- **AND** does not redirect to the home route

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
The HTMX viewer SHALL compose each full-page response from a viewer-owned bounded document scaffold and either one authoritative application-authored body shell or the built-in fallback shell.
The document scaffold SHALL own the doctype, `<html>` runtime attributes, framework-sensitive `<head>`, HTMX configuration and assets, common component assets, CSP-sensitive delivery, authentication metadata, and configured application stylesheet.
The application shell SHALL own the `<body>`, stable `<cw-graphql-client>`, branding, `<cw-menubars>` placement, authentication chrome, announcements, loading state, result presentation, route geometry, auxiliary body regions, and footer presentation.
The renderer SHALL bind documented values and structural slots without manufacturing semantic provider elements or prescribing the shell's visual hierarchy.

#### Scenario: Full page is requested
- **WHEN** an ordinary browser request loads the viewer root or an object route
- **THEN** the server binds the viewer-owned document scaffold and one validated application or fallback body shell
- **AND** installs the requested landing or object fragment through the shell's exact route-content slot

#### Scenario: Document scaffold is rendered
- **WHEN** the viewer composes a full-page response
- **THEN** language, base path, canonical path, widget-policy attributes, authentication metadata, title, context-relative framework assets, and configured application stylesheet are bound into the internal document scaffold
- **AND** application shell content cannot replace or duplicate the document root or head

#### Scenario: Application shell is rendered
- **WHEN** a valid authoritative application shell is selected
- **THEN** escaped base path, brand, and GraphQL endpoint values and validated authentication, route-content, and comparison-link structural fragments are bound only through the documented closed vocabulary
- **AND** inserted structural fragments are not reparsed for further token substitution

#### Scenario: Secured shell is rendered
- **WHEN** authentication state contributes metadata and authenticated or challenge chrome
- **THEN** metadata remains in the viewer-owned document head and chrome is installed at the application shell's exact authentication slot
- **AND** the shell cannot omit the slot or cause a second authentication control to be manufactured

#### Scenario: HTMX fragment is requested
- **WHEN** a valid request carries `HX-Request: true`
- **THEN** the server returns only the complete authored route fragment and canonical history instruction
- **AND** it does not reload or duplicate the stable document, application shell, GraphQL client, menu boundary, or result outlet

#### Scenario: Object route changes
- **WHEN** HTMX replaces the route-content fragment
- **THEN** the authored `<cw-menubars>` and `<cw-graphql-client>` remain coordinated in the stable application shell
- **AND** menu state is invalidated only by its documented application-entry context

#### Scenario: Shell layout differs from the fallback
- **WHEN** an authoritative application shell places branding, menus, results, route content, auxiliary regions, or footer differently from the built-in shell
- **THEN** direct and HTMX navigation, history, authentication, loading state, results, announcements, focus, and route-context disposal retain their established behavior
- **AND** browser bootstrap code diagnoses missing runtime landmarks without manufacturing or relocating them

#### Scenario: Shell contract is defective
- **WHEN** an authoritative application or fallback shell violates its binding, body-boundary, cardinality, containment, semantic-provider, authentication, result, loading, announcement, or route contract
- **THEN** startup or the affected reload render fails with a bounded safe diagnostic
- **AND** no partial shell, stale shell, built-in fallback for a defective application shell, or backend-created replacement element is served

### Requirement: Transient application menu dismissal
The viewer's semantic application menus SHALL close an expanded menu panel after an enabled action is selected, when focus leaves its owning menubar, and when the user dismisses it with Escape or outside activation.
Disclosure accessibility state, panel visibility, action dispatch, and focus behavior MUST remain synchronized during dismissal.

#### Scenario: Enabled menu action is selected
- **WHEN** the user activates an enabled action in an expanded application menu by pointer or keyboard
- **THEN** the containing menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** the selected semantic action request is dispatched exactly once
- **AND** the resulting prompt, result, or route transition continues according to its existing focus policy

#### Scenario: Focus remains inside the menubar
- **WHEN** keyboard or scripted focus moves between disclosures or actions within one semantic menubar
- **THEN** the active expanded menu panel remains open
- **AND** its disclosure and controlled-panel accessibility state remain synchronized

#### Scenario: Focus leaves the menubar
- **WHEN** Tab, Shift+Tab, pointer focus, or scripted focus moves from an expanded application menu to a target outside its owning semantic menubar
- **THEN** the active disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus remains on the newly selected external target
- **AND** no service action is requested

#### Scenario: Expanded menu is dismissed with Escape
- **WHEN** focus is within an expanded application menu and the user presses Escape
- **THEN** the active menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus returns to that menu's disclosure control
- **AND** no service action is requested

#### Scenario: Menu dismissal is scoped
- **WHEN** an expanded menu is dismissed after selection, focus departure, outside activation, or Escape
- **THEN** only the transient menu panel is closed
- **AND** the surrounding menu bar and stable application shell remain operable

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

### Requirement: Object-home and semantic result policy
The viewer SHALL translate the established object-home entry and semantic result events through replaceable viewer policy rather than changing component contracts.
Every newly installed landing fragment SHALL apply that object-home policy while preventing concurrent resolution for the same landing fragment.

#### Scenario: Object home is available
- **WHEN** targeted application-entry discovery returns a valid public object home
- **THEN** default home policy routes to its canonical object route
- **AND** no home service-action descriptor is inferred

#### Scenario: Landing fragment is installed after shell startup
- **WHEN** brand navigation or another canonical route transition installs a landing fragment after the stable shell has started
- **THEN** the viewer resolves the GraphQL-authoritative object home for that new landing fragment
- **AND** replaces the landing history entry with the canonical object route
- **AND** concurrent lifecycle callbacks for the same landing fragment issue no duplicate home-resolution work

#### Scenario: Home is unavailable
- **WHEN** the object home is absent, hidden, invalid, unsupported, or partially failing
- **THEN** the shell renders a bounded accessible landing state
- **AND** menus and explicit canonical routes remain usable

#### Scenario: Object result is published
- **WHEN** an interaction publishes a semantic object result without an application override
- **THEN** default policy requests its canonical route

#### Scenario: Non-object result is published
- **WHEN** an interaction publishes scalar, collection, or void semantics without an application override
- **THEN** default policy presents or announces that result in the documented shell region
- **AND** void completion refreshes the current object context only when one exists

#### Scenario: Application overrides result handling
- **WHEN** an application registers a scoped handler for a semantic result kind
- **THEN** that handler receives the semantic result and public target detail
- **AND** component interaction, validation, and mutation behavior remain unchanged

### Requirement: Accessible route lifecycle
The viewer SHALL provide accessible loading, ready, not-found, access-denied, partial-error, invalid-route, unsupported, and terminal-error states.

#### Scenario: Navigation starts
- **WHEN** a canonical HTMX request begins
- **THEN** the shell exposes a non-blocking busy state and polite loading announcement
- **AND** existing content is not made keyboard-inert before replacement succeeds

#### Scenario: Navigation completes
- **WHEN** a new route fragment replaces the active page
- **THEN** the route heading or main landmark receives documented focus
- **AND** the viewport starts at the beginning of the new route while explicit in-place refresh and browser-history restoration retain their established scroll policy
- **AND** a concise route announcement is published without trapping focus

#### Scenario: Domain object cannot be presented
- **WHEN** the route context reports absent, unauthorized, partial, or terminal GraphQL state
- **THEN** the shell maps it to a bounded non-disclosing route presentation
- **AND** stable menus and browser navigation remain operable

### Requirement: Cohesive Wicket-inspired default theme
The viewer SHALL provide an optional responsive theme that closely follows the current Wicket viewer's information hierarchy and affordances using documented semantic-component hooks and CSS variables rather than Wicket or Bootstrap markup.

#### Scenario: Wide application shell is rendered
- **WHEN** the viewport has sufficient width
- **THEN** branding, primary menus, secondary and tertiary menus, contained page content, object title, actions, cards, tabs, aligned properties, and compact tables follow the documented Wicket-inspired hierarchy
- **AND** the page has consistent spacing, alignment, boundaries, line length, and focus treatment

#### Scenario: Narrow application shell is rendered
- **WHEN** the viewport crosses the documented narrow threshold
- **THEN** navigation collapses accessibly, columns and property labels stack in meaningful order, and tables scroll within their own regions
- **AND** the page has no horizontal overflow or overlapping controls

#### Scenario: User selects a color preference
- **WHEN** light, dark, reduced-motion, or forced-colors preferences apply
- **THEN** the theme retains readable contrast, visible focus, semantic boundaries, and operable controls
- **AND** decorative motion is removed when requested

#### Scenario: Application supplies a theme
- **WHEN** an application overrides documented variables or selectors
- **THEN** it can change brand, color, spacing, typography, surfaces, borders, radii, shadows, label width, content width, and breakpoint presentation
- **AND** semantic behavior and DOM ownership remain unchanged

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

### Requirement: Wicket-relative visual acceptance
The Petclinic sample SHALL compare HTMX and current Wicket presentation by shared content hierarchy and interaction affordances rather than pixel identity.

#### Scenario: Equivalent Petclinic object is opened
- **WHEN** browser acceptance opens the same fixture object in Wicket and HTMX
- **THEN** both expose recognizable branding, menus, title, actions, groups, tabs, properties, collections, tables, prompts, disabled state, and errors in equivalent semantic order
- **AND** framework-specific markup and lifecycle remain internal to each viewer

#### Scenario: Visual acceptance runs
- **WHEN** desktop and mobile browser checks execute
- **THEN** screenshots, responsive checks, focus checks, overflow checks, console checks, and accessibility audits cover both representative object and home routes
- **AND** the HTMX routes meet the documented Lighthouse targets

### Requirement: Repaired vanilla sample presentation
The existing vanilla HTML sample SHALL use the cohesive design tokens and semantic-component styling while retaining its established executable contracts.

#### Scenario: Existing sample route is loaded
- **WHEN** `/sample-html/index.html` renders bookmark `s_sample-1`
- **THEN** existing selectors, readiness states, GraphQL endpoint, ESM registration, vanilla custom-element composition, and interaction assertions continue to pass
- **AND** domain and member state remain component-rendered

#### Scenario: Existing sample is visually verified
- **WHEN** desktop and mobile light and dark checks run
- **THEN** headings, cards, columns, menus, properties, collections, forms, prompts, results, disclosures, focus, spacing, alignment, and overflow satisfy the same documented visual quality gates
- **AND** no HTMX or Petclinic dependency is required by the sample

### Requirement: Full-project reactor integration
The existing top-level project aggregation SHALL include the web-components reactor with the other viewer reactors while preserving application-level opt-in enablement.

#### Scenario: Full project is built
- **WHEN** Maven builds the ordinary top-level released reactor
- **THEN** `viewers/webcomponents` and its non-skipped modules are compiled and tested through the established `core` aggregation
- **AND** no application enables the HTMX viewer unless it imports the viewer module configuration

### Requirement: Executable Playwright Petclinic acceptance
The Petclinic sample SHALL provide opt-in Playwright browser tests that exercise its complete exposed service-action and object-action vocabulary through the HTMX user interface.

#### Scenario: Playwright acceptance profile runs
- **WHEN** a maintainer activates the documented Petclinic Playwright profile with an available Chromium browser
- **THEN** tests exercise home and generic routes, menus, object links, history, properties, collections, validation, cancellation, every exposed service action, and every exposed object action
- **AND** mutable and destructive journeys use disposable data or restore deterministic fixture state

#### Scenario: Action interaction completes
- **WHEN** a service or object action is opened, validated, cancelled, invoked, or returns a scalar, object, collection, or void outcome
- **THEN** the UI reaches the expected prompt, result, refreshed context, or canonical route without an unexpected GraphQL or browser failure
- **AND** tests fail on unexpected console errors, failed resources, or unsuccessful GraphQL responses

#### Scenario: Interaction focus changes
- **WHEN** a prompt opens, validation fails, a prompt is cancelled, a non-navigation interaction completes, or route navigation completes
- **THEN** focus moves to the documented prompt field, invalid field, invoking control, refreshed semantic control or result region, or route landmark respectively
- **AND** focus is not lost to the document body or moved by an unrelated refresh

#### Scenario: Ordinary reactor build runs
- **WHEN** the Playwright profile is not active
- **THEN** ordinary unit and integration tests remain browser-download independent
- **AND** the documented profile remains available for explicit end-to-end verification

### Requirement: Cross-viewer route compatibility
Canonical route meaning, custom-page precedence, and generic `<cw-object>` fallback SHALL remain semantically compatible with the generic Vue and Svelte viewers.

#### Scenario: Viewer implementation changes
- **WHEN** the same authorized bookmark is opened in another generic viewer
- **THEN** logical route identity and custom-versus-generic resolution have the same semantic outcome
- **AND** framework-specific lifecycle mechanics remain internal to that viewer

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

### Requirement: Common internal editor toolkit policy
The existing `causeway.viewer.webcomponents.htmx.editor-toolkit` property SHALL remain readable with the bounded values `vaadin` and `native` as a deprecated compatibility input.
When `component-toolkit` is absent and `editor-toolkit` is explicitly configured, its value SHALL resolve the complete component policy, including qualified Grid presentation, so existing explicit native rollback remains complete.

#### Scenario: Deprecated editor policy selects Vaadin
- **WHEN** `component-toolkit` is absent and `editor-toolkit=vaadin` is explicit
- **THEN** the resolved compatibility policy enables every qualified reference, field, read-only presentation, ordinary action, and Grid adapter
- **AND** shell diagnostics identify `component-toolkit` as the replacement without changing application markup

#### Scenario: Deprecated editor policy selects native
- **WHEN** `component-toolkit` is absent and `editor-toolkit=native` is explicit
- **THEN** every qualified Vaadin adapter and style hash, including Grid, is disabled
- **AND** routes request no Vaadin closure

#### Scenario: Component policy takes precedence
- **WHEN** component and editor toolkit properties are both explicit with conflicting values
- **THEN** the component policy determines all adapters and CSP hashes
- **AND** the editor value cannot create a mixed policy

#### Scenario: Only deprecated pilot properties are configured
- **WHEN** component and editor toolkit properties are absent and either reference-widget or field-family pilot property is explicitly configured
- **THEN** the viewer preserves the former independent editor-only policy in which references default false and field families default empty unless their corresponding old value is supplied
- **AND** read-only presentation, ordinary action buttons, and Grid remain native during the compatibility period

#### Scenario: Editor toolkit value is invalid
- **WHEN** configuration supplies an editor-toolkit value other than `vaadin` or `native`
- **THEN** configuration binding rejects it with a bounded error
- **AND** the viewer does not silently select a broader policy

### Requirement: Default route-lazy toolkit delivery
The generic HTMX viewer SHALL enable every qualified packaged adapter under the resolved component policy without eagerly importing any reference, field-family, action-button, Grid, or Menu Bar closure.
A member-local closure MUST load only after an eligible connected Causeway presentation or editor selects its internal adapter, and Menu Bar MUST load only after an authenticated stable shell connects a non-empty qualified tier.

#### Scenario: Route contains one eligible read-only family
- **WHEN** the first read-only property eligible for one default field family connects
- **THEN** only that family's same-origin closure is requested and upgraded
- **AND** route readiness, other families, references, actions, Grid, and Menu Bar do not wait for it

#### Scenario: Route contains one eligible editor family
- **WHEN** the first editor eligible for one default field family connects before a read-only property from that family
- **THEN** only that family's same-origin closure is requested and upgraded
- **AND** the later read-only adapter reuses the same closure

#### Scenario: Route contains an ordinary action
- **WHEN** the first visible qualified ordinary action connects on a route without an eligible field
- **THEN** only the independently packaged action-button closure is requested
- **AND** no field-family, Grid, or Menu Bar closure is downloaded as a transitive convenience

#### Scenario: Route contains a qualified wide collection
- **WHEN** the first active wide collection satisfies Grid window, ordering, total or paging, column, renderer, and lifecycle qualification
- **THEN** only the independently packaged Grid closure is requested for collection presentation
- **AND** route readiness, fields, references, actions, Menu Bar, and GraphQL loading do not wait for unrelated closures

#### Scenario: Eligible collection is narrow
- **WHEN** an otherwise eligible collection remains at or below the documented 48rem container boundary
- **THEN** it uses native responsive presentation and does not request Grid solely for that collection
- **AND** widening may request Grid later without changing route readiness

#### Scenario: Authenticated shell contains qualified menus
- **WHEN** the first non-empty representable application-menu tier connects in an authenticated stable shell
- **THEN** one independently packaged same-origin Menu Bar closure is requested and shared across tiers and later routes
- **AND** route readiness, GraphQL state, and unrelated adapter families do not wait for it

#### Scenario: Authentication or empty-menu chrome renders
- **WHEN** login, authentication challenge, failure, logged-out, unsupported-menu, or empty-menu chrome is current
- **THEN** no Menu Bar closure is requested
- **AND** authentication remains Spring-owned and native

#### Scenario: Route contains no eligible presentation or editor
- **WHEN** a landing, menu-free, custom, narrow-collection, or other unaffected route contains no eligible reference, field presentation, editor, ordinary action, wide Grid collection, or qualified application-menu tier
- **THEN** it requests zero reference, basic, numeric, local-temporal, action-button, Grid, and Menu Bar Vaadin assets
- **AND** default CSP hash permission does not cause a network request

#### Scenario: One family fails
- **WHEN** a default field, action, Grid, or Menu Bar closure fails to load or define its controls
- **THEN** its existing Causeway failure boundary activates the matching native implementation
- **AND** other family closures remain independently eligible and lazy

### Requirement: Supported exact-hash toolkit CSP
The HTMX response CSP SHALL include only the generated reviewed style-hash union for the resolved internal component toolkit policy.
It MUST retain `style-src-attr 'none'`, same-origin script and connection sources, and no `unsafe-inline` source.

#### Scenario: Default component policy renders CSP
- **WHEN** the effective policy is the supported Vaadin default
- **THEN** `style-src` and `style-src-elem` contain the deterministic deduplicated reference, field-family, action-button, Grid, and Menu Bar hash union
- **AND** every hash corresponds to pinned generated policy metadata

#### Scenario: Deprecated editor compatibility renders CSP
- **WHEN** the component property is absent and explicit `editor-toolkit=vaadin` resolves the complete Vaadin compatibility policy
- **THEN** CSP contains the same reviewed union, including Grid and Menu Bar, as the default component policy
- **AND** shell diagnostics identify the deprecated source

#### Scenario: Deprecated pilot subset renders CSP
- **WHEN** compatibility mode enables only a subset of old editor adapters
- **THEN** CSP contains exactly the reviewed union required by that old subset
- **AND** read-only-presentation-only, action-button, Grid, and Menu Bar hashes are absent

#### Scenario: Native component policy renders CSP
- **WHEN** the effective component policy is native
- **THEN** CSP contains no Vaadin style hash, including no Grid or Menu Bar hash
- **AND** route, GraphQL, application stylesheet, canonical identity, and authentication policy remain unchanged

### Requirement: Supported default and native release qualification
The viewer SHALL treat default-Vaadin and explicit-native component modes as supported release configurations rather than sample-scoped modes.
Petclinic, the vanilla sample, the pinned Reference Application, deterministic packaging, strict CSP, accessibility, browser isolation, bundle budgets, licenses, vulnerabilities, and ordinary Maven packaging MUST remain passing gates for qualified Menu Bar, Grid, and all previously accepted adapters.

#### Scenario: Default release matrix runs
- **WHEN** release qualification runs with no toolkit override
- **THEN** eligible references, read-only fields, editors, ordinary actions, wide collections, and application-menu tiers use internal Vaadin adapters and preserve authoritative outcomes
- **AND** unexpected CSP, accessibility, console, page, external-request, stale-state, stale-item, duplicate-control, focus, order, overlay, clipping, or overflow failures fail the gate

#### Scenario: Native release matrix runs
- **WHEN** the same journeys run with `component-toolkit=native`
- **THEN** native controls, collections, and menus preserve the same GraphQL values, windows, routes, interactions, semantic events, action results, and classifications
- **AND** all Vaadin closure requests and style hashes, including Grid and Menu Bar, are absent

#### Scenario: Deprecated editor compatibility matrix runs
- **WHEN** compatibility qualification runs with explicit `editor-toolkit=vaadin` and no component property
- **THEN** authoritative outcomes and qualified Grid and Menu Bar presentation match default component mode
- **AND** bounded diagnostics identify the replacement property

#### Scenario: Authentication exclusions run
- **WHEN** login, authentication failure, challenge, logout, and authenticated-shell journeys execute under default and native modes
- **THEN** authentication chrome requests no Menu Bar assets and authenticated menu behavior preserves session and security policy
- **AND** no adapter broadens Spring-owned authentication or authorization behavior

### Requirement: Common internal component toolkit policy
The HTMX viewer SHALL expose `causeway.viewer.webcomponents.htmx.component-toolkit` with the bounded values `vaadin` and `native` and SHALL default effectively to `vaadin`.
The resolved component policy SHALL govern eligible references, editors, read-only field presentation, ordinary action buttons, qualified Grid collection presentation, qualified application Menu Bar presentation, and their CSP and asset delivery.

#### Scenario: No toolkit property is configured
- **WHEN** an application starts without the component, editor, or deprecated pilot toolkit properties
- **THEN** the resolved component policy enables every qualified Vaadin reference, field, read-only presentation, ordinary action, Grid, and Menu Bar adapter
- **AND** unsupported or excluded shapes, collections, menus, and controls retain native or explicit unsupported presentation

#### Scenario: Native component policy is explicit
- **WHEN** `component-toolkit=native` is configured
- **THEN** the shell explicitly disables every Vaadin component adapter and emits no Vaadin CSP hash
- **AND** routes request no reference, field-family, action-button, Grid, or Menu Bar closure

#### Scenario: Component policy overrides compatibility inputs
- **WHEN** `component-toolkit` and `editor-toolkit` or a deprecated pilot property are configured with conflicting values
- **THEN** the explicit component policy determines all qualified adapters and CSP hashes
- **AND** compatibility values cannot create a mixed or broadened policy

#### Scenario: Toolkit value is invalid
- **WHEN** configuration supplies a component-toolkit value other than `vaadin` or `native`
- **THEN** configuration binding rejects it with a bounded error
- **AND** the viewer does not silently select a broader policy

### Requirement: Petclinic navigable breadcrumbs demonstration
The Petclinic HTMX sample SHALL demonstrate `<cw-breadcrumbs>` through standard navigable-parent annotations and HTML-authored page composition without application-specific breadcrumb rendering or route code.
It SHALL omit breadcrumb presentation for root objects that have no navigable parents.

#### Scenario: Pet owner page is rendered
- **WHEN** the Pet Owner custom page renders for Mary Smith
- **THEN** no breadcrumb landmark or current-only breadcrumb item is rendered
- **AND** the owner header continues to identify Mary Smith

#### Scenario: Pet page is rendered
- **WHEN** Mary Smith's pet Basil is rendered
- **THEN** the breadcrumb landmark contains a Mary Smith ancestor link followed by Basil as the current item
- **AND** the hierarchy is derived from `Pet.petOwner` marked `Navigable.PARENT`

#### Scenario: Visit page is rendered
- **WHEN** Basil's scheduled visit is rendered
- **THEN** the breadcrumb landmark contains Mary Smith and Basil ancestor links in that order followed by the visit as the current item
- **AND** the hierarchy is derived through `Visit.pet` and then `Pet.petOwner`

#### Scenario: User follows a breadcrumb
- **WHEN** a user activates the Mary Smith or Basil ancestor link from a descendant route
- **THEN** the established HTMX semantic navigation bridge installs the corresponding canonical object route
- **AND** no breadcrumb-specific route handler or URL construction is required

### Requirement: Petclinic breadcrumb regression coverage
Petclinic integration and browser acceptance SHALL verify navigable annotations, rich GraphQL breadcrumb metadata, root-only omission, descendant component rendering, accessibility, responsive presentation, and canonical navigation while retaining existing custom-page behavior.

#### Scenario: Rich GraphQL hierarchy is queried
- **WHEN** integration coverage reads metadata for owner, pet, and visit fixtures
- **THEN** it observes deterministic zero-, one-, and two-ancestor breadcrumb chains

#### Scenario: Browser exercises root and descendant hierarchy
- **WHEN** browser automation opens owner, pet, and visit routes at desktop and mobile widths
- **THEN** the owner route exposes no breadcrumb landmark
- **AND** descendant breadcrumb order, current state, focusable links, no-overflow presentation, and navigation are correct
- **AND** no unexpected console, page, resource, or GraphQL failure is observed

### Requirement: Full-width Petclinic presentation
The Petclinic HTMX application SHALL override the shared bounded shell and content widths through its application stylesheet so wide routes use the available viewport while retaining the viewer's responsive gutters.
The application MUST NOT require a shared foundation, HTMX viewer, or web-component source change to obtain this presentation.

#### Scenario: Petclinic is opened on a wide viewport
- **WHEN** a Petclinic HTMX route is rendered at desktop width
- **THEN** the stable shell and route content extend across the available viewport inside the established inline gutters
- **AND** they are not capped by the shared fixed desktop width defaults

#### Scenario: Petclinic is opened on a narrow viewport
- **WHEN** the same application is rendered below its responsive breakpoints
- **THEN** existing gutters, stacking, contained collection presentation, and no-overflow behavior remain effective
- **AND** the full-width override does not introduce horizontal document overflow

### Requirement: Petclinic collection heading demonstrations
The Petclinic HTMX application SHALL selectively demonstrate canonical collection descriptions, HTML-authored collection names, and HTML-authored collection description overrides without requiring application-specific component code.

#### Scenario: Domain facet supplies a description
- **WHEN** a Petclinic collection annotated with `@CollectionLayout(describedAs)` is rendered without an HTML description override
- **THEN** its canonical description appears below the effective collection name
- **AND** collections without a description remain free of placeholder prose

#### Scenario: HTML supplies collection heading overrides
- **WHEN** a Petclinic resource page declares `named` or `described-as` on selected `<cw-collection>` elements
- **THEN** those explicit values appear for those collections only
- **AND** the same component continues using canonical metadata or fallbacks for other collections

#### Scenario: Petclinic owner visits are rendered
- **WHEN** the mixed-in owner visits collection loads
- **THEN** its selected name and description are visible and accessible
- **AND** “Cannot edit a mixed-in collection” or an equivalent collection-level tooltip is absent

### Requirement: Petclinic collection heading regression coverage
Petclinic integration and browser acceptance SHALL verify canonical and HTML-authored collection heading combinations, quiet read-only collection presentation, and unaffected collection navigation and actions.

#### Scenario: Petclinic heading combinations are exercised
- **WHEN** integration and browser tests inspect selected home and owner collections
- **THEN** names and descriptions follow documented precedence and selective application
- **AND** existing rows, links, paging, responsive behavior, add/remove/book actions, and error monitoring remain valid

### Requirement: Selective Petclinic collection paging overrides
Petclinic HTML resource pages SHALL demonstrate declarative paging with sample-appropriate bounded sizes on owner, pet, and visit collections that can grow materially.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner collection
- **THEN** its HTML override declares `paged="5"`
- **AND** it does not rely on inert `offset` or `size` attributes

#### Scenario: Owner pet collection renders
- **WHEN** an owner page composes its companion-animal collection
- **THEN** that collection declares `paged="5"`
- **AND** sorting, filtering, associated actions, semantic columns, and row peeks remain unchanged

#### Scenario: Owner visit history renders
- **WHEN** an owner page composes visit history
- **THEN** that collection declares `paged="8"`
- **AND** its associated actions, semantic columns, and row peeks remain unchanged

#### Scenario: Nested pet visits render
- **WHEN** an expanded pet row peek composes the selected pet's visit collection
- **THEN** that nested collection declares `paged="10"`
- **AND** its dedicated row context and semantic columns remain unchanged

#### Scenario: Upcoming-visit summary renders
- **WHEN** the Petclinic home page composes the clinic-wide upcoming-visit collection
- **THEN** its HTML override declares `paged="10"`
- **AND** filtering, semantic columns, and row peeks remain unchanged

#### Scenario: Browser navigates a paged collection
- **WHEN** normalized metadata reports another page
- **THEN** the application exposes accessible Causeway previous and next controls and the configured range size
- **AND** navigation does not duplicate rows, associated actions, requests, row previews, or page-level headings

### Requirement: Selective Petclinic collection sorting and filtering
Petclinic HTML resource pages SHALL demonstrate server-backed collection sorting and filtering on selected collections without changing domain membership, associated actions, or unselected collection behavior.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner list
- **THEN** its HTML override opts into sortable and filterable collection behavior
- **AND** owner filtering uses an application `CollectionFilterService` over bounded non-sensitive owner tokens

#### Scenario: Owner sorting is exercised
- **WHEN** browser automation changes the owner-name sort direction
- **THEN** the complete filtered owner result is ordered before paging
- **AND** moving between pages does not duplicate, omit, or locally reshuffle owners

#### Scenario: Owner filtering is exercised
- **WHEN** browser automation enters a bounded owner search term
- **THEN** matching owners and filtered paging metadata are returned by the authoritative GraphQL window
- **AND** clearing search restores the unfiltered owner list from offset zero

#### Scenario: Upcoming visits are filtered
- **WHEN** the Petclinic home page composes its upcoming-visit collection
- **THEN** its HTML override opts into filtering backed by a `Visit` collection filter service
- **AND** bounded search covers the visible pet name, visit time, reason, and notes tokens before window selection

#### Scenario: An owner's pets are filtered
- **WHEN** a Petclinic owner page composes its pet collection
- **THEN** its HTML override opts into filtering while retaining sortable behavior and associated actions
- **AND** bounded search covers the visible pet name, species, and notes tokens before window selection

#### Scenario: Unselected collections render
- **WHEN** Petclinic composes a collection without `sortable` or `filterable`
- **THEN** it retains established paging, ordering, Grid qualification, rows, and associated actions
- **AND** no sorting or filtering control is introduced solely by another collection's configuration

### Requirement: Same-origin Font Awesome action assets
The generic HTMX shell SHALL provide the pinned Font Awesome stylesheet and fonts from same-origin packaged WebJar resources for action and menu icon presentation.
Asset delivery MUST remain compatible with production CSP, offline operation, application styling, and browser external-request isolation.

#### Scenario: HTMX shell renders
- **WHEN** the generic shell is requested
- **THEN** it links the pinned same-origin Font Awesome stylesheet before application styles
- **AND** no CDN or external font request is introduced

#### Scenario: Font Awesome asset is requested
- **WHEN** a declared action icon causes the browser to resolve a font resource
- **THEN** the asset is served from the packaged application origin
- **AND** the response participates in established cache and security policy

### Requirement: Selective Petclinic action presentation demonstration
Petclinic HTML pages and domain actions SHALL demonstrate selected authored names, canonical descriptions, disabled tooltip sections, parameterized prompt descriptions, and left and right Font Awesome icon positions without changing unselected actions.

#### Scenario: Selected object action renders
- **WHEN** a representative Petclinic page composes a selected `<cw-action>`
- **THEN** its authored `named` value takes precedence for visible control and prompt text
- **AND** canonical tooltip and icon metadata remain domain-driven

#### Scenario: Selected service action renders
- **WHEN** a representative Petclinic service action appears in the application menu
- **THEN** its canonical description and positioned Font Awesome icon appear in native and Vaadin-backed menu presentations
- **AND** parameterized invocation uses the same effective action presentation

#### Scenario: Selected action is disabled
- **WHEN** Petclinic state disables a representative described action
- **THEN** its tooltip separates canonical description from the disabled reason
- **AND** browser acceptance verifies that the action cannot be invoked

#### Scenario: Unselected action renders
- **WHEN** a Petclinic action has no new authored name, description, or icon metadata
- **THEN** its established label, tooltip absence, and invocation behavior remain unchanged
- **AND** no default icon is fabricated

### Requirement: Application-owned Petclinic object heading actions
The Petclinic owner custom page SHALL compose its object title and object-level actions in an application-owned responsive heading row without changing semantic object-header or action component contracts.

#### Scenario: Owner page renders at wide width
- **WHEN** a Petclinic owner route renders with sufficient inline space
- **THEN** the remove-owner action appears immediately after the object title in the same heading row
- **AND** the page does not place that object-level action in a separate toolbar row below the title

#### Scenario: Owner heading has insufficient width
- **WHEN** the title and object-level action cannot fit on one line
- **THEN** the application heading row wraps in title-before-action document order
- **AND** the title and action remain readable, operable, and free of overlap or horizontal page overflow

#### Scenario: Application owns heading placement
- **WHEN** the owner page composes the title and object-level action
- **THEN** placement is defined by the Petclinic HTML resource and application stylesheet
- **AND** `<cw-object-header>` and `<cw-action>` retain their established framework-neutral rendering and semantic behavior

### Requirement: Petclinic selective action-parameter presentation
Petclinic custom HTML pages SHALL demonstrate optional `<cw-parameter>` presentation declarations on selected parameterized actions while leaving other parameters and actions canonical.

#### Scenario: Maintainer inspects Petclinic HTML
- **WHEN** a maintainer opens the packaged Petclinic custom-page resources
- **THEN** selected `<cw-action>` elements contain natural nested `<cw-parameter>` declarations using representative `named`, `described-as`, `description-as`, and `multi-line` attributes
- **AND** at least one sibling parameter and at least one other parameterized action remain undeclared

#### Scenario: Selected Petclinic action prompt opens
- **WHEN** browser automation activates an action with matching parameter declarations
- **THEN** the prompt applies the authored parameter name, description presentation, and compatible multiline hint
- **AND** canonical defaults, choices, validation, invocation, result, focus, and route behavior remain unchanged

#### Scenario: Undeclared Petclinic parameter renders
- **WHEN** the same or another Petclinic prompt contains an authoritative parameter without a matching declaration
- **THEN** that parameter retains its established canonical name, description, and qualified editor
- **AND** no HTML declaration is required for it to participate in validation or invocation

### Requirement: Petclinic action prompt-style demonstration
The Petclinic HTMX sample SHALL demonstrate `INLINE`, `DIALOG_MODAL`, and `DIALOG_SIDEBAR` parameter prompts through ordinary `<cw-action prompt-style>` declarations and standard semantic interactions.
The demonstration MUST NOT require application-specific prompt rendering, event handling, or mutation code.

#### Scenario: Property-associated inline action opens
- **WHEN** the authored owner-page action associated with the name property declares `prompt-style="INLINE"` and is activated
- **THEN** its standard parameter prompt temporarily replaces the name property composition
- **AND** cancellation restores the property value and associated action control with focus returned

#### Scenario: Modal action opens
- **WHEN** a representative authored Petclinic action declares `prompt-style="DIALOG_MODAL"` and is activated
- **THEN** its standard parameter prompt opens as a centred movable modal dialog
- **AND** pointer movement of the heading changes the bounded dialog position without changing parameter values

#### Scenario: Sidebar action opens
- **WHEN** a representative authored Petclinic action declares `prompt-style="DIALOG_SIDEBAR"` and is activated
- **THEN** its standard parameter prompt opens as a vertical panel at the viewport's inline end
- **AND** cancellation closes the panel and restores originating-action focus

#### Scenario: Petclinic prompt submits
- **WHEN** browser automation enters and submits valid values through any demonstrated style
- **THEN** the established GraphQL preparation, validation, mutation, result, refresh, and routing contracts remain authoritative
- **AND** no unexpected console, page, resource, GraphQL, focus, or overflow failure occurs

### Requirement: Petclinic prompt-style regression coverage
Petclinic integration and opt-in browser acceptance SHALL verify canonical rich GraphQL prompt-style metadata, authored override precedence, the three effective prompt surfaces, responsive behavior, and unchanged invocation semantics.

#### Scenario: Rich GraphQL action metadata is queried
- **WHEN** integration coverage reads representative object or service action metadata
- **THEN** `promptStyle` exposes the resolved canonical enum name
- **AND** existing metadata and action state remain unchanged

#### Scenario: Browser exercises styled prompts
- **WHEN** browser automation opens inline, modal, and sidebar prompts at desktop and narrow viewport widths
- **THEN** each prompt uses the declared placement, accessible semantics, focus behavior, and cancellation restoration
- **AND** modal dragging remains bounded while inline and sidebar presentation remain free of horizontal overflow

### Requirement: Faithful Petclinic owner deletion policy
The Petclinic sample SHALL retain the pinned original application's observable owner-deletion policy while demonstrating standard are-you-sure confirmation through the HTMX Web Components viewer.

#### Scenario: Owner has related visits
- **WHEN** a Petclinic owner has one or more related visits
- **THEN** Delete is disabled with the reason `This owner has N visits` using the authoritative count
- **AND** the viewer does not offer confirmation or attempt deletion

#### Scenario: Owner has no related visits
- **WHEN** a Petclinic owner has no related visits
- **THEN** Delete remains enabled with canonical are-you-sure metadata
- **AND** its description does not claim that related visits will be deleted

#### Scenario: Eligible deletion is cancelled
- **WHEN** a user activates Delete for an eligible disposable owner and declines confirmation
- **THEN** the owner remains available on its canonical route
- **AND** no delete mutation is issued

#### Scenario: Eligible deletion is confirmed
- **WHEN** a user activates Delete for an eligible disposable owner and explicitly confirms
- **THEN** the action completes through the established GraphQL mutation and void-result policy
- **AND** the deleted object route returns to the configured home route after post-action refresh establishes that the owner no longer exists

### Requirement: Petclinic destructive-action regression coverage
Petclinic integration and opt-in browser acceptance SHALL verify canonical confirmation metadata, visit-based disable behavior, cancellation, successful confirmed deletion, focus, routing, and error monitoring.

#### Scenario: Rich GraphQL delete state is queried
- **WHEN** integration coverage reads Delete for a fixture owner with visits and an eligible owner without visits
- **THEN** both expose `areYouSure` as true
- **AND** only the fixture owner exposes the visit-count disabled reason

#### Scenario: Browser exercises destructive deletion
- **WHEN** browser automation cancels one eligible deletion and confirms another
- **THEN** focus and route outcomes follow the standard interaction and void-action recovery contracts
- **AND** no unexpected console, page, resource, GraphQL, or referential-integrity failure occurs

### Requirement: Canonical self-linked object heading
Every generic or application-authored HTMX object route that renders the standard object header SHALL expose the displayed current-object title as a semantic link to the route's same canonical identity.
The viewer SHALL handle that link through its established semantic navigation policy rather than embedding a second route grammar in component markup.

#### Scenario: User activates the current title
- **WHEN** a user activates the object title on a canonical object route
- **THEN** the standard navigation event identifies the same logical type and opaque identifier as the current route
- **AND** viewer routing retains the canonical encoded object route and ordinary history behavior

#### Scenario: Custom page uses the standard header
- **WHEN** an application-owned resource page composes `<cw-object-header>` beneath the route object context
- **THEN** it receives the same self-linked title and available icon presentation as the generic fallback page
- **AND** requires no application-specific URL, image, or event-handler markup

### Requirement: Petclinic icon-bearing object navigation demonstration
Petclinic SHALL demonstrate authoritative domain icons on standard object navigation links without reproducing icon URLs in its private HTML pages or application stylesheet.

#### Scenario: User views Petclinic object navigation
- **WHEN** an owner page renders its current heading, a pet reference or collection row, and navigable breadcrumbs with available icon metadata
- **THEN** each standard object link presents the corresponding domain icon and title
- **AND** activating each link continues through canonical HTMX object routing

#### Scenario: Automated browser verification
- **WHEN** Petclinic browser coverage inspects representative current-object, property-reference, collection-row, and breadcrumb links
- **THEN** it observes decorative icon images and the expected semantic titles
- **AND** verifies that current-title self-navigation and referenced-object navigation reach the expected canonical routes without browser console errors

### Requirement: Petclinic reference-modal Escape regression coverage
Petclinic browser verification SHALL exercise Escape cancellation for the `removePet` reference-first modal action prompt.

#### Scenario: User dismisses Remove Pet with Escape
- **WHEN** the owner page opens `removePet` with effective style `DIALOG_MODAL` and its pet reference editor is ready
- **THEN** focus is within the reference editor
- **AND** pressing Escape with its dropdown closed removes the modal prompt
- **AND** no removal mutation occurs and focus returns to the Remove Pet action

#### Scenario: User reopens the cancelled action
- **WHEN** the same action is activated after Escape cancellation
- **THEN** its authoritative parameter values and choices are prepared normally
- **AND** the earlier cancellation has not invoked or corrupted the action lifecycle

### Requirement: Petclinic time and multiline editor usability qualification
The Petclinic browser profile SHALL qualify minute-formatted date-time parameter entry with quarter-hour picker choices, keyboard and pointer clock-trigger operation, and single-ring multiline parameter focus through the public HTMX viewer.

#### Scenario: Visit date-time parameter is operated
- **WHEN** the browser opens the demonstrated visit-booking action and reaches its date-time parameter
- **THEN** the time field displays without seconds and selects from quarter-hour picker choices
- **AND** keyboard and pointer activation of its labelled clock trigger opens the time overlay without invoking the action

#### Scenario: Multiline parameter receives keyboard focus
- **WHEN** the browser focuses the demonstrated multiline reason parameter
- **THEN** exactly one visible focus ring identifies the editor
- **AND** the parameter remains editable, described, and submittable through its existing action prompt

#### Scenario: Browser qualification remains clean
- **WHEN** the time and multiline journeys complete or cancel
- **THEN** no unexpected mutation, action invocation, focus loss, console error, page error, CSP violation, external request, overlay leak, or horizontal overflow occurs

### Requirement: Petclinic corrected editor and pager presentation
The Petclinic browser profile SHALL verify user-visible keyboard time selection, one multiline editor boundary, and authoritative paged collection totals through the public HTMX viewer.

#### Scenario: Visit time overlay is keyboard opened
- **WHEN** the browser tabs from the visit time input to its clock trigger and presses Enter or Space
- **THEN** the real Vaadin time overlay is visibly open with quarter-hour choices formatted to minutes
- **AND** no booking mutation occurs until the user invokes the action

#### Scenario: Visit reason has one boundary
- **WHEN** the multiline visit reason receives keyboard focus
- **THEN** only the toolkit input-container boundary and focus ring are visible
- **AND** the slotted internal textarea contributes no nested native border or outline

#### Scenario: Visit pages show authoritative totals
- **WHEN** the browser navigates first, middle, or final pages of a multi-page Visits collection
- **THEN** each live range label includes the same authoritative total
- **AND** its start and end positions match the rows shown on that page

#### Scenario: Corrected journeys remain clean
- **WHEN** the editor and collection journeys complete or cancel
- **THEN** no unexpected mutation, invocation, focus loss, console error, page error, CSP violation, external request, overlay leak, or horizontal overflow occurs

### Requirement: Petclinic compact collection and top-action qualification
The Petclinic browser profile SHALL verify compact authoritative small-Grid sizing and integrated responsive collection-heading action placement through the public HTMX viewer.

#### Scenario: Owner has few companion animals
- **WHEN** the wide owner page renders a paged Pets Grid whose authoritative total fits within its configured page size
- **THEN** the Grid height fits its header and current data rows without reserving a full five-row empty scrolling viewport
- **AND** sorting, filtering, paging, links, columns, and collection metadata remain operable

#### Scenario: Owner pet actions render at wide width
- **WHEN** the Pets collection exposes Register a pet and Remove Pet as associated actions
- **THEN** Companion animals and the compact actions share the collection's bordered header row
- **AND** keyboard traversal reaches the actions in declaration order before collection search, sort, row-link, and paging controls

#### Scenario: Collection action header becomes narrow
- **WHEN** the owner page is resized below its responsive collection breakpoint
- **THEN** the title remains first and the compact action toolbar wraps beneath it within the same header area
- **AND** no control overlaps, clips, reorders keyboard focus, or causes horizontal document overflow

#### Scenario: Collection heading remains authoritative
- **WHEN** collection state changes through loading, filtering, sorting, refresh, empty, disabled, or error presentation
- **THEN** the effective name, description, tooltip, and `aria-labelledby` relationship remain current
- **AND** associated action nodes retain identity, declaration order, pending interaction state, and focus semantics

#### Scenario: Collection action tooltip is revealed
- **WHEN** pointer or keyboard interaction reveals an associated action tooltip in the Pets collection header
- **THEN** the tooltip opens below its control and remains fully visible over the collection body
- **AND** it is not clipped above the bounded header or hidden behind collection content

#### Scenario: Petclinic collection journey remains clean
- **WHEN** browser acceptance operates the collection actions and collection controls
- **THEN** prompts, cancellation, invocation, refresh, navigation, focus restoration, and authoritative GraphQL results remain unchanged
- **AND** no console error, page error, CSP violation, external request, stale control, duplicate ID, or overlay leak occurs

### Requirement: Petclinic bounded property-date qualification
The Petclinic owner page SHALL demonstrate declarative temporal range constraints on its editable `lastVisit` property through the public `<cw-property>` contract.

#### Scenario: Owner last-visit editor opens
- **WHEN** browser acceptance edits the owner's `lastVisit` `LocalDate` property
- **THEN** the qualified or native control exposes the authored absolute minimum and resolved `today` maximum
- **AND** localized British date presentation retains the same ISO boundaries and current value

#### Scenario: Future last-visit date is attempted
- **WHEN** the user enters a valid local date after the resolved maximum
- **THEN** the pending value remains available with a local range error
- **AND** no GraphQL property validation or mutation occurs

#### Scenario: In-range last-visit date is entered
- **WHEN** the user corrects the pending date to the closed admissible interval
- **THEN** canonical property validation and save proceed normally
- **AND** the authoritative owner refresh displays the accepted date

#### Scenario: Interaction is cancelled
- **WHEN** the bounded date editor is opened and cancelled without save
- **THEN** the authoritative date, focus restoration, picker accessibility, and local temporal precision remain unchanged

#### Scenario: Both toolkit policies run
- **WHEN** Petclinic runs with Vaadin-default and explicit native field policy
- **THEN** the same authored `min` and `max` declaration constrains both editors
- **AND** no raw Vaadin element or API is required in Petclinic markup

### Requirement: Petclinic bounded visit parameter qualification
The Petclinic owner page SHALL demonstrate declarative action-parameter ranges by booking visits with a future `LocalDate` and an inclusive office-hours `LocalTime` through direct-child `<cw-parameter>` declarations.
The domain action MUST combine accepted date and time values into the existing authoritative visit date-time and retain canonical validation for both constraints.

#### Scenario: Book Visit prompt opens
- **WHEN** browser acceptance opens `bookVisit`
- **THEN** the date editor exposes the resolved `tomorrow` minimum and the time editor exposes `08:00` and `17:00` bounds
- **AND** defaults, parameter order, labels, prompt style, picker accessibility, and cancellation remain authoritative or presentation-driven as before

#### Scenario: Non-future visit date is attempted
- **WHEN** the user enters a date before the resolved future-date minimum
- **THEN** the prompt retains the date and presents a local minimum reason
- **AND** no `bookVisit` GraphQL preparation, validation, or mutation request occurs for the rejected attempt

#### Scenario: Time outside office hours is attempted
- **WHEN** the user enters a well-formed time before `08:00` or after `17:00`
- **THEN** the prompt retains the time and presents the matching local boundary reason
- **AND** no `bookVisit` GraphQL preparation, validation, or mutation request occurs for the rejected attempt

#### Scenario: Visit date and time are corrected
- **WHEN** both pending values lie within their declared ranges
- **THEN** canonical action validation and one `bookVisit` mutation proceed
- **AND** the refreshed visit collection exposes the combined authoritative local date-time

#### Scenario: Native and Vaadin policies are exercised
- **WHEN** Petclinic runs under the default Vaadin and explicit native component-toolkit policies
- **THEN** both date and time controls receive equivalent bounds and local request gating
- **AND** Petclinic markup contains only public Causeway elements and attributes

### Requirement: Semantic standalone collection action outcomes
The generic HTMX viewer SHALL present collection-valued action outcomes through `<cw-standalone-collection>` in the resolved action-result outlet or stable shell fallback.
The viewer MUST retain policy ownership while delegating collection row markup, links, value rendering, responsive presentation, and optional Grid qualification to the semantic component.

#### Scenario: Action returns a collection
- **WHEN** the standard interaction controller publishes a normalized collection-valued action result with an immutable resolved presentation snapshot
- **THEN** the viewer creates one live `<cw-standalone-collection>`, applies that snapshot, and assigns the normalized result through its property
- **AND** the authored declaration node is neither moved nor reused as the live result node
#### Scenario: Collection contains domain objects
- **WHEN** returned rows advertise navigable object metadata
- **THEN** the standalone component presents their semantic object links in the stable result region
- **AND** established HTMX navigation policy handles link activation through the normal navigation event
#### Scenario: Collection result is announced
- **WHEN** standalone result presentation becomes ready or empty
- **THEN** the shell announces the effective action heading and authoritative finite result count through its established live-region policy
- **AND** the interaction controller's duplicate local result presentation is dismissed as before
#### Scenario: Application handles the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims the collection result
- **THEN** the default live standalone component is not created
- **AND** the application receives unchanged normalized result data plus additive presentation context
#### Scenario: Action returns another result kind
- **WHEN** an action returns an object, scalar, or void result
- **THEN** existing object navigation, scalar presentation, void refresh, missing-object recovery, and result preservation remain unchanged apart from resolved outlet placement
- **AND** no standalone collection is created
#### Scenario: Later result replaces the collection
- **WHEN** another action outcome is handled by default policy
- **THEN** the resolved result destination replaces the prior live presentation according to established result lifecycle
- **AND** prior rows, outlet ownership, and toolkit state cannot remain interactive or overwrite the newer result
#### Scenario: Collection contains selected domain values
- **WHEN** returned rows advertise navigable object metadata and authoritative wrappers selected for resolved columns
- **THEN** the standalone component presents semantic object links and declared cells in the resolved result destination
- **AND** established HTMX navigation policy handles link activation through the normal navigation event

### Requirement: Standalone collection viewer qualification
The Petclinic browser acceptance application SHALL exercise default and action-specific collection-valued outcomes through semantic action-result outlets under default Vaadin and explicit native component-toolkit policies.
Unexpected GraphQL, fragment, CSP, accessibility, console, page, external-request, stale-state, focus, overlay, route, or overflow failures MUST fail qualification.

#### Scenario: Petclinic action returns objects
- **WHEN** browser acceptance invokes a deterministic collection-valued Petclinic action
- **THEN** the stable shell result contains one ready `<cw-standalone-collection>` with the authoritative object count and links
- **AND** following a result link uses the canonical route lifecycle
#### Scenario: Native toolkit policy runs
- **WHEN** the same default and inline collection-result journeys run with `component-toolkit=native`
- **THEN** each outlet renders equivalent semantic rows without requesting a Vaadin Grid asset
- **AND** no application-specific raw result-list markup or follow-up row request is required
#### Scenario: Result lifecycle remains accessible
- **WHEN** a result appears, is replaced, survives a permitted refresh, falls back, or its link receives keyboard focus
- **THEN** region naming, heading, count, row navigation, announcements, and focus remain understandable and operable
- **AND** the stable shell, current route, and application override retain their established ownership
#### Scenario: Petclinic action uses a type default
- **WHEN** browser acceptance invokes a deterministic collection-valued action with a registered element-type presentation and no inline override
- **THEN** the active page outlet contains one ready standalone collection with authoritative object count, selected columns, icons, and links
- **AND** following a result link uses the canonical route lifecycle
#### Scenario: Petclinic action uses an inline override
- **WHEN** another authored action returning the same element type declares its own direct-child standalone collection
- **THEN** the result uses exactly the inline heading and columns rather than the type default
- **AND** the original GraphQL invocation contains the bounded authoritative fields needed by that presentation

### Requirement: Default collection-result presentation resources
The generic HTMX viewer SHALL discover bounded default standalone collection presentations from `META-INF/causeway/webcomponents/collections/*.html` and SHALL key each accepted resource by its canonical logical-type filename.
A default resource MUST be resolved inertly before invocation and MUST NOT execute arbitrary fragment markup or acquire result ownership.

#### Scenario: Valid default resource is discovered
- **WHEN** one valid UTF-8 resource named `petclinic.PetOwner.html` contains exactly one supported standalone collection root and bounded direct-child collection columns
- **THEN** the registry exposes that presentation for canonical logical type `petclinic.PetOwner`
- **AND** cached or reload behavior follows configured resource-page policy with collection-specific diagnostics

#### Scenario: Matching result type is prepared
- **WHEN** an action advertises collection element logical type `petclinic.PetOwner` and has no valid inline override
- **THEN** the viewer resolves and normalizes the matching default before invocation
- **AND** its valid columns can shape the original authoritative result selection

#### Scenario: Domain service action advertises a matching result type
- **WHEN** a domain service collection action advertises an element logical type with a matching default resource
- **THEN** menu projection and invocation preserve the authoritative element type
- **AND** the same default presentation and original-invocation column projection apply as for object actions

#### Scenario: No default resource exists
- **WHEN** no resource is registered for the advertised element logical type
- **THEN** the action proceeds with generic standalone result presentation
- **AND** an absent presentation does not become an invocation error

#### Scenario: Resource is malformed or duplicated
- **WHEN** a resource is oversized, invalid UTF-8, invalidly named, duplicated, has an unsupported root, or exceeds bounded discovery limits
- **THEN** startup or reload resolution reports a deterministic collection-presentation diagnostic according to established resource policy
- **AND** arbitrary classpath paths, scripts, event handlers, and unsupported elements are not exposed as live result content

#### Scenario: First lookup is cached
- **WHEN** repeated actions use the same unchanged result element logical type
- **THEN** accepted normalized presentation or confirmed absence is reused according to current cache policy
- **AND** redundant same-origin lookups do not delay every invocation

### Requirement: HTMX action-result outlet resolution
The generic HTMX viewer SHALL prefer one unique `<cw-action-results>` in the active route page for successful non-navigating action-result presentation and SHALL retain its stable shell result region as fallback.
Result routing MUST remain host-owned and generation-safe, while the resolved outlet's normalized presentation style controls only the visual surface.

#### Scenario: Active page has one outlet
- **WHEN** an action interaction begins while exactly one connected action-result outlet belongs to the active route page
- **THEN** the viewer snapshots that outlet and route generation as the preferred destination
- **AND** a later successful scalar, void-status, or collection presentation is mounted there only while the destination remains current

#### Scenario: Inline result is outside the viewport
- **WHEN** the current host mounts a successful non-navigating result outside the visible viewport into an `INLINE` outlet
- **THEN** the viewer scrolls the result outlet into view without moving keyboard focus
- **AND** reduced-motion preference disables animated scrolling

#### Scenario: Dialog or sidebar result is mounted
- **WHEN** the resolved outlet normalizes to `DIALOG` or `SIDEBAR`
- **THEN** the viewer supplies the originating action focus target and mounts the unchanged host-owned result presentation into that outlet
- **AND** opening the styled surface does not scroll the underlying route or alter result data

#### Scenario: Styled result is dismissed
- **WHEN** a dialog or sidebar dismiss control or supported Escape lifecycle requests dismissal
- **THEN** the viewer clears the current result through the established destination lifecycle
- **AND** focus returns to the eligible originating object-action or service-action control when it remains connected

#### Scenario: Active page has no outlet
- **WHEN** no applicable page outlet exists
- **THEN** successful non-navigating results use the stable shell result region and its normalized presentation style
- **AND** existing applications require no markup migration

#### Scenario: Active page has duplicate outlets
- **WHEN** more than one equally applicable outlet exists in the active route page
- **THEN** the viewer exposes a bounded ambiguity diagnostic and uses the stable shell fallback
- **AND** DOM order is not used to choose an arbitrary owner or presentation style

#### Scenario: Captured outlet disconnects
- **WHEN** route replacement disconnects the captured outlet before an asynchronous result completes
- **THEN** stale work cannot mount into that outlet, reopen its styled surface, or use a different page's outlet
- **AND** the still-current host policy may use the stable shell fallback without transferring stale node or focus state

#### Scenario: Application claims the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims a result
- **THEN** neither page outlet nor shell fallback is modified or opened by default policy
- **AND** the application receives the authoritative semantic result detail and additive resolved-presentation snapshot

#### Scenario: Object result is returned
- **WHEN** a successful result advertises one navigable object identity
- **THEN** established canonical object navigation remains authoritative
- **AND** no inline, dialog, or sidebar result surface is mounted

#### Scenario: Void refresh preserves status
- **WHEN** void-result policy refreshes the active route while preserving current result status
- **THEN** the still-current status is rehomed to the unique equivalent outlet in the refreshed page or stable shell fallback
- **AND** obsolete outlet, route, surface, or focus generations cannot reclaim it

### Requirement: Action-result presentation style qualification
The Petclinic browser acceptance application SHALL exercise `INLINE`, `DIALOG`, and `SIDEBAR` action-result outlets under default Vaadin and explicit native component-toolkit policies.
Unexpected invocation, routing, focus, Escape, backdrop, overflow, responsive, replacement, dismissal, announcement, console, page, or external-request failures MUST fail qualification.

#### Scenario: Inline result is qualified
- **WHEN** a deterministic Petclinic action returns a non-navigating result to an `INLINE` outlet
- **THEN** sticky-header-aware reveal, links, values, announcements, replacement, and dismissal remain operable
- **AND** long result content scrolls within a bounded area above a visible Dismiss control
- **AND** the result surface introduces no modal or sidebar behavior

#### Scenario: Dialog result is qualified
- **WHEN** a deterministic Petclinic action returns a result to a `DIALOG` outlet
- **THEN** labelled modal semantics, backdrop, initial focus, Tab containment, Escape, explicit dismissal, replacement, and origin focus restoration are verified
- **AND** bounded result scrolling keeps the Dismiss control visible below the content
- **AND** route content cannot remain interactively exposed through stale modal state

#### Scenario: Sidebar result is qualified
- **WHEN** a deterministic Petclinic action returns a result to a `SIDEBAR` outlet
- **THEN** right-side placement, non-modal page access, ordinary Tab order, Escape while focused within, explicit dismissal, replacement, and origin focus restoration are verified
- **AND** wide and narrow viewports retain bounded internal result scrolling above a visible Dismiss control with no horizontal document overflow

#### Scenario: Styled result respects established ownership
- **WHEN** duplicate, disconnected, superseded, application-claimed, object-valued, or void-refresh result paths are exercised
- **THEN** established outlet fallback, canonical navigation, application ownership, preservation, and stale-generation behavior remain authoritative
- **AND** presentation style cannot select a destination or reinterpret the result

### Requirement: Default collection row preview resources
The generic HTMX viewer SHALL discover bounded runtime-type preview resources from `META-INF/causeway/webcomponents/previews/<logical-type-name>.html` and SHALL expose safe definitions to empty collection peek declarations through a host resolver.
Preview resources MUST remain presentation-only and MUST NOT select row identity, alter collection projection, bypass member metadata, or invoke domain behavior independently.

#### Scenario: One valid preview resource exists
- **WHEN** exactly one bounded UTF-8 resource with one supported `<cw-peek>` root is discovered for a valid logical type
- **THEN** the viewer registers an immutable inert definition for that exact logical type
- **AND** an empty collection peek can clone its content for an eligible row of that runtime type

#### Scenario: Preview resource is absent
- **WHEN** no registered preview exists for a row's runtime logical type
- **THEN** lookup resolves as absent without failing collection data
- **AND** the collection renders no preview disclosure for that row

#### Scenario: Inline preview content is authored
- **WHEN** a collection's direct `<cw-peek>` has meaningful inline content
- **THEN** the viewer performs no default preview lookup for that declaration
- **AND** a registered runtime-type default cannot merge with or override the inline template

#### Scenario: Preview resource is unsafe or malformed
- **WHEN** a resource exceeds configured bounds, contains invalid UTF-8, has an invalid filename or root, advertises row identity, or contains executable, embedding, event-handler, or unsupported markup
- **THEN** the viewer rejects it with a stable bounded diagnostic
- **AND** no rejected markup enters a live preview or collection data path

#### Scenario: Duplicate preview resources are discovered
- **WHEN** more than one classpath resource claims the same logical type
- **THEN** registry construction fails deterministically with bounded safe source identifiers
- **AND** classpath order does not select an arbitrary definition

#### Scenario: Client resolves a preview
- **WHEN** an empty peek resolves a valid runtime logical type through the private preview endpoint
- **THEN** the response is privately non-cacheable at the HTTP boundary and distinguished as a Causeway preview resource
- **AND** cached viewer mode reuses the validated inert template while cloning fresh live content per expansion
- **AND** reload mode re-resolves according to established resource-page policy

#### Scenario: Preview lookup fails at runtime
- **WHEN** a lookup returns an unexpected status, malformed body, or unsupported document
- **THEN** the affected row has no expander and the viewer publishes a bounded presentation diagnostic
- **AND** the containing collection remains usable and does not disclose resource bodies or row values

### Requirement: Collection row preview qualification
The Petclinic browser acceptance application SHALL demonstrate inline and runtime-type default collection peeks under default Vaadin and explicit native component-toolkit policies.
Unexpected context, projection, request, disclosure, focus, Escape, refresh, virtualization, overflow, console, page, or external-network failures MUST fail qualification.

#### Scenario: Inline preview is demonstrated
- **WHEN** a Petclinic collection with non-empty inline peek content expands an eligible row
- **THEN** declared properties, actions, collections, layout, row identity, keyboard disclosure, and canonical object links are verified
- **AND** no type-default preview request is made

#### Scenario: Default preview is demonstrated
- **WHEN** a Petclinic collection has an empty peek and a matching resource exists under `previews/`
- **THEN** the runtime-type default renders inside the selected hydrated row context
- **AND** a row whose runtime type has no default exposes no expander

#### Scenario: Single-row and Escape behavior is qualified
- **WHEN** users expand successive rows and press Escape from preview content
- **THEN** only one details subtree remains, prior contexts are retired, focus returns correctly, and no stale preview reopens

#### Scenario: Preview action refreshes the collection
- **WHEN** a deterministic Petclinic action succeeds inside an expanded preview
- **THEN** normal action-result policy remains authoritative
- **AND** the parent collection reloads current authoritative rows and remains collapsed

#### Scenario: Preview property update refreshes the collection
- **WHEN** a deterministic editable property update succeeds inside an expanded preview
- **THEN** the parent collection applies the same authoritative reload and collapsed result used for an action

#### Scenario: Collection lifecycle collapses the preview
- **WHEN** sorting, filtering, paging, reload, responsive Grid replacement, virtual range supersession, or route replacement occurs with a preview open
- **THEN** expansion is not preserved and late preview work cannot alter the current collection

### Requirement: Representative deterministic Petclinic demo data
The Petclinic acceptance application SHALL seed a deterministic clinic graph rich enough to present representative owners, pets, historical visits, and upcoming visits across configured collection pages.
The richer graph MUST preserve established fixture identities and MUST remain idempotent across application startup.

#### Scenario: Petclinic sample starts with an empty database
- **WHEN** seed initialization runs for the first time
- **THEN** it creates varied owners and pets with stable IDs, species, contact details, notes, and optional values
- **AND** it creates both historical and upcoming visits using deterministic clock-relative dates and stable reasons

#### Scenario: Established acceptance fixtures are inspected
- **WHEN** integration or browser tests resolve Mary, Basil, Samantha, Helen, Max, or another pre-existing fixture by stable identity
- **THEN** the established identity and demonstrated values remain available
- **AND** additive demo data does not change canonical links, mutation targets, or row identity

#### Scenario: Seed initialization runs again
- **WHEN** the application starts after the established seed marker already exists
- **THEN** no duplicate owner, pet, or visit is created
- **AND** collection totals remain deterministic

#### Scenario: Demo collections are inspected
- **WHEN** owners, one owner's pets, one owner's visit history, and clinic-wide upcoming visits are loaded
- **THEN** each representative collection contains enough authoritative rows to cross its configured page boundary
- **AND** both sparse and populated owner pages remain available for compact and multi-page demonstrations

### Requirement: Convention-registered application shell
The HTMX viewer SHALL discover a trusted application shell from the exact private classpath resource `META-INF/causeway/webcomponents/shells/htmx.html`.
It SHALL accept zero or one unique application shell, SHALL use a separately packaged built-in default when none exists, and SHALL reject duplicate or defective application shells without relying on classloader shadowing order.
The application shell SHALL follow the configured `CACHED` or `RELOAD` resource-page mode while its zero-or-one registration remains immutable between application-context startups.

#### Scenario: Application packages a shell
- **WHEN** exactly one application module or dependency packages `META-INF/causeway/webcomponents/shells/htmx.html`
- **THEN** viewer startup registers that resource as the authoritative application shell
- **AND** no application Java bean, controller, annotation, manifest, public shell URL, or filesystem path is required

#### Scenario: Application does not package a shell
- **WHEN** no application shell exists at the private convention
- **THEN** the viewer selects its validated built-in default shell
- **AND** existing applications retain the established full-page structure and behavior without configuration changes

#### Scenario: Multiple shells are discovered
- **WHEN** more than one distinct application shell is contributed by application modules or dependency jars
- **THEN** viewer startup fails with a bounded duplicate-shell diagnostic
- **AND** the viewer does not choose according to classpath order, merge shells, or silently use its default

#### Scenario: Cached shell is loaded
- **WHEN** the authoritative shell is decoded during startup in default `CACHED` mode
- **THEN** it is read as bounded non-empty well-formed UTF-8 declarative HTML and retained immutably for subsequent full-page renders
- **AND** only documented exact shell bindings are eligible for escaped or validated structural substitution

#### Scenario: Reload shell is initially loaded
- **WHEN** the authoritative shell is decoded during startup in configured `RELOAD` mode
- **THEN** bounded UTF-8, NUL-content, required-binding, document-boundary, and semantic-landmark validation runs before registration completes
- **AND** the registration retains authority without retaining startup content as a stale fallback

#### Scenario: Existing reload shell is edited
- **WHEN** the registered shell content changes on the running classpath in `RELOAD` mode
- **AND** a subsequent ordinary full-page request is rendered
- **THEN** the viewer reopens, validates, and binds the current shell content
- **AND** no application-context restart, classpath rescan, public shell fetch, or Java fragment factory is required

#### Scenario: Reloaded shell is defective
- **WHEN** a registered reload shell becomes unavailable, unreadable, oversized, empty, malformed UTF-8, contains forbidden NUL content, or violates its declarative contract
- **THEN** the affected full-page render fails with a bounded safe diagnostic
- **AND** the viewer does not serve stale shell content, expose an absolute resource path, repair the shell, or silently use its default

#### Scenario: Shell registration changes while running
- **WHEN** an application shell is added, deleted, or replaced by another resource after viewer startup
- **THEN** the immutable registration does not appear, disappear, or change authority in either resource-page mode
- **AND** an application-context restart is required to re-run bounded discovery and conflict validation

#### Scenario: Shell resource is requested directly
- **WHEN** a client requests the application shell convention as a public HTTP resource
- **THEN** the ordinary static-resource handler does not expose it
- **AND** only the server-side full-page composition path consumes the trusted resource

### Requirement: Application shell structural contract
An application shell SHALL have exactly one `<body>` root containing exactly one stable `<cw-graphql-client>`, and that client SHALL contain the unique menu boundary, route region, route-content slot, stable action-result outlet, loading region, announcement region, and authentication-chrome slot required by the viewer protocol.
The application SHALL control the ordinary HTML wrappers, ordering, classes, body attributes, branding, menu placement, auxiliary stable regions, and footer presentation around those landmarks.
The shell MUST NOT contain a document root, document head, executable script, route-level object context, unknown reserved binding, or a second instance of a unique protocol landmark.

#### Scenario: Application chooses a top menu layout
- **WHEN** the shell places `<cw-menubars>` within an application-authored header above the route region
- **THEN** the shell passes structural validation
- **AND** the viewer does not add another header, menu boundary, or layout wrapper

#### Scenario: Application chooses a side-region layout
- **WHEN** the shell places `<cw-menubars>` within an application-authored aside next to the route region
- **THEN** the shell passes structural validation when all protocol landmarks and containment rules remain satisfied
- **AND** the viewer does not require the menu to be a direct child of a navbar or header

#### Scenario: Application customizes stable presentation
- **WHEN** the shell changes body attributes, ordinary wrapper elements, CSS classes, branding markup, auxiliary regions, footer content, or landmark order
- **THEN** those choices are preserved in the full-page response
- **AND** validation remains based on protocol identity, cardinality, containment, and required bindings rather than the built-in shell's visual hierarchy

#### Scenario: Shell omits a protocol landmark
- **WHEN** the application shell omits or duplicates the body root, GraphQL client, menu boundary, route region, route-content slot, result outlet, loading region, announcement region, or authentication-chrome slot
- **THEN** validation fails with a bounded landmark-specific diagnostic
- **AND** the viewer does not manufacture, select, move, or repair an element

#### Scenario: Route content is misplaced
- **WHEN** the route-content slot is outside the unique route region or a required global landmark is outside the GraphQL client
- **THEN** validation fails with a bounded containment diagnostic
- **AND** the viewer does not infer an alternative provider or replacement target

#### Scenario: Shell crosses the document boundary
- **WHEN** the application shell declares `<html>`, `<head>`, executable `<script>`, or content outside its single `<body>` root
- **THEN** validation fails closed
- **AND** application content cannot replace viewer-managed metadata, framework assets, HTMX configuration, or CSP-sensitive document structure

#### Scenario: Shell attempts route-context ownership
- **WHEN** an application shell declares a route-level `<cw-object-context>` or embeds route identity bindings outside the route-content slot
- **THEN** validation fails closed
- **AND** canonical route identity remains owned by the replaceable route page

#### Scenario: Optional presentation binding is omitted
- **WHEN** an otherwise valid shell does not reference an optional binding such as brand text or comparison link
- **THEN** the shell remains valid
- **AND** required provider, authentication, and route structural bindings remain mandatory

#### Scenario: Unknown binding is authored
- **WHEN** a shell contains a reserved `{{causeway.*}}` token outside the documented closed vocabulary
- **THEN** validation fails with a bounded unresolved-binding diagnostic
- **AND** no expression, request parameter, configuration property, metamodel value, persistence value, or GraphQL result is evaluated

### Requirement: Generic HTMX framework Logout safety

The generic HTMX viewer SHALL treat `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no authentication integration claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom action requests for the same exact identity.
An installed authentication integration SHALL remain responsible for presenting an accessible logout control, submitting the configured method and current CSRF evidence, clearing the session, and selecting the post-logout destination.

#### Scenario: Generic HTMX viewer has no authentication integration

- **WHEN** application menus contain the framework Logout action but the HTMX host has no registered logout capability
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** the viewer does not imply that a browser session can be ended

#### Scenario: Stale Logout request is published

- **WHEN** stale or custom markup publishes an action request for the exact framework Logout identity without a host logout claim
- **THEN** the HTMX viewer cancels the request before GraphQL validation or invocation
- **AND** it emits a bounded unavailable-operation announcement or diagnostic without a successful result

#### Scenario: Authenticated HTMX integration is active

- **WHEN** an installed HTMX authentication integration claims Logout and supplies its host-owned control
- **THEN** the existing protected logout flow remains available outside domain action invocation
- **AND** its endpoint, HTTP method, CSRF, session, cookie, and redirect policies remain authoritative

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the HTMX viewer does not suppress or reinterpret it as framework Logout
- **AND** normal action and local-resource policies apply

### Requirement: HTMX local-resource result navigation

The HTMX viewer SHALL resolve a local-resource semantic result against the current application context and SHALL perform full-document navigation according to its supported opening strategy.
It MUST validate the resolved target as same-origin and application-local before navigation and MUST NOT interpret any target path as an authentication operation.

#### Scenario: Same-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `SAME_WINDOW`
- **THEN** the HTMX host navigates the current browsing context to the context-aware local target
- **AND** it does not issue an HTMX fragment request or canonical object-route transition

#### Scenario: New-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `NEW_WINDOW`
- **THEN** the HTMX host requests a new opener-isolated browsing context for the local target
- **AND** the current object route remains unchanged

#### Scenario: Local resource target is unsafe

- **WHEN** a result path is malformed, scheme-relative, cross-origin, credential-bearing, outside the configured application-local boundary, or paired with an unknown strategy
- **THEN** the HTMX host refuses navigation and reports a bounded result-policy error
- **AND** no target value is repaired, rewritten to another origin, or treated as Logout

#### Scenario: Application uses a servlet context path

- **WHEN** the HTMX application is deployed beneath a non-root servlet context and returns an application-local resource path
- **THEN** resolution preserves the authoritative servlet context exactly once
- **AND** the viewer does not confuse the HTMX route base with the application context
