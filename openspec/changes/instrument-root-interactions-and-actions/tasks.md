## 1. Instrument Root Interactions

- [x] 1.1 Inject `CausewayObservationIntegration` into `InteractionServiceDefault` and create a module-qualified provider for root-interaction observations.
- [x] 1.2 Start `causeway.root.interaction` only when the first interaction layer opens, retain its lifecycle alongside the existing thread-local stack, and leave nested or reused layers within that observation.
- [x] 1.3 Report work and close-path failures to the root observation without changing rollback or rethrow behavior.
- [x] 1.4 Close and clear root observation state whenever the interaction stack is reduced to zero, including defensive and exceptional cleanup paths.

## 2. Instrument Action Invocations

- [x] 2.1 Inject `CausewayObservationIntegration` into `MemberExecutorServiceDefault` and update constructor call sites and tests.
- [x] 2.2 Wrap the complete `invokeAction(...)` boundary in one `causeway.action.invocation` observation while preserving pass-through and transactional execution behavior.
- [x] 2.3 Add `causeway.action.id` and `causeway.execution.initiatedBy` low-cardinality attributes without capturing arguments, results, targets, users, tenancy values, or other instance data.
- [x] 2.4 Verify that propagated action failures are recorded and that action observation cleanup preserves the original exception behavior.

## 3. Add Focused Runtime-Service Coverage

- [x] 3.1 Add descriptive recording or approval tests for one root observation, nested-layer suppression, callback ordering, and final current-observation cleanup.
- [x] 3.2 Add focused tests for root work failure, root-close failure, rollback preservation, and lifecycle-state removal.
- [x] 3.3 Add focused tests for stable action naming, required tags, parentage beneath a root observation, success, failure, and inactive-registry behavior.
- [x] 3.4 Run the affected `core/runtimeservices` and `core/config` tests on the Java 11 compilation target.

## 4. Verify Real-Agent Semantic Parentage

- [x] 4.1 Extend the tracing compatibility fixture with an agent-instrumented HTTP entry that executes the production semantic interaction and action path before JDBC work.
- [x] 4.2 Extend exported-span assertions to verify the ordered ancestry `HTTP → causeway.root.interaction → causeway.action.invocation → JDBC` using stable names rather than generated identifiers.
- [x] 4.3 Verify that active observation without the Java agent still completes interaction and action execution safely.
- [x] 4.4 Run dependency convergence, the Java 11 real-agent regression test, strict OpenSpec validation, and confirm that no SDK, exporter, additional semantic span family, or public applib API was introduced.
