## Context

The archived `evaluate-vaadin-web-components-for-graphql-viewer` change demonstrated that Vaadin 25.2.8 free-core reference controls can consume GraphQL-shaped choices, preserve canonical identities, operate without Flow, survive route replacement, and package selectively through Maven.
The evaluated cold reference closure was 57,145 bytes gzip, all Vaadin runtime packages declared Apache-2.0, automated accessibility journeys reported zero violations, and no external runtime request occurred.
The real Petclinic check also recorded four component-originated inline-style violations under the viewer's current `style-src 'self'` policy.
Basic interactions continued in that check, but blocked styles can invalidate overlay position, sizing, focus indication, or responsive presentation, so production integration must fail closed until the exact operations and an acceptable remedy are proven.

The existing GraphQL autocomplete operation accepts search text but does not expose page, size, total count, or continuation state.
The current Wicket Select2 integration also does not truly page its returned choices, so a bounded search-only pilot can establish parity without a candidate-only endpoint, but it must not claim unbounded remote paging.

The browser viewer already owns route lifecycle, GraphQL state, validation, interaction outcomes, semantic events, canonical navigation, and editor selection.
Vaadin must remain an internal rendering implementation beneath those contracts.

## Goals / Non-Goals

**Goals:**

- Resolve or reject strict-CSP compatibility before adding a production Vaadin dependency.
- Add an opt-in single-reference and multi-reference editor pilot using only approved Vaadin free-core browser components.
- Preserve GraphQL as the sole authority for choices, values, validation, mutation, interaction, cancellation, and domain identity.
- Keep public Causeway tags, semantic events, routes, route contexts, and theme variables stable.
- Package deterministic selective assets through Maven with no browser runtime CDN or external request.
- Keep unaffected routes free of Vaadin requests and keep the existing editor available for rollback.
- Establish measurable CSP, accessibility, lifecycle, performance, licensing, and sample acceptance gates.

**Non-Goals:**

- Vaadin Flow, Binder, Java `DataProvider`, server-side routing, or the Flow synchronization protocol.
- Grid, broad field replacement, action-dialog replacement, upload, or application-shell replacement.
- A supported raw `<vaadin-*>` extension tier for application code.
- A new private GraphQL endpoint or a claim of server-paged autocomplete.
- Default production-wide enablement.
- Any server-side Vaadin viewer or Java Vaadin extension model.

## Decisions

### Treat CSP compatibility as a stage-zero stop gate

The first implementation stage will run one control and one operation at a time under the exact production policy and record `SecurityPolicyViolationEvent` data, browser console evidence, violated directive, source location where available, and whether the operation creates a style element, style attribute, CSSOM mutation, or Constructable StyleSheet.
The matrix will cover disconnected and connected controls, opening and closing overlays, filtering, selecting, clearing, validation, disabled state, narrow layout, dark mode, route removal, and repeated reconnection for both Combo Box variants.

The accepted result must produce zero unexpected violations under a documented policy approved for the viewer.
Static styles should remain same-origin external assets or approved Constructable StyleSheets.
Nonce propagation may be used only where the browser and component path honor it consistently.
A CSP Level 3 distinction between `style-src-elem` and `style-src-attr` may be evaluated when dynamic attributes are the irreducible source, but any policy change requires explicit security rationale and browser coverage.
A blanket `style-src 'unsafe-inline'` is not an acceptable pilot outcome.
If no acceptable strategy exists, implementation stops before adding the dependency to production modules.

This fail-closed sequence is preferred over adding the dependency and repairing policy later because the evaluation already proved that local unrestricted success does not imply real-viewer compatibility.

### Keep Vaadin behind the existing semantic editor registry

The deterministic Causeway editor registry will select the pilot implementation only for supported reference input descriptors and explicit pilot configuration.
Ordinary application markup continues to use existing semantic property and action-parameter elements rather than Vaadin tags.

The adapter owns label, required and optional state, disabled reason, initial selection, clearing, loading, empty and error presentation, canonical identity, validation, cancellation, stale-response suppression, and conversion between widget values and Causeway pending values.
Application code observes Causeway semantic changes and never needs Vaadin `value-changed`, data-provider, overlay, or renderer protocols.

This internal boundary is preferred over a raw-widget profile because it permits toolkit upgrades or rollback without making Vaadin's versioned DOM and events an application contract.

### Use existing GraphQL search with an explicit bounded policy

The adapter will debounce filter text, cancel superseded requests, and reject stale route generations using existing context operations.
It may locally slice one bounded GraphQL result into Vaadin callback pages, but metrics, documentation, and tests must identify this as local presentation rather than server paging.

