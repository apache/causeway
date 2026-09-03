# generic-vue-web-component-viewer Specification

## Purpose
Provide an optional client-rendered Vue 3 and Vue Router 4 host viewer that owns application routing, shell composition, and lifecycle policy while preserving semantic web components as the authority for domain behavior.

## Requirements

### Requirement: Optional packaged Vue viewer

The project SHALL provide an explicitly installed Vue 3 viewer package that uses Vue Router 4 for host routing and semantic Causeway custom elements for domain behavior.
Vue, Vue Router, and Vue build tooling MUST NOT become runtime or build dependencies of the framework-neutral component, HTMX viewer, or vanilla HTML artifacts.

#### Scenario: Vue viewer is installed

- **WHEN** a Vue application installs the package and its router integration
- **THEN** the documented Causeway route records, route pages, composables, and policy hooks are available
- **AND** authoritative domain state remains owned by semantic components

#### Scenario: Vue viewer is absent

- **WHEN** an application does not install the package
- **THEN** Vue, Vue Router, and Vite are not required to consume the framework-neutral component library
- **AND** existing HTMX and vanilla applications retain their established behavior

#### Scenario: Package artifact is inspected

- **WHEN** the Vue package is built and packed from its lockfile
- **THEN** it contains ECMAScript library output, TypeScript declarations, documented peer dependencies, licenses, and only intended public files
- **AND** a clean consumer can install and import the packed artifact

### Requirement: Application-owned Vue Router integration

The viewer SHALL export namespaced Causeway route records and installation helpers for an application-owned Vue Router instance.
The viewer MUST NOT create a hidden router or prevent the application from defining unrelated routes, guards, history behavior, or scroll policy.

#### Scenario: Causeway routes are added

- **WHEN** an application adds the exported records to its router and installs the viewer plugin
- **THEN** Causeway home, object, invalid-route, and fallback states coexist with application routes
- **AND** viewer navigation uses that same router instance and history

#### Scenario: Viewer is mounted beneath a base

- **WHEN** the application configures a valid non-root browser-history base and route mount
- **THEN** home, object, asset, refresh, and history URLs consistently honor that deployment
- **AND** the viewer does not assume origin-root deployment

#### Scenario: Direct history refresh reaches the application

- **WHEN** a canonical Vue object bookmark is requested directly from the application server
- **THEN** application-owned server fallback returns the Vue document for that client route
- **AND** does not intercept GraphQL, static asset, authentication, or unrelated backend routes

### Requirement: Canonical Vue bookmark routing

The viewer SHALL map a public logical type and opaque identifier to the documented `<base-path>/object/<logical-type>/<identifier>` grammar.
Route generation and raw-path acceptance MUST apply the same canonical UTF-8 percent encoding, separator rules, malformed-input rejection, and encoded-segment bound as the cross-viewer contract.

#### Scenario: Canonical bookmark loads

- **WHEN** a valid authorized bookmark is opened, refreshed, restored, or revisited through browser history
- **THEN** Vue Router renders the page for the exact logical type and identifier
- **AND** the browser location has the same route meaning as the HTMX viewer

#### Scenario: Semantic navigation is requested

- **WHEN** a component publishes complete authoritative object identity
- **THEN** default Vue navigation pushes the codec-produced canonical path
- **AND** does not reinterpret or truncate the identifier

#### Scenario: Route encoding is invalid

- **WHEN** a raw route contains malformed escapes, empty values, encoded separators, control characters, dot segments, malformed Unicode, overlong encoded segments, or non-canonical encoding
- **THEN** the viewer presents a bounded invalid-route state before mounting an object context
- **AND** does not disclose submitted route content, object state, authorization rules, or raw decoder exceptions

#### Scenario: Shared route fixtures are exercised

- **WHEN** the Vue route codec is verified
- **THEN** every valid and invalid entry in `viewers/webcomponents/canonical-route-fixtures.yaml` has the same outcome as the established viewer contract
- **AND** generated valid identities round-trip without loss

### Requirement: Immutable exact-type Vue page registry

