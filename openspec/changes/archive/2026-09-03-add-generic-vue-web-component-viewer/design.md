## Context

Vue provides component composition, application plugins, dynamic components, async components, and a mature router.
Causeway's custom elements already own GraphQL execution, semantic context, object layout, menus, editors, validation, interaction dispatch, action-result rendering, and domain-state concurrency.
Applications can declare those elements directly in Vue single-file component templates.
The Vue viewer should therefore be an integration package for browser routing, route-value binding, semantic-event policy, and framework lifecycle rather than a Vue rewrite of the component library.

The accepted HTMX viewer established two relevant ownership rules.
The application owns the stable application shell, while each replaceable route page owns its semantic object context and interaction controller.
The Vue design applies the same semantic split through framework-native source templates rather than through classpath HTML resources.

## Goals / Non-Goals

**Goals:**

- Provide canonical deep-linkable object routes through an application-owned Vue Router instance.
- Support exact-logical-type custom Vue pages and a generic `<cw-object>` fallback.
- Keep the GraphQL provider, menu coordination, result outlet, and application chrome stable across route changes.
- Bind endpoint and route identity into authored custom elements without imperatively creating them.
- Bridge semantic navigation and result events into replaceable Vue policy.
- Package a typed reusable Vue integration and a production-built Petclinic acceptance application.
- Preserve deterministic lifecycle, accessibility, theming, and cross-viewer route meaning.

**Non-Goals:**

- Wrapping each Causeway custom element as a Vue component.
- Reimplementing GraphQL operations, metadata interpretation, domain validation, object layout, menus, editors, or result rendering in Vue.
- Making `<cw-object>` inspect Vue Router, Vue injection state, or the custom-page registry.
- Introducing Pinia or another duplicate domain-state store.
- Owning an application's complete router, HTML document, deployment topology, authentication UI, or static-resource server.
- Supporting Nuxt, server-side rendering, streaming hydration, or server-only route data in the first version.
- Adding inherited-logical-type page matching or runtime page-registry mutation in the first version.

## Decisions

### Deliver an optional typed Vue package

The primary frontend artifact is an npm package under `viewers/webcomponents/vue` targeting Vue 3 and Vue Router 4.
Vue and Vue Router are peer dependencies and are external to the library bundle, so installing or building the framework-neutral component modules does not acquire a Vue runtime.
The package exports ECMAScript modules and TypeScript declarations and is built reproducibly with Vite library mode from a committed lockfile.
Package verification includes unit tests, production build, declaration generation, package-content inspection, dependency licensing, and an install-from-packed-artifact smoke test.

A lightweight Maven module integrates verification and release metadata with the web-components reactor without making Vue transitive to the foundation or HTMX artifacts.
Any generated browser assets committed or packaged for a sample are reproducible from locked sources and retain the required third-party notices.

### Integrate with an application-owned router

The package does not create or hide a router instance.
It exports namespaced Causeway route records and a plugin that an application installs beside its own Vue Router instance.
The application remains free to add unrelated routes, guards, scroll behavior, and deployment-specific history configuration.

The application supplies the history base to `createWebHistory`, mounts the returned Causeway records at the documented viewer path, and configures its server to return the Vue document for valid client routes.
The package documents this direct-refresh contract but does not catch unrelated backend, GraphQL, or asset URLs.
Hash history is not the default because it would change the cross-viewer bookmark grammar.

### Use an independent canonical route codec

Route creation and route acceptance use one Vue viewer codec for the canonical `<base-path>/object/<logical-type>/<identifier>` grammar.
The codec applies the same canonical UTF-8 percent encoding, separator rejection, dot-segment rejection, control-character rejection, malformed-Unicode rejection, and encoded-segment bound as the existing viewer contract.
It validates the raw URL path before binding decoded values because Vue Router parameters alone cannot prove that the original encoding was canonical.

