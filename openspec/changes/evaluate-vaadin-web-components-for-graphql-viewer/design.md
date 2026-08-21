## Context

The web-component viewer deliberately separates domain semantics from page composition.
Public rich GraphQL operations supply type descriptions, object state, choices, validation, interactions, and collection data; `<causeway-*>` elements translate that contract into semantic browser behavior; and the HTMX router selects generic or application-provided HTML fragments for individual domain object types.

The archived theming analysis found that Bootstrap is a credible visual-system source but not a comprehensive widget suite.
The Wicket viewer depends extensively on WicketStuff Select2 for searchable single and multi-reference choices, delayed remote lookup, stable object identity, and selection lifecycle events, so adopting a theme alone would reproduce the existing pattern of assembling separate add-ons.

Vaadin publishes standalone Web Components for filtered and lazy Combo Box selection, multi-selection, Grid data providers, date and time fields, uploads, and other application controls.
Vaadin Flow wraps those browser components for server-side Java applications, but this evaluation does not use Flow: the existing GraphQL contract remains the browser viewer's data and interaction plane.
A possible future server-side Vaadin viewer is architecturally independent and is relevant only as a potential source of visual and widget consistency.

The current production modules package ES modules and CSS as Maven resources without a frontend package manager or runtime CDN.
The evaluation therefore needs an analysis-only npm lock and selective-bundle proof while keeping production POMs and browser behavior unchanged.

## Goals / Non-Goals

**Goals:**

- Determine whether the Apache-2.0 Vaadin Web Components free core can provide enough low-level widget behavior to avoid a Bootstrap-plus-add-ons outcome.
- Prove or disprove that Vaadin Grid and selection components can consume the existing public GraphQL APIs directly from browser-side adapters.
- Preserve the semantic `<causeway-*>` contract, GraphQL object context, HTMX lifecycle, and router-selected custom HTML page model.
- Evaluate a tiered extension model with stable Causeway elements and optional allowlisted raw Vaadin widgets for advanced custom pages.
- Measure accessibility, lifecycle correctness, theming, payload, dependencies, licensing, offline packaging, and maintenance cost using reproducible evidence.
- Produce a recommendation and bounded implementation outline without adopting Vaadin in production.

**Non-Goals:**

- Introducing Vaadin Flow, Flow routing, server-side component state, Binder, or Java DataProvider APIs into the GraphQL web-component viewer.
- Implementing or specifying a server-side Vaadin viewer.
- Replacing the public GraphQL data plane with Vaadin-specific endpoints or server callbacks.
- Changing production `<causeway-*>` elements, HTMX routes, custom-page resolution, Maven dependencies, or default presentation.
- Evaluating commercially licensed Vaadin components such as Grid Pro or Rich Text Editor as required capabilities.
- Guaranteeing that a future server-side Vaadin viewer shares renderer code or extension APIs with the web-component viewer.

## Decisions

### 1. Treat the work as an analysis-only vertical slice

All new JavaScript, CSS, lockfiles, fixtures, Maven packaging proofs, screenshots, and reports remain beneath the OpenSpec change directory.
The production foundation, HTMX viewer, GraphQL schema, and samples are observed or exercised but not modified.

A separate OpenSpec proposal is mandatory before any runtime dependency or supported Vaadin extension surface is introduced.
This preserves the rollback-free character of the research and keeps candidate convenience code from becoming an accidental production contract.

### 2. Evaluate only an explicit free-core package allowlist

The evaluation will freeze exact versions, integrity values, transitive dependencies, licenses, provenance, and release status for every imported Vaadin package.
The initial package families to assess are Grid, Combo Box, Multi-Select Combo Box, Date Picker, Time Picker or Date-Time Picker, Upload, and the minimum supporting field, overlay, icon, and theme modules they require.

Every required package must be available under Apache-2.0 or another project-approved permissive license.
Packages declaring a commercial, subscription, evaluation, or ambiguous product license are excluded even if they improve prototype scores.
The analysis must detect when documentation examples silently rely on a Pro package.

### 3. Use Vaadin Web Components directly without Flow

Prototype pages import browser ES modules and instantiate `<vaadin-*>` elements directly.
No Flow bootstrap, server-side element tree, Flow client protocol, Java component wrapper, Flow router, Binder, or server DataProvider participates.

