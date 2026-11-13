## Why

The archived collection-windowing change bounds GraphQL response rows but intentionally materializes the complete Causeway collection before ordering and slicing.
That protects clients and transport payloads without reducing database retrieval, persistence-session work, or server-side memory for large associations.
Causeway collection members can be computed, persistent, ordered, authorized, or application-defined, so transparent `OFFSET` and `LIMIT` pushdown cannot be assumed safe or generally available.
An evidence-backed analysis is required before introducing a persistence-specific optimization, domain programming-model extension, or public window-provider SPI.

## What Changes

- Measure the current collection-window retrieval path across persistent, lazy, computed, ordered, hidden, and in-memory collections.
- Identify which Causeway and persistence abstractions can retrieve a bounded ordered window without first materializing the association.
- Compare transparent persistence pushdown, an opt-in application provider, a domain programming-model extension, and repository-backed alternatives.
- Prototype at least one backend-aware path with an explicit semantic-equivalence gate and automatic fallback to the established materializing implementation.
- Preserve authorization, configured ordering, nullable totals, concurrent-change semantics, bounded errors, and legacy `get` compatibility.
- Record SQL, rows fetched, objects materialized, latency, memory, count cost, and fallback reasons for representative fixtures.
- Produce separately reviewable implementation proposals rather than changing production behavior during this analysis.

## Capabilities

### New Capabilities

- `rich-graphql-collection-query-pushdown-analysis`: Defines reproducible evidence, semantic gates, prototype boundaries, and an implementation roadmap for backend-aware rich GraphQL collection windows.

### Modified Capabilities

None.

## Impact

- Adds analysis documents, instrumented fixtures, disposable prototypes, query traces, benchmarks, and decision records.
- Depends on the archived `rich-graphql-collection-windowing` contract.
- May identify later changes in the GraphQL viewer, metamodel, persistence integrations, or application-facing SPIs.
- Does not change the public `window` or `get` schema, promise database-level pagination, or make a persistence implementation a normal GraphQL dependency.
