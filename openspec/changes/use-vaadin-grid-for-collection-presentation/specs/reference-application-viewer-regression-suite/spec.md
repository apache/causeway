## ADDED Requirements

### Requirement: Vaadin Grid collection regression coverage
The pinned Reference Application browser suite SHALL compare default Grid and explicit-native collection journeys against authoritative GraphQL windows, rows, values, routes, and interactions.
The retained target catalogue MUST cover every important Grid qualification and exclusion class deterministically.

#### Scenario: Stable-total scalar collection is exercised
- **WHEN** a wide deterministic collection with safe total and scalar columns is visited in default and native modes
- **THEN** Grid and native presentations expose equivalent labels, descriptions, row identities, column order, values, totals, and navigation outcomes
- **AND** default mode issues only bounded deduplicated window requests

#### Scenario: Reference and polymorphic cells are exercised
- **WHEN** retained collections contain object references, runtime-polymorphic rows, nullable values, resources, hidden cells, disabled reasons, or application renderers
- **THEN** qualified cells preserve established semantic rendering and exact navigation
- **AND** any unqualified collection remains wholly native with a bounded reason

#### Scenario: Unavailable total is exercised
- **WHEN** a deterministic retained target reports unavailable total count with previous or next windows
- **THEN** default mode uses bounded Grid plus Causeway paging without an invented size
- **AND** native mode reaches the same windows and row outcomes

#### Scenario: Unstable ordering is exercised
- **WHEN** a retained collection reports encounter or unstable ordering
- **THEN** default and native profiles both retain native presentation
- **AND** no speculative Grid range request is emitted

#### Scenario: Narrow collection is exercised
- **WHEN** the same eligible collection is rendered at or below the accepted container breakpoint
- **THEN** default and native profiles use the established native responsive presentation
- **AND** there is no Grid request, clipped focus, inaccessible cell, or page overflow attributable to that collection

#### Scenario: Collection actions are exercised
- **WHEN** a retained collection displays enabled, disabled, hidden, parameterless, and parameterized associated actions
- **THEN** Grid and native modes preserve order, preparation, validation, invocation, results, routing, request counts, and focus restoration
- **AND** Grid never owns an action callback or GraphQL mutation

### Requirement: Grid range lifecycle and delivery regression coverage
The Reference Application suite SHALL exercise Grid loading, caching, paging, refresh, replacement, failure, responsiveness, policy precedence, CSP, packaging, and native rollback against real HTMX lifecycles.

#### Scenario: Overlapping virtual ranges are requested
- **WHEN** browser scrolling causes overlapping and repeated Grid range callbacks
- **THEN** GraphQL requests remain bounded and identical current ranges are deduplicated
- **AND** visible rows, total, order, errors, and focus correspond only to the latest current generation

#### Scenario: Membership changes between ranges
- **WHEN** authoritative action or fixture mutation changes collection membership or ordering before refresh
- **THEN** the old range generation is retired and the collection requalifies from the refreshed first window
- **AND** stale cached rows and totals cannot mix with current data

#### Scenario: Route changes during Grid upgrade or range load
- **WHEN** module import, custom-element definition, renderer work, or range request is pending while HTMX replaces the route
- **THEN** disconnected work cannot restore old Grid, cells, controls, focus, errors, or route state
- **AND** the new route remains authoritative

#### Scenario: Grid family failure is injected
- **WHEN** Grid fails before connection, after connection, during rendering, or during a data-provider callback
- **THEN** connected collections recover to current native presentation without duplicate controls or requests
- **AND** reference, field, action, other routes, values, errors, and recoverable focus remain correct

#### Scenario: Responsive mode changes repeatedly
- **WHEN** an eligible collection crosses the 48rem boundary repeatedly while requests and focus are active
- **THEN** only the current wide mode contains Grid and every narrow mode remains native
- **AND** stale callbacks, duplicate controls, clipped focus, and page overflow remain absent

#### Scenario: Policy precedence profiles run
- **WHEN** browser profiles cover default, explicit component Vaadin, explicit native, deprecated editor Vaadin and native, former pilot subsets, and conflicting properties
- **THEN** Grid eligibility, shell diagnostics, closure requests, CSP hashes, and complete rollback match documented precedence
- **AND** former pilot subsets do not enable Grid

#### Scenario: Clean and incremental inventory runs
- **WHEN** capability inventory and retained target generation run from clean and incremental builds
- **THEN** output remains byte-identical unless reviewed Grid evidence intentionally changes the baseline
- **AND** every qualified and excluded target remains discoverable

#### Scenario: Grid accessibility matrix runs
- **WHEN** default and native journeys run with keyboard, pointer, zoom, wide and narrow containers, theme switching, reduced motion, forced colors, disabled reasons, partial errors, paging, and refresh
- **THEN** accessible names, descriptions, busy state, row and cell order, object links, visible focus, authoritative outcomes, and native parity remain accepted
- **AND** unexpected axe, CSP, console, page, external-request, duplicate-control, stale-cell, clipping, or overflow failures fail the suite
