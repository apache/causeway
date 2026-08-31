# generic-htmx-web-component-viewer Specification

## Purpose
TBD - created by archiving change add-generic-htmx-web-component-viewer. Update Purpose after archive.
## Requirements
### Requirement: Optional router-led HTMX viewer
The project SHALL provide an explicitly enabled generic HTMX viewer whose primary responsibility is application routing, shell composition, and page-fragment lifecycle over the semantic web-component library.

#### Scenario: Viewer is enabled
- **WHEN** an application includes and imports the HTMX viewer module
- **THEN** it serves the documented shell and canonical object routes beneath the configured base path
- **AND** uses semantic components for domain, interaction, menu, and layout behavior

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

### Requirement: Convention-registered private HTML pages
The HTMX viewer SHALL discover trusted `.html` page resources from one documented private classpath root and SHALL register each resource by the exact public logical type represented by its filename.
It SHALL support configured `CACHED` and `RELOAD` resource-page modes, SHALL default to `CACHED`, and MUST keep the registered page set immutable between application-context startups in both modes.

#### Scenario: Application packages an exact logical-type page
- **WHEN** an application packages `META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html`
- **THEN** viewer startup registers the literal resource for exact logical type `petclinic.PetOwner`
- **AND** no application Java bean, annotation, manifest, template controller, or client-side page fetch is required

#### Scenario: Page is supplied by a dependency jar
- **WHEN** one application module or dependency jar contributes a qualifying page beneath the private root
- **THEN** classpath discovery includes that page in the same immutable registry
- **AND** the resource is not exposed by the ordinary static-resource handler

#### Scenario: Cached page content is loaded
- **WHEN** a qualifying resource is decoded during startup in the default `CACHED` mode
- **THEN** it is read as bounded non-empty UTF-8 literal HTML and retained immutably for subsequent renders
- **AND** no expression, route value, object identifier, metamodel value, persistence value, or GraphQL result is interpolated into its text

#### Scenario: Reload page content is initially loaded
- **WHEN** a qualifying resource is decoded during startup in configured `RELOAD` mode
- **THEN** the same bounded non-empty UTF-8 and NUL-content validation runs before registration completes
- **AND** the definition retains authority for its exact logical type without retaining startup content as a stale fallback

#### Scenario: Existing reload page is edited
- **WHEN** the content of an already-registered page resource changes on the running classpath in `RELOAD` mode
- **AND** a subsequent route render selects that page
- **THEN** the viewer opens and validates the resource again and renders its current literal HTML
- **AND** no Spring application-context restart, classpath rescan, public page fetch, or Java fragment-factory invocation is required

#### Scenario: Reloaded content is defective
- **WHEN** an already-registered page in `RELOAD` mode becomes unreadable, oversized, empty, malformed UTF-8, or contains forbidden NUL content
- **THEN** the affected render fails with a bounded safe diagnostic
- **AND** the viewer does not serve cached stale content, expose an absolute resource path, or silently use generic object layout fallback

#### Scenario: Page registration changes while running
- **WHEN** a page resource is added, deleted, renamed, or changed to claim another logical type after viewer startup
- **THEN** the immutable registry does not add, remove, or rename that registration in either mode
- **AND** an application-context restart is required to re-run bounded discovery and conflict validation

#### Scenario: Invalid resource-page mode is configured
- **WHEN** application configuration supplies a resource-page mode other than `CACHED` or `RELOAD`
- **THEN** viewer startup fails through bounded configuration binding
- **AND** it does not infer behavior from the classpath launch mechanism

#### Scenario: Page registration is defective
- **WHEN** a discovered page has an invalid logical-type filename, exceeds a documented bound, is empty, contains malformed UTF-8 or forbidden NUL content, cannot be read, or conflicts with another definition
- **THEN** viewer startup fails with a bounded safe diagnostic
- **AND** the viewer does not silently use generic layout fallback for that defective registration

#### Scenario: Registry discovery is bounded
- **WHEN** classpath discovery reaches the documented finite page-count ceiling
- **THEN** startup rejects additional registrations deterministically
- **AND** does not allocate an unbounded page registry

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

### Requirement: Source-visible Petclinic page customization
Petclinic SHALL demonstrate application-owned HTMX presentation through private `.html` files containing ordinary HTML and public semantic Causeway components without application-specific page-rendering Java code.

#### Scenario: Maintainer inspects Petclinic source
- **WHEN** a maintainer opens the Petclinic application resources
- **THEN** `petclinic.HomePage.html`, `petclinic.PetOwner.html`, `petclinic.Pet.html`, and `petclinic.Visit.html` visibly contain the sample's page composition
- **AND** no Petclinic `HtmxPageFragmentFactory` or equivalent Java renderer supplies page markup

#### Scenario: Petclinic page loads
- **WHEN** a canonical route selects one of the four Petclinic logical types
- **THEN** the exact HTML resource is rendered as the custom page beneath one route context
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
The HTMX viewer SHALL NOT construct GraphQL domain operations, translate GraphQL response data, parse Causeway grid or menu resources, or directly access Causeway metamodel and persistence internals.

#### Scenario: Page requires domain state
- **WHEN** a custom or generic page connects beneath its route context
- **THEN** semantic components obtain domain state through GraphQL context contracts
- **AND** HTMX handles only routing, shell, history, announcements, and fragment lifecycle

#### Scenario: HTMX JavaScript is unavailable
- **WHEN** a user follows a canonical route link without HTMX enhancement
- **THEN** the server returns a complete navigable shell for that route
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
Petclinic HTML resource pages SHALL demonstrate declarative paging on collections that can grow materially while leaving smaller or summary collections on established loading behavior.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner collection
- **THEN** its HTML override declares a bounded `paged` size
- **AND** does not rely on inert `offset` or `size` attributes

#### Scenario: Owner visit history renders
- **WHEN** an owner page composes visit history
- **THEN** that collection declares a bounded `paged` size
- **AND** its associated actions and semantic columns remain unchanged

#### Scenario: Small or summary collection renders
- **WHEN** Petclinic composes an owner's pets or the upcoming-visit summary
- **THEN** the HTML override does not declare `paged`
- **AND** the collection retains established default loading behavior

#### Scenario: Browser navigates a paged collection
- **WHEN** normalized metadata reports another page
- **THEN** the application exposes accessible Causeway previous and next controls and the configured range size
- **AND** navigation does not duplicate rows, associated actions, requests, or page-level headings

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
