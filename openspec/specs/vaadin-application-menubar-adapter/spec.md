# vaadin-application-menubar-adapter Specification

## Purpose
TBD - created by archiving change use-vaadin-menu-bar-for-application-menus. Update Purpose after archive.
## Requirements
### Requirement: Internal Menu Bar adapter boundary
Qualified application-menu presentation SHALL remain behind `<cw-menubars>`, `<cw-menubar-primary>`, `<cw-menubar-secondary>`, and `<cw-menubar-tertiary>` public contracts.
Raw Vaadin elements, item objects, events, methods, properties, theme internals, and Shadow DOM MUST remain unsupported application integration points.

#### Scenario: Application shell declares semantic menu bars
- **WHEN** the stable shell contains the established Causeway menu-bar elements
- **THEN** each qualified tier may select an internal Menu Bar without changing that markup
- **AND** application code does not receive or construct Vaadin items

#### Scenario: Menu Bar emits an internal event
- **WHEN** Menu Bar selects, opens, closes, overflows, focuses, or recreates an item
- **THEN** the adapter translates only the reviewed signal into Causeway-owned behavior
- **AND** raw toolkit events do not become public semantic events

### Requirement: Independently preserved semantic tiers
The adapter SHALL preserve primary, secondary, and tertiary tier identity through one independently qualified internal Menu Bar per non-empty tier.
It MUST preserve source order and MUST NOT merge tiers or move content between them.

#### Scenario: Three tiers are non-empty
- **WHEN** accepted menu state contains primary, secondary, and tertiary menus
- **THEN** each public tier hosts its own internal control in semantic order
- **AND** CSS, diagnostics, refresh, and focus continue to identify the original tier

#### Scenario: One tier is empty
- **WHEN** an accepted generation contains no visible menu in one tier
- **THEN** that public tier remains hidden and creates no internal control
- **AND** the other tiers remain independently eligible

### Requirement: Immutable Causeway item projection
The host SHALL project accepted menu state into bounded immutable descriptors before creating adapter-private items.
Descriptors MUST preserve menu, section, action, service, label, description, icon, visibility, usability, disabled-reason, tier, generation, and source-order semantics without containing executable callbacks or domain values.

#### Scenario: Menu contains labeled and unlabeled sections
- **WHEN** a representable menu contains actions grouped by labeled and unlabeled sections
- **THEN** the item hierarchy preserves every grouping and action in source order
- **AND** only leaf service actions are activatable

#### Scenario: Labels are duplicated or localized
- **WHEN** multiple actions share a label or labels change between generations
- **THEN** selection resolves through opaque generation-scoped semantic identity rather than visible text
- **AND** the current service logical type and action ID remain authoritative

#### Scenario: Projection data is inspected
- **WHEN** adapter-private item objects or diagnostics are reviewed
- **THEN** they contain no GraphQL variables, prepared argument snapshots, protected values, serialized domain values, full errors, or executable interaction callbacks
- **AND** metadata remains bounded to presentation identity and state

### Requirement: Causeway-owned service-action activation
Menu Bar item selection SHALL delegate exactly once to the established Causeway service-action interaction path.
Vaadin callbacks MUST NOT construct GraphQL, navigate, interpret results, or bypass authorization, validation, preparation, cancellation, and host action-result policy.

#### Scenario: Enabled leaf action is selected
- **WHEN** the current generation resolves an enabled selected leaf identity
- **THEN** the host invokes the same service context and action ID used by native presentation exactly once
- **AND** existing interaction and result semantic events remain authoritative

#### Scenario: Disabled item is selected internally
- **WHEN** a selected item is disabled, stale, unknown, a menu, or a section grouping
- **THEN** no service action is invoked
- **AND** no navigation or GraphQL work is created

#### Scenario: Action returns an object or void result
- **WHEN** the established interaction controller publishes an action result
- **THEN** existing host policy determines navigation, rendering, refresh, dismissal, or no-op behavior
- **AND** Menu Bar does not infer a route from the result