The viewer SHALL normalize an application-supplied registry from exact public logical type to a Vue component or async component loader during plugin creation.
Exact registration SHALL take precedence over the generic route page, and runtime registry mutation or inheritance fallback SHALL NOT be part of the initial contract.

#### Scenario: Vue page is registered

- **WHEN** a canonical route resolves a logical type with an exact synchronous registration
- **THEN** the registered Vue component receives immutable canonical route props
- **AND** the generic page is not mounted

#### Scenario: Async Vue page is registered

- **WHEN** the exact registration is an async component loader
- **THEN** the viewer presents the current route's accessible loading state until that component resolves
- **AND** loader rejection presents a bounded current-route error

#### Scenario: No Vue page is registered

- **WHEN** no exact registration exists
- **THEN** the route renders the package's declarative generic page
- **AND** that page contains `<cw-object editable>` within the route object context

#### Scenario: Registry is defective

- **WHEN** registration has an empty or malformed logical type, an unsupported value, or a duplicate effective key
- **THEN** plugin installation fails with a bounded diagnostic
- **AND** no arbitrary registration is selected

### Requirement: Declarative Vue route context boundary

Every custom and generic Vue object page SHALL declare exactly one marked route-level `<cw-object-context>` bound to the canonical logical type and identifier and exactly one `<cw-interaction-controller>` contained by that context.
The viewer SHALL validate this authored boundary without creating, moving, selecting, or repairing semantic elements.

#### Scenario: Valid custom page mounts

- **WHEN** a registered page declares and binds the required context and interaction controller
- **THEN** semantic descendants obtain the established GraphQL, object, and interaction services
- **AND** Vue does not mirror object, member, validation, or interaction state

#### Scenario: Declared context awaits Vue bindings

- **WHEN** an authored context connects before Vue has supplied complete route props
- **THEN** it issues no object query while identity is incomplete
- **AND** becomes operational after complete canonical identity is bound

#### Scenario: Route boundary is invalid

- **WHEN** the mounted page omits or duplicates the marked context, misplaces the interaction controller, or binds identity different from the canonical route
- **THEN** the route fails closed to a bounded diagnostic
- **AND** does not query through an ancestor context, retain prior content, or manufacture a replacement

#### Scenario: Generic component connects

- **WHEN** `<cw-object>` renders the route object
- **THEN** it uses effective or fallback grid behavior from the semantic component contract
- **AND** does not inspect Vue Router, Vue injection state, or the page registry

### Requirement: Stable application-authored Vue shell

The application root Vue component SHALL declare one stable `<cw-graphql-client>` containing application chrome, `<cw-menubars>`, loading and announcement landmarks, one default result outlet, and the router-view region.
The viewer SHALL expose endpoint and policy values for binding but SHALL NOT manufacture or prescribe the visual placement of those semantic elements.

#### Scenario: Vue object route changes

- **WHEN** the keyed router-view region changes canonical object identity
- **THEN** the declared GraphQL client and menu coordination remain connected
- **AND** the obsolete route context and interaction controller disconnect deterministically

#### Scenario: Application chooses its shell layout

- **WHEN** an application places menus, branding, results, route content, auxiliary regions, or footer differently from the reference shell
- **THEN** routing, menu events, loading, results, announcements, focus, and context disposal retain their documented behavior
- **AND** no vertical-menu or other visual hierarchy is imposed by the viewer package

#### Scenario: Shell protocol is defective

- **WHEN** the root composition omits or duplicates its provider, route region, default result outlet, loading landmark, or announcement landmark
- **THEN** bounded development and test diagnostics identify the invalid contract
- **AND** the viewer does not create, move, or silently select a landmark

#### Scenario: Application document is authored

- **WHEN** the Vue application loads
- **THEN** its application-owned HTML document selects Causeway registration, toolkit assets, locale, CSP, base URL, theme, and Vue entry assets
- **AND** the viewer package does not replace that document policy

### Requirement: Native Vue custom-element integration

The package and documented consuming build SHALL classify `cw-` tags as native custom elements and preserve their attributes, DOM properties, slots, `CustomEvent` payloads, upgrade behavior, and disconnect lifecycle.
Structured values and services SHALL be assigned as DOM properties rather than serialized into unintended attributes.

