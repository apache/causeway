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
- **THEN** the viewer renders `<causeway-object editable>` beneath the same route object context
- **AND** absence is treated as ordinary generic fallback rather than a configuration failure

#### Scenario: Generic component renders
- **WHEN** `<causeway-object>` connects
- **THEN** it renders the effective or fallback object layout
- **AND** does not discover custom pages or inspect router state

### Requirement: Convention-registered private HTML pages
The HTMX viewer SHALL discover trusted `.html` page resources from one documented private classpath root and SHALL register each resource by the exact public logical type represented by its filename.

#### Scenario: Application packages an exact logical-type page
- **WHEN** an application packages `META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html`
- **THEN** viewer startup registers the literal resource for exact logical type `petclinic.PetOwner`
- **AND** no application Java bean, annotation, manifest, template controller, or client-side page fetch is required

#### Scenario: Page is supplied by a dependency jar
- **WHEN** one application module or dependency jar contributes a qualifying page beneath the private root
- **THEN** classpath discovery includes that page in the same immutable registry
- **AND** the resource is not exposed by the ordinary static-resource handler

#### Scenario: Page content is loaded
- **WHEN** a qualifying resource is decoded during startup
- **THEN** it is read as bounded non-empty UTF-8 literal HTML
- **AND** no expression, route value, object identifier, metamodel value, persistence value, or GraphQL result is interpolated into its text

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
- **THEN** its ordinary HTML and `<causeway-*>` elements render beneath the route's existing `<causeway-object-context>`
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
Every custom or generic object fragment SHALL contain exactly one route-level `<causeway-object-context>` for the canonical logical route identity.

#### Scenario: Route fragment is replaced
- **WHEN** HTMX installs a newer object fragment
- **THEN** the prior route context disconnects and releases obsolete requirements
- **AND** stale GraphQL or structural-resource responses cannot render into the new route

#### Scenario: Custom page composes semantic members
- **WHEN** a custom page uses properties, actions, collections, ordinary HTML, or `<causeway-object>`
- **THEN** those elements consume the same nearest route context
- **AND** the custom page does not create a parallel domain-state channel

### Requirement: Stable semantic application shell
The viewer SHALL keep application branding, menu bars, announcements, loading state, result presentation, and other global shell state outside replaceable route fragments.

#### Scenario: Full page is requested
- **WHEN** an ordinary browser request loads the viewer root or an object route
- **THEN** the server returns one complete document containing the GraphQL client, `<causeway-menubars>`, and route region
- **AND** the requested landing or object fragment appears inside that region

#### Scenario: HTMX fragment is requested
- **WHEN** a valid request carries `HX-Request: true`
- **THEN** the server returns only the route fragment and canonical history instruction
- **AND** does not duplicate the stable shell

#### Scenario: Object route changes
- **WHEN** HTMX replaces the route-content fragment
- **THEN** `<causeway-menubars>` remains coordinated in the stable shell
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
- **AND** no Petclinic Java page renderer or custom-page knowledge inside `<causeway-object>` is required

#### Scenario: Petclinic HTML page is absent
- **WHEN** a Petclinic logical type is run without its corresponding packaged HTML resource
- **THEN** the HTMX router uses the generic `<causeway-object editable>` page
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
Canonical route meaning, custom-page precedence, and generic `<causeway-object>` fallback SHALL remain semantically compatible with the generic Vue and Svelte viewers.

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
The HTMX viewer SHALL expose `causeway.viewer.webcomponents.htmx.editor-toolkit` with the bounded values `vaadin` and `native` and SHALL default effectively to `vaadin`.
An explicitly configured common policy MUST take precedence over deprecated reference-widget and field-family properties.

#### Scenario: No toolkit property is configured
- **WHEN** an application starts without the common or deprecated toolkit properties
- **THEN** the resolved policy enables qualified reference, basic, numeric, and local-temporal Vaadin adapters
- **AND** unsupported shapes and failed closures retain native or explicit unsupported presentation