The Vue codec is tested against `viewers/webcomponents/canonical-route-fixtures.yaml` and against generated round trips.
Navigation policy pushes codec-produced paths rather than relying on implicit router-parameter encoding.
Invalid routes render a bounded invalid-route page and never reach a semantic object context.

### Normalize an immutable exact-type page registry

Plugin creation accepts a map from exact public logical type to either a Vue component or an async component loader.
Registration keys are normalized once, and empty, malformed, or duplicate effective registrations fail during application startup.
Arbitrary factories and inheritance fallback are excluded from the first contract because Vue components and async loaders already cover synchronous and code-split pages without introducing a second lifecycle model.

The object route controller validates canonical identity and chooses the exact registration before mounting route content.
If no registration exists, it selects the package's generic route page.
Async loading is associated with the current route key, exposes an accessible loading and error presentation, and cannot mount a component after that route has been superseded.

### Keep semantic route boundaries application-authored

Every custom route-page component receives immutable `logicalTypeName`, `objectId`, and `routeKey` props.
Its source template declares one marked route-level `<cw-object-context>`, binds the identity props to that element, and declares one `<cw-interaction-controller>` within the context.
The generic route page follows the same contract and additionally declares `<cw-object editable>`.

The package may render an ordinary non-semantic wrapper for lifecycle observation, but it does not create, move, select, or repair semantic provider elements.
After each page mount, a bounded development validator confirms the route-context and interaction-controller cardinality, containment, and bound identity.
An invalid page fails closed to a safe diagnostic rather than querying through an ancestor context or retaining stale route content.

`RouterView` uses a canonical route key rather than component type alone.
Changing logical type or identifier therefore disconnects the old object context and lets the established component disconnect contract release requirements, subscriptions, cancellation state, and obsolete responses.

### Keep the stable shell application-owned

The application's root `App.vue` owns the complete stable body composition inside the Vue mount point.
It declares exactly one shell-level `<cw-graphql-client>`, binds the configured endpoint, and places `<cw-menubars>`, branding, authentication chrome when applicable, route loading, announcements, a default `<cw-action-results>` outlet, and the router-view region beneath that provider.
The provider and menus remain mounted while keyed route pages change.

The package exports composables and small route-view helpers, not a mandatory visual shell hierarchy.
A reference shell and theme are supplied as copyable source in the Vue Petclinic application and documentation.
A bounded shell validator diagnoses missing or duplicate protocol landmarks during development and tests, but never manufactures or relocates them.

The HTML document remains application-owned and loads the framework-neutral Causeway registration module and selected toolkit assets before or alongside the bundled Vue entry point.
Applications retain authority over CSP, base URL, locale, initial color scheme, global CSS, authentication metadata, and deployment-specific script delivery.

### Treat custom elements as the data plane

Both the package build and documented consuming Vite configuration classify `cw-` tags as native custom elements through Vue compiler options.
String identity values may use ordinary bindings, while structured services and values use DOM-property bindings, including Vue's explicit `.prop` modifier where necessary.
Native `CustomEvent` payloads remain authoritative and are not converted into Vue component-emits contracts.

The plugin installs a scoped event bridge on the stable shell boundary and removes it when the Vue application unmounts.
The bridge receives semantic navigation, interaction completion, and result events and invokes injected viewer policy.
It does not alter GraphQL documents, validation outcomes, authoritative identity, interaction ordering, or component refresh behavior.

### Define default navigation, home, and result policy

Default object navigation and object results use the canonical codec and `router.push` to reach the corresponding object route.
The policy accepts only complete advertised public identity and presents a bounded unsupported result when canonical navigation identity is unavailable.

The home route uses the accepted application-entry semantics and permits an application-provided home component or handler.
An object-valued default home outcome navigates canonically, while an unavailable home remains an accessible landing state.

