## Context

Causeway currently creates framework-owned Micrometer observations through `CausewayObservationIntegration`, whose dedicated registry is connected to the OpenTelemetry Java agent only when the `observation` profile is active.
Applications can technically depend on that internal class or its qualified registry, but there is no supported applib contract for dividing long-running domain actions into application-meaningful regions.

An action invocation exposes its logical identity through `InteractionService.currentInteraction()`, the current `Execution`, and `Execution.getLogicalMemberIdentifier()`.
Declared actions already carry the required domain-facing identifier, but contributed mixin actions currently construct `ActionInvocation` with the mixin implementation identifier, such as an identity ending in `#act`, rather than the mixed-in action identifier.
The change must correct that execution identity at its construction boundary before the application span service can use it without reconstructing identity from implementation details.
The exported tracing span name is selected from Micrometer's contextual observation name and is bounded to 50 characters by `CausewayTracingObservationHandler`.
Application-defined region names therefore need a deterministic suffix-preserving compaction policy rather than ordinary right-side truncation.

The API must remain independent of Micrometer and OpenTelemetry so applications do not acquire an SDK or exporter and so the Java agent remains the sole telemetry owner.

## Goals / Non-Goals

**Goals:**

- Provide an injectable applib service that wraps synchronous application work in an observation span.
- Support both value-returning callables and non-returning throwing runnables.
- Ensure declared and contributed mixin action executions expose their domain-facing logical member identifier.
- Derive a child span's contextual name from the current execution's canonical `<logical-type-name>#<member-logical-name>` identity and an application-provided static suffix.
- Preserve suffixes of at most 46 characters exactly in every accepted contextual name while keeping the name within 50 characters.
- Keep the full logical member identifier available as canonical span metadata when its display form is compacted or truncated.
- Preserve current observation parentage, error reporting, return values, and exception behavior.
- Degrade to normal unobserved execution when framework observation is inactive.

**Non-Goals:**

- Expose Micrometer `Observation`, OpenTelemetry `Span`, scopes, registries, SDKs, samplers, or exporters through applib.
- Support asynchronous context propagation or spans whose lifecycle outlives a single service call.
- Accept dynamic instance data, action arguments, user identities, bookmarks, or record identifiers as names or attributes.
- Replace Causeway's action-invocation span or change its existing naming requirements.
- Add configurable sampling or export behavior.

## Decisions

### Add an `ApplicationSpanService` applib contract

Add `org.apache.causeway.applib.services.span.ApplicationSpanService` with synchronous `call(String suffix, Callable<T>)` and `run(String suffix, ThrowingRunnable)` operations.
The methods follow the established `InteractionService` convention: they do not declare checked exceptions, but an original checked exception, runtime exception, or error escapes unchanged and is not wrapped.
The service owns the complete span lifecycle so application code cannot leak a scope or forget to stop a span.

The alternative of exposing a span handle implementing `AutoCloseable` was rejected because it makes incorrect lifecycle ordering and missing error recording easier.
The alternative of exposing Micrometer directly was rejected because it would add telemetry-specific API surface and dependencies to applib.

### Use one stable observation category and a contextual display name

Every application-defined span uses stable observation name `causeway.application.span`.
Its exported contextual name is formed as `<logical-member-identifier> <suffix>` using one plain-space separator when a current execution and logical member identifier are available.
The canonical logical member identifier is formatted as `<logical-type-name>#<member-logical-name>` without action parentheses or parameter type signatures.
The implementation obtains the identifier components through `InteractionService.currentInteraction()`, then the current `Execution`, rather than reconstructing them from the target class or Java method.

Before adding the service, correct `MemberExecutorServiceDefault` so a contributed mixin `ActionInvocation` is constructed with the domain-facing action `Identifier` exposed by its `ActionInteractionHead`, while retaining the implementation action identifier for invocation internals that still require it.
This makes the execution API truthful for mixins and lets nested wrapper invocations naturally expose whichever execution is current.

