## ADDED Requirements

### Requirement: Collection-wide interactive window criteria
The rich GraphQL collection `window` operation SHALL accept optional bounded search and single-column sort criteria and SHALL apply accepted criteria before offset selection.
Existing documents that provide only offset and size MUST remain valid.

#### Scenario: Client sorts by an accepted column
- **WHEN** a client requests an accepted sortable member and ascending or descending direction
- **THEN** the complete authorized execution-time collection is sorted by that Causeway table column before slicing
- **AND** every requested offset uses the same accepted criterion

#### Scenario: Client searches a supported collection
- **WHEN** a non-blank bounded search is supplied for an element type handled by `CollectionFilterService`
- **THEN** the service's token semantics filter the complete authorized execution-time collection before sorting and slicing
- **AND** total count and continuation metadata describe the filtered result

#### Scenario: Criteria are absent
- **WHEN** sort and search criteria are omitted or blank
- **THEN** the established configured-comparator or encounter-order behavior remains authoritative
- **AND** existing offset-and-size-only documents retain their response semantics

#### Scenario: Criterion is invalid or unsupported
- **WHEN** a sort member is not an accepted table column, direction is invalid, search exceeds its bound, or non-blank search has no applicable filter service
- **THEN** GraphQL returns a bounded validation error
- **AND** does not silently apply page-local, partial, or unrelated criteria

### Requirement: Discoverable collection criteria capabilities
A collection window response SHALL advertise the sortable member ids and quick-search capability accepted for that collection execution.
Capability metadata MUST NOT expose hidden members, row values, filter tokens, or implementation services.

#### Scenario: Sortable members are available
- **WHEN** the authorized Causeway table model exposes sortable columns
- **THEN** the window returns their bounded semantic member ids in deterministic order
- **AND** a client can restrict sort controls to those ids

#### Scenario: Quick search is available
- **WHEN** an applicable `CollectionFilterService` handles the collection element type
- **THEN** the window reports search support and a bounded translated prompt
- **AND** the client need not infer support from row values

#### Scenario: Quick search is unavailable
- **WHEN** no applicable filter service handles the collection element type
- **THEN** the window reports search as unsupported
- **AND** omitting search retains ordinary collection loading
