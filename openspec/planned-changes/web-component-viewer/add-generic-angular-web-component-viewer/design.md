## Context

Angular provides standalone components, dependency injection, lazy route loading, component input binding, and a mature router.
Causeway's custom elements already provide domain controls, GraphQL contexts, object and menu composition, validation, interactions, and results.
Applications declare those elements directly in Angular templates.
A generic Angular viewer should therefore be a router, route-value binder, and shell integration rather than an Angular rewrite of the semantic component library.

The final customization architecture is route-first.
Angular Router resolves a bookmark, chooses an exact-logical-type standalone application page if registered, and otherwise renders a generic route page containing `<cw-object>`.

## Goals / Non-Goals

**Goals:**

- Provide canonical deep-linkable bookmark routes through Angular Router.
- Support exact-logical-type standalone Angular pages and generic object fallback.
- Preserve a stable application-authored client and menu shell across route changes.
- Bridge semantic custom-element navigation and result events into replaceable Angular policy.
- Integrate custom-element schemas, attribute/property binding, lazy loading, and cleanup deterministically.
- Provide a complete accessible production-style viewer package and acceptance application.

**Non-Goals:**

- Wrapping every Causeway custom element as a separate Angular component or directive.
- Reimplementing GraphQL operations, object layout, menu parsing, editors, or validation in Angular services or signals.
- Making `<cw-object>` inspect Angular Router or the page registry.
- Supporting Angular Universal, server-side rendering, or hydration in the first version.
- Defining HTMX, Vue, or Svelte lifecycle behavior.

## Decisions

### Use Angular Router as the customization boundary

The viewer exports route providers or documented route records mounted beneath a configurable base path.
The object route resolves public logical type and identifier, then selects the exact-logical-type page registration or generic route page.
Page selection completes before `<cw-object>` connects.

### Register standalone application pages

Applications register a standalone component or lazy `loadComponent` loader against an exact Causeway logical type.
The selected application page declares one `<cw-object-context>` and binds canonical identity supplied through route data or component inputs.
The viewer diagnoses an invalid boundary but does not manufacture it imperatively.

Application pages can compose Causeway elements, Angular components, ordinary HTML, and application custom elements.
They consume domain state through their declared object context rather than constructing GraphQL documents in Angular services.

### Render a declarative generic fallback

The fallback standalone component template declares its route-level object context and `<cw-object>` fallback.
The router binds endpoint and canonical identity values into declared elements.
A route key or component replacement based on canonical bookmark identity ensures obsolete contexts disconnect.

### Keep the application shell stable

The application-authored root shell declares one stable `<cw-graphql-client>` containing `<cw-menubars>` outside `router-outlet`.
The Angular adapter binds configured client properties but does not create the provider element.
Menu outcomes and interaction results flow into replaceable router and result policy.

### Treat custom elements as the data plane

Standalone route and shell components opt into `CUSTOM_ELEMENTS_SCHEMA` or the documented equivalent needed for Causeway tags.
Bindings preserve browser custom-element properties and native custom events rather than mirroring component state into Angular forms, services, or signals.
A narrow directive or adapter may normalize event subscription and cleanup but does not own domain state.

### Keep home and result policy replaceable

Application-entry semantics supply home objects and service actions.
Default object results navigate through Angular Router, while scalar, collection, and void results use replaceable Angular presentations.
Applications can override policy without changing semantic components.

### Defer server-side rendering

The first viewer targets client-rendered standalone Angular applications.
Angular SSR, hydration, streaming, and server route-data integration remain separate compatibility work because custom-element upgrade and context lifecycle require explicit qualification.

## Risks / Trade-offs

- [Angular template validation rejects custom elements] → Document and test narrowly scoped `CUSTOM_ELEMENTS_SCHEMA` configuration.
- [Angular bindings set attributes and properties differently] → Test identity, endpoint, boolean, object-property, and native-event behavior explicitly.
- [Route reuse retains obsolete context] → Key or replace route components by canonical bookmark identity and verify disconnect.
- [Lazy components race navigation] → Associate loaders with current route identity and discard superseded activations.
- [Angular services duplicate domain state] → Restrict services and signals to routing, shell, and result policy.
- [A page omits or duplicates its declarative context] → Present a bounded development diagnostic without silently adding a context.
- [Viewer routes diverge] → Share canonical route and fallback fixtures with HTMX, Vue, and Svelte.

## Migration Plan

The Angular viewer is additive and installed only by applications that select it.
Applications may begin with generic routes and incrementally register standalone logical-type pages.
Rollback removes the providers, route records, and package without changing GraphQL or component contracts.

## Open Questions

- Whether the package should primarily export providers and route factories or an installable shell feature.
- Whether later versions should support inheritance fallback after exact logical type.
- Which default non-object result presentation belongs in the shell.
