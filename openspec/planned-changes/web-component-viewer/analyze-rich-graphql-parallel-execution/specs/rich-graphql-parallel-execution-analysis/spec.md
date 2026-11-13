## ADDED Requirements

### Requirement: Reproducible GraphQL execution baseline
The analysis SHALL establish actual threading, ordering, and timing for current Causeway GraphQL queries and mutations without assuming that the configured async strategy makes synchronous fetchers concurrent.

#### Scenario: Current execution is measured
- **WHEN** a reviewer runs the documented synchronous, delayed, future-returning, nested, list, DataLoader, query, and mutation fixtures
- **THEN** request threads, field dispatch, Causeway interactions, persistence work, completion stages, and result assembly are traceable
- **AND** parsing, validation, domain, persistence, waiting, batching, and stitching time are distinguishable

### Requirement: Safe query concurrency boundary
The analysis SHALL identify only query work whose Causeway and persistence semantics permit bounded overlap.

#### Scenario: Independent query roots are prototyped
- **WHEN** sibling query roots have no shared mutable interaction, transaction, persistence session, or managed source object
- **THEN** each concurrent task establishes its own required context and lifecycle
- **AND** the final response preserves GraphQL field order and partial-data semantics

#### Scenario: Fields are dependent or thread-confined
- **WHEN** nested fields depend on a managed parent or work shares mutable or thread-confined state
- **THEN** the prototype keeps that work within a safe serial or task-local boundary
- **AND** does not pass unsafe persistence or interaction objects across threads

### Requirement: Serial mutation invariant
The analysis SHALL require top-level mutation fields and dependent domain interactions to retain serial document-order execution.

#### Scenario: Async-capable mutation fetchers are evaluated
- **WHEN** a candidate execution strategy can return incomplete futures from mutation fields
- **THEN** the prototype proves that top-level mutations still complete serially in document order
- **AND** rejects any configuration that permits overlapping mutation side effects

### Requirement: Explicit context propagation and isolation
Concurrent prototypes SHALL enumerate and test user identity, authorization, interaction, locale, transaction, persistence, diagnostics, tracing, timeout, and cancellation context.

#### Scenario: Query work moves to another thread
- **WHEN** an executor runs an independent query task
- **THEN** immutable request identity is propagated explicitly and mutable execution state is independently established
- **AND** authorization is evaluated inside the task's Causeway interaction

### Requirement: Bounded executor lifecycle
Concurrent prototypes SHALL use an application-managed bounded executor with documented limits, queueing, rejection, observability, shutdown, and rollback behavior.

#### Scenario: Concurrency capacity is exhausted
- **WHEN** active and queued work reach configured bounds
- **THEN** the prototype applies the documented bounded rejection, fallback, or backpressure policy
- **AND** does not spill work into an unbounded or implicit common pool

#### Scenario: Request is cancelled or times out
- **WHEN** the client disconnects, a deadline expires, or the GraphQL request is cancelled
- **THEN** queued and active work receive the documented cancellation signal where possible
- **AND** interactions, transactions, and executor ownership are closed or released deterministically

### Requirement: Concurrency alternatives and performance evidence
The analysis SHALL compare bounded query concurrency with DataLoader batching, query consolidation, projection reduction, and unchanged synchronous execution.

#### Scenario: Concurrency recommendation is made
- **WHEN** representative benchmarks are complete
- **THEN** latency, throughput, tail latency, thread and queue use, database pressure, connections, cancellation, and resource consumption are reported
- **AND** the selected boundary demonstrates a material benefit without violating safety invariants

### Requirement: Analysis-only parallel-execution scope
The analysis SHALL NOT change production GraphQL threading, executor configuration, query behavior, or mutation behavior.

#### Scenario: Analysis completes
- **WHEN** evidence and recommendations are accepted
- **THEN** production runtime behavior remains unchanged
- **AND** implementation work is represented by separately reviewable proposals