This decision isolates the question being asked: whether Vaadin's browser controls are suitable implementation building blocks for a GraphQL-driven and HTML-composable viewer.
The possible server-side viewer remains free to use Flow later without changing this architecture.

### 4. Keep GraphQL authoritative through explicit browser adapters

The prototype adapter boundary is:

```text
custom or generic HTML fragment
        |
<causeway-object-context> and semantic descendants
        |
Causeway-owned Vaadin renderer adapter
        |
<vaadin-combo-box>, <vaadin-grid>, or field component
        |
public rich GraphQL schema and execution contract
```

Combo Box adapters translate filter text, page requests, cancellation, stable object identity, current selection, disabled reasons, and validation into existing GraphQL choice and interaction operations.
Grid adapters translate visible-range or page requests, sorting, supported filtering, stable row identity, selection, object navigation, loading, partial errors, and terminal errors into existing collection-window and object-context behavior.
Date, time, scalar, and action adapters translate browser values without bypassing Causeway conversion, validation, concurrency, cancellation, or result semantics.

When the current GraphQL API cannot express a required operation, the prototype records an API gap rather than creating an analysis-only endpoint that makes the integration appear viable.
Any GraphQL extension requires its own specification delta and implementation proposal.

### 5. Evaluate both semantic wrappers and optional raw widget composition

Stable Causeway elements remain the primary page-authoring and application compatibility contract.
A generic page or ordinary custom object page must be able to obtain richer behavior through Causeway-owned wrappers without addressing Vaadin-specific events or data-provider protocols.

The analysis also evaluates whether an allowlisted raw `<vaadin-*>` tier is useful for advanced custom HTML pages.
That tier, if recommended, is explicitly lower-level, tied to the viewer's bundled Vaadin version, and separate from the long-lived semantic contract.
Applications must not be forced to use raw Vaadin tags to obtain standard Causeway object behavior.

The decision record will compare:

- Internal-only Vaadin components behind Causeway renderers.
- A supported but version-coupled raw-widget profile for custom pages.
- Unrestricted application-owned third-party widgets outside the viewer distribution.

### 6. Prototype the widgets that drove the strategy change

The shared fixture and bounded Petclinic integration cover:

- Searchable single-reference selection with debounce, paging, stable identity, clearing, disabled state, and validation.
- Searchable multi-reference selection with token or chip presentation and deterministic add and remove events.
- Lazy collection Grid with paging or visible-window loading, sorting, supported filtering, row identity, object navigation, empty state, errors, and narrow-layout behavior.
- Date, time, and date-time action parameters and editable properties with locale, conversion, required-state, and validation evidence.
- Standard scalar input, multiline input, enum selection, boolean selection, action invocation, cancellation, loading, scalar result, object result, collection result, and void completion where needed to prove adapter consistency.
- A router-selected custom object fragment containing one route-level object context and a mixture of semantic Causeway elements and evaluated Vaadin controls.
- Repeated HTMX connection, disconnection, route replacement, stale-request cancellation, focus restoration, and listener cleanup.

The Wicket Select2 implementation and current web-component behavior supply the parity baseline for reference choices.
The existing foundation and Petclinic fixtures supply the baseline for navigation, actions, collections, accessibility, and theme behavior.

### 7. Preserve a Causeway-owned theme boundary

The prototype evaluates Vaadin's supplied theme and component custom properties without making Vaadin tokens the application-facing contract.
Existing `--causeway-*` variables remain the stable customization surface and map into candidate-specific properties or parts inside adapters.

Evidence covers desktop and narrow layouts, light and dark modes, reduced motion, forced colors, focus visibility, disabled states, validation, long labels, dense collections, and page-level overflow.
The analysis records any shadow DOM part that cannot be themed sufficiently or any global theme requirement that leaks into unrelated custom HTML.

### 8. Prove deterministic selective packaging

An analysis-only package manifest and lockfile pin the evaluated modules.
A reproducible build emits only the component modules, shared chunks, styles, icons, fonts, and licenses needed by the fixture.
A small analysis Maven module packages those outputs under `META-INF/resources` without runtime CDN access.

The evidence distinguishes source package size, complete installed dependency size, selective production bundle size, compressed transfer size, request count, parse and initialization cost, and per-route lazy-loading opportunities.
The recommended production path must be invocable by Maven and must document cache, checksum, release, NOTICE, and update implications.