### Requirement: Disabled reasons and accessible semantics
The adapter SHALL preserve labels, descriptions, hierarchy, disabled state, and bounded disabled reasons as accessible relationships.
It MUST retain visible focus and distinguish expandable groupings from activatable actions.

#### Scenario: Action is disabled with a reason
- **WHEN** preparation marks a visible action unusable with a bounded reason
- **THEN** the corresponding item is disabled and its accessible description exposes that reason
- **AND** the item cannot invoke by keyboard or pointer

#### Scenario: Assistive technology traverses a tier
- **WHEN** a qualified tier is inspected or operated
- **THEN** tier, menu, grouping, and action names and expanded or disabled states are available in semantic order
- **AND** decorative icon hints are not announced as duplicate labels

### Requirement: Responsive overflow qualification
The host SHALL select Vaadin or complete native tier presentation from current policy, family health, generation, hierarchy, visibility, connection, and container-width qualification.
Narrow Vaadin presentation MUST be used only when every visible authorized action retains order, keyboard access, accessible labeling, Escape behavior, focus return, and zero page overflow.

#### Scenario: Wide representable tier qualifies
- **WHEN** a connected non-empty representable tier is wide under the Vaadin policy
- **THEN** it may use the internal Menu Bar and reviewed overflow behavior
- **AND** all current actions remain reachable in source order

#### Scenario: Narrow hierarchy is qualified
- **WHEN** browser evidence accepts the hierarchy under current narrow width
- **THEN** Menu Bar overflow preserves every authorized action, internationalized overflow labeling, dismissal, and focus
- **AND** resizing issues no GraphQL request

#### Scenario: Responsive equivalence is not proven
- **WHEN** current width or hierarchy cannot preserve the accepted narrow contract
- **THEN** the complete tier uses the established native responsive disclosure
- **AND** no mixed native and Vaadin controls remain inside the tier

### Requirement: Generation and revision scoped lifecycle
Every asynchronous import, definition wait, projection, render, event, responsive transition, and focus restoration SHALL be bound to current connection, menu generation, policy, adapter, responsive, and family revisions.
Cleanup MUST release generated controls, items, listeners, observers, and focus intent exactly once.

#### Scenario: Refresh supersedes adapter work
- **WHEN** menu refresh replaces a generation while import, definition, projection, or rendering remains pending
- **THEN** stale work cannot install controls or activate items
- **AND** only current accepted menu state is rendered

#### Scenario: Tier disconnects or becomes native
- **WHEN** a tier disconnects, empties, is replaced, changes policy, becomes unsupported, or becomes responsively disqualified
- **THEN** adapter resources are released and native or hidden state is current
- **AND** delayed work cannot restore the superseded control

#### Scenario: Selected stale item arrives
- **WHEN** an item-selected event references an earlier generation or revision
- **THEN** it is ignored without invocation, navigation, focus corruption, or unbounded diagnostic data

### Requirement: Semantic focus and dismissal continuity
Focus and transient dismissal SHALL remain Causeway-owned across internal item recreation, overflow, refresh, responsive transitions, action completion, and native fallback.
Focus identity MUST use tier and semantic action or grouping identity rather than toolkit DOM identity.

#### Scenario: User dismisses with Escape
- **WHEN** the user closes an expanded nested or overflow menu with Escape
- **THEN** focus returns to the semantic item or safe tier target that opened it
- **AND** sibling tiers retain their own transient state

#### Scenario: Focused action disappears on refresh
- **WHEN** authorization or state refresh removes the focused action
- **THEN** focus resolves to a safe current tier target or shell location
- **AND** it does not restore a stale or hidden toolkit node

### Requirement: Family-scoped fallback and recovery
Explicit native policy and automatic disqualification SHALL preserve the complete established native menu implementation.
One Menu Bar family failure SHALL return every connected tier natively without disabling any other toolkit family or viewer subsystem.

#### Scenario: Policy is native
- **WHEN** `component-toolkit=native` is effective
- **THEN** every menu tier uses native controls and requests no Menu Bar closure
- **AND** GraphQL, menu resources, action semantics, routes, authentication, and markup remain unchanged

