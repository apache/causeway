## Why

Svelte applications need a complete generic Causeway viewer rather than only examples or thin wrappers around individual custom elements.
Because core Svelte does not prescribe application routing, the generic viewer should use SvelteKit's routing and layout boundaries to choose a logical-type-specific Svelte page or a generic `<cw-object>` page.
The semantic components remain the portable GraphQL and domain layer.

The architectural ownership boundary is recorded in `coverage-matrix.yaml` entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`.

## What Changes

- Add an opt-in generic Svelte viewer package for SvelteKit applications.
- Implement canonical bookmark routing through a thin SvelteKit route that preserves cross-viewer route meaning.
- Resolve an exact-logical-type Svelte page registration before falling back to a generic route page containing `<cw-object>`.
- Keep `<cw-object>` unaware of SvelteKit routes, component loaders, and custom-page registration.
- Compose `<cw-menubars>` in a stable SvelteKit layout and translate semantic custom-element events into navigation under replaceable policy.
- Support Svelte components and lazy component loaders as custom logical-type pages.
- Define home-page, result, client-upgrade, loading, not-found, access-denied, partial-error, terminal-error, theme, and accessibility behavior.
- Add a production-style SvelteKit acceptance application rather than a documentation-only sample.

## Capabilities

### New Capabilities

- `generic-svelte-web-component-viewer`: Provides a router-led generic SvelteKit Causeway viewer over the framework-neutral semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional Svelte package, SvelteKit route and layout integration, route pages, tests, and documentation.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, and menu-bar capabilities.
- Uses native custom elements and semantic events without reimplementing GraphQL schema interpretation in Svelte stores or load functions.
- Preserves canonical route and fallback semantics shared with the generic HTMX and Vue viewers.
- Does not require non-Svelte applications to install Svelte or SvelteKit.
