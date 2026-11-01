## MODIFIED Requirements

### Requirement: Vue navigation, home, and result policy

The viewer SHALL provide replaceable policy for object navigation, application-entry home behavior, and scalar, object, collection, void, local-resource, and unsupported action results.
Application handlers SHALL receive semantic payloads through a documented single-claim protocol without replacing component interaction behavior.
For every unclaimed result, default policy SHALL perform one deterministic host handoff, dismiss transient result presentation owned by the interaction source, and prevent obsolete viewer-owned result presentation from surviving a later replacement or navigation outcome.

#### Scenario: Object result uses default policy

- **WHEN** an interaction returns complete navigable object identity, including a view-model identity such as `UserMemento`, and no application handler claims it
- **THEN** default policy clears obsolete viewer-owned result presentation and navigates to the canonical Vue object route
- **AND** the component-provided logical type and opaque identifier remain authoritative
- **AND** transient source result chrome is dismissed after handoff

#### Scenario: Object result lacks navigation identity

- **WHEN** an object-shaped result lacks the required public logical type or opaque identifier
- **THEN** default policy replaces the selected result outlet with a bounded unsupported result
- **AND** dismisses transient source result chrome
- **AND** does not infer identity from title, type name, action identity, current route, or Vue state

#### Scenario: Non-object result uses a page outlet

- **WHEN** a scalar or collection result is unclaimed and the active page declares exactly one eligible result outlet
- **THEN** default policy assigns the normalized result to that outlet and replaces its earlier viewer-owned presentation
- **AND** established semantic result components retain presentation authority
- **AND** transient source result chrome is dismissed after handoff

#### Scenario: Non-object result uses the shell outlet

- **WHEN** a scalar or collection result is unclaimed and the active page has no eligible result outlet
- **THEN** default policy assigns it to the one declared shell outlet and replaces its earlier viewer-owned presentation
- **AND** duplicate or disconnected outlets fail closed rather than receiving an arbitrary result
- **AND** transient source result chrome is dismissed after handoff

#### Scenario: Later object result replaces an earlier unsupported result

- **WHEN** a result has been presented in a viewer-owned shell or page outlet and a later unclaimed action returns a navigable object
- **THEN** the obsolete result presentation is cleared before or as canonical navigation is committed
- **AND** the new object route contains no heading, message, or dismissal control belonging to the earlier result

#### Scenario: Void result retains its target

- **WHEN** a successful void action leaves the current route object available
- **THEN** default policy refreshes the current object context without adding a history entry
- **AND** replaces obsolete viewer-owned result presentation with accessible completion status
- **AND** dismisses transient source result chrome

#### Scenario: Void result removes its target

- **WHEN** the authoritative post-action refresh establishes that the current route object no longer exists
- **THEN** default policy clears obsolete viewer-owned result presentation and replaces the route with configured home
- **AND** does not leave a stale object page or result in history state

#### Scenario: Application replaces policy

- **WHEN** an application handler claims a navigation, home, or result event
- **THEN** the handler receives the unchanged semantic payload
- **AND** default policy does not navigate, present, clear, or dismiss on the handler's behalf

### Requirement: Executable Vue Petclinic acceptance application

The project SHALL provide a production-built Vue Petclinic application that reuses the same deterministic Petclinic domain, public logical types, and fixture identities as the HTMX sample without copying domain implementation.
The application SHALL demonstrate source-visible exact-type Vue pages, generic fallback, a stable application-owned shell, one current-username utility menu, interactions, results, canonical routing, and server refresh fallback.
The application SHALL use Account only as a non-identifying fallback when authoritative current-user display identity is unavailable and SHALL give exact Logout a distinct accessible outlined presentation.
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
- **THEN** branding, primary menu order, one tertiary utility menu, header geometry, content inset, typography, palette, footer content, and document-title suffix are equivalent to the HTMX shell
- **AND** the utility menu title is the current username supplied by authoritative host context, or Account when no trustworthy display identity is available
- **AND** no separate System or Account menu duplicates **Me** or remains empty
- **AND** exact Logout is labelled **Sign out** and has a distinct accessible outlined or equivalently bounded treatment under native and Vaadin-backed presentation
- **AND** adjacent actions do not inherit the sign-out treatment
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

#### Scenario: Tertiary object actions navigate without stale results

- **WHEN** browser acceptance activates **Me** and then **Configuration** through the username-labelled utility menu
- **THEN** each complete object result navigates to its canonical generic Vue object route
- **AND** the second route retains no source panel or host result belonging to the first action

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
- **THEN** it covers direct links, refresh, back and forward, exact custom-page precedence, deliberate generic fallback, username-labelled utility menus, distinct exact-Logout presentation, tertiary object-result lifecycle, property and action interactions, object and collection results, partial errors, invalid routes, absent objects, responsive layout, keyboard focus, announcements, and both supported component toolkit policies
- **AND** browser console errors, page errors, failed resource requests, and accessibility violations fail the suite
