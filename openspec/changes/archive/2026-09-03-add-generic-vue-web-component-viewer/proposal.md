## Why

Vue applications need a complete generic Causeway viewer rather than examples that only place individual custom elements.
The accepted component contracts now provide application entry points, semantic object contexts, object composition, menus, interactions, result outlets, collection previews, and toolkit-backed presentation.
The remaining Vue-specific work is therefore deliberately thin: own browser routing and host lifecycle policy, bind route and endpoint values into application-authored semantic markup, and leave authoritative domain behavior in the framework-neutral components.

## What Changes

- Add an optional Vue 3 and Vue Router 4 package with TypeScript declarations, reproducible Vite library output, and no Vue dependency in the framework-neutral component artifact.
- Export an application-owned-router integration consisting of route-record creation, a Vue plugin, route codecs, route-page components, composables, and replaceable navigation, home, and result policies.
- Implement the canonical `<base-path>/object/<logical-type>/<identifier>` bookmark grammar and validate it against the shared cross-viewer route fixtures.
- Resolve an exact logical-type Vue component or async component before falling back to a generic Vue route page containing `<cw-object>`.
- Require application-authored Vue route pages to declare one `<cw-object-context>` and one route interaction controller, with Vue binding canonical identity rather than manufacturing semantic elements.
- Require the application root component to own the stable shell, including one `<cw-graphql-client>`, `<cw-menubars>`, announcements, loading presentation, a result outlet, and the keyed router-view region.
- Bridge authoritative custom-element navigation and interaction-result events into replaceable Vue policy without constructing GraphQL operations or mirroring domain state into a Vue store.
- Configure and document native custom-element compilation, property binding, event handling, upgrade ordering, and deterministic cleanup.
- Provide accessible route lifecycle, invalid-route, unavailable, partial-error, terminal-error, theme, and responsive behavior.
- Add a production-built Vue Petclinic acceptance application that reuses the deterministic Petclinic domain without duplicating it and supports direct browser-history refresh through an application-owned server fallback.
- Integrate package, browser, accessibility, Maven-reactor, licensing, and distribution verification while keeping npm installation and Vue runtime opt-in.

## Capabilities

### New Capabilities

- `generic-vue-web-component-viewer`: Provides a router-led generic Vue Causeway viewer over the framework-neutral semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds Vue viewer package sources and generated distribution metadata under `viewers/webcomponents/vue`.
- Adds a Vue Petclinic sample and the minimal shared Petclinic domain module needed by the HTMX and Vue host applications.
- Updates the web-components reactor, test profiles, documentation, shared route fixtures, and release/licensing checks.
- Relies on the accepted `graphql-web-component-context`, `domain-web-components`, rich GraphQL application-entry and interaction capabilities, and their declarative ownership contracts.
- Preserves the existing GraphQL schema, public logical types, deterministic Petclinic identities, HTMX routes, and component semantics.
- Does not require Vue, Vue Router, Vite, or a frontend build runtime for HTMX, vanilla HTML, or direct framework-neutral component consumers.
- Does not add Nuxt, server-side rendering, streaming hydration, or a Vue-specific domain data store.
