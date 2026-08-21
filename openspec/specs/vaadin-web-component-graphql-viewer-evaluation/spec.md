# vaadin-web-component-graphql-viewer-evaluation Specification

## Purpose
Defines requirements for evaluating standalone Vaadin Web Components as GraphQL-backed widgets for the Causeway web-component viewer while preserving semantic contracts, custom HTML composition, free-core licensing, deterministic Maven packaging, and production security constraints.

## Requirements
### Requirement: Standalone browser-side Vaadin evaluation
The evaluation SHALL use Vaadin Web Components directly in the browser as candidate implementation widgets for the existing GraphQL web-component viewer.
The evaluation MUST NOT use Vaadin Flow bootstrap, routing, server-side component state, Binder, Java DataProvider, or the Flow client-server synchronization protocol.
The public rich GraphQL schema and execution contract MUST remain the exclusive source of domain descriptions, object state, choices, validation, interactions, and collection data.

#### Scenario: Candidate page loads
- **WHEN** a maintainer opens a documented Vaadin prototype page
- **THEN** the browser loads pinned Vaadin Web Component modules without a Flow runtime or server-side Vaadin UI
- **AND** all domain data requests use the existing public GraphQL execution path

#### Scenario: Server-side Vaadin strategy is discussed
- **WHEN** the decision record considers consistency with a possible server-side Vaadin viewer
- **THEN** it treats shared widgets and visual language as strategic context only
- **AND** does not require shared state management, renderer code, routing, or extension APIs

#### Scenario: Required behavior needs Flow
- **WHEN** a candidate widget cannot provide required behavior through its documented standalone browser API
- **THEN** the evaluation records that behavior as an integration failure or limitation
- **AND** does not introduce Flow to make the prototype pass

### Requirement: GraphQL-backed reference selection prototypes
The evaluation SHALL prototype searchable single-reference and multi-reference selection against existing rich GraphQL choice and interaction operations.
The prototypes MUST assess filter text, debounce, paging, stable identity, initial values, clearing, disabled reasons, validation, cancellation, stale responses, and deterministic semantic selection events.

#### Scenario: Single reference is searched
- **WHEN** a user enters filter text into the candidate Combo Box
- **THEN** the adapter obtains matching reference choices through the public GraphQL contract
- **AND** presents loading, empty, partial-error, terminal-error, and paged-result states without losing focus or the current valid selection

#### Scenario: Multi-reference choices change
- **WHEN** a user adds or removes a reference in the candidate multi-selection control
- **THEN** the adapter preserves stable object identities and deterministic selection order
- **AND** emits Causeway-owned semantic change information rather than requiring application code to interpret Vaadin-specific events

#### Scenario: Lookup responses arrive out of order
- **WHEN** a newer filter or route generation supersedes an in-flight choice request
- **THEN** the adapter cancels or ignores stale work
- **AND** does not display results, validation, or selection state from the superseded request

#### Scenario: Existing GraphQL choices are insufficient
- **WHEN** the public GraphQL contract cannot express required filtering, paging, identity, or multi-selection behavior
- **THEN** the evaluation records the exact API gap and its impact
- **AND** does not create a private candidate-only endpoint

### Requirement: GraphQL-backed lazy collection prototype
The evaluation SHALL prototype a Vaadin Grid backed by existing GraphQL collection-window and object-context operations.
The Grid prototype MUST assess lazy loading, count, paging or visible windows, supported sorting and filtering, stable row identity, object navigation, empty state, partial errors, terminal errors, selection, focus, and constrained-width presentation.

#### Scenario: Grid requests a data window
- **WHEN** the candidate Grid needs rows for a visible range or page
- **THEN** the adapter maps the request to the public GraphQL collection contract
- **AND** reconciles the response by stable row identity without duplicating or reordering unrelated rows

#### Scenario: Grid query changes
- **WHEN** the user changes supported sorting, filtering, or page state
- **THEN** the adapter invalidates superseded collection work and requests the corresponding GraphQL window
- **AND** preserves keyboard-operable loading and error presentation

