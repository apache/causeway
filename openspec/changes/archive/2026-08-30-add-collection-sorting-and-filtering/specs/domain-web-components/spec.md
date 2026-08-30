## ADDED Requirements

### Requirement: Declarative collection sorting and filtering
`<cw-collection>` SHALL accept optional `sortable` and `filterable` attributes that activate only server-advertised collection-wide criteria.
Both attributes SHALL default off and MUST NOT transform only the currently loaded rows.

#### Scenario: Sorting is enabled
- **WHEN** a collection declares `sortable` and its window advertises one or more declared columns as sortable
- **THEN** accessible column-header controls can select one ascending or descending server criterion
- **AND** the collection reloads offset zero before paging or virtual ranges continue with that criterion

#### Scenario: Filtering is enabled
- **WHEN** a collection declares `filterable` and its window advertises quick-search support
- **THEN** one labelled bounded search control applies the server's collection-wide filtering semantics
- **AND** filtering resets to offset zero and preserves normalized paging

#### Scenario: Capability is unavailable
- **WHEN** the server does not advertise sortable declared columns or quick-search support
- **THEN** the corresponding opt-in attribute does not expose a misleading active control
- **AND** established collection loading and presentation remain available

#### Scenario: Criteria change reactively
- **WHEN** sorting, search text, or either opt-in attribute changes
- **THEN** current loading, row contexts, range cache, focus intent, and paging state are retired or reconciled before offset-zero reload
- **AND** stale rows, totals, errors, controls, or criteria cannot replace the new state

#### Scenario: Native fallback is active
- **WHEN** responsive, policy, ordering, capability, or adapter failure selects native presentation
- **THEN** the same Causeway sorting and filtering criteria remain operable through native semantic controls
- **AND** no toolkit element or event becomes an application dependency

## MODIFIED Requirements

### Requirement: Collection sorting and filtering remain collection-wide concerns
The component SHALL expose sorting or filtering only when the collection-window contract can apply the selected criteria across the complete authorized execution-time collection.
It MUST NOT apply criteria only to loaded rows while presenting them as collection-wide.

#### Scenario: Server contract advertises sort and search inputs
- **WHEN** the collection window advertises bounded sorting or quick-search capability
- **THEN** opted-in controls send normalized criteria with every initial, paging, refresh, and virtual-range request
- **AND** deterministic server results remain authoritative across windows

#### Scenario: Server contract lacks criteria inputs
- **WHEN** the discovered collection-window operation accepts offset and size but no ordering or search criteria
- **THEN** the component does not enable sorting or filtering controls
- **AND** established server ordering remains authoritative across windows
