## Context

The HTMX and Vue Petclinic applications share deterministic domain classes, GraphQL behavior, web components, route syntax, and fixture identities, but they do not currently share a presentation contract.
A headless comparison at a 1440-pixel viewport found that the Vue shell uses a two-row 116-pixel dark header instead of the HTMX single-row 52-pixel header, reverses the primary menu order, exposes three utility menus instead of the HTMX `System` group, applies different typography and spacing, omits the Wicket comparison footer link, and leaves a route-container focus border visible around the page.

The route differences are more substantial.
HTMX provides application-authored pages for `petclinic.HomePage`, `petclinic.PetOwner`, `petclinic.Pet`, and `petclinic.Visit`, whereas Vue registers only `petclinic.PetOwner` and delegates the other three routes to generic `<cw-object>` composition.
The Vue owner page also combines the HTMX Identity, Contact, and Details sections, omits `noOwners` and `daysSinceLastVisit`, places Visits in the narrow grid column, and omits portions of the authoritative standalone-result and visit-collection declarations.
Generic Vue pages consequently expose technical `id` and `version` members and different actions or collections on Pet and Visit routes.

The comparison concerns application-owned composition and theme rather than a difference in canonical domain behavior.
The existing ownership boundary remains: metadata and semantic components determine meaning, while each host application selects pages, binds values, routes semantic events, and owns shell geometry.

## Goals / Non-Goals

**Goals:**

- Make the Vue Petclinic shell and its four user-visible route types presentation-equivalent to HTMX at wide and narrow viewports.
- Preserve the HTMX member selection, grouping, labels, descriptions, action placement, collection behavior, and paging values in source-visible Vue pages.
- Keep Vue routing and lifecycle semantics intact while eliminating accidental visible differences such as technical members, title suffixes, menu grouping, and persistent container outlines.
- Retain executable generic-fallback coverage without using one of the four reconciled Petclinic routes as the fallback example.
- Add stable regression checks for semantic structure and high-value computed presentation properties without requiring pixel-identical rendering.

**Non-Goals:**

- Changing HTMX page composition, the shared Petclinic domain, GraphQL metadata, or custom-element behavior to accommodate Vue.
- Making HTMX and Vue DOM trees byte-for-byte identical.
- Replacing host-owned shells with a framework-neutral shell runtime or shared server-side template language.
- Introducing visual screenshot goldens whose antialiasing or browser-version sensitivity would make the build unreliable.
- Changing Vue Router ownership, canonical route encoding, SSR policy, component-toolkit policy, or package peer dependencies.
- Reconciling Wicket itself with either web-component viewer.

## Decisions

### Use an explicit presentation-equivalence contract rather than pixel identity

Parity means that the same shell regions, menu groups, route headings, sections, declared members, actions, result outlets, collection columns, preview affordances, pagination limits, and responsive column relationships are presented in the same order.
Tests will also compare bounded computed properties such as header row count and height range, content insets, column ratios, breakpoint collapse, footer alignment, typography family, and palette tokens.
Minor browser rendering differences and framework-required wrapper elements are permitted.

Pixel snapshots were considered but rejected because font rasterization, Vaadin internals, and browser upgrades would create noise unrelated to presentation intent.
A purely subjective manual review was also rejected because it would not prevent recurrence.

### Keep HTMX source authoritative and translate it into Vue single-file components

The four HTMX page templates and the Petclinic HTMX application stylesheet define the initial parity baseline.
Vue will register exact-type single-file components for HomePage, PetOwner, Pet, and Visit, using Vue property bindings only where structured or dynamic values require them.
The components will preserve the required one-context and one-controller boundary contract and will not reproduce metadata or interaction logic in Vue state.

Reusing generic `<cw-object>` for Pet, Visit, or HomePage was rejected because generic layout intentionally exposes the complete effective grid and therefore cannot reproduce application-authored member selection or section composition.
Generating Vue templates from HTMX resources was rejected because the formats have different binding and lifecycle semantics and such generation would introduce a new template compiler for a small sample.

### Reuse the authoritative class vocabulary and port only application-owned CSS

Vue pages will adopt the HTMX Petclinic class names and document structure where the semantics match, and `petclinic.css` will express the corresponding application-owned rules using the established foundation design tokens.
The Vue shell will mirror the HTMX shell geometry and branding while retaining Vue event and router bindings.
Viewer-specific HTMX selectors and transport behavior will not be copied.