#### Scenario: Vue template is compiled

- **WHEN** a package or application single-file component contains a `cw-` tag
- **THEN** Vue compiles it as a native custom element rather than resolving a Vue component
- **AND** authored descendants and slots remain available to the element's declarative capture lifecycle

#### Scenario: Structured value is bound

- **WHEN** Vue supplies a structured executor, result, or other property-only value to a custom element
- **THEN** the value is assigned through the element's DOM property contract
- **AND** is not stringified as an HTML attribute

#### Scenario: Semantic event is published

- **WHEN** a Causeway component emits navigation, interaction, or result semantics
- **THEN** the scoped Vue bridge forwards the authoritative payload once to replaceable viewer policy
- **AND** does not modify the originating GraphQL operation or semantic outcome

#### Scenario: Vue application unmounts

- **WHEN** the host application is unmounted
- **THEN** the bridge removes its listeners and pending viewer lifecycle work
- **AND** disconnected semantic elements perform their normal cleanup

### Requirement: Vue navigation, home, and result policy

The viewer SHALL provide replaceable policy for object navigation, application-entry home behavior, and scalar, object, collection, and void action results.
Application handlers SHALL receive semantic payloads through a documented single-claim protocol without replacing component interaction behavior.

#### Scenario: Object result uses default policy

- **WHEN** an interaction returns complete navigable object identity and no application handler claims it
- **THEN** default policy navigates to the canonical Vue object route
- **AND** the component-provided identity remains authoritative

#### Scenario: Object result lacks navigation identity

- **WHEN** an object-shaped result lacks the required public logical type or opaque identifier
- **THEN** default policy presents a bounded unsupported result
- **AND** does not infer identity from title, type name, current route, or Vue state

#### Scenario: Non-object result uses a page outlet

- **WHEN** a scalar or collection result is unclaimed and the active page declares exactly one eligible result outlet
- **THEN** default policy assigns the normalized result to that outlet
- **AND** established semantic result components retain presentation authority

#### Scenario: Non-object result uses the shell outlet

- **WHEN** a scalar or collection result is unclaimed and the active page has no eligible result outlet
- **THEN** default policy assigns it to the one declared shell outlet
- **AND** duplicate or disconnected outlets fail closed rather than receiving an arbitrary result

#### Scenario: Void result retains its target

- **WHEN** a successful void action leaves the current route object available
- **THEN** default policy refreshes the current object context without adding a history entry
- **AND** preserves accessible result status

#### Scenario: Void result removes its target

- **WHEN** the authoritative post-action refresh establishes that the current route object no longer exists
- **THEN** default policy replaces the route with configured home
- **AND** does not leave a stale object page in history state

#### Scenario: Application replaces policy

- **WHEN** an application handler claims a navigation, home, or result event
- **THEN** the handler receives the unchanged semantic payload
- **AND** default policy does not also handle that event

### Requirement: Accessible generation-scoped route lifecycle

The viewer SHALL coordinate accessible loading, ready, invalid-route, unavailable, partial-error, unsupported, and terminal-error states for the current route generation.
Focus, busy state, and announcements MUST belong only to the current canonical route.

#### Scenario: Navigation begins

- **WHEN** Vue Router starts a Causeway route transition or an async page begins loading
- **THEN** the authored route region exposes a busy state and the authored live region announces bounded progress
- **AND** the previous route cannot overwrite that generation's status

#### Scenario: Navigation completes

- **WHEN** the current page is mounted and reaches its presentable state
- **THEN** focus moves to the current page heading or documented route container
- **AND** the live region announces the current authoritative title without duplicating component-local announcements

#### Scenario: Async page is superseded

- **WHEN** navigation changes before an async registered page resolves or rejects
- **THEN** the obsolete page, error, focus movement, and announcement are discarded
- **AND** the current route remains authoritative

#### Scenario: Domain data is partially available