#### Scenario: User follows a row object
- **WHEN** a user activates the semantic object link or documented row-navigation affordance
- **THEN** navigation uses the viewer's canonical bookmark route
- **AND** does not expose an opaque Vaadin row identity as the public domain identity

#### Scenario: Existing GraphQL collection operations are insufficient
- **WHEN** Grid count, sorting, filtering, paging, or identity cannot be represented by the public GraphQL schema
- **THEN** the evaluation records the exact schema or adapter gap
- **AND** does not claim parity through fixture-local data

### Requirement: Representative field and interaction coverage
The evaluation SHALL exercise the candidate widget layer with date, time, date-time, scalar, multiline, enum, boolean, and required-value states plus representative action interaction.
Causeway conversion, disabled reasons, validation, invocation, cancellation, concurrency, and result semantics MUST remain authoritative.

#### Scenario: Typed value is edited
- **WHEN** a user edits a representative date, time, date-time, scalar, enum, boolean, or multiline value
- **THEN** the adapter maps the browser value through existing Causeway conversion and validation behavior
- **AND** presents required, invalid, disabled, read-only, and localized states accessibly

#### Scenario: Action is invoked
- **WHEN** a candidate-backed action prompt has valid arguments and the user invokes it
- **THEN** the existing Causeway interaction controller performs validation and GraphQL invocation
- **AND** scalar, object, collection, void, cancellation, and error outcomes remain semantic Causeway outcomes

#### Scenario: Widget supplies conflicting state management
- **WHEN** Vaadin widget behavior conflicts with existing Causeway validation, dialog, navigation, or interaction state
- **THEN** the adapter keeps Causeway as the authoritative state machine
- **AND** records any duplicate or irreconcilable toolkit behavior as candidate cost

### Requirement: Composable custom HTML page evaluation
The evaluation SHALL demonstrate the candidate widgets inside a router-selected custom object HTML fragment while preserving the generic viewer's exact-type resolution, canonical route identity, and one disposable route-level `<causeway-object-context>`.
Application composition MUST remain possible with ordinary HTML and browser modules rather than requiring Vaadin Flow or Java Vaadin extension APIs.

#### Scenario: Custom object page is selected
- **WHEN** the HTMX router resolves a registered custom fragment for an exact public logical type
- **THEN** the fragment composes semantic Causeway elements and evaluated browser-side Vaadin controls beneath one route object context
- **AND** obtains all domain behavior through GraphQL and browser-side adapters

#### Scenario: Route fragment is replaced repeatedly
- **WHEN** navigation connects and disconnects candidate-backed custom and generic fragments across multiple route generations
- **THEN** listeners, overlays, pending requests, focus, and component state remain generation-scoped
- **AND** stale widgets cannot update the current page

#### Scenario: Application uses only semantic elements
- **WHEN** a custom page author chooses not to use raw Vaadin tags
- **THEN** standard Causeway object, property, action, reference, and collection behavior remains composable through stable `<causeway-*>` elements
- **AND** the page does not need Vaadin event or data-provider knowledge

### Requirement: Explicit extension-tier decision
The evaluation SHALL compare internal-only Vaadin rendering with an optional allowlisted raw-widget tier for advanced custom HTML pages.
The final decision MUST distinguish stable Causeway semantic contracts from any Vaadin API that is version-coupled, lower-level, or application-owned.

#### Scenario: Vaadin remains internal
- **WHEN** the internal-only strategy is evaluated
- **THEN** applications address Causeway elements, events, routes, GraphQL contexts, and `--causeway-*` variables only
- **AND** adapter effort and toolkit replaceability are recorded

#### Scenario: Raw widgets are offered to applications
- **WHEN** the optional raw-widget strategy is evaluated
- **THEN** the evidence defines the allowlisted packages, loading contract, version policy, theming boundary, accessibility responsibility, and compatibility expectations
- **AND** does not describe raw `<vaadin-*>` elements as equivalent to long-lived Causeway domain semantics

#### Scenario: Standard behavior requires raw Vaadin APIs
- **WHEN** an application must address Vaadin-specific tags or events to obtain ordinary Causeway domain behavior
- **THEN** the candidate receives an explicit contract-leakage and migration cost
- **AND** any adoption recommendation identifies the required later specification change

