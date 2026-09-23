## 1. Characterize the recursive path

- [ ] 1.1 Add a representative fixture with multiple mixins, mixed-in associations, navigation-action synthesis, and action element types that trigger nested specification loading.
- [ ] 1.2 Add a test seam that records the nested specification types and reasons without depending on a production `StackOverflowError` for ordinary assertions.
- [ ] 1.3 Confirm the fixture exercises the same key framework methods seen in the reported stack trace.

## 2. Deterministic mixin traversal

- [ ] 2.1 Change the central mixin-type stream to order classes by fully qualified class name without changing the registry's lookup maps.
- [ ] 2.2 Add unit coverage proving identical traversal for registries populated in different orders.
- [ ] 2.3 Add coverage proving mixed-in action and association discovery retain existing applicability and explicit member-order behavior.

## 3. Introspection-chain diagnostics

- [ ] 3.1 Add an internal thread-local diagnostic scope around nested specification loading with type, requested state, available current state, depth, and compact caller information.
- [ ] 3.2 Report the retained chain once when a `StackOverflowError` crosses the loader boundary, then rethrow the original error unchanged.
- [ ] 3.3 Add tests for ordered chain content, per-thread isolation, cleanup after successful loading, and duplicate-report suppression while an error unwinds.
- [ ] 3.4 Verify successful metamodel creation produces no new diagnostics at standard log levels.

## 4. Regression and operational validation

- [ ] 4.1 Extend full-boot command-recording regression coverage with the representative nested graph and verify repeated boots use the same mixin traversal order.
- [ ] 4.2 Run targeted metamodel and recording-navigation tests using the production JVM stack-size setting.
- [ ] 4.3 Exercise the previously failing application or closest available fixture, capture the reported type chain, and document whether deterministic ordering makes the outcome repeatable.
- [ ] 4.4 Record follow-up recommendations for flattening or removing the identified recursive path without expanding this diagnostic change into the final correction.
