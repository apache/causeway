## 1. Establish the Current Retrieval Baseline

- [ ] 1.1 Create deterministic persistent, lazy, computed, ordered, hidden, disabled, and in-memory collection fixtures with small and large datasets.
- [ ] 1.2 Instrument association invocation, backend statements, rows fetched, objects materialized, count work, response rows, latency, and memory evidence.
- [ ] 1.3 Record current behavior for first, middle, empty, out-of-range, default, maximum, and rejected windows.

## 2. Map Candidate Integration Boundaries

- [ ] 2.1 Trace collection association, metamodel, transaction, interaction, authorization, and persistence-adapter APIs relevant to bounded retrieval.
- [ ] 2.2 Evaluate transparent persistence pushdown, opt-in application providers, domain programming-model extensions, repository-backed results, and materializing fallback.
- [ ] 2.3 Define the semantic-equivalence gate for owner identity, visibility, row authorization, configured ordering, totals, errors, and concurrent changes.
- [ ] 2.4 Record coupling, portability, migration, testing, and security trade-offs in a decision matrix.

## 3. Prototype and Measure

- [ ] 3.1 Build a disposable backend-aware prototype for the strongest viable candidate without changing production packages.
- [ ] 3.2 Demonstrate automatic fallback for computed, unsupported, unsafe, and ordering-incompatible collections.
- [ ] 3.3 Compare SQL or backend calls, rows transferred, objects materialized, count cost, latency, and memory against the established implementation.
- [ ] 3.4 Test hidden members, authorization-sensitive rows, nullable totals, configured ordering, concurrent changes, partial errors, and cancellation.

## 4. Publish the Recommendation

- [ ] 4.1 Document the recommended capability boundary, rejected alternatives, compatibility behavior, optional persistence integration, and remaining risks.
- [ ] 4.2 Define separately reviewable implementation changes for any GraphQL, metamodel, persistence, or application-facing work that is justified.
- [ ] 4.3 Publish reproducible fixtures, measurements, and trace commands without adding a normal-build reference-application dependency.
- [ ] 4.4 Run analysis-document, prototype, security, formatting, and strict OpenSpec validation checks.
