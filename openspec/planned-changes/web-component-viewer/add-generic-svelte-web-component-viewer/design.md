## Context

Svelte is well suited to consuming custom elements directly, while SvelteKit provides route, layout, history, loading, and error boundaries.
Causeway's semantic components already own GraphQL contexts, object layout, menu layout, editors, validation, interactions, and results.
A generic Svelte viewer should integrate those components with SvelteKit routing rather than reproduce them as Svelte domain components.

The router resolves a canonical bookmark and chooses a registered logical-type Svelte page or a generic page containing `<causeway-object>`.
The component itself remains unaware that custom pages exist.

## Goals / Non-Goals

**Goals:**

- Provide canonical bookmark routes through SvelteKit.
- Support exact-logical-type custom Svelte pages and generic object fallback.
- Keep application menu bars stable in a SvelteKit layout across page navigation.
- Bridge semantic custom-element events to replaceable navigation and result policy.
- Define deterministic browser upgrade, cleanup, and superseded navigation behavior.
- Provide a complete accessible viewer package and production-style acceptance application.

**Non-Goals:**

- Selecting or bundling a router for standalone non-SvelteKit Svelte applications.
- Wrapping every Causeway custom element as a Svelte component.
- Constructing GraphQL documents or duplicating domain state in Svelte stores or load functions.
- Making `<causeway-object>` inspect SvelteKit route state.
- Claiming complete server-rendered custom-element content in the first version.

## Decisions

### Use a thin canonical SvelteKit route

The package supplies route-page and layout helpers that an application mounts beneath a configurable base path using SvelteKit's routing conventions.
The object route decodes public logical type and identifier and establishes canonical route identity.
It selects an exact logical-type page loader or the generic fallback.

The filesystem route remains thin and contains no metamodel or member enumeration logic.
Applications can wrap or relocate it while preserving canonical semantic route meaning.

### Register framework-native Svelte pages

Applications register a Svelte component or lazy component loader against an exact Causeway logical type.
The chosen page renders beneath one route-level `<causeway-object-context>` created by the viewer route page.

Custom pages can compose Causeway custom elements, Svelte components, ordinary HTML, and application elements.
They rely on the object context for domain state rather than duplicating the object in Svelte stores.

### Use `<causeway-object>` as the generic fallback

When no custom loader exists, the route page renders `<causeway-object>` beneath the same context contract.
Page selection completes before the generic component connects.
The generic component never discovers custom pages or reads SvelteKit route state.

### Keep menus in the root viewer layout

A viewer `+layout` component or equivalent shell renders `<causeway-menubars>` outside the route-page slot.
Page navigation does not recreate menu coordination.
Semantic menu outcomes flow to replaceable Svelte navigation and result policy.

### Isolate browser custom-element lifecycle

The initial viewer ensures Causeway elements are registered in the browser before interactive page readiness.
Server output may contain stable inert custom-element hosts, but authoritative domain loading and interaction begin after browser upgrade.
The first version may disable SSR for the viewer route if that is the only deterministic way to prevent hydration mismatch.

The implementation must explicitly test SvelteKit navigation reuse, cleanup, route invalidation, and obsolete async component loaders.

### Keep home and result behavior replaceable

The application-entry contract supplies home-page object or service-action semantics.
Default object results navigate through SvelteKit to canonical routes.
Scalar, collection, and void results use replaceable Svelte presentations without changing component interaction behavior.

### Keep framework state above the domain data plane

Svelte stores may hold viewer theme, current shell policy, or non-object result presentation.
They do not mirror GraphQL object snapshots, member state, validation state, or interaction state already owned by semantic components.

## Risks / Trade-offs

- [SvelteKit SSR can precede custom-element registration] → Define client upgrade explicitly and disable SSR for the viewer route if required for correctness.
- [Filesystem routes can make a package intrusive] → Export thin reusable route and layout helpers with documented mounting rather than generating an application tree silently.
- [Lazy custom page loaders can race navigation] → Associate loaders with route identity and ignore superseded results.
- [Stores can duplicate component state] → Restrict package stores to shell and viewer policy.
- [Viewer routes may diverge] → Share canonical route and fallback fixtures with HTMX and Vue implementations.

## Migration Plan

The Svelte viewer is additive and installed only by SvelteKit applications that select it.
Applications can mount generic routes first and incrementally register custom logical-type page loaders.
Rollback removes the route helpers and package without changing GraphQL or component contracts.

## Open Questions

- Whether initial viewer routes disable SSR or render inert custom-element hosts and hydrate only the interactive layer.
- Whether route helpers should be source components copied into the application or published package exports imported by thin application route files.
- Which default non-object result presentation belongs in the root layout.
