## Context

`CausewaySystemEnvironment.onContextAboutToClose(...)` deliberately clears its `SpringContextHolder` when Spring publishes `ContextClosedEvent`. Spring invokes `SpecificationLoaderDefault.shutdown()` later as a bean destroy callback. That callback disposes the metamodel, including `clearLayoutCaches()`, whose `GridService` lookup reaches `ServiceRegistryDefault.select(...)` after the holder has become `null`.

Spring logs and suppresses the resulting destroy-method `NullPointerException`, so affected tests and applications can appear to stop successfully even though metamodel disposal did not reach its remaining cache-clearing steps. The maintenance branch addressed the equivalent `_IocContainer` path in CAUSEWAY-4002 by treating selection against an unavailable container as an empty result. Causeway 4 retained the lifecycle ordering but did not inherit that guard when its IoC abstraction changed to `SpringContextHolder`.

## Goals / Non-Goals

**Goals:**

- Make `ServiceRegistryDefault.select(...)` safe after the Spring context holder has been cleared.
- Allow `SpecificationLoaderDefault.disposeMetaModel()` to finish during normal Spring shutdown.
- Preserve selection and qualifier semantics while the context holder remains available.
- Cover both the focused unavailable-holder behavior and the observed integration-test shutdown path.

**Non-Goals:**

- Reorder Spring lifecycle events or defer clearing the global `_Context`.
- Keep Spring beans available after context closure has begun.
- Broaden the change to unrelated service-registry operations that are not exercised by this disposal path.
- Address the unrelated Restful Objects assertion failure originally tracked by the wider CAUSEWAY-4002 CI investigation.

## Decisions

### Treat an unavailable context as an empty selection

`ServiceRegistryDefault.select(...)` will read the current `SpringContextHolder` once and return `Can.empty()` when it is absent; otherwise it will delegate with the requested type and qualifiers exactly as today.

This preserves the service-registry meaning of “no selectable service” during teardown and ports the proven maintenance-branch behavior. Throwing a lifecycle-specific exception was rejected because it would still abort destruction. Retaining a holder captured at construction was rejected because it would permit bean access after the environment has deliberately declared the context unavailable.

### Guard the selection boundary rather than special-case metamodel disposal

The null handling belongs at the component that owns the nullable context access. A conditional in `clearLayoutCaches()` would repair only the currently observed caller and leave any other shutdown-time selection vulnerable. Changing `ContextClosedEvent` ordering was rejected because clearing the global framework context before destruction is an intentional lifecycle invariant.

### Use focused and lifecycle-level regression evidence

A focused `core/mmtest` test will construct the registry with an environment whose holder is unavailable and prove selection returns an empty `Can` without throwing. Existing tests continue to cover normal delegation. A focused Restful Objects integration test will exercise a real application-context close and its output will be checked for successful metamodel disposal without the destroy-method warning.

## Risks / Trade-offs

- [A service lookup attempted after shutdown begins becomes indistinguishable from a normally empty selection] → Restrict the fallback to the already-null holder state and document it as teardown behavior.
- [Other registry methods can still dereference the cleared holder] → Keep this change aligned with the maintenance fix and the demonstrated disposal call path; investigate other methods separately if a concrete shutdown caller is found.
- [A unit test alone could miss Spring lifecycle ordering] → Retain integration-level shutdown verification in addition to the focused regression.

## Migration Plan

No migration is required. The implementation is a backward-compatible defensive change. Reverting the guard restores the previous behavior if rollback is needed.

## Open Questions

None. The lifecycle ordering, failure path, and maintenance-branch precedent have been reproduced and confirmed.
