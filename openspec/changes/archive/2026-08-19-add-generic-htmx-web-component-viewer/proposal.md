## Why

The semantic web-component library now provides complete object and application-menu components, but users also need an opt-in generic application viewer with canonical routes, deep links, history, custom pages, and generic fallback pages.
The architectural review in `Causeway web components.pdf` concluded that routing, rather than `<causeway-object>` or a framework-neutral page provider, must choose between a logical-type-specific page and the generic object page.
HTMX provides the lightest server-oriented reference implementation of that router boundary.

The existing vanilla sample proves individual contracts but no longer provides a cohesive application experience, and its accumulated styling defects obscure the capabilities already implemented.
A current-Causeway port of the Apache Causeway Petclinic application provides a recognizable domain, effective layouts, menus, home page, actions, collections, and fixture data against which the generic viewer can be judged.
The Wicket viewer over the same Petclinic model provides a concrete visual and interaction reference without requiring the HTMX viewer to depend on Wicket or Bootstrap.

The evidence and ownership boundary are recorded in `coverage-matrix.yaml` entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`.

## What Changes

- Add an opt-in generic HTMX viewer whose primary responsibility is canonical routing, a stable application shell, and page-fragment lifecycle.
- Define independently encoded canonical bookmark routes beneath a configurable base path, defaulting to `/htmx`.
- Resolve an application page registered for the exact public logical type before falling back to a generic fragment containing `<causeway-object>`.
- Keep `<causeway-object>` unaware of custom-page routing, page registration, and browser history.
- Render `<causeway-menubars>` in the stable shell and translate semantic component navigation and result events into HTMX route requests under replaceable policy.
- Support object home entries exposed by the established rich application-entry contract, without inventing a home-action descriptor.
- Provide application extension points for custom server fragment factories, navigation, themes, home behavior, and result presentation.
- Add deep-link, refresh, back, forward, loading, not-found, partial-error, access-denied, and terminal-error behavior.
- Package HTMX `2.0.6` through its WebJar and keep HTMX above the component GraphQL data plane.
- Add a current-Causeway Petclinic demonstration application copied and ported from `apache/causeway-app-petclinic` commit `16a10608129ca9ce8ae04d21df1462f4d69ac018`, retaining source provenance and the Pet Owner, Pet, Visit, home, fixture, menu, and grid concepts.
- Run the Wicket and HTMX viewers over the same demonstration model so their shell, object, action, property, tab, card, collection, table, and prompt presentation can be compared directly.
- Introduce one cohesive Wicket-inspired theme for the generic viewer and semantic components without adding Bootstrap or Wicket dependencies to the framework-neutral component layer.
- Eradicate the known spacing, alignment, overflow, hierarchy, disclosure, form, table, prompt, focus, responsive, and light/dark styling defects in `sample-html` while preserving its established URL, selectors, bookmark, readiness, and vanilla-HTML contracts.
- Add opt-in Playwright acceptance tests that exercise every Petclinic service action and object action through the HTMX UI, verify representative property and collection interactions, and make focus, navigation, GraphQL, and console regressions executable.
- Include the web-components reactor beneath the existing top-level `core` aggregation so ordinary full-project builds compile and test it with the other viewers.

## Capabilities

### New Capabilities

- `generic-htmx-web-component-viewer`: Provides a router-led HTMX Causeway application viewer over the framework-neutral semantic web-component library, together with the Petclinic reference application and cohesive default presentation.

### Modified Capabilities

None.

## Impact

- Adds optional `viewers/webcomponents/htmx` and `viewers/webcomponents/sample-htmx-petclinic` Maven modules, server routes, browser assets, fragment handling, a default theme, and executable acceptance coverage.
- Adds an opt-in Playwright profile to the Petclinic sample and aggregates `viewers/webcomponents` from `core/pom.xml` alongside the established viewer reactors.
- Updates the framework-neutral theme hooks and the existing `sample-html` presentation without changing its semantic component ownership or stable automation contract.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, and menu-bar capabilities.
- Uses the public rich GraphQL endpoint and semantic component events; it does not access Causeway metamodel internals or parse layout resources itself.
- Establishes route and fallback semantics that the generic Vue and Svelte viewers also preserve.
- Includes Wicket only in the Petclinic sample as a comparison viewer; the generic HTMX module and component foundation do not depend on it.
- Does not require applications using the component library to adopt HTMX, Petclinic, Wicket, or the default theme.
