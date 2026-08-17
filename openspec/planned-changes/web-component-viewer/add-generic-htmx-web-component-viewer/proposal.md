## Why

The semantic web-component library will provide complete object and application-menu components, but users also need an opt-in generic application viewer with canonical routes, deep links, history, custom pages, and generic fallback pages.
The architectural review in `Causeway web components.pdf` concluded that routing, rather than `<causeway-object>` or a framework-neutral page provider, must choose between a logical-type-specific page and the generic object page.
HTMX provides the lightest server-oriented reference implementation of that router boundary.

The evidence and ownership boundary are recorded in `coverage-matrix.yaml` entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`.

## What Changes

- Add an opt-in generic HTMX viewer whose primary responsibility is canonical routing and page-fragment lifecycle.
- Define canonical bookmark routes based on public logical type name and identifier.
- Resolve an application page registered for the logical type before falling back to a generic fragment containing `<causeway-object>`.
- Keep `<causeway-object>` unaware of custom-page routing and page registration.
- Render `<causeway-menubars>` in a stable shell and translate semantic component navigation events into HTMX route requests.
- Resolve configured home-page objects or service actions through replaceable viewer policy.
- Provide application extension points for custom HTML fragments or factories, navigation, themes, home behavior, and result presentation.
- Add deep-link, refresh, back, forward, loading, not-found, partial-error, access-denied, and terminal-error behavior.

## Capabilities

### New Capabilities

- `generic-htmx-web-component-viewer`: Provides a router-led HTMX Causeway application viewer over the framework-neutral semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional HTMX viewer module, server routes, browser assets, fragment handling, default theme, and demonstration application.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, and menu-bar capabilities.
- Uses the public rich GraphQL endpoint and semantic component events; it does not access Causeway metamodel internals or parse layout resources itself.
- Establishes route and fallback semantics that the generic Vue and Svelte viewers also preserve.
- Does not require applications using the component library to adopt HTMX.