The pilot configuration must define a maximum accepted result count and minimum search policy appropriate to the sample domain.
If a response exceeds the accepted bound or cannot preserve stable identity, the adapter presents a Causeway-owned limitation state or falls back rather than silently truncating an apparently complete list.
True server paging remains a separate GraphQL capability change.

### Package one route-lazy reference closure

The production build will pin direct and transitive npm inputs and use a selective entry containing only Combo Box, Multi-Select Combo Box, and required shared modules.
Maven will invoke or verify generation, package browser assets beneath `META-INF/resources`, and package accepted license and notice material.
The build will fail on an unexpected version, integrity drift, Pro package, unknown license, vulnerability threshold, external runtime reference, telemetry submission, or nondeterministic generated hash.

The cold closure budget is 65 KB gzip.
Routes that do not instantiate an enabled pilot editor must make zero Vaadin requests.
A broad fourteen-component bundle is not packaged by this change.

### Make rollout explicit and reversible

The current editor remains the default unless an application or sample explicitly enables the pilot for a supported reference shape.
Petclinic and the vanilla HTML sample are the only initial consumers.
Disabling the mapping removes the route-lazy import and restores the previous editor without GraphQL, route, persisted-data, or custom-page migration.

This opt-in approach is preferred over automatic replacement because the score advantage was small and the CSP strategy needs operational evidence before wider use.

### Preserve route-generation ownership

Each adapter belongs to the disposable route-level object or interaction context.
Disconnect, route supersession, and HTMX fragment replacement cancel pending search, remove listeners and overlays, suppress late callbacks, and restore focus according to Causeway lifecycle rules.
Vaadin global state, overlays, or caches must not become a second route context.

### Validate behavior at semantic and real-viewer levels

Foundation tests will verify adapter mapping and semantic events without requiring application knowledge of Vaadin internals.
Headless browser journeys will cover single and multi-selection, initial values, clear, required and disabled states, validation, stale responses, cancellation, keyboard focus, overlays, reconnects, custom fragments, narrow layouts, dark mode, reduced motion, forced colors, and CSP reporting.
Petclinic tests will assert current routing, menu behavior, canonical navigation, no external requests, no Flow runtime, no unexpected CSP violation, and no effect on routes without the pilot.

## Risks / Trade-offs

- [The CSP violation may be fundamental to required Vaadin overlay behavior] → Stop before production dependency adoption and retain the existing editor.
- [A narrowly scoped `style-src-attr` relaxation still increases style-injection capability] → Require explicit security review, retain strict `style-src-elem`, and document the exact dynamic operations and browser matrix.
- [Autocomplete responses may be too large without server paging] → Enforce a documented bound, fail visibly above it, and propose paged GraphQL choices separately when needed.
- [Multi-reference editing may not be valid for every current property or parameter shape] → Enable only introspected shapes with stable list semantics and leave all other shapes on existing editors.
- [Shadow DOM may hide accessibility or theme regressions] → Test semantic accessibility, actual keyboard journeys, supported theme properties, forced colors, and focus behavior in the real viewer.
- [The frontend toolchain increases release complexity] → Pin the lock, disallow lifecycle scripts during acquisition, verify hashes and licenses in Maven, and keep generated output reproducible.
- [The candidate payload may regress ordinary routes] → Use route-lazy loading, enforce the 65 KB cold budget, and assert zero candidate requests on unaffected routes.
- [Vaadin usage-statistics code may remain in the closure] → Prove it inert or exclude it, and fail browser tests on any external request.

## Migration Plan

1. Build the CSP isolation fixture and classify every violation without changing production policy.
2. Select and document an acceptable strategy, obtain security acceptance, and stop the change if no strategy passes.
3. Add the pinned selective frontend build and Maven verification without enabling any editor.
4. Add internal single-reference and supported multi-reference adapters behind explicit configuration.
5. Enable the pilot in vanilla HTML and Petclinic samples only.
6. Run foundation, Maven, Playwright, axe, CSP, lifecycle, package, license, vulnerability, bundle, and external-request gates.
7. Retain opt-in status until a later change reviews production experience and compatibility.

Rollback disables the editor mapping and removes the lazy candidate import.
No GraphQL operation, route, persisted object, semantic event, or custom fragment requires migration.

## Open Questions

- Which exact Vaadin operations generated the four retained violations, and are they style-element or style-attribute violations across supported browsers?
- Can the accepted strategy preserve the current policy exactly, or is a narrowly scoped and security-approved directive change necessary?
- Which existing introspected member shapes provide authoritative multi-reference pending values and validation suitable for the pilot?
- What maximum autocomplete response bound is safe for the initial samples and general opt-in documentation?
- Should the selective generation run during every Maven build or be regenerated only through a verified maintenance profile while checked-in assets are consumed normally?
