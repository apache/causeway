## Why

Angular applications need a complete generic Causeway viewer rather than framework-specific rewrites or examples that place isolated custom elements.
The viewer's primary responsibility is to use Angular Router to select an exact-logical-type standalone page or a generic page containing `<cw-object>`, while application-authored semantic elements continue to own GraphQL and domain behavior.

## What Changes

- Add an opt-in generic Angular viewer package for standalone Angular applications.
- Implement canonical bookmark routing with Angular Router while preserving cross-viewer route meaning.
- Resolve an exact-logical-type standalone Angular page registration before falling back to a generic route page containing `<cw-object>`.
- Require application-authored Angular shell and page templates to declare `<cw-graphql-client>` and `<cw-object-context>`, with Angular Router binding endpoint and canonical identity rather than manufacturing those elements.
- Configure Angular custom-element recognition without wrapping every Causeway element as an Angular component.
- Keep `<cw-object>` unaware of Angular routes, custom-page registration, dependency injection, and framework lifecycle.
- Compose `<cw-menubars>` in a stable Angular shell and translate semantic custom-element events into router navigation under replaceable policy.
- Support standalone components and lazy `loadComponent` registrations as custom logical-type pages.
- Define home-page, result, loading, not-found, access-denied, partial-error, terminal-error, theme, and accessibility behavior.
- Add a production-style Angular acceptance application rather than a documentation-only sample.

## Capabilities

### New Capabilities

- `generic-angular-web-component-viewer`: Provides a router-led generic Angular Causeway viewer over the framework-neutral semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional Angular package, route providers, shell and route components, tests, and documentation.
- Depends on accepted P0 and P1 rich GraphQL coverage plus completed application-entry, composite-object, menu-bar, and declarative context-boundary capabilities.
- Uses application-authored native custom elements and semantic events without reimplementing GraphQL schema interpretation or semantic context hierarchy in Angular services or signals.
- Preserves canonical route and fallback semantics shared with the generic HTMX, Vue, and Svelte viewers.
- Does not require non-Angular applications to install Angular or Angular Router.
