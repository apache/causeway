## 1. Package and Build Contract

- [x] 1.1 Add the optional `viewers/webcomponents/vue` Vue 3 and Vue Router 4 package with TypeScript sources, package metadata, a committed lockfile, Vite library mode, peer dependencies, ECMAScript exports, and declarations.
- [x] 1.2 Add the lightweight Maven/reactor integration and explicit frontend regeneration profile without making Vue dependencies transitive to foundation, HTMX, or vanilla consumers.
- [x] 1.3 Configure Vue single-file component compilation to recognize `cw-` tags as native custom elements in the library and documented consumer setup.
- [x] 1.4 Add package unit-test, type-check, production-build, stale-output, license, `npm pack`, package-content, and clean packed-consumer smoke checks.
- [x] 1.5 Document the public package exports and keep unimplemented Nuxt, SSR, runtime registry mutation, and inherited-type matching out of the initial API.

## 2. Canonical Router Integration

- [x] 2.1 Implement the bounded canonical UTF-8 route encoder and raw-path decoder for `<base-path>/object/<logical-type>/<identifier>` without relying on already-decoded Vue Router parameters.
- [x] 2.2 Test valid, invalid, overlong, Unicode, non-canonical, and generated round-trip routes against `viewers/webcomponents/canonical-route-fixtures.yaml`.
- [x] 2.3 Export namespaced home, object, invalid-route, and fallback route records for an application-owned Vue Router instance.
- [x] 2.4 Implement plugin installation and injected viewer configuration without creating a hidden router or overriding unrelated application routes, guards, scroll behavior, or history policy.
- [x] 2.5 Implement codec-produced push and replace helpers that preserve configured base-path meaning and complete advertised identity.
- [x] 2.6 Document and integration-test application-owned browser-history server fallback without intercepting GraphQL, assets, authentication, or unrelated backend routes.

## 3. Custom and Generic Route Pages

- [x] 3.1 Normalize an immutable exact-logical-type registry for Vue components and async component loaders and reject malformed, unsupported, or duplicate effective registrations at startup.
- [x] 3.2 Implement custom-page precedence and the generic route-page fallback containing authored `<cw-object-context>`, `<cw-object editable>`, and `<cw-interaction-controller>` elements.
- [x] 3.3 Define immutable `logicalTypeName`, `objectId`, and `routeKey` props for application-authored route pages and provide source examples for binding them to the declared context.
- [x] 3.4 Add bounded post-mount validation for route-context cardinality, interaction-controller containment, and canonical identity without manufacturing, moving, or selecting semantic elements.
- [x] 3.5 Key `RouterView` content by canonical identity and verify that route changes disconnect obsolete contexts, requirements, listeners, and interaction state.
- [x] 3.6 Scope async component loading, rejection, diagnostics, focus, and announcements to the active route generation and discard superseded completion.
- [x] 3.7 Add a structural test proving `<cw-object>` does not import or inspect Vue Router, Vue injection state, or the page registry.

## 4. Stable Shell and Native Element Bridge

- [x] 4.1 Export the shell composables and route-view helpers needed by an application-authored root component without exporting a mandatory visual shell hierarchy.
- [x] 4.2 Implement bounded development validation for one stable `<cw-graphql-client>`, one router-view region, one default result outlet, and the documented loading and announcement landmarks without repair.
- [x] 4.3 Implement endpoint and string-identity binding through authored templates and property-only structured bindings through DOM properties, including explicit Vue `.prop` use where required.
- [x] 4.4 Implement a shell-scoped semantic-event bridge for authoritative object navigation, interaction completion, and result events with deterministic listener cleanup on application unmount.
- [x] 4.5 Verify native custom-element attributes, properties, slots, pre-upgrade children, `CustomEvent` payloads, event bubbling, and connect/disconnect lifecycle under Vue rendering.
- [x] 4.6 Keep GraphQL objects, metadata, members, validation, invocation, and interaction state out of Vue stores and duplicate adapter state.

