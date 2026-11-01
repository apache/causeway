## ADDED Requirements

### Requirement: Causeway-owned Grid criteria affordances
The private Grid adapter SHALL render only Causeway-owned sorting affordances described by the current immutable presentation and SHALL report normalized intent through a private host callback.
Filtering SHALL remain a collection-host control shared with native presentation.

#### Scenario: Sortable Grid column renders
- **WHEN** the current presentation marks a declared column sortable
- **THEN** its Grid header contains an accessible Causeway sort control with current direction state
- **AND** activating it reports only the semantic member and next bounded direction to the host

#### Scenario: Grid presentation is replaced
- **WHEN** criteria, responsive mode, route generation, policy, columns, or collection state replaces the adapter presentation
- **THEN** current header controls reflect only the current immutable criterion
- **AND** stale header callbacks cannot restore superseded rows or criteria

#### Scenario: Grid falls back to native
- **WHEN** Grid no longer qualifies while sorting or filtering is active
- **THEN** native controls retain the same current Causeway criteria
- **AND** no duplicate toolkit or native criteria state machine remains connected

## MODIFIED Requirements

### Requirement: Unsupported sort and filter hints are not advertised
The Grid adapter SHALL NOT delegate collection criteria ownership to Vaadin sorter, filter, array, or data-provider behavior.
It SHALL accept only Causeway presentation options whose callbacks trigger complete server-window criteria.

#### Scenario: Adapter creates columns under the criteria-aware range contract
- **WHEN** generated columns are marked sortable by the current Causeway presentation
- **THEN** Causeway-owned header controls invoke the host's normalized server-backed sort callback
- **AND** raw toolkit sort orders or filters are neither exposed nor silently ignored

#### Scenario: Criteria-aware server contract is absent
- **WHEN** the collection host cannot apply sorting or filtering across the complete collection
- **THEN** the adapter renders no active sorting affordance
- **AND** it does not approximate the behavior over bounded items or cached virtual ranges
