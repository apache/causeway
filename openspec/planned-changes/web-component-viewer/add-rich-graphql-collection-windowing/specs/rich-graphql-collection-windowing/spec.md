## ADDED Requirements

### Requirement: Additive bounded offset windows
The rich GraphQL schema SHALL provide a discoverable collection `window` operation that limits rows returned for one request without changing the established unargumented list shape.

#### Scenario: Client requests a valid window
- **WHEN** a client requests a valid zero-based offset and positive size within the configured maximum
- **THEN** the response contains no more than the requested rows
- **AND** identifies requested offset, returned count, previous availability, and next availability

#### Scenario: Requested size exceeds the maximum
- **WHEN** the requested size exceeds the configured hard maximum
- **THEN** GraphQL returns a bounded validation error
- **AND** does not materialize or serialize the requested oversized response

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
- **THEN** the contract identifies per-request consistency limitations
- **AND** does not claim stable cursor continuation

### Requirement: Per-request consistency
Window responses SHALL represent one execution-time view and SHALL document behavior when collections change between requests.

#### Scenario: New component request supersedes an old window
- **WHEN** an earlier window response arrives after a later object-context generation
- **THEN** the component contract permits the stale response to be discarded

#### Scenario: Collection changes between windows
- **WHEN** collection membership or ordering changes before another offset request
- **THEN** the new response reflects current state
- **AND** the contract does not promise that prior offsets retain the same members

### Requirement: Backward-compatible collection migration
The bounded capability SHALL preserve established unargumented collection documents for the documented compatibility period.

#### Scenario: Existing client requests collection get
- **WHEN** an existing client executes the established unargumented field
- **THEN** its document remains schema-valid
- **AND** its established list response shape remains available during migration

### Requirement: Explicit materialization behavior
Documentation and diagnostics SHALL distinguish bounded GraphQL response rows from persistence-level bounded retrieval.

#### Scenario: Domain collection is fully materialized
- **WHEN** the server materializes the complete domain collection before selecting the response window
- **THEN** the serialized response remains bounded
- **AND** documentation does not claim database-level paging efficiency