- **WHEN** the current object context reports a partial GraphQL error with usable sibling data
- **THEN** the page preserves usable semantic content and exposes bounded partial-error status
- **AND** does not replace the whole route with a terminal state

#### Scenario: Object cannot be presented

- **WHEN** a canonical object route yields unavailable or denied data
- **THEN** the default presentation reveals neither domain state nor authorization rules
- **AND** does not claim to distinguish outcomes that the authoritative transport leaves indistinguishable

### Requirement: Vue theme and responsive shell contract

The viewer SHALL provide a copyable reference theme and stable styling hooks while leaving application CSS and shell geometry authoritative.
The reference application SHALL support wide and narrow layouts, light and dark color preferences, reduced motion, keyboard operation, and visible focus.

#### Scenario: Application supplies its own theme

- **WHEN** an application overrides documented custom properties or shell classes
- **THEN** routing and semantic component behavior remain unchanged
- **AND** component shadow-part and custom-property contracts remain available

#### Scenario: Narrow or reduced-motion presentation applies

- **WHEN** viewport width or motion preference changes
- **THEN** shell content remains operable without obscuring the active route or result
- **AND** transitions do not interfere with focus or announcements

### Requirement: Executable Vue Petclinic acceptance application

The project SHALL provide a production-built Vue Petclinic application that reuses the same deterministic Petclinic domain, public logical types, and fixture identities as the HTMX sample without copying domain implementation.
The application SHALL demonstrate a source-visible custom Vue page, generic fallback, stable shell, menus, interactions, results, canonical routing, and server refresh fallback.

#### Scenario: Custom Petclinic page loads

- **WHEN** the configured Petclinic logical type is opened through a direct canonical bookmark
- **THEN** its registered Vue single-file component binds the declared object context and renders semantic members
- **AND** the source demonstrates application-owned layout and interaction-controller placement

#### Scenario: Generic Petclinic page loads

- **WHEN** another Petclinic logical type has no registration
- **THEN** the generic Vue page renders `<cw-object>` for the same GraphQL identity
- **AND** direct refresh and browser history retain that route

#### Scenario: Existing HTMX Petclinic is rebuilt

- **WHEN** the Petclinic domain is shared with the new Vue application
- **THEN** existing HTMX routes, pages, shell, seed cardinalities, stable identities, and acceptance behavior remain unchanged
- **AND** the shared module introduces no Vue dependency

#### Scenario: Acceptance suite runs

- **WHEN** the Vue browser profile executes
- **THEN** it covers direct links, refresh, back and forward, exact custom-page precedence, generic fallback, menus, property and action interactions, object and collection results, partial errors, invalid routes, absent objects, responsive layout, keyboard focus, announcements, and both supported component toolkit policies
- **AND** browser console errors, page errors, failed resource requests, and accessibility violations fail the suite

### Requirement: Reactor and release integration

The Vue package, shared sample domain, and Vue Petclinic application SHALL integrate with the web-components reactor and project release checks while retaining opt-in frontend dependency installation.
Reproducible frontend regeneration SHALL be separated from unrelated runtime consumers and documented for maintainers.

#### Scenario: Ordinary project build runs

- **WHEN** the normal Maven reactor builds the web-components modules
- **THEN** Java artifacts and committed or packaged Vue outputs are verified without making Vue runtime classes transitive to non-Vue modules
- **AND** licensing and stale-generated-output checks remain enforceable

#### Scenario: Vue verification profile runs

- **WHEN** the documented Vue verification profile or frontend command is selected
- **THEN** locked dependencies are installed, unit tests and type checks run, production artifacts are regenerated, package contents are verified, and browser acceptance can be selected
- **AND** a generated-output difference fails verification

### Requirement: Initial client-rendered scope

The first generic Vue viewer SHALL document browser client rendering as its supported lifecycle and SHALL NOT imply verified Nuxt, SSR, or streaming-hydration compatibility.

#### Scenario: SSR support is requested

- **WHEN** an application requires Nuxt, SSR, streaming hydration, or server-only route data
- **THEN** the viewer reports that capability as separate compatibility work
- **AND** does not silently provide inconsistent custom-element hydration
