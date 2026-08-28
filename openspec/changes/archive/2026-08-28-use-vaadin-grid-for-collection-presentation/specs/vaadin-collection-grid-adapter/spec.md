## ADDED Requirements

### Requirement: Internal Grid adapter boundary
Qualified collection presentation SHALL remain behind `<cw-collection>` and `<cw-collection-column>` public contracts.
Raw Vaadin elements, item objects, data providers, renderer callbacks, events, methods, properties, and Shadow DOM MUST remain unsupported application integration points.

#### Scenario: Application declares a collection
- **WHEN** application markup declares a collection and semantic columns using `cw-*` elements
- **THEN** the Causeway component may select an internal Grid adapter without changing that markup
- **AND** application code does not receive or construct Vaadin item or data-provider objects

#### Scenario: Grid emits an internal callback or event
- **WHEN** Grid requests data, recycles a cell, changes internal focus, or emits another toolkit event
- **THEN** the adapter translates only the reviewed signal into a Causeway-owned callback
- **AND** raw toolkit events do not become public semantic events

### Requirement: Qualified Grid presentation modes
The collection host SHALL select virtual Grid, bounded Grid, or native presentation from explicit semantic qualification.
It MUST distinguish a safe zero total from an unavailable total and MUST NOT invent size or ordering guarantees.

#### Scenario: Stable virtual Grid qualifies
- **WHEN** an active visible wide collection exposes bounded windows, deterministic ordering, a safe stable total, and supported semantic columns
- **THEN** it may use virtual Grid with bounded range requests
- **AND** the reported total is the only logical size supplied to the data provider

#### Scenario: Total is unavailable
- **WHEN** deterministic ordering and bounded windows are available but total count is unavailable
- **THEN** the adapter renders only the current bounded window in Grid
- **AND** Causeway-owned previous and next controls use normalized window metadata without presenting returned count as total

#### Scenario: Ordering is not deterministic
- **WHEN** the window reports encounter or otherwise unstable cross-request ordering
- **THEN** the collection retains its established native presentation
- **AND** Grid does not issue additional range requests that could duplicate or omit rows

#### Scenario: Window capability is absent
- **WHEN** the server lacks the additive collection `window` operation
- **THEN** established backward-compatible collection loading remains available
- **AND** Grid presentation is not approximated over an unbounded `get` result

### Requirement: Causeway-owned column projection
Declarative and effective-grid collection columns SHALL remain ordered and interpreted by Causeway before Grid columns are created.
The adapter MUST NOT discover hidden domain members or broaden the GraphQL row selection.

#### Scenario: Declarative columns qualify
- **WHEN** `<cw-collection-column>` children declare labels, order, members, and test identities
- **THEN** Grid columns preserve that accepted configuration and the leading object-identity column
- **AND** selected row requirements remain bounded to those columns

#### Scenario: Column configuration changes
- **WHEN** a column is added, removed, relabelled, reordered, hidden, or replaced
- **THEN** the host invalidates the current adapter and range generation before reprojecting columns
- **AND** stale columns and callbacks cannot reappear

#### Scenario: Column is unsupported
- **WHEN** one column cannot preserve its semantic renderer, lifecycle, accessibility, or bounded selection
- **THEN** the collection remains wholly native rather than mixing unqualified Grid cells
- **AND** diagnostics identify only a bounded classification without row values

### Requirement: Authoritative semantic cell rendering
Causeway SHALL retain authority over object links, values, nulls, references, resources, disabled reasons, hidden state, errors, unsupported values, and application renderers inside Grid cells.
Grid MUST provide containers and recycling only.

#### Scenario: Standard scalar cell renders
- **WHEN** a visible row property has an accepted standard renderer
- **THEN** the established Causeway renderer output populates the current Grid cell
- **AND** value conversion and display semantics are unchanged from native collection presentation

#### Scenario: Object identity or reference renders
- **WHEN** a row identity or cell value is navigable
- **THEN** the established Causeway object-link behavior emits the existing navigation request exactly once
- **AND** Grid selection or activation does not navigate independently

#### Scenario: Application renderer is authoritative
- **WHEN** an application renderer selects a cell presentation, including one that reuses a standard renderer identifier
- **THEN** its explicit authority remains intact
- **AND** Grid qualification never infers standard ownership from the identifier alone

