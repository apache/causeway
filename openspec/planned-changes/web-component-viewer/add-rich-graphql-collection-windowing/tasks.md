## 1. Confirm Window Model

- [ ] 1.1 Use analysis evidence to select offset, cursor, or combined semantics and record compatibility constraints.
- [ ] 1.2 Define ordering, count availability, continuation, generation, out-of-range, and concurrent-change behavior.

## 2. Implement Schema and Fetching

- [ ] 2.1 Add the bounded collection field or compatible arguments and window result descriptor.
- [ ] 2.2 Apply supported stable configured ordering before selecting rows.
- [ ] 2.3 Bound serialized rows and instrument whether the underlying collection was fully materialized.
- [ ] 2.4 Preserve established unargumented collection documents during the compatibility period.

## 3. Context and Component Consumption Contracts

- [ ] 3.1 Make bounded capability discoverable through targeted introspection.
- [ ] 3.2 Add object-context secondary-operation support for requested windows and superseded responses.
- [ ] 3.3 Define component-facing next, previous, range, count, and unavailable-count semantics without implementing presentation policy here.

## 4. Verification and Documentation

- [ ] 4.1 Test empty, small, large, sorted, hidden, disabled, partial-error, out-of-range, concurrent-change, and stale-window cases.
- [ ] 4.2 Add schema compatibility tests for established collection documents.
- [ ] 4.3 Measure and document response bounds and underlying materialization behavior.
- [ ] 4.4 Document the public window contract, ordering guarantees, count behavior, consistency limits, and migration.
- [ ] 4.5 Run GraphQL, context, compatibility, documentation, formatting, and strict OpenSpec validation checks.
