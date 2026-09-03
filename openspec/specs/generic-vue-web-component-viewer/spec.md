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
The application SHALL demonstrate source-visible exact-type Vue pages, generic fallback, a stable application-owned shell, menus, interactions, results, canonical routing, and server refresh fallback.
For the shell and the `petclinic.HomePage`, `petclinic.PetOwner`, `petclinic.Pet`, and `petclinic.Visit` routes, the HTMX Petclinic application SHALL be the authoritative presentation reference.
Presentation equivalence SHALL preserve user-visible information architecture and responsive relationships without requiring framework wrapper markup or browser pixels to be identical.

#### Scenario: Reconciled Petclinic page loads

- **WHEN** HomePage, PetOwner, Pet, or Visit is opened through a direct canonical Vue bookmark
- **THEN** an exact-type Vue single-file component binds the declared object context and interaction controller
- **AND** it presents the same ordered headings, sections, selected semantic members, action placement, result outlets, descriptions, collection columns, paging, filtering, sorting, and row-preview affordances as the corresponding HTMX Petclinic page
- **AND** it does not expose technical or additional members omitted by that HTMX page

#### Scenario: Vue PetOwner page matches the reference composition

- **WHEN** the deterministic PetOwner fixture is rendered by Vue
- **THEN** Identity, Contact, and Details occupy the reference details column while Pets and Visits occupy the reference collections column
- **AND** the owner actions include the empty-result action and the declared standalone related-owner result
- **AND** the derived last-visit value, visit notes column, paging values, parameter presentation, and nested preview declarations match the HTMX page

#### Scenario: Vue shell is presented at a wide viewport

- **WHEN** a reconciled route is ready at the documented wide acceptance viewport
- **THEN** branding, primary menu order, utility-menu grouping, header geometry, content inset, typography, palette, footer content, and document-title suffix are equivalent to the HTMX shell
- **AND** the route uses the available width and reference column relationships without an accidental persistent container outline
- **AND** keyboard users retain a visible focus indication on an appropriate heading or route landmark

#### Scenario: Vue shell is presented at a narrow viewport

- **WHEN** a reconciled route is ready at the documented narrow acceptance viewport
- **THEN** shell navigation and page columns collapse in the same order and at an equivalent breakpoint to the HTMX presentation
- **AND** menus, actions, results, fields, collections, previews, and pagination remain visible and keyboard operable without horizontal page overflow

#### Scenario: Generic fallback fixture loads

- **WHEN** a deliberately unregistered acceptance logical type is opened through a direct canonical Vue bookmark
- **THEN** the generic Vue page renders `<cw-object>` for the same GraphQL identity
- **AND** direct refresh and browser history retain that route
- **AND** none of the four reconciled Petclinic route types is used to demonstrate fallback

#### Scenario: Existing HTMX Petclinic is rebuilt

- **WHEN** Vue presentation is reconciled with the HTMX reference
- **THEN** existing HTMX routes, page composition, shell, seed cardinalities, stable identities, and acceptance behavior remain unchanged
- **AND** the shared domain introduces no Vue dependency or presentation authority

#### Scenario: Presentation parity is regression tested

- **WHEN** the Vue browser profile executes
- **THEN** it verifies semantic parity for all four reconciled route types and high-value computed shell and layout relationships at representative wide and narrow viewports
- **AND** comparisons use bounded structural or computed-style assertions rather than pixel-perfect screenshot equality
- **AND** it covers both supported component-toolkit policies where their presentation differs

#### Scenario: Acceptance suite runs

- **WHEN** the Vue browser profile executes
- **THEN** it covers direct links, refresh, back and forward, exact custom-page precedence, deliberate generic fallback, menus, property and action interactions, object and collection results, partial errors, invalid routes, absent objects, responsive layout, keyboard focus, announcements, and both supported component toolkit policies
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

### Requirement: Vue host-owned action policy

The Vue viewer SHALL expose a pre-invocation action policy that receives canonical action identity, a single-claim token, and the current immutable viewer-policy context.
A claimed action MUST NOT proceed to foundation GraphQL validation or invocation, and asynchronous policy failure SHALL reach the configured viewer error policy without creating a successful action result.
The policy SHALL compose with application-owned routing and lifecycle disposal without introducing authentication state into Vue route components.

#### Scenario: Vue action policy claims a request

- **WHEN** the configured Vue action policy claims a service or object action request
- **THEN** ordinary GraphQL dispatch is canceled before invocation
- **AND** the application policy owns any replacement effect

#### Scenario: Vue action policy does not claim a request

- **WHEN** the configured action policy returns without claiming an ordinary action
- **THEN** existing parameter, validation, confirmation, invocation, result, refresh, and navigation behavior continues
- **AND** the action is not duplicated