A new shared presentation module was considered but rejected for this change because it would couple a framework-neutral domain sample to browser assets or add a module whose only consumers are two acceptance applications.
The parity tests provide the drift control while allowing each host to remain independently authored.

### Reconcile menus through supported projection and application configuration

The implementation will first identify whether the current menu difference comes from module composition, authentication chrome, application-menu metadata, or shell styling.
It will then use existing menu projection and shell contracts to present the HTMX reference order and grouping; it will not rewrite GraphQL menu payloads, duplicate menu actions, or hide domain actions with arbitrary selectors.
At a wide viewport the visible top-level presentation will be Pet Owners, Visits, and the reference utility grouping, with narrow behavior preserving keyboard access and all actions.

Hardcoded replacement menus were rejected because they would duplicate semantic service-action dispatch and undermine the generic menu components.

### Preserve generic fallback with a deliberate acceptance fixture

All four visible Petclinic route types will become exact registrations.
Generic fallback will be exercised through a deliberately unregistered acceptance-only logical type or an existing suitable fixture that is not part of the reconciled route set.
If a new fixture is required, it will live in the shared sample domain without Vue dependencies and will have stable identity and bounded metadata.

Leaving one user-visible Petclinic route generic was rejected because it makes parity impossible.
Removing generic-fallback acceptance was rejected because fallback remains a required public viewer capability.

### Test semantic parity and responsive invariants headlessly

The Vue Playwright suite will cover each reconciled route in both native and Vaadin policies where component presentation can differ, and representative wide and narrow viewports will cover shell and grid behavior.
Assertions will identify the expected sections and members, their order and containment, menu labels and grouping, result outlets, collection configuration and row previews, document-title suffix, footer content, and absence of technical fields.
Computed-style and bounding-box assertions will use tolerances and structural relationships rather than exact full-page coordinates.

The existing HTMX acceptance behavior will remain the oracle.
Where a shared fixture or helper is practical, both suites will consume the same parity expectations; otherwise the Vue tests will name the corresponding HTMX source contract explicitly.
All browser execution remains headless by default.

## Risks / Trade-offs

- [Risk] Independently authored HTMX and Vue templates can drift again. → Mitigation: encode the parity matrix in focused acceptance assertions and document HTMX as the change authority.
- [Risk] Menu differences may reveal a missing generic Vue shell integration capability rather than sample CSS. → Mitigation: use existing semantic menu and authentication contracts first; if a public viewer API change is truly required, stop implementation and update this proposal/spec before expanding scope.
- [Risk] A visible focus ring may be removed in pursuit of screenshot parity. → Mitigation: retain a visible keyboard focus indication on the heading or route landmark and suppress only accidental persistent container framing.
- [Risk] Exact custom pages reduce the sample's obvious generic-fallback demonstration. → Mitigation: retain a dedicated fallback fixture and document both paths.
- [Risk] CSS parity can become overfitted to one desktop width. → Mitigation: specify relationships, token usage, and breakpoints and test at least one wide and one narrow viewport.
- [Risk] Adding an acceptance fixture to the shared domain can affect menus or fixture cardinalities. → Mitigation: prefer an existing suitable type; if adding one, keep it excluded from normal Petclinic menus and seeded collections.

## Migration Plan

1. Capture the authoritative shell and four-route parity matrix in tests or a small test fixture before changing Vue markup.
2. Reconcile shell structure, menu projection, title and footer behavior, and responsive theme rules.
3. Add and register HomePage, Pet, and Visit Vue components and reconcile PetOwner against its HTMX template.
4. Move generic-fallback acceptance to the deliberate fixture and regenerate committed Vite assets.
5. Run Vue package tests, Vue and HTMX integration and Playwright suites, both component-toolkit policies, frontend stale-output checks, Maven verification, and licensing checks.

Rollback consists of reverting the Vue sample source, generated assets, and tests because no persisted data, route format, or public API migration is involved.

## Open Questions

None that block implementation.
The menu investigation is intentionally an implementation task bounded by the existing projection contracts; discovery of a required public Vue viewer API would require planning review before code proceeds.
