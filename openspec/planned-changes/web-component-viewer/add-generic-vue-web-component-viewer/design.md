## Context

Vue provides component composition, dynamic components, application plugins, and a mature router.
Causeway's custom elements already provide the domain-aware controls, object contexts, grid composition, menu composition, validation, and interaction results.
Applications declare those elements directly in Vue templates.
A generic Vue viewer should therefore be a router, route-value binder, and lifecycle integration, not a Vue rewrite of those components.

The final customization architecture is route-first.
The Vue router resolves a bookmark, chooses an exact logical-type application page if registered, and otherwise renders a generic route page containing `<cw-object>`.

## Goals / Non-Goals

**Goals:**

- Provide canonical deep-linkable bookmark routes using Vue Router.
- Support exact-logical-type custom Vue pages and generic object fallback.
- Preserve stable menu shell state across route-page changes.
- Bridge semantic custom-element navigation and result events into replaceable Vue policy.
- Integrate custom-element registration and lifecycle cleanly with Vue rendering.
- Provide a complete accessible production-style viewer package and acceptance application.

**Non-Goals:**

- Wrapping every Causeway custom element as a separate Vue component.
- Reimplementing GraphQL operations, object layout, menu parsing, editors, or validation in Vue.
- Making `<cw-object>` inspect Vue Router or the page registry.
- Supporting Nuxt or server-side rendering in the first version.
- Defining HTMX or Svelte lifecycle behavior.

## Decisions

### Use Vue Router as the customization boundary

The viewer installs canonical object and application-entry routes into a Vue Router instance or exposes route records for an application-owned instance.
The object route resolves public logical type and identifier.
It then selects the exact logical-type page registration or the generic route page.

Page selection occurs before `<cw-object>` is created.
The generic object component remains unaware of custom pages.

### Register framework-native custom pages

Applications register a Vue component, async component, or documented page factory against an exact Causeway logical type.
The selected application page declares one `<cw-object-context>` and binds the canonical logical type and identifier supplied by the generic route-page boundary.
The viewer validates or diagnoses the declared boundary but does not manufacture it imperatively.

Custom Vue pages can compose Causeway custom elements, ordinary Vue components, HTML, and application custom elements.
They consume domain state through their declared object context rather than constructing GraphQL documents in Vue stores.

### Render a generic page when no registration exists

The fallback route-page template declares its GraphQL client association, route-level object context, and `<cw-object>` fallback.
The router binds endpoint and canonical identity values into those declared elements.
A stable route key based on canonical bookmark identity ensures old contexts disconnect when navigation changes.
Superseded component responses remain governed by object-context generation handling.

### Keep the application shell stable

The application-authored root viewer shell declares one stable `<cw-graphql-client>` containing `<cw-menubars>` outside `RouterView` or its equivalent route-page boundary.
The Vue adapter binds configured client properties but does not create the provider element.
Menu disclosure, service-action interaction, and menu layout remain component-owned.
Vue policy receives semantic outcomes and may call router navigation or render non-object results.

### Treat custom elements as the data plane

The package configures or documents Vue custom-element recognition so Causeway element attributes, properties, and native custom events retain their browser semantics.
A small integration adapter may normalize event subscription and cleanup, but it does not mirror component state into a duplicate Vue domain store.

### Keep home and result policy replaceable

Home-page objects and service actions are resolved from the application-entry contract.
Default object results navigate to canonical routes, while scalar, collection, and void results use replaceable Vue presentations.
Applications can override each policy without changing semantic components.

### Defer server-side rendering

The first viewer targets a client-rendered Vue application and browser history.
Nuxt, SSR, streaming hydration, and server-only route data are separate compatibility work because custom-element upgrade and GraphQL context lifecycle need explicit validation there.

## Risks / Trade-offs

- [Vue may treat unknown tags or properties specially] → Configure custom-element recognition and test attributes, properties, slots, and native events.
- [Route reuse can retain obsolete object context] → Key route pages by canonical bookmark identity and verify deterministic disconnect.
- [A duplicate Vue store can emerge] → Keep domain state in semantic object contexts and expose only viewer policy through Vue state.
- [A page can omit or duplicate its declarative context] → Validate the route-page contract and present a bounded development diagnostic without silently adding a context.
- [Async custom pages can race navigation] → Associate imports with route generation and discard superseded resolutions.
- [Viewer routes may diverge] → Share canonical route and fallback fixtures with HTMX and Svelte viewers.

## Migration Plan

The Vue viewer is additive and installed only by Vue applications that select it.
Applications may begin with generic routes and incrementally register custom logical-type pages.
Rollback removes the plugin and route records without changing GraphQL or component contracts.

## Open Questions

- Whether the package owns a router instance or primarily exports route records and installation helpers.
- Whether custom page registration should permit inheritance fallback after exact logical type in a later version.
- Which non-object result presentation should be supplied by default.