#### Scenario: Vue action policy fails

- **WHEN** a synchronous or asynchronous action policy throws or rejects
- **THEN** the viewer reports the error through its configured error policy
- **AND** fail-closed actions such as framework Logout do not fall through to GraphQL invocation

### Requirement: Generic Vue framework Logout safety

The generic Vue viewer SHALL classify `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no application authentication policy claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom requests for the exact identity.
A future authentication integration SHALL own endpoint selection, HTTP method, current anti-forgery evidence, session cleanup, and post-logout navigation.

#### Scenario: Vue application has no authentication integration

- **WHEN** application menus contain the framework Logout action and no Vue host logout capability is configured
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** direct or stale requests cannot reach GraphQL invocation

#### Scenario: Vue application supplies a logout policy

- **WHEN** an application explicitly registers a policy that claims the framework Logout identity
- **THEN** the viewer delegates the operation before GraphQL dispatch
- **AND** the application remains responsible for a secure accessible logout affordance and complete browser-session behavior

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the Vue viewer does not suppress or reinterpret it as framework Logout
- **AND** ordinary action or local-resource policy applies

### Requirement: Vue local-resource result navigation

The Vue viewer SHALL interpret a local-resource semantic result through a bounded host policy and a documented application-local resource base.
Its default behavior SHALL perform validated full-document same-origin navigation according to `SAME_WINDOW` or `NEW_WINDOW` and MUST NOT send the target through Vue Router.

#### Scenario: Same-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `SAME_WINDOW`
- **THEN** the Vue host performs full-document navigation to the resolved application-local target
- **AND** the canonical object router does not claim the target

#### Scenario: New-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `NEW_WINDOW`
- **THEN** the Vue host requests a new opener-isolated browsing context for the resolved target
- **AND** the current route generation remains active unless ordinary lifecycle policy later changes it

#### Scenario: Application claims local-resource navigation

- **WHEN** the configured result policy claims a valid local-resource result
- **THEN** default browser navigation does not run
- **AND** the application can apply stricter deployment policy without mutating the canonical result

#### Scenario: Local resource target is unsafe

- **WHEN** a path is malformed, scheme-relative, cross-origin, credential-bearing, outside the configured application-local boundary, or paired with an unknown strategy
- **THEN** the Vue viewer refuses navigation and reports the failure through its error policy
- **AND** it does not repair the value, route it as a domain object, or infer Logout semantics

#### Scenario: Vue application uses a nested deployment context

- **WHEN** the application configures a non-root local-resource base and receives an application-local path
- **THEN** resolution preserves that deployment context exactly once
- **AND** it remains independent of the Vue object-route base path

### Requirement: Authentication-neutral secured-host integration points

The generic Vue viewer SHALL remain authentication-neutral while allowing an application-owned secured shell to supply an authenticated GraphQL executor, current-user chrome, native logout form, and exact pre-invocation Logout policy.
The viewer MUST NOT fetch credentials or CSRF state, create login or logout endpoints, persist tokens, or infer authentication from route or result paths.

#### Scenario: Secured application binds an executor

- **WHEN** an authenticated Vue application binds a CSRF-decorating executor to its stable `<cw-graphql-client>`
- **THEN** semantic components use that executor without knowing the authentication mechanism
- **AND** generic routing, contexts, interaction results, and lifecycle remain unchanged

#### Scenario: Secured application owns logout chrome

- **WHEN** an application renders current-user and POST logout controls in its stable shell and claims exact framework Logout requests
- **THEN** the host policy can submit its native logout contract before GraphQL invocation
- **AND** the viewer neither manufactures nor duplicates authentication chrome

#### Scenario: Generic application has no authentication context

- **WHEN** the same Vue application runs without an authentication integration
- **THEN** it may omit executor decoration and secured-session chrome
- **AND** the generic viewer introduces no authentication request or endpoint

### Requirement: Dedicated secured Vue launcher

The Vue Petclinic sample SHALL provide a repository-root-relative `run-secured.sh` launcher that selects its secured Maven profile while preserving the environment and argument behavior of `run.sh`.

#### Scenario: Secured Vue script runs

- **WHEN** a maintainer executes `viewers/webcomponents/sample-vue-petclinic/run-secured.sh`
- **THEN** Maven launches the secured Vue Petclinic application and local login flow
- **AND** `JAVA_HOME`, `MVN`, and additional Maven arguments are honored

#### Scenario: Ordinary Vue script runs

- **WHEN** a maintainer executes the existing Vue `run.sh`
- **THEN** the bypass-secured ordinary Vue application remains selected
- **AND** the new secured integration remains opt-in