#### Scenario: Row-relative error renders
- **WHEN** a bounded GraphQL response contains an error for one row and column
- **THEN** that cell exposes the bounded error while safe sibling cells and rows remain usable
- **AND** Grid does not enlarge or retry the requested window implicitly

#### Scenario: Hidden or disabled cell renders
- **WHEN** a row member is hidden or disabled
- **THEN** hidden content is absent from interaction and accessibility while disabled content retains its reason
- **AND** neither state enables unsupported Grid interaction

### Requirement: Bounded range broker
Causeway SHALL own a generation-scoped range broker over the existing collection-window operation.
The broker MUST bound offset, size, concurrency, cache entries, errors, row contexts, and retained snapshots.

#### Scenario: Grid requests a range
- **WHEN** virtual Grid requests an eligible offset and size
- **THEN** the broker maps it to one bounded existing GraphQL window operation
- **AND** preserves configured maximum size, authorization, ordering, selection, and normalized metadata

#### Scenario: Identical range is already available
- **WHEN** Grid repeats a range that is cached or in flight for the same generation and column selection
- **THEN** the broker reuses the retained result or promise
- **AND** it does not publish a duplicate GraphQL request

#### Scenario: Overlapping ranges arrive
- **WHEN** Grid requests overlapping or adjacent ranges while another request is in flight
- **THEN** the broker preserves only the bounded accepted request set and callbacks
- **AND** one obsolete callback cannot cancel or overwrite another still-current visible range

#### Scenario: Broker limit is reached
- **WHEN** scrolling would exceed the accepted cache, request, row-context, or snapshot bound
- **THEN** least-current entries are disconnected and released before new work is retained
- **AND** no unbounded route-lifetime cache forms

#### Scenario: Request becomes stale
- **WHEN** route generation, member identity, columns, policy, renderer registry, responsive mode, refresh, or connection lifetime supersedes a range
- **THEN** its request is aborted or its callback ignored
- **AND** stale rows, totals, errors, paging, focus, and context cannot replace current state

### Requirement: Collection-owned paging and refresh
Causeway SHALL retain paging, refresh, busy, empty, disabled, partial-error, and terminal-error state around Grid.
The adapter MUST NOT create an independent collection state machine.

#### Scenario: Bounded Grid moves to next window
- **WHEN** a user activates an available Causeway next control
- **THEN** the host loads the normalized next offset and rerenders the bounded Grid window
- **AND** request count, focus intent, errors, and previous availability remain authoritative

#### Scenario: Last bounded window is reached
- **WHEN** normalized metadata reports no next window
- **THEN** the next control is absent or disabled accessibly
- **AND** Grid does not probe beyond the terminal range

#### Scenario: Collection refreshes
- **WHEN** authoritative interaction or explicit refresh invalidates collection data
- **THEN** all range and row-context generations are retired before current data is requested
- **AND** changed total, ordering, and membership re-enter qualification from first principles

#### Scenario: Empty collection renders
- **WHEN** an eligible collection reports a safe zero total or returns an empty terminal first window
- **THEN** the established accessible empty state remains authoritative
- **AND** no empty Grid or speculative request replaces it

### Requirement: Wide-only responsive Grid qualification
Grid SHALL qualify only when the collection's existing container exceeds 48rem.
At or below that boundary the native responsive collection presentation MUST remain authoritative.

#### Scenario: Wide collection qualifies
- **WHEN** an otherwise eligible collection is wider than 48rem
- **THEN** it may connect Grid without causing horizontal page overflow
- **AND** column and focus semantics remain readable at supported zoom

#### Scenario: Collection becomes narrow
- **WHEN** a connected Grid crosses to 48rem or narrower
- **THEN** the host removes Grid and renders the native responsive presentation from current Causeway state
- **AND** late Grid work cannot restore controls, cells, focus, or overflow

#### Scenario: Collection becomes wide again
- **WHEN** a narrow native collection later exceeds 48rem
- **THEN** the host may lazily reconnect Grid from the current authoritative state
- **AND** it does not reload a range already valid and available solely because presentation changed

### Requirement: Collection focus and associated-action continuity
Causeway SHALL preserve semantic focus intent, object navigation, row-context cleanup, and associated-action composition across Grid rendering.
Recycled cell nodes MUST NOT become durable identity.

