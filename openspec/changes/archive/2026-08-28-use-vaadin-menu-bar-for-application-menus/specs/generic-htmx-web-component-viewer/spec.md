## MODIFIED Requirements

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
