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
The HTMX route resolver SHALL select one custom server fragment factory registered for the exact public logical type before using the generic object page.

#### Scenario: Custom page is registered
- **WHEN** the route contains a logical type with one application fragment factory
- **THEN** the viewer renders that trusted application definition beneath the route object context
- **AND** the factory receives validated route identity rather than metamodel, persistence, authorization, or GraphQL internals

#### Scenario: Duplicate pages are registered
- **WHEN** two factories claim the same exact logical type
- **THEN** viewer startup fails with a bounded configuration error
- **AND** bean ordering does not select an arbitrary page

#### Scenario: No custom page is registered
- **WHEN** the route contains a logical type without an exact registration
- **THEN** the viewer renders `<causeway-object>` beneath the same route object context

#### Scenario: Generic component renders
- **WHEN** `<causeway-object>` connects
- **THEN** it renders the effective or fallback object layout
- **AND** does not discover custom pages or inspect router state

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
The viewer's semantic application menus SHALL close an expanded menu panel after an enabled action is selected and SHALL allow the user to dismiss an expanded menu with Escape.
Disclosure accessibility state, panel visibility, action dispatch, and focus behavior MUST remain synchronized during dismissal.

#### Scenario: Enabled menu action is selected
- **WHEN** the user activates an enabled action in an expanded application menu by pointer or keyboard
- **THEN** the containing menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** the selected semantic action request is dispatched exactly once
- **AND** the resulting prompt, result, or route transition continues according to its existing focus policy

#### Scenario: Expanded menu is dismissed with Escape
- **WHEN** focus is within an expanded application menu and the user presses Escape
- **THEN** the active menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus returns to that menu's disclosure control
- **AND** no service action is requested

#### Scenario: Menu dismissal is scoped
- **WHEN** an expanded menu is dismissed after selection or with Escape
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

#### Scenario: Object home is available
- **WHEN** targeted application-entry discovery returns a valid public object home
- **THEN** default home policy routes to its canonical object route
- **AND** no home service-action descriptor is inferred

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
The project SHALL include a deterministic current-Causeway Petclinic application ported from the documented pinned Apache source and exposing HTMX and Wicket viewers over the same domain model.

#### Scenario: Petclinic sample starts
- **WHEN** the documented Maven profile launches the sample
- **THEN** Pet Owners, Pets, Visits, object home, service actions, object actions, choices, defaults, validation, effective menus, effective grids, and fixture data are available through GraphQL and the HTMX viewer
- **AND** the Wicket viewer is available at its documented comparison path over the same state

#### Scenario: Copied source is reviewed
- **WHEN** a maintainer inspects the Petclinic sample
- **THEN** provenance identifies repository commit `16a10608129ca9ce8ae04d21df1462f4d69ac018`, copied concepts, license, omissions, and current-API porting changes
- **AND** obsolete starter, security, operational, and deployment infrastructure is not represented as current viewer behavior

#### Scenario: Petclinic object has no custom page
- **WHEN** its exact logical type has no custom fragment factory
- **THEN** the HTMX router uses the generic `<causeway-object>` page

#### Scenario: Petclinic object has a custom page
- **WHEN** one exact logical type is registered for the sample's custom fragment factory
- **THEN** the custom page composes ordinary HTML and semantic components beneath one route context
- **AND** demonstrates precedence without adding custom-page knowledge to `<causeway-object>`

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

### Requirement: Opt-in route-lazy reference widget delivery
The generic HTMX viewer SHALL load the candidate reference-widget closure only when explicit configuration and an eligible semantic reference editor require it.
Generic routes, custom object fragments, menus, and shell behavior that do not use the pilot MUST remain independent of candidate readiness and requests.

#### Scenario: Route contains an enabled candidate reference editor
- **WHEN** route rendering encounters the first eligible explicitly enabled reference editor
- **THEN** the viewer resolves the same-origin packaged candidate entry lazily and upgrades the internal editor
- **AND** the route retains one disposable Causeway context and existing canonical navigation

#### Scenario: Route contains no candidate editor
- **WHEN** a generic or custom route uses existing editors or no reference input
- **THEN** the browser requests no Vaadin asset
- **AND** viewer readiness, menu behavior, custom-fragment composition, and route replacement remain unchanged

#### Scenario: Candidate loading fails
- **WHEN** the route-lazy asset cannot load, initialize, satisfy CSP, or pass supported-browser checks
- **THEN** the viewer uses the existing reference editor or presents a Causeway-owned recoverable failure according to configuration
- **AND** does not leave an unupgraded raw toolkit tag as ordinary domain UI

### Requirement: Production CSP compatibility for candidate widgets
The generic HTMX viewer SHALL preserve a documented security-reviewed Content Security Policy when the candidate pilot is enabled.
The viewer MUST test component connection, overlay operation, interaction, responsive layout, and route disposal with zero unexpected policy violations and MUST NOT require blanket inline-style permission.

#### Scenario: Production-like CSP journey runs
- **WHEN** Petclinic exercises single and multi-reference candidate states under the documented production-like policy
- **THEN** browser violation events, console output, requests, overlays, focus, overflow, and viewer readiness satisfy the accepted baseline
- **AND** the journey fails on any unclassified or newly introduced violation

#### Scenario: Application does not enable the pilot
- **WHEN** an application retains default viewer configuration
- **THEN** its CSP, browser assets, routes, semantic markup, and custom fragment contract remain unchanged
- **AND** no Vaadin dependency is required at browser runtime

### Requirement: Sample-scoped pilot qualification
Petclinic and the vanilla HTML sample SHALL exercise the candidate pilot as explicit qualification consumers before any wider default adoption.
Their browser evidence MUST cover semantic correctness, keyboard operation, accessibility, narrow and themed presentation, cancellation, repeated route replacement, external-request isolation, and rollback.

#### Scenario: Sample qualification passes
- **WHEN** the complete pilot acceptance suite runs headlessly
- **THEN** existing viewer tests and candidate-specific CSP, accessibility, lifecycle, package, bundle, and interaction assertions pass
- **AND** results distinguish adapter defects, toolkit defects, content exceptions, and unsupported GraphQL behavior

#### Scenario: Sample qualification fails
- **WHEN** a hard gate, budget, existing viewer regression, or unsupported production behavior is detected
- **THEN** the pilot remains disabled outside analysis or sample troubleshooting
- **AND** the existing editor remains the supported viewer behavior

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
