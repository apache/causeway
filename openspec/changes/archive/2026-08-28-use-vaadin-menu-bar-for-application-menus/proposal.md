## Why

`<cw-menubars>` currently implements menu buttons, nested disclosures, responsive collapse, keyboard behavior, focus, and styling with Causeway-owned native controls.
Vaadin Menu Bar provides established nested-item, disabled-item, overflow, internationalization, and keyboard behavior that could reduce duplicate presentation work and align application navigation with the selected Vaadin toolkit.

## What Changes

- Add a qualified internal `vaadin-menu-bar` adapter behind the existing `<cw-menubars>`, `<cw-menubar-primary>`, `<cw-menubar-secondary>`, and `<cw-menubar-tertiary>` semantic vocabulary after the Grid tranche has established the common toolkit, packaging, lifecycle, CSP, and rollback patterns.
- Preserve primary, secondary, and tertiary bar identity by using one independently qualified internal Menu Bar per non-empty semantic tier rather than merging the tiers.
- Preserve source order, labels, descriptions, icon hints, sections, action order, visibility, usability, disabled reasons, and independently refreshed service-action state.
- Map each non-empty semantic bar into an internal Vaadin item tree rather than collapsing the three public Causeway tiers into one application-facing menu model.
- Map nested menu and section structure to reviewed Vaadin item children while preserving exact Causeway action identity and disabled reasons.
- Translate Vaadin item activation into the existing Causeway action-request and interaction-controller path; Vaadin callbacks MUST NOT invoke GraphQL or navigation directly.
- Qualify Menu Bar overflow, nested keyboard navigation, pointer behavior, visible focus, accessible names, disabled items, internationalized overflow labels, escape behavior, and focus restoration against the established wide and narrow contracts.
- Replace custom narrow disclosures only when Vaadin overflow and nested menus preserve every visible authorized action without page overflow or order changes; otherwise retain the complete native responsive presentation for that tier at that width.
- Preserve generation-scoped application metadata, menu-resource loading, service-action preparation, cancellation, refresh, stale-result protection, and route-independent stable-shell ownership.
- Keep unauthenticated login, failure, and authentication-challenge chrome native and prevent those routes from requesting Menu Bar assets.
- Use the existing common `component-toolkit=vaadin|native` policy and deprecated compatibility precedence without adding a Menu-Bar-specific public configuration property.
- Retain native menu rendering as explicit toolkit rollback and as automatic fallback for empty tiers, unsupported hierarchy, policy failure, responsive disqualification, adapter-load failure, definition timeout, rendering failure, or accessibility failure.
- Treat one Menu Bar closure failure as family-scoped so every connected tier returns natively while reference, field, action, Grid, GraphQL, routing, and authentication behavior remain unaffected.
- Package Menu Bar and only its reviewed free-core transitive closure independently with pinned inputs, deterministic bytes, checksums, licenses, vulnerability review, exact CSP hashes, compressed budgets, accessibility, keyboard, responsive, theme, and lifecycle evidence.
- Accept default Menu Bar use only if its stable-shell payload, behavior, and fallback evidence remain within reviewed budgets.

## Capabilities

### New Capabilities

- `vaadin-application-menubar-adapter`: Defines semantic-tier mapping, item-tree conversion, invocation boundaries, responsive overflow, accessibility, delivery, and native fallback for application menus.

### Modified Capabilities

- `domain-web-components`: Allow application menu components to use an internal Vaadin Menu Bar while preserving public Causeway elements, semantic ordering, action behavior, events, and lifecycle.
- `generic-htmx-web-component-viewer`: Add stable-shell Menu Bar delivery, CSP, toolkit rollback, configuration, authentication exclusions, and documentation.
- `reference-application-viewer-regression-suite`: Add broad menu hierarchy, disabled, hidden, parameterized, nested, overflow, keyboard, refresh, and action-result journeys in Vaadin and native modes.

## Impact

Because `<cw-menubars>` lives in the stable application shell, default Vaadin Menu Bar selection loads its closure on essentially every authenticated viewer page with a non-empty qualified tier rather than only after an object member connects.
The change affects menu rendering, item-tree adaptation, action activation, focus management, packaged browser assets, CSP, themes, authentication chrome, samples, and browser evidence.
Public menu resources, GraphQL service-action operations, `<cw-menubars>` markup, canonical routes, action results, and application-facing Causeway events remain stable.
It introduces no Vaadin Flow, Binder, server-side Vaadin state, commercial component, CDN asset, telemetry, direct navigation policy, or raw application-facing Menu Bar API.
