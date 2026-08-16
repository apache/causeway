## Why

The semantic web-component library will provide complete layout-aware object and application-menu components, but users also need a usable default application shell with routes, history, fragment navigation, and page customization.
An HTMX reference viewer will provide that shell while proving that framework-neutral components remain the sole implementation of domain and layout semantics.
The evidence and ownership boundary are recorded in `coverage-matrix.yaml` entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`.

## What Changes

- Add an opt-in generic viewer whose shell, navigation, history, and page-fragment lifecycle use HTMX.
- Define canonical object routes based on logical type name and identifier and translate semantic component navigation events into those routes.
- Render `<causeway-menubars>` in the application shell using the rich GraphQL application-entry contract.
- Add a page-definition resolver that selects an application page registered for a logical type or falls back to `<causeway-object>` beneath one route-level object context.
- Delegate grid interpretation, fallback object decomposition, member behavior, service-action interaction, and menu accessibility to the high-level framework-neutral components.
- Provide application extension points for custom page templates or factories, navigation policy, theme, home-page policy, and result presentation.
- Add deep-link, refresh, back and forward, loading, not-found, partial-error, and terminal-error behavior.

## Capabilities

### New Capabilities

- `generic-web-component-viewer`: Provides an HTMX-based Causeway application shell, canonical domain-object routes, menu-bar placement, and per-logical-type page customization over the semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional viewer module, browser assets, route and fragment handling, default theme, and demonstration application.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, and menu-bar capabilities; narrow member metadata and diagnostics may be adopted independently.
- Uses the public rich GraphQL endpoint and semantic component events; it does not access Causeway metamodel internals or parse layout resources itself.
- Does not require applications using the component library to adopt HTMX.
- Initially focuses on application shell and bookmark-addressable object pages rather than authentication pages, standalone values, or parity with every existing viewer extension.