#### Scenario: Focused row survives refresh
- **WHEN** refresh or paging retains the same route, collection, row identity, column, visibility, and usability
- **THEN** focus may restore to the corresponding current semantic target
- **AND** it never restores to a recycled or disconnected cell

#### Scenario: Focused row disappears
- **WHEN** refresh, paging, authorization, or concurrent change removes the focused row or member
- **THEN** focus moves to the current collection or paging fallback according to existing policy
- **AND** hidden or stale content is not revived

#### Scenario: Associated action accompanies Grid
- **WHEN** collection-associated `<cw-action>` children are visible
- **THEN** they remain outside Grid in declaration order and retain independent preparation, prompting, invocation, navigation, and focus behavior
- **AND** Grid does not convert them to row or toolbar callbacks

### Requirement: Grid-scoped fallback and native rollback
Grid failure SHALL be bounded independently from reference, field, action, and future menu families.
Explicit native policy SHALL provide complete Grid rollback without application or data migration.

#### Scenario: Grid module fails before connection
- **WHEN** the same-origin Grid module fails to load or define its controls
- **THEN** the family enters one bounded failed state and eligible collections render natively
- **AND** values, rows, errors, requests, and other adapter families remain unaffected

#### Scenario: Grid fails after connection
- **WHEN** an existing Grid encounters module, definition, renderer, data-provider, policy, or lifecycle failure
- **THEN** all connected Grid collections rerender from current authoritative Causeway state using native presentation
- **AND** duplicate controls, callbacks, requests, stale cells, or focus targets do not remain

#### Scenario: Explicit native mode runs
- **WHEN** `component-toolkit=native` is effective
- **THEN** no Grid adapter, closure request, data provider, candidate style hash, or Grid diagnostic is enabled
- **AND** GraphQL operations, canonical routes, persisted data, and application markup remain unchanged

#### Scenario: Grid family recovers
- **WHEN** a later valid configuration revision replaces a failed Grid module policy
- **THEN** currently connected wide eligible collections may requalify once
- **AND** stale failure or import work from the previous revision cannot alter them

### Requirement: Deterministic secure Grid packaging
The Grid closure SHALL use only reviewed pinned free-core dependencies and same-origin deterministic generated assets.
It MUST satisfy accepted checksum, legal, vulnerability, telemetry, CSP, and compressed-budget policy before default qualification.

#### Scenario: Grid candidate is built twice
- **WHEN** the pinned build runs from clean dependency installation twice
- **THEN** bundle bytes, checksum, legal metadata, entry points, package graph, and style hashes are identical
- **AND** gzip size does not exceed 196608 bytes

#### Scenario: Candidate requires excluded capability
- **WHEN** the candidate requires Grid Pro, commercial code, Flow, Binder, server-side Vaadin state, Menu Bar, telemetry, CDN content, external resources, or blanket inline style
- **THEN** qualification fails and collections remain native
- **AND** policy is not broadened to make the candidate pass

#### Scenario: Exact CSP is enforced
- **WHEN** representative virtual and bounded Grid states run under production CSP
- **THEN** only reviewed exact candidate-originated style hashes are permitted with `style-src-attr 'none'`
- **AND** there are no unexpected CSP violations or external requests

#### Scenario: Native or unaffected route loads
- **WHEN** policy is native or no connected wide collection qualifies
- **THEN** Grid contributes zero requested bytes
- **AND** route readiness does not wait for Grid

### Requirement: Grid accessibility and browser qualification
Default and native collection journeys SHALL remain release-qualified across keyboard, accessibility, responsive, theme, security, and lifecycle matrices.

#### Scenario: Accessibility matrix runs
- **WHEN** representative Grid states are exercised with keyboard, pointer, zoom, narrow and wide containers, light and dark themes, reduced motion, and forced colors
- **THEN** labels, descriptions, busy state, empty state, disabled reasons, errors, focus order, visible focus, row and cell navigation, object links, and paging satisfy the accepted contract
- **AND** axe, console, page, external-request, duplicate-control, stale-cell, clipping, and overflow results are clean

#### Scenario: Values remain bounded in failures
- **WHEN** module, definition, renderer, data-provider, route, or CSP failure is recorded
- **THEN** events and diagnostics contain only family, phase, bounded classification, and revision metadata
- **AND** row values, protected values, GraphQL variables, serialized snapshots, and full server errors remain absent