#### Scenario: Closure or definition fails
- **WHEN** Menu Bar import, definition, projection, rendering, CSP, or listener setup fails
- **THEN** all connected tiers rerender from current authoritative state using native controls
- **AND** references, fields, actions, Grid, GraphQL, routing, and authentication remain independently operational

#### Scenario: Connected retry succeeds
- **WHEN** the failed family is explicitly retried after the closure becomes available
- **THEN** current qualified tiers may upgrade from current state
- **AND** stale controls, items, listeners, or failure metadata are not reused

### Requirement: Authenticated stable-shell lazy delivery
The Menu Bar closure SHALL load only after an authenticated stable shell connects a non-empty qualified tier.
Login, authentication challenge, failure, empty-menu, unsupported, and explicit-native states MUST request zero Menu Bar bytes.

#### Scenario: Authenticated shell has a qualified tier
- **WHEN** the first qualified tier connects under effective Vaadin policy
- **THEN** one same-origin Menu Bar closure request is shared by all tiers and subsequent routes
- **AND** route data and unrelated adapter readiness do not wait for it

#### Scenario: Authentication chrome is rendered
- **WHEN** login, authentication failure, access challenge, or logged-out chrome is shown
- **THEN** no Menu Bar closure or internal control is requested
- **AND** authentication behavior remains native and Spring-owned

### Requirement: Deterministic secure Menu Bar packaging
The implementation SHALL package a pinned independently reproducible free-core Menu Bar closure with accepted checksums, legal metadata, production vulnerability evidence, exact CSP style hashes, and raw and compressed budgets.
Runtime closure paths MUST exclude telemetry activation, development-only code, commercial components, CDN assets, and external requests.

#### Scenario: Ordinary Maven build runs
- **WHEN** the Web Components reactor builds without an explicit regeneration profile
- **THEN** checked-in Menu Bar bytes, policy metadata, hashes, licenses, and budgets are verified without npm
- **AND** packaged resources are byte-identical to accepted assets

#### Scenario: Explicit regeneration runs
- **WHEN** the Menu Bar regeneration profile is invoked with supported Node and pinned inputs
- **THEN** it audits, builds, verifies, and refreshes deterministic assets and legal metadata
- **AND** unexpected dependency, checksum, hash, budget, telemetry, or vulnerability changes fail the gate

#### Scenario: Production CSP runs
- **WHEN** representative menu states execute under production CSP
- **THEN** only reviewed exact candidate-originated style hashes are permitted with `style-src-attr 'none'`
- **AND** there are no unexpected CSP violations or external requests

### Requirement: Menu Bar accessibility and browser qualification
Default and native application-menu journeys SHALL remain release-qualified across hierarchy, interaction, responsive, theme, security, authentication, failure, and lifecycle matrices.

#### Scenario: Browser matrix runs
- **WHEN** primary, secondary, tertiary, nested, labeled, disabled, hidden, parameterized, overflowed, refreshed, and failed menus run by keyboard and pointer in wide and narrow containers
- **THEN** order, names, descriptions, expanded state, disabled reasons, focus, Escape, invocation, action results, native parity, and zero overflow satisfy the accepted contract
- **AND** unexpected axe, CSP, console, page, external-request, duplicate-control, stale-item, clipping, overlay, focus, or order failures fail the suite

#### Scenario: Themes and user preferences run
- **WHEN** light, dark, reduced-motion, forced-colors, and zoom matrices exercise qualified and native menus
- **THEN** content, visible focus, contrast, overflow, and action reachability remain accepted
- **AND** no preference changes authoritative application-menu state

#### Scenario: Failure diagnostics are inspected
- **WHEN** import, definition, projection, render, interaction, route, refresh, or CSP failure is recorded
- **THEN** diagnostics contain only family, phase, bounded classification, tier, and revision metadata
- **AND** action values, GraphQL variables, prepared snapshots, protected values, and full errors remain absent
