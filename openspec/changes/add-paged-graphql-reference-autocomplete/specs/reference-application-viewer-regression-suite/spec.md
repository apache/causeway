## ADDED Requirements

### Requirement: Multi-window reference autocomplete regression coverage
The pinned Reference Application suite SHALL exercise deterministic rich autocomplete results larger than its configured qualification page for property or action-parameter reference selection.
It MUST cover GraphQL metadata, later-window selection, argument dependencies, cancellation, legacy compatibility, and toolkit-neutral outcomes.

#### Scenario: GraphQL autocomplete spans several windows
- **WHEN** a deterministic search returns more references than the configured page size
- **THEN** adjacent windows report accurate items, offsets, counts, totals, maximum, ordering, and continuation state
- **AND** their combined authoritative encounter order contains no omitted or duplicated position

#### Scenario: Invalid window is requested
- **WHEN** qualification requests a negative offset, non-positive size, or size above the configured maximum
- **THEN** GraphQL returns a bounded non-disclosing error
- **AND** existing legacy autocomplete remains executable

#### Scenario: Browser selects a later-window reference
- **WHEN** the reference editor searches and requests an item absent from the first window
- **THEN** the internal adapter obtains the later server window and selection preserves the item's semantic identity
- **AND** validation or submission uses the existing Causeway interaction contract

#### Scenario: Search generation is superseded
- **WHEN** the browser changes filter or dependent parameter values while a page is in flight or replaces the route
- **THEN** obsolete pages cannot alter suggestions, total count, selection, error, focus, or current route state
- **AND** no unexpected GraphQL, console, page, CSP, external-request, accessibility, or overflow failure occurs

#### Scenario: Native and candidate modes are qualified
- **WHEN** the same semantic autocomplete target runs with native fallback and explicitly enabled Vaadin reference widgets
- **THEN** both modes preserve authoritative values and honest continuation or refinement behavior
- **AND** unaffected routes retain zero Vaadin asset requests
