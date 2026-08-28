## ADDED Requirements

### Requirement: Toolkit-backed qualified collection presentation
The `<cw-collection>` component SHALL select an internal Grid only for semantically qualified active wide collections while preserving its public markup, state, events, renderers, navigation, and associated actions.
Causeway SHALL remain the owner of collection loading, rows, columns, totals, ordering, paging, errors, focus, and lifecycle.

#### Scenario: Qualified wide collection renders
- **WHEN** a visible active collection has bounded windows, deterministic ordering, supported semantic columns, wide layout, and a healthy enabled Grid family
- **THEN** `<cw-collection>` may render one internal Grid presentation
- **AND** application-authored `<cw-collection-column>` and `<cw-action>` children remain the authoritative declarations

#### Scenario: Stable total supports virtualization
- **WHEN** a qualified collection reports a safely available stable total
- **THEN** Causeway may serve bounded ranges to Grid virtualization
- **AND** no Grid callback constructs GraphQL, domain identity, authorization, or navigation independently

#### Scenario: Total is unavailable
- **WHEN** a qualified deterministic window reports unavailable total count
- **THEN** the collection renders only its current bounded window through Grid with Causeway-owned previous and next controls
- **AND** it does not invent a total or silently traverse beyond normalized window metadata

#### Scenario: Collection is narrow or unqualified
- **WHEN** container width is at most 48rem or window, ordering, column, renderer, policy, or family qualification fails
- **THEN** the established native collection presentation remains authoritative
- **AND** no partial Grid or unsupported mixed presentation remains

#### Scenario: Native toolkit is explicit
- **WHEN** the common component toolkit resolves to native
- **THEN** collections use established native presentation without a Grid request or style hash
- **AND** GraphQL operations, routes, data, semantic events, and application markup are unchanged

### Requirement: Toolkit-neutral collection range lifecycle
Collection range work SHALL remain bounded by the current Causeway route, object generation, member identity, column selection, responsive mode, toolkit policy, renderer registry, and connection lifetime.
Hydrated row contexts and focus restoration MUST follow domain identity rather than recycled presentation nodes.

#### Scenario: Grid requests an additional range
- **WHEN** a still-current virtual Grid requests a bounded range
- **THEN** the collection host coordinates it through the existing object-context window operation
- **AND** identical current work is deduplicated while accepted concurrent work remains independently cancellable

#### Scenario: Collection lifetime supersedes range work
- **WHEN** route replacement, refresh, column change, width change, policy revision, member change, or disconnect makes a request obsolete
- **THEN** the request and its row contexts are aborted, disconnected, or ignored
- **AND** stale rows, cells, errors, total, paging, focus, and controls cannot alter the current component

#### Scenario: Cell is recycled
- **WHEN** Grid reuses a presentation node for another row or column
- **THEN** Causeway repopulates it from the current frozen semantic descriptor and cleans previous relationships
- **AND** semantic events and focus identity refer to current domain row and member identity

#### Scenario: Associated actions remain composed
- **WHEN** a collection uses Grid and contains associated actions
- **THEN** those actions remain Causeway siblings outside Grid in declaration order
- **AND** their visibility, usability, prompting, execution, navigation, and focus lifecycles remain independent