Scalar and collection results are assigned to the declared active-page result outlet when exactly one valid outlet exists, otherwise to the one shell-level default outlet.
The existing `<cw-action-results>` and `<cw-standalone-collection>` components retain presentation authority.
Void results refresh the current object context when it still exists and return home only when an authoritative refresh establishes that the target disappeared.
Applications may replace navigation, home, and result handlers, and a documented claim protocol prevents both default and application policy from handling the same event.

### Separate route failure from domain failure

Malformed or non-canonical paths fail before object lookup.
A valid route then relies on object-context observable states for schema loading, object loading, ready, partial error, and terminal error.
Not-found and access-denied presentation does not reveal domain state or authorization rules, and the default unavailable presentation does not claim to distinguish outcomes that GraphQL intentionally makes indistinguishable.

Route navigation sets the route region busy, announces completion or failure through the authored live region, and moves focus to the current page heading or route container according to documented policy.
Superseded async imports and obsolete context responses cannot update the current route's focus, announcement, or status.

### Reuse Petclinic rather than duplicate it

A small shared Petclinic domain module is extracted from the existing HTMX sample so the HTMX and Vue host applications use the same entities, public logical types, fixtures, GraphQL behavior, and deterministic identities.
The extraction changes Java packaging only where required and must not alter domain semantics, HTMX routes, resource-page lookup, seed cardinalities, or existing acceptance selectors.

The new Vue Petclinic module owns its Spring Boot entry point, Vite application, `index.html`, `App.vue`, custom logical-type pages, generic fallback demonstration, and client-route forwarding.
It uses production Vite output served by the application rather than a development server or runtime template compiler.
At least one Petclinic logical type has a source-visible custom Vue page, and another uses the generic route fallback.

### Keep first release client-rendered

The supported lifecycle is a browser-created Vue application using Vue Router history.
Nuxt, SSR, streaming hydration, and server-rendered custom-element upgrade require separate compatibility work and are explicitly unsupported rather than partially emulated.

## Risks / Trade-offs

- [Raw route encoding can be lost by router normalization] → Validate the raw path with the canonical codec before using decoded route parameters and test all shared invalid fixtures.
- [Vue can resolve `cw-` tags as Vue components] → Configure `isCustomElement` in library and consuming builds and test compiled output.
- [Vue can assign an attribute where a DOM property is required] → Document and test property bindings, including explicit `.prop` bindings for structured values.
- [Route reuse can retain obsolete object state] → Key the rendered route component by canonical identity and verify disconnect and cancellation behavior.
- [An authored page can omit or duplicate its semantic boundary] → Validate cardinality, containment, and bound identity and fail closed without repair.
- [An async page can resolve after navigation] → Scope imports, diagnostics, focus, and announcements to the active route key.
- [A duplicate Vue domain store can emerge] → Keep only router and viewer policy in Vue state and forbid mirroring GraphQL object, member, validation, or interaction state.
- [Browser-history refresh can be mistaken for a frontend-only concern] → Specify and test the application server fallback independently of package route resolution.
- [Sharing Petclinic can regress the HTMX sample] → Preserve logical types and fixture identities and run the existing HTMX Java and Playwright suites after extraction.
- [Frontend tooling can leak into unrelated builds] → Keep Vue dependencies in the opt-in package and sample profiles and verify unaffected foundation and HTMX consumers.

## Migration Plan

The Vue viewer is additive and installed only by applications that select it.
Applications create or reuse a Vue Router instance, add the exported route records, install the viewer plugin, author their stable shell, and configure server fallback for the chosen history base.
They may begin with the generic route page and incrementally register exact logical-type custom pages.
Rollback removes the plugin, route records, and Vue application assets without changing GraphQL or semantic component contracts.
The Petclinic domain extraction is performed in one compatibility-preserving step before the Vue host application depends on it.

## Deferred Work

- Inherited or assignable logical-type page matching.
- Runtime mutation of the page registry.
- Nuxt, SSR, streaming hydration, and server route-data integration.
- A framework-specific authentication package beyond application-owned shell chrome and existing GraphQL transport policy.
