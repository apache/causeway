## ADDED Requirements

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

## MODIFIED Requirements

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
