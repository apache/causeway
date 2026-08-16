## ADDED Requirements

### Requirement: Bounded deterministic collection reads
The rich GraphQL schema SHALL provide a discoverable collection operation that limits the rows returned for one request.

#### Scenario: Client requests a valid window
- **WHEN** a client requests a valid start position or cursor and size
- **THEN** the response contains no more than the requested maximum rows
- **AND** identifies the returned count and continuation state

#### Scenario: Requested window is out of range
- **WHEN** the requested position is beyond the current collection
- **THEN** the response returns a documented empty or range outcome
- **AND** does not substitute an unrelated window

### Requirement: Window count semantics
The collection window SHALL expose total count when it is safely available and SHALL distinguish unavailable count from zero.

#### Scenario: Count is available
- **WHEN** the server can determine the authorized collection count under the documented policy
- **THEN** the response returns that count

#### Scenario: Count is unavailable
- **WHEN** count cannot be determined safely or efficiently under policy
- **THEN** the response reports count as unavailable
- **AND** continuation semantics remain usable

### Requirement: Stable configured ordering
Supported Causeway collection ordering SHALL be applied before window selection and SHALL be consistent across continuation requests under the documented consistency model.

#### Scenario: Collection has supported ordering
- **WHEN** the metamodel supplies a supported stable comparator or ordering
- **THEN** each window follows that ordering before rows are selected

#### Scenario: Stable ordering is unavailable
- **WHEN** the collection cannot provide stable ordering
- **THEN** the response or capability metadata identifies the limitation
- **AND** does not claim stable cursor continuation

### Requirement: Concurrent and stale window behavior
Window responses SHALL have documented semantics for superseded requests and collections changed between requests.

#### Scenario: New component request supersedes an old window
- **WHEN** an earlier window response arrives after a later context generation
- **THEN** the client contract permits the stale response to be discarded

#### Scenario: Collection changes between windows
- **WHEN** collection membership or ordering changes
- **THEN** count, position, cursor, or stale-state behavior follows the documented consistency contract

### Requirement: Backward-compatible collection migration
The bounded capability SHALL preserve established unargumented collection documents for the documented compatibility period.

#### Scenario: Existing client requests collection get
- **WHEN** an existing client executes the established unargumented field
- **THEN** its document remains schema-valid
- **AND** its established response shape remains available during migration

### Requirement: Explicit materialization behavior
Documentation and diagnostics SHALL distinguish bounded GraphQL response rows from persistence-level bounded retrieval.

#### Scenario: Domain collection is fully materialized
- **WHEN** the server must materialize the complete domain collection before selecting the response window
- **THEN** the serialized response remains bounded
- **AND** documentation does not claim database-level paging efficiency
