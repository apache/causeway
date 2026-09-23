## Context

Metamodel creation performs demand-driven, nested specification introspection.
When command-log recording support is enabled, `SynthesizeNavigationActionsPostProcessor` streams associations including mixed-in members, which can fully introspect mixin specifications.
Other postprocessors, notably `DescribedAsFromTypePostProcessor`, can then resolve action element types and trigger further specification loads.

The observed failure repeats this path until the JVM stack is exhausted, but the exception contains method names rather than the domain and mixin classes being traversed.
In addition, `CausewayBeanTypeRegistryDefault` stores mixin metadata in a `HashMap`, and `streamMixinTypes()` exposes that unspecified iteration order to both mixed-in action and association discovery.
This can change the nesting depth and the point of failure between otherwise equivalent JVM runs.

The relevant CAUSEWAY-4039 regression tests cover a small cyclic graph but do not expose a long chain involving numerous mixins and action element-type post-processing.
No metamodel classes on the failing path changed after `maintenance-branch`, so the immediate objective is to expose and stabilize the latent path before selecting a corrective redesign.

## Goals / Non-Goals

**Goals:**

- Report the actual domain and mixin type chain involved when nested specification introspection overflows the JVM stack.
- Include enough state and caller information to distinguish navigation synthesis, mixed-in member discovery, and action element-type resolution.
- Make mixin traversal repeatable for a fixed set of registered types.
- Add focused regression coverage for diagnostics, ordering, and the suspected cross-postprocessor path.

**Non-Goals:**

- Redesign metamodel introspection or eliminate all recursive loading in this change.
- Change mixin applicability, member ordering, or the public domain programming model.
- Introduce a general-purpose tracing subsystem or a new dependency.
- Treat a larger JVM stack as a correction for the underlying dependency chain.

## Decisions

### Maintain a thread-local introspection chain at the specification-loader boundary

`SpecificationLoaderDefault` is the common boundary through which nested type loads pass, so diagnostics will maintain a per-thread stack around `_loadSpecification` or the narrowest equivalent internal method.
Each entry will contain the requested Java type, requested introspection state, current specification state when available, nesting depth, and a compact caller identifier.
The scope will be removed in a `finally` block during normal execution.

This captures the type information absent from a Java stack trace while preserving isolation between the concurrent metamodel bootstrap tasks.
A global stack was rejected because concurrent task output would be interleaved and misleading.
Adding type logging independently at every postprocessor was rejected because it would duplicate instrumentation and still leave gaps between callers.

### Emit the retained chain when a stack overflow crosses the loader boundary

The innermost loader boundary that observes a `StackOverflowError` will emit the chain once and rethrow the original error unchanged.
A per-thread reported flag will suppress duplicate reports as the same error unwinds through outer loader calls.
The report will use compact, depth-ordered lines so it remains usable even for a long chain.

Optional trace-level entry and exit messages may reuse the same data, but normal successful startup will not produce diagnostic noise at standard log levels.
Catching and replacing the error was rejected because it would obscure the original failure and could require unsafe allocation while the stack is exhausted.
Logging only at the outer task-list boundary was rejected because nested loader scopes have already unwound there.

### Derive caller information without changing public loader APIs

The diagnostic entry will identify the nearest relevant framework caller, using a small internal helper and Java's built-in stack walking facilities or an equivalently bounded internal mechanism.
Framework plumbing frames from the specification loader itself will be skipped.
This avoids propagating a diagnostic-reason parameter through public and widely used `SpecificationLoader` methods.

Explicit reason parameters at every call site would provide stronger semantics, but they create a broader, riskier change in a decommissioning branch.
If caller derivation proves too costly or unreliable, the minimum acceptable fallback is the type and introspection-state chain, because that is sufficient to reconstruct the domain dependency path.

### Sort mixin types by stable class name at the registry stream boundary

`CausewayBeanTypeRegistry.streamMixinTypes()` will return mixin classes ordered by their fully qualified class name.
Centralizing the ordering there applies consistently to mixed-in actions and mixed-in associations and leaves the underlying lookup maps unchanged.

Replacing all registry maps with sorted maps was rejected because only mixin traversal is implicated and broader map-order changes could affect unrelated bootstrap behavior.
Sorting separately in each metamodel consumer was rejected because duplicated ordering logic could drift.

### Test the combined path rather than only a two-node collection cycle

Tests will cover stable class-name ordering directly and will repeat metamodel construction to verify a stable traversal sequence.
A full-boot or suitably production-equivalent regression fixture will combine multiple mixins, mixed-in associations, navigation-action synthesis, and actions whose element types trigger further specification introspection.
The fixture will verify that the diagnostic chain identifies participating types if overflow is deliberately reproduced, or otherwise verify the captured nested sequence through a test seam.

The test need not prescribe a permanent failing order.
Its purpose is to prove observability and reproducibility before a follow-up change removes or flattens the problematic recursion.

## Risks / Trade-offs

- [Risk] Capturing caller information for every specification load adds startup overhead. → Keep the helper bounded, avoid full stack materialization, and measure or disable detailed caller capture unless diagnostics require it.
- [Risk] Logging while handling `StackOverflowError` can itself fail because stack and allocation headroom are limited. → Keep the catch block shallow, retain compact pre-existing entries, emit once, and always rethrow the original error.
- [Risk] Class-name ordering changes which member clash or validation issue is encountered first. → Preserve all existing filtering and member ordering semantics and test representative mixed-in actions and associations.
- [Risk] A deterministic order may make the application fail consistently rather than intermittently. → This is an intentional diagnostic outcome; rollback is the isolated ordering change, while the emitted chain supplies the evidence for the corrective follow-up.
- [Risk] The representative fixture may still be smaller than the production graph. → Include the interaction between all three observed triggers and allow production logs to provide the exact chain.

## Migration Plan

No data or configuration migration is required.
Deploy the diagnostic and ordering changes together to an environment that has exhibited the intermittent failure, retaining the existing command-log recording-support setting and JVM stack size.
Compare repeated starts and collect the emitted type chain if a failure occurs.
Rollback consists of reverting the change; no persisted state is altered.
Use the resulting chain to decide whether a follow-up should defer navigation synthesis, avoid fully introspecting inapplicable mixins, or otherwise flatten the dependency traversal.

## Open Questions

- What JVM stack size was used by the failing process, and does it match the integration-test runtime?
- Should successful nested-chain tracing be available only through the existing metamodel logger, or also through a dedicated diagnostic logger category?
- Is fully qualified class-name ordering sufficient in environments that load same-named classes through different class loaders?
- Which production domain and mixin types form the failing chain once diagnostics are available?
