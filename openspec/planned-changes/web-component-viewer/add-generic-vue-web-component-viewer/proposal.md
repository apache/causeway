## Why

Vue applications need a complete generic Causeway viewer rather than only examples showing how to place individual custom elements.
The viewer's primary responsibility is to use Vue's routing and composition model to choose a logical-type-specific Vue page or a generic page containing `<cw-object>`, while the framework-neutral components continue to own GraphQL and domain semantics.

The architectural ownership boundary is recorded in `coverage-matrix.yaml` entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`.

## What Changes

- Add an opt-in generic Vue viewer package and application plugin.
- Implement canonical bookmark routing using Vue Router while preserving the cross-viewer route meaning.
- Resolve an exact-logical-type Vue page registration before falling back to a generic Vue route page containing `<cw-object>`.
- Require application-authored Vue shell and page templates to declare `<cw-graphql-client>` and `<cw-object-context>`, with the router binding endpoint and canonical identity rather than manufacturing those elements.
- Keep `<cw-object>` unaware of Vue routes, custom-page registration, and framework lifecycle.
- Compose `<cw-menubars>` in a stable Vue shell and translate semantic custom-element events into router navigation under replaceable policy.
- Support Vue components, async components, and application page factories as custom logical-type pages.
- Define home-page, result, loading, not-found, access-denied, partial-error, terminal-error, theme, and accessibility behavior.
- Add a production-style Vue acceptance application rather than a documentation-only sample.

## Capabilities

### New Capabilities

- `generic-vue-web-component-viewer`: Provides a router-led generic Vue Causeway viewer over the framework-neutral semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional Vue package, router integration, plugin configuration, shell components, route pages, tests, and documentation.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, and menu-bar capabilities.
- Uses application-authored native custom elements and semantic events without reimplementing GraphQL schema interpretation or semantic context hierarchy in Vue adapters.
- Preserves canonical route and fallback semantics shared with the generic HTMX and Svelte viewers.
- Does not require non-Vue applications to install Vue or Vue Router.
