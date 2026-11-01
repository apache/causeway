## ADDED Requirements

### Requirement: Reproducible collection retrieval baseline
The analysis SHALL measure backend retrieval and materialization separately from GraphQL response size for representative Causeway collection kinds.

#### Scenario: Current window behavior is measured
- **WHEN** a reviewer runs the documented fixtures and trace procedure
- **THEN** backend statements, rows fetched, domain objects materialized, count work, response rows, latency, and relevant memory evidence are distinguishable
- **AND** dataset size, persistence implementation, configuration, warm-up, and environment are recorded

### Requirement: Semantic-equivalence gate
Any candidate bounded backend retrieval strategy SHALL define when it is semantically equivalent to the established materializing collection window.

#### Scenario: Candidate strategy is applicable
- **WHEN** owner identity, member visibility, row authorization, configured ordering, transaction behavior, and element identity can be preserved
- **THEN** the prototype may retrieve a backend-bounded window
- **AND** produces the same public window semantics as the established path for the execution-time state

#### Scenario: Equivalence cannot be established
- **WHEN** a collection is computed, unsupported, ordering-incompatible, authorization-sensitive in an unsafe way, or otherwise lacks required capability
- **THEN** the candidate uses the established materializing fallback
- **AND** does not return a plausibly bounded but semantically different result

### Requirement: Multiple pushdown boundaries compared
The analysis SHALL compare transparent persistence optimization, opt-in application providers, domain programming-model extensions, repository-backed alternatives, and materializing fallback.

#### Scenario: Recommendation is selected
- **WHEN** the decision matrix is complete
- **THEN** it records portability, coupling, authorization, transaction, ordering, count, migration, and testing evidence for every candidate
- **AND** explains why rejected candidates are insufficient

### Requirement: Rows and total count evaluated independently
The analysis SHALL not require complete row retrieval merely to populate an exact total for an otherwise bounded source.

#### Scenario: Rows are bounded but count is expensive
- **WHEN** a prototype can retrieve the requested rows without safely obtaining an efficient exact count
- **THEN** it preserves nullable total-count semantics or evaluates bounded look-ahead
- **AND** does not materialize all rows solely to replace an unavailable total with a number

### Requirement: Authorization and ordering safety
Backend-aware prototypes SHALL execute within the Causeway interaction boundary and preserve collection visibility and supported ordering before slicing.

#### Scenario: Authorization or ordering changes the eligible sequence
- **WHEN** backend slicing could occur before a framework rule that changes membership or order
- **THEN** the strategy is rejected or adapted so offsets apply to the authorized ordered sequence
- **AND** hidden data does not enter rows, counts, traces, diagnostics, or fallback reasons

### Requirement: Optional persistence integration
The analysis SHALL keep ORM-specific dependencies outside the mandatory GraphQL collection-window implementation.

#### Scenario: Persistence-specific adapter is recommended
- **WHEN** a JPA, JDO, or other backend adapter provides the strongest safe optimization
- **THEN** its module boundary and optional activation are documented
- **AND** computed, in-memory, and non-relational collections retain compatible behavior without that dependency

### Requirement: Analysis-only collection scope
The analysis SHALL NOT change production collection retrieval or the public `window` and `get` schema contracts.

#### Scenario: Analysis completes
- **WHEN** evidence and recommendations are accepted
- **THEN** production behavior remains unchanged
- **AND** implementation work is represented by separately reviewable proposals
