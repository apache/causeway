## 1. Root Interaction Correlation

- [x] 1.1 Pass the newly created `CausewayInteraction` identity into root observation construction and add `causeway.interaction.id` as a high-cardinality canonical UUID string before the observation starts.
- [x] 1.2 Preserve existing nested-layer reuse, inactive-observation behavior, error recording, cleanup, span naming, and span parentage.

## 2. Automated Verification

- [x] 2.1 Extend `InteractionServiceDefaultObservationTest` with a deterministic interaction ID and assertions that the root observation context carries the exact high-cardinality attribute across normal and failure lifecycles.
- [x] 2.2 Extend the tracing compatibility fixture and OTLP export assertions to prove that `causeway.root.interaction` exports the expected `causeway.interaction.id` attribute without changing HTTP, action, or JDBC ancestry.
- [x] 2.3 Run the targeted runtime-services tests and the Java 11 tracing compatibility regression, and resolve any failures.

## 3. Operational Documentation

- [x] 3.1 Update the Micrometer tracing operations runbook to describe the root-span attribute, exact Jaeger tag lookup, operation-filter constraint, high-cardinality trade-off, and in-memory retention limitation.
