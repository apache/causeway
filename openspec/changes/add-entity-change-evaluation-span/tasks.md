## 1. Entity-Change Evaluation Instrumentation

- [ ] 1.1 Inject the existing `CausewayObservationIntegration` into `EntityChangeTrackerDefault` without adding a tracing SDK, exporter, or persistence dependency.
- [ ] 1.2 Add stable `causeway.entitychange.evaluate` and contextual `evaluate property changes` observation construction using static persistence-commons module metadata.
- [ ] 1.3 Wrap the complete `evaluateChangedProperties()` operation so current post-value access, comparison, and any defensive-copy retry share one synchronous observation.
- [ ] 1.4 Bypass observation creation when no property-change records are enlisted while retaining one observation when candidates are evaluated but none require publication.
- [ ] 1.5 Preserve handled accessor-failure behavior, record and rethrow escaping failures, and close the observation scope on success and failure.
- [ ] 1.6 Keep audit publication outside the evaluation observation and attach no entity, property, value, identity, transaction, interaction, sequence, retry, or count metadata.

## 2. Focused and Persistence Tests

- [ ] 2.1 Add focused tracker tests proving one observation encloses post-value evaluation and comparison for one or multiple candidate records.
- [ ] 2.2 Test that an empty enlisted-record collection creates no evaluation observation and does not alter transaction completion behavior.
- [ ] 2.3 Test that candidates with no publishable difference still produce one completed evaluation observation.
- [ ] 2.4 Test that the concurrent-modification recovery path remains within one observation and preserves its defensive-copy retry result.
- [ ] 2.5 Test current-parent inheritance, synchronous child-operation parentage, success cleanup, escaping-failure recording and rethrow, and handled unknown-value behavior.
- [ ] 2.6 Test no-op registry behavior and verify that exported names and low-cardinality metadata contain only the stable static bean and module identifiers.
- [ ] 2.7 Run existing JDO and JPA entity property-change publication and audit-trail persistence tests to confirm adapter, derived-property, ordering, and transaction semantics remain unchanged.

## 3. Java-Agent Compatibility and Operations Evidence

- [ ] 3.1 Extend the Java-agent compatibility fixture with representative aggregate entity-change evaluation that performs multiple automatic JDBC operations before audit writing.
- [ ] 3.2 Assert `action -> evaluate property changes -> JDBC` ancestry, sibling evaluation and audit spans, one aggregate evaluation span, and no duplicate framework JDBC instrumentation.
- [ ] 3.3 Verify the compatibility fixture both without the Java agent and with the supported Java 11 and OpenTelemetry Java-agent combination.
- [ ] 3.4 Update the tracing operations guide with stable and contextual names, transaction-completion hierarchy, JDO-derived-property behavior, activation requirements, and privacy exclusions.
- [ ] 3.5 Record representative trace evidence showing derived-property SQL grouped beneath `evaluate property changes` and separated from command and audit persistence.
- [ ] 3.6 Run focused persistence and compatibility test suites, run `git diff --check`, validate the OpenSpec change strictly, and record commands and results in `validation.md`.