#### Scenario: Native policy is explicit
- **WHEN** `editor-toolkit=native` is configured
- **THEN** the shell explicitly disables reference and field Vaadin adapters and emits no Vaadin CSP hashes
- **AND** routes request no reference or field-family closure

#### Scenario: Common policy overrides deprecated properties
- **WHEN** the common property and one or both deprecated properties are configured with conflicting values
- **THEN** the resolved common policy determines references, every qualified field family, and CSP hashes
- **AND** the deprecated values cannot create a mixed or broadened policy

#### Scenario: Only deprecated properties are configured
- **WHEN** the common property is absent and either deprecated property is explicitly configured
- **THEN** the viewer preserves the former complete policy in which references default false and field families default empty unless their corresponding old value is supplied
- **AND** shell diagnostics identify compatibility policy without changing application markup

#### Scenario: Toolkit value is invalid
- **WHEN** configuration supplies a value other than `vaadin` or `native`
- **THEN** configuration binding rejects it with a bounded error
- **AND** the viewer does not silently select a broader policy

### Requirement: Default route-lazy toolkit delivery
The generic HTMX viewer SHALL enable every qualified packaged adapter by default without eagerly importing any reference or field-family closure.
A closure MUST load only after an eligible connected Causeway editor selects its internal adapter.

#### Scenario: Route contains one eligible family
- **WHEN** the first editor eligible for one default family connects
- **THEN** only that family's same-origin closure is requested and upgraded
- **AND** route readiness and other families do not wait for it

#### Scenario: Route contains no eligible editor
- **WHEN** a landing, menu-only, read-only, custom, or other unaffected route renders
- **THEN** it requests zero reference, basic, numeric, and local-temporal Vaadin assets
- **AND** default CSP hash permission does not cause a network request

#### Scenario: One family fails
- **WHEN** a default family closure fails to load or define its controls
- **THEN** its existing Causeway failure boundary activates the matching native implementation
- **AND** other family closures remain independently eligible and lazy

### Requirement: Supported exact-hash toolkit CSP
The HTMX response CSP SHALL include only the generated reviewed style-hash union for the resolved internal toolkit policy.
It MUST retain `style-src-attr 'none'`, same-origin script and connection sources, and no `unsafe-inline` source.

#### Scenario: Default policy renders CSP
- **WHEN** the effective policy is the supported Vaadin default
- **THEN** `style-src` and `style-src-elem` contain the deterministic deduplicated reference and field-family hash union
- **AND** every hash corresponds to pinned generated policy metadata

#### Scenario: Deprecated subset policy renders CSP
- **WHEN** compatibility mode enables only a subset of old adapters
- **THEN** CSP contains exactly the reviewed union required by that resolved subset
- **AND** disabled-family-only hashes are absent where generated policy distinguishes them

#### Scenario: Native policy renders CSP
- **WHEN** the effective common policy is native
- **THEN** CSP contains no Vaadin style hash
- **AND** route, GraphQL, application stylesheet, and canonical identity policy remains unchanged

### Requirement: Supported default and native release qualification
The viewer SHALL treat default-Vaadin and explicit-native modes as supported release configurations rather than sample-scoped pilot modes.
Petclinic, the vanilla sample, the pinned Reference Application, deterministic packaging, strict CSP, accessibility, browser isolation, bundle budgets, licenses, vulnerabilities, and ordinary Maven packaging MUST remain passing gates.

#### Scenario: Default release matrix runs
- **WHEN** release qualification runs with no toolkit override
- **THEN** eligible reference and field journeys use internal Vaadin adapters and preserve authoritative outcomes
- **AND** unexpected CSP, accessibility, console, page, external-request, stale-state, focus, overlay, or overflow failures fail the gate

#### Scenario: Native release matrix runs
- **WHEN** the same journeys run with `editor-toolkit=native`
- **THEN** native controls preserve the same GraphQL values, routes, interactions, and classifications
- **AND** all Vaadin closure requests and style hashes are absent
