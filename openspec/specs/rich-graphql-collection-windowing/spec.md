# Rich GraphQL Collection Windowing Specification

## Purpose

Define bounded, discoverable, ordered, authorized, and backward-compatible rich GraphQL collection windows together with the framework-neutral secondary-operation contract.

## Requirements

### Requirement: Additive bounded offset windows
The rich GraphQL schema SHALL provide a discoverable collection `window` operation that limits rows returned for one request without changing the established unargumented list shape.

#### Scenario: Client requests a valid window
- **WHEN** a client requests a valid zero-based offset and positive size within the configured maximum
- **THEN** the response contains no more than the requested rows
- **AND** identifies requested offset and size, configured maximum, returned count, ordering mode, previous availability, and next availability

#### Scenario: Client uses configured defaults
- **WHEN** a client omits offset or size
- **THEN** offset defaults to zero and size defaults to the configured default of 20
- **AND** the configured default cannot exceed the configured maximum of 100 unless either value is overridden consistently

#### Scenario: Requested size exceeds the maximum
- **WHEN** the requested size exceeds the configured hard maximum
- **THEN** GraphQL returns a bounded validation error
- **AND** does not read, materialize, or serialize the requested oversized response

#### Scenario: Requested range is otherwise invalid
- **WHEN** the requested offset is negative or size is not positive
- **THEN** GraphQL returns a bounded validation error before reading the collection

#### Scenario: Requested offset is out of range
- **WHEN** the requested offset is beyond the current authorized collection
- **THEN** the response returns an empty window at that offset
- **AND** does not substitute an unrelated range

### Requirement: Window count semantics
The collection window SHALL expose total count when safely available and SHALL distinguish unavailable count from zero.

#### Scenario: Count is available
- **WHEN** the server can determine the authorized collection count under the documented policy
- **THEN** the response returns the exact count for that execution-time collection

#### Scenario: Count is unavailable
- **WHEN** count cannot be determined safely or efficiently
- **THEN** the response returns null count rather than zero
- **AND** continuation semantics remain usable

### Requirement: Stable configured ordering before slicing
Supported Causeway collection ordering SHALL be applied before offset selection.

#### Scenario: Collection has supported deterministic ordering
- **WHEN** the metamodel supplies a supported comparator or ordering
- **THEN** every requested window applies that ordering before rows are selected

#### Scenario: Stable ordering is unavailable
- **WHEN** the collection cannot provide deterministic cross-request ordering
- **THEN** the response identifies encounter ordering
- **AND** the contract identifies per-request consistency limitations and does not claim stable cursor continuation

### Requirement: Per-request consistency
Window responses SHALL represent one execution-time view and SHALL document behavior when collections change between requests.

#### Scenario: New component request supersedes an old window
- **WHEN** an earlier window response arrives after a later object-context generation
- **THEN** the component contract permits the stale response to be discarded

#### Scenario: Collection changes between windows
- **WHEN** collection membership or ordering changes before another offset request
- **THEN** the new response reflects current state
- **AND** the contract does not promise that prior offsets retain the same members

### Requirement: Authorized and partially successful windows
Collection reads SHALL preserve member visibility and GraphQL partial-data semantics without treating disabled presentation as a read veto.

#### Scenario: Collection is hidden
- **WHEN** a client requests `get` or `window` for a hidden collection
- **THEN** no collection rows are returned
- **AND** GraphQL reports the existing bounded hidden-member error

#### Scenario: Collection is disabled but visible
- **WHEN** a client requests a visible collection whose disabled reason is non-null
- **THEN** collection rows remain readable because the operation does not modify domain state
- **AND** the existing disabled field continues to report the reason

#### Scenario: Nested row field fails
- **WHEN** GraphQL can represent a failed nested row field as nullable
- **THEN** safe rows and window metadata remain available according to GraphQL partial-data rules
- **AND** the response reports the nested error without enlarging the requested window

### Requirement: Backward-compatible collection migration
The bounded capability SHALL preserve established unargumented collection documents for the documented compatibility period.

#### Scenario: Existing client requests collection get
- **WHEN** an existing client executes the established unargumented field
- **THEN** its document remains schema-valid
- **AND** its established list response shape remains available during migration

### Requirement: Secondary collection-operation contract
The object context SHALL prefer discovered window capability and expose semantic range metadata without defining paging presentation.

#### Scenario: Window capability is discovered
- **WHEN** targeted introspection finds a collection `window` field
- **THEN** the collection descriptor records its result shape and argument defaults
- **AND** secondary collection reads request bounded rows with the caller's offset and size

#### Scenario: Window request is superseded
- **WHEN** a later window request for the same component supersedes an earlier request
- **THEN** the earlier request is aborted where possible and its response is discarded
- **AND** only the latest result may update component collection state

#### Scenario: Server lacks window capability
- **WHEN** targeted introspection finds only the established `get` field
- **THEN** the context retains the compatibility read path
- **AND** does not invent unavailable range metadata

### Requirement: Explicit materialization behavior
Documentation and diagnostics SHALL distinguish bounded GraphQL response rows from persistence-level bounded retrieval.

#### Scenario: Domain collection is fully materialized
- **WHEN** the server materializes the complete domain collection before selecting the response window
- **THEN** the serialized response remains bounded
- **AND** documentation does not claim database-level paging efficiency
- **AND** materialization remains an implementation diagnostic rather than a semantic window field

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
