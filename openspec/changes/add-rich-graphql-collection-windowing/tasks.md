## 1. Pin the Offset-Window Contract

- [ ] 1.1 Translate matrix entries `REF-COLLECTION-01`, `REF-COLLECTION-02`, and prerequisite `REF-COLLECTION-03` into reduced deterministic fixtures.
- [ ] 1.2 Define offset, size, hard maximum, ordering, nullable count, continuation, out-of-range, and concurrent-change behavior.

## 2. Implement Schema and Fetching

- [ ] 2.1 Add the additive `window(offset, size)` field and generated window result shape.
- [ ] 2.2 Apply supported deterministic configured ordering before selecting rows.
- [ ] 2.3 Bound serialized rows and instrument whether the underlying collection was fully materialized.
- [ ] 2.4 Preserve established unargumented `get` documents during the compatibility period.

## 3. Context and Component Consumption Contracts

- [ ] 3.1 Make bounded capability discoverable through targeted introspection.
- [ ] 3.2 Add object-context secondary-operation support for requested offsets and superseded responses.
- [ ] 3.3 Define component-facing previous, next, range, count, and unavailable-count semantics without implementing presentation policy here.

## 4. Verification and Documentation

- [ ] 4.1 Test empty, small, large, sorted, unstable-order, hidden, disabled, partial-error, out-of-range, concurrent-change, and stale-response cases.
- [ ] 4.2 Add schema compatibility tests for established collection documents.
- [ ] 4.3 Measure and document response bounds and underlying materialization behavior.
- [ ] 4.4 Document the public offset-window contract, ordering guarantees, count behavior, consistency limits, maximums, and migration.
- [ ] 4.5 Run GraphQL, context, compatibility, documentation, formatting, and strict OpenSpec validation checks.