## 5. Navigation, Home, and Result Policy

- [x] 5.1 Implement replaceable single-claim policy hooks for navigation, application-entry home behavior, and object, scalar, collection, void, unsupported, and error outcomes.
- [x] 5.2 Route complete object identities canonically and reject object-shaped results that lack authoritative public navigation identity without inference.
- [x] 5.3 Resolve exactly one eligible active-page result outlet before the shell default and fail closed for duplicate, disconnected, or otherwise ambiguous outlets.
- [x] 5.4 Assign normalized scalar and collection results to `<cw-action-results>` so existing result and standalone-collection components retain presentation authority.
- [x] 5.5 Refresh the current object context after void results and replace the route with home only when authoritative refresh establishes that the target disappeared.
- [x] 5.6 Test application handler replacement so claimed events are handled once and component interaction behavior remains unchanged.

## 6. Route Lifecycle, Accessibility, and Theme

- [x] 6.1 Coordinate busy state, loading, ready, invalid-route, unavailable, partial-error, unsupported, and terminal-error presentation from router and object-context state.
- [x] 6.2 Implement generation-scoped focus movement and live-region announcements that cannot be overwritten by obsolete navigation or async-page work.
- [x] 6.3 Ensure unavailable and denied outcomes reveal neither domain state nor authorization rules and do not claim distinctions absent from the authoritative transport.
- [x] 6.4 Add a copyable reference shell theme with documented custom properties and hooks for wide, narrow, light, dark, reduced-motion, keyboard, and visible-focus behavior.
- [x] 6.5 Run automated accessibility checks and manual keyboard journeys for routing, menus, dialogs, sidebars, result dismissal, and route restoration.

## 7. Shared Petclinic Domain and Vue Application

- [x] 7.1 Extract the deterministic Petclinic domain and seed configuration into a small shared non-viewer module while preserving public logical types, fixture identities, cardinalities, GraphQL semantics, and source ownership.
- [x] 7.2 Update the HTMX Petclinic application to consume the shared module without changing its routes, classpath pages, application shell, selectors, security variants, or toolkit policies.
- [x] 7.3 Add a separate Vue Petclinic Spring Boot application with its own entry point, GraphQL endpoint, Vite source application, production output, client-route forwarding, and run profile.
- [x] 7.4 Author the Vue Petclinic `index.html` and root `App.vue` with a stable GraphQL provider, menus, branding, loading, announcements, default result outlet, and keyed router-view region.
- [x] 7.5 Add at least one source-visible Petclinic custom Vue page declaring its route context and interaction controller and leave another logical type unregistered to demonstrate generic fallback.
- [x] 7.6 Demonstrate menus, breadcrumbs, properties, actions, collection paging and filtering, row previews, object navigation, object results, and collection-result presentation without Vue-owned domain queries.
- [x] 7.7 Verify production output and direct-refresh routing without requiring a Vite development server or runtime Vue template compiler.

## 8. Verification and Documentation

- [x] 8.1 Add browser acceptance for direct links, refresh, back and forward, custom-page precedence, async-page races, generic fallback, invalid routes, absent objects, partial errors, menus, interactions, and results.
- [x] 8.2 Run Vue acceptance under default Vaadin and native component policies and fail on browser console errors, page errors, failed resources, or accessibility violations.
- [x] 8.3 Re-run foundation Node tests, HTMX Node and Java tests, HTMX Petclinic integration and security tests, and existing native, Vaadin, and secured Playwright profiles after Petclinic extraction.
- [x] 8.4 Document installation, package exports, Vite configuration, router records, route grammar, history fallback, shell ownership, custom pages, generic fallback, policies, toolkit assets, theming, lifecycle, diagnostics, and CSR-only support.
- [x] 8.5 Verify Maven reactor integration, reproducible generated output, dependency notices, Apache RAT, formatting, `git diff --check`, and strict OpenSpec validation.