### 9. Make widget completeness the leading decision criterion

Hard gates are:

- Approved free-core licensing and provenance for every required package.
- No Vaadin Flow runtime or state dependency in the GraphQL viewer.
- Exclusive use of the public GraphQL contract for domain data and interactions.
- Preservation of custom HTML page resolution and semantic Causeway composition.
- Credible pinned, offline, Maven-driven packaging.
- Keyboard-operable and screen-reader-credible reference selection, Grid, dialogs, validation, and navigation.
- Working searchable reference selection and lazy collection behavior without a separate Select2-like widget library.

Passing strategies are scored using:

- Domain widget coverage and behavior parity: 30%.
- GraphQL architecture and custom-page composability: 25%.
- Accessibility and interaction correctness: 15%.
- Supply chain, maintenance, and Maven packaging: 15%.
- Performance and selective delivery: 10%.
- Theming and visual consistency: 5%.

The decision compares the Vaadin free-core prototype with the current web-component baseline and relevant Wicket Select2 behavior.
It may recommend adoption, a constrained widget subset, retention of current components, or a follow-up comparison with another enterprise Web Component suite.

## Risks / Trade-offs

- [The existing GraphQL schema may not express Vaadin's filter, count, sorting, or lazy-window protocol] → Map only to public operations, retain raw request evidence, and identify exact schema deltas rather than adding hidden endpoints.
- [Vaadin's component API may be easier through Flow than through standalone browser adapters] → Exercise only documented client-side Web Component APIs and treat any Flow requirement as a hard failure for this viewer.
- [A nominally free component may depend on commercially licensed behavior] → Produce a package-level license closure and fail the candidate when required behavior crosses the free-core boundary.
- [Shadow DOM and theme internals may prevent Causeway-compatible customization] → Test every representative state through documented custom properties and parts and retain the current theme as the baseline.
- [Raw Vaadin tags could become an accidental permanent application contract] → Separate semantic and lower-level tiers, document version coupling, and score internal-only integration independently.
- [Grid and Combo Box bundles may be too large for common routes] → Measure selective entry points, shared chunks, route lazy loading, parse cost, and realistic cache behavior before recommending adoption.
- [HTMX fragment replacement may leak listeners, pending requests, overlays, or focus] → Run repeated connect and disconnect journeys with request cancellation, DOM-count, focus, console, and heap-oriented evidence.
- [A shared Vaadin visual language may overstate code reuse with a future server-side viewer] → Record strategic consistency only and keep Flow architecture, renderer code, and Java extension APIs outside this change.
- [The evaluation may optimize for Petclinic while missing Causeway scalar or collection cases] → Derive the fixture from the existing semantic editor matrix, rich GraphQL value semantics, Wicket choices behavior, and representative long, empty, error, and large-data states.

## Migration Plan

This change has no production migration because it adds only analysis artifacts.

If adoption is recommended, a separate implementation proposal will stage a free-core package allowlist, Maven-integrated selective build, internal adapters, opt-in sample use, compatibility and extension policy, acceptance gates, and rollback to current renderers.
The initial production slice should target the highest-value gap, normally searchable reference selection or lazy collections, rather than replacing every control at once.

If the candidate fails a hard gate, the analysis harness remains archived evidence and production modules remain unchanged.
A subsequent comparison may evaluate UI5 Web Components or another free enterprise suite against the same fixture without weakening the GraphQL and composability requirements.

## Open Questions

- Which exact Vaadin package set and version provide all required free-core behavior without importing Pro code or assets?
- Can current rich GraphQL choice operations support debounced filter text, paging, stable identities, and multi-selection at the scale expected by Combo Box?
- Can current collection-window operations support Grid count, sorting, filtering, stable row identity, and request cancellation without schema changes?
- Should a future implementation support an allowlisted raw Vaadin tier, or keep all bundled Vaadin tags internal?
- Can the supplied Vaadin theme map cleanly to `--causeway-*`, or is a Causeway-owned theme package required?
- What browser floor is required by the selected Vaadin release and its accessibility behavior?
- Which components should load in the shell and which should be route-lazy chunks?
- Is visual consistency with a possible server-side Vaadin viewer valuable enough to influence theme selection without creating architectural coupling?