The full canonical identifier is attached as low-cardinality attribute `causeway.member.id`, and the supplied suffix is attached as `causeway.application.span.suffix`.
The API documentation requires the suffix to be a static operation or phase identifier rather than instance-specific data.

Using the suffix as the stable observation name was rejected because it would create an unbounded observation-name vocabulary and would lose the stable category used for filtering.
Using only the logical identifier as the contextual name was rejected because sibling regions inside one action would be indistinguishable.

### Preserve the suffix when bounding contextual names

The contextual-name builder first tries the full logical member identifier followed by one space and the suffix.
If that exceeds 50 characters, it retries with the logical type namespace removed, following existing semantic naming behavior.
If the compact form still exceeds 50 characters, it truncates only the logical-member portion to fit while preserving the separator and complete suffix.

A suffix longer than 46 characters is rejected with `IllegalArgumentException` before the callable or runnable executes.
Null and blank suffixes are also rejected.
The context-independent 46-character limit reserves four characters for the fallback prefix `app `, so every accepted suffix fits whether or not an execution identity is available.
This makes the suffix-preservation guarantee explicit instead of silently producing an ambiguous or altered region name.

When no current execution identity is available, the contextual name is `app <suffix>` and no `causeway.member.id` attribute is attached.
The same validation and 50-character bound apply to this fallback.
This allows use from anonymous interactions and tests without making telemetry context a prerequisite for application behavior.

The alternative of applying the existing generic right-side truncation was rejected because it can remove the application-provided region suffix, which is the information that distinguishes sibling spans.

### Implement in runtime services over the existing observation integration

Place the default implementation in core runtime services and inject `InteractionService` plus `CausewayObservationIntegration`.
Create each observation from the existing dedicated registry, add module and bean metadata consistently with framework observations, set its contextual name and canonical attributes, and execute work through Micrometer's lifecycle helper.

This preserves nesting beneath the active action observation and beneath an enclosing application span.
When the registry is `ObservationRegistry.NOOP`, the implementation still invokes the supplied work exactly once and returns or throws exactly as it would with observation active.
No application-defined `ObservationRegistry`, SDK, or exporter participates.

## Risks / Trade-offs

- [Risk] A dynamic suffix can create high-cardinality names and attributes even though the API describes it as static. → Document the restriction prominently and test examples using fixed phase identifiers; the service cannot reliably infer whether arbitrary text is dynamic.
- [Risk] Rejecting an overlong suffix can make telemetry naming affect application execution. → Define and document the context-independent 46-character maximum at compile-facing API entry points and validate it before invoking work.
- [Risk] Correcting mixin `ActionInvocation` identity could affect consumers that accidentally depend on the implementation `#act` identifier. → Treat the current value as inconsistent with the execution API's logical-identity contract and add focused DTO, publishing, and nested-execution regressions.
- [Risk] `Interaction.getCurrentExecution()` could change during wrapper-mediated nested invocations. → Resolve the logical identity when each span starts so the span describes the execution that actually requested it.
- [Risk] Callable exception semantics can be accidentally changed by the implementation. → Follow the existing `InteractionService` sneaky-throw convention and add tests for checked exceptions, runtime exceptions, errors, return values, and exactly-once invocation.
- [Trade-off] The synchronous closure API cannot represent asynchronous regions. → Keep asynchronous context propagation out of scope rather than exposing unsafe span handles in this maintenance branch.
- [Trade-off] Fallback names outside a member execution omit canonical member identity. → Prefer useful standalone spans and safe execution over failing solely because no Causeway execution is current.

## Migration Plan

The API and implementation are additive and require no application migration.
Applications may replace direct use of `CausewayObservationIntegration`, the qualified registry, or OpenTelemetry span lifecycle code with `ApplicationSpanService` incrementally.
Rollback consists of removing those service calls or deploying the previous framework version; no persisted data or exporter configuration changes are involved.

## Open Questions

None.
