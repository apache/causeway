## Context

Maintenance implemented application spans in `ea32b739d0f`, added `NOOP` in `5da64c34b57`, and corrected invoked-versus-domain-facing action identity in `f66b8f17a89`.
The earlier archived specification predates that identity correction, so final code and tests are authoritative for that distinction.
V4 already preserves physical invocation identity and exposes `ActionInvocation.getDomainFacingLogicalMemberIdentifier()` through the completed action-context port.
It also has a robust `commons.internal.observation.ObservationClosure` shared by existing instrumentation.

## Goals / Non-Goals

**Goals:** An injectable telemetry-independent public API, synchronous nested spans, current-member identity, bounded suffix-preserving names, original throwables and no-op execution.

**Non-Goals:** Async span lifetimes, user attributes, caller-controlled tracing objects, new SDK/exporter configuration, action identity rewrites, application priming or general filtering.

## Decisions

### Keep the maintenance public service shape

Add `ApplicationSpanService.call(String, Callable<T>)` and `run(String, ThrowingRunnable)` to applib with no telemetry types.
Follow the existing interaction-service convention: checked exceptions are not declared but escape unchanged using Lombok's established sneaky-throw mechanism.
Expose the maintenance `NOOP` constant for standalone tests; it executes work directly without naming validation or telemetry infrastructure.
The injectable default validates suffixes even when its selected registry is no-op.
Register the implementation explicitly in the runtime module, with standard service/qualifier/priority annotations and package exports.

### Preserve invoked action identity while naming domain-facing work

Resolve the current interaction's current execution on every call.
For `ActionInvocation`, use its separate domain-facing identifier; for other execution kinds, use the existing logical member identifier.
Format canonical metadata as logical type plus `#` plus member logical name, omitting parameter signatures.
Do not use the outer command, Java callable class, mixin implementation class or target title to reconstruct identity.
No action executor change is needed: v4 already supplies both physical and domain-facing identifiers correctly.
Test that nested current executions override outer identity and that mixins retain their invoked `#act` identity while application spans use the contributed action.

### Reuse existing names and lifecycle ownership

Add `CausewayObservationNaming.forApplicationSpan` and maximum suffix length 46.
Reject null/blank suffixes and overlong suffixes before executing work.
Use a maximum 50-character contextual name: full canonical identity if it fits, then namespace-free identity if it fits, otherwise truncate only identity while preserving the complete suffix and one space.
Without an identity use `app <suffix>`.
The stable operation is `causeway.application.span`; static framework bean/module metadata plus canonical `causeway.member.id` when present and `causeway.application.span.suffix` are the only added attributes.
Use the existing observation provider and robust v4 `ObservationClosure` so failures, including checked exceptions and errors, retain identity even when error reporting or cleanup also fails.
Natural observation context determines parentage, including nested application spans, in both Boot and agent modes.

## Risks / Trade-offs

- Suffix content could contain private or high-cardinality data → Document static phase names and enforce bounded length; semantic privacy remains the caller's responsibility.
- Historical identity wording conflicts with the final maintenance fix → Preserve v4's current physical identity and explicitly use its separate domain-facing getter.
- Checked exceptions can escape despite the signature → Document the established interaction-service convention and test identical throwable instances.
- Handler failures can mask work failures → Reuse the robust shared closure and test reporting/close failures without replacing the primary failure.
- Stacked history includes evaluation work → Keep this change separate and rebase after PR #3898 merges.

## Migration Plan

This is additive API with no configuration, schema or data migration.
Applications inject the service and choose static suffixes for synchronous phases.
Inactive deployments need no conditional instrumentation code.
Rollback removes the new service calls before removing the additive API.

## Open Questions

None requiring a product decision.