### Requirement: Free-core licensing and deterministic packaging
Every required Vaadin module SHALL pass an explicit package-level license, provenance, maintenance, and commercial-feature gate.
The evaluation MUST pin all direct and transitive inputs and demonstrate selective, offline, Maven-driven packaging without runtime CDN access.
Commercially licensed Vaadin components MUST NOT be required by a passing strategy.

#### Scenario: Candidate package set is frozen
- **WHEN** prototype implementation begins
- **THEN** the evidence records exact package versions, integrity values, licenses, repositories, dependency closure, release status, and imported entry points
- **AND** identifies which packages and documentation examples are free core, ambiguous, or commercial

#### Scenario: Selective browser bundle is built
- **WHEN** a maintainer runs the documented analysis build from a clean checkout and package cache policy
- **THEN** it produces deterministic JavaScript, CSS, theme, icon, font, license, and notice outputs for only the evaluated widget set
- **AND** a Maven build packages those outputs beneath `META-INF/resources` without network access at browser runtime

#### Scenario: Required component is commercial
- **WHEN** behavior required for reference selection, collections, fields, interactions, or custom pages depends on Vaadin Pro or another unapproved license
- **THEN** the candidate fails the free-core hard gate for that behavior
- **AND** the analysis records the gap rather than substituting an unlicensed or evaluation-only component

### Requirement: Comparable quality and lifecycle evidence
The evaluation SHALL retain comparable evidence for widget coverage, GraphQL correctness, accessibility, keyboard and focus behavior, HTMX lifecycle, responsive presentation, light and dark themes, reduced motion, forced colors, visual consistency, bundle size, request count, initialization cost, and rendering performance.
The current web-component viewer and relevant Wicket Select2 behavior SHALL provide the documented baselines.

#### Scenario: Accessibility journeys run
- **WHEN** browser evaluation exercises reference selectors, Grid, fields, validation, dialogs, navigation, and custom pages
- **THEN** automated results and manual keyboard journeys record names, roles, focus order, active descendants, announcements, hidden focus, errors, and contrast
- **AND** toolkit defects are separated from adapter defects and product-content exceptions

#### Scenario: HTMX lifecycle evidence runs
- **WHEN** candidate-backed fragments are repeatedly inserted, superseded, and removed
- **THEN** evidence covers custom-element upgrade, request cancellation, event cleanup, overlay cleanup, focus restoration, console errors, and retained DOM growth
- **AND** includes both generic and custom object pages

#### Scenario: Performance evidence runs
- **WHEN** the candidate is measured from cold and warm cache states
- **THEN** evidence distinguishes complete installed packages, selective bundle and shared chunks, compressed transfer, requests, parse and initialization cost, route-ready time, and relevant rendering timings
- **AND** compares eager shell loading with documented route-lazy alternatives

### Requirement: Widget-first adoption decision
The evaluation SHALL publish hard-gate results, a weighted decision matrix, architectural decision record, GraphQL gap analysis, extension-tier recommendation, migration estimate, and explicit recommendation to adopt Vaadin free core, adopt a constrained subset, retain current components, or compare another enterprise suite.
A production Vaadin dependency MUST NOT be added by this analysis change.

#### Scenario: Decision matrix is completed
- **WHEN** all required prototypes and evidence are complete
- **THEN** the score weights domain widget coverage and parity at 30%, GraphQL architecture and custom-page composability at 25%, accessibility at 15%, supply chain and packaging at 15%, performance at 10%, and theming at 5%
- **AND** every score links to retained evidence or a documented limitation

#### Scenario: Adoption is recommended
- **WHEN** Vaadin free core passes every hard gate and demonstrates sufficient value over current components and add-on assembly
- **THEN** the decision defines a staged internal-adapter and optional-extension rollout with budgets and rollback
- **AND** outlines a separate implementation proposal and all required specification deltas

#### Scenario: Candidate is insufficient
- **WHEN** Vaadin fails a hard gate or does not provide sufficient net benefit
- **THEN** the decision records whether current components should be retained or the same fixture should be used to evaluate UI5 or another free enterprise suite
- **AND** leaves production dependencies and runtime behavior unchanged
