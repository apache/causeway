## Context

Baseline inspected: `6f10ac34cac` on `CAUSEWAY-4096`, after the archived observation-policy change. No remote refresh or new implementation branch is implied by this proposal. Main uses Boot 4.2.0-M1 and supports an explicit agent-owned bridge; core/config already depends on Micrometer Tracing, but not the OpenTelemetry API.

Maintenance sources:
- `024bd33128d`: current-span foreground filter and background-command job classification.
- `0325232a3c7`: interaction UUID on the root observation.
- `3083a51eabd`: `causeway.execution.mode.key`. Its surrounding filter also contains later semantic-naming support, which is excluded here.

Main differs materially: `InteractionServiceDefault.openInteractionLayer` creates root/nested observations through `InteractionLayerStack`; interaction carriers allocate command identity. `CommandExecutorServiceDefault.doExecute` later calls `setCommandDtoAndIdentifier`, replacing that identity for replay. Copying maintenance's opening-time tag would leave a stale correlation value. The recently corrected Wicket listener order must remain intact.

## Goals / Non-Goals

**Goals:** bounded entry-span classification, operator-configurable classification key, trustworthy root interaction UUID correlation, and exported evidence for Boot and agent modes. Preserve inactive-profile behavior, scope cleanup, existing names and transaction semantics.

**Non-goals:** semantic naming, rendering detail, correlation baggage/MDC, cross-process identity propagation, new SDK/exporter ownership, generic scheduler instrumentation, adding metric identity labels, or forcing every span to carry classification.

## Decisions

### Classify the existing entry span through the configured tracing bridge

Introduce a small classifier backed by the single/primary Micrometer `Tracer`, using `currentSpan().tag(key, value)` only when a span exists. Configure an inert implementation when Causeway's `observation` profile is inactive or no tracer is available. Resolve any ambiguous active tracer configuration explicitly using Spring's single/primary rules rather than guessing. Keep key/value constants and typed configuration available to consuming applications.

Boot supplies the tracer. Refactor the documented sample agent bridge to expose its existing `OtelTracer` as a bean and inject that same instance into the observation handler; do not create a second tracer pipeline or SDK. Update the compatibility fixture equivalently. A custom registry alone without an exposed tracer still supports observations, but cannot classify non-observation entry spans; document this prerequisite.

Alternative rejected: directly copying maintenance's `Span.current()` helper into core/config would add an OpenTelemetry API dependency and assume agent context ownership. Tagging only the current Observation is insufficient because an agent-owned HTTP/Quartz entry span need not have a Micrometer observation.

### Register classification inside HTTP instrumentation and before application/security work

Use a Jakarta servlet filter across `/*`, ordered after Boot's HTTP observation filter and before nested security/viewer observations. In agent mode the agent's servlet entry span must already be current. Verify this ordering with actual exported HTTP spans, not just mocked classifier calls. Do not copy maintenance's servlet priority blindly or import its semantic trace scope.

Foreground includes Wicket, GraphQL/HTMX, REST and static resources: it denotes an HTTP entry, not necessarily a human action. Use request-dispatch semantics that prevent async/error redispatch from retagging arbitrary child spans. Classification creates no scopes and no spans; request exceptions propagate unchanged. No universal propagation to children is promised.

### Classify the command-log job at entry

Call the classifier at the start of `RunBackgroundCommandsJob.execute`, before its paused check, matching maintenance behavior. Label the current instrumented job entry span `background`, preserving retries, callbacks and transaction behavior. Do not classify every anonymous interaction or arbitrary Quartz job.

Boot does not automatically guarantee a Quartz entry span. Without one, the classifier is a no-op. Tests explicitly provide a recording entry span for the Boot job fixture; an agent fixture should exercise real Quartz instrumentation. Documentation must distinguish this fixture/setup requirement from out-of-the-box Petclinic behavior. Petclinic is suitable for foreground verification, not a claim of a configured command-log scheduler.

### Keep the maintenance configuration contract, using main's immutable conventions

Expose `causeway.execution.mode.key`, default `causeway.execution.mode`, as typed configuration following main's record/default conventions. Reject empty and whitespace-only values at binding/startup. Retain the exact values `foreground` and `background`; an override replaces the emitted default key rather than adding aliases. Do not add a second observation-policy switch for the same setting. Audit `Execution`/`Mode` type-name collisions instead of copying maintenance's mutable configuration class.

### Bind correlation to the root interaction lifecycle

Add `causeway.interaction.id` only to the existing depth-zero `Causeway Root Interaction` observation, as high-cardinality metadata. Source it from that interaction's `getInteractionId()`; do not allocate an ID for telemetry or rename observations. Leave main's existing nested/reused-layer identity semantics unchanged, and do not assume maintenance's shared-ID stack model applies.

Attach the initial value once the root carrier exists, then refresh the same attribute from that root interaction immediately before its observation stops. Bind the supplier to that root instance, not to whichever layer is current at cleanup. This handles replay's identifier replacement, preserves the attribute on failures, and prevents nested interactions from overwriting the outer root. If an equivalent existing lifecycle hook provides this guarantee, reuse it rather than introducing separate ThreadLocal state. Protect scope-close/stop guarantees if correlation enrichment fails.

Rejected alternatives: tagging every HTTP/JDBC span creates needless correlation duplication; recording only at observation creation loses replay identity; changing command identity semantics to simplify tagging is outside scope.

## Risks / Trade-offs

- Wrong servlet filter order labels a child or misses the entry → actual servlet/export tests for both trace owners, success and failure, followed by thread reuse.
- ID replacement or nested carriers produce misleading searches → deterministic UUID tests, real replay path and final exported-value assertions.
- No current job span in Boot → explicit no-op contract and example instrumentation prerequisite; no invented background trace.
- Custom registries may omit a tracer bean → document shared bridge wiring and cover registry-only operation without failure.
- Classification is span-local, while correlation lives on a different span → document separate Jaeger searches using All Span Names; do not promise same-span conjunctions will match.
- In-memory Jaeger restarts and sampling can remove matches → document retention and sampling limits; correlation does not recover unexported traces.

## Migration Plan

Additive span attributes; names, exporters, sampling, identity opt-ins and filtering defaults remain unchanged. Applications using the documented agent bridge expose the shared tracer bean before checking classification. Override the key only when required by existing telemetry conventions and update searches accordingly. Removing the change removes the new attributes without changing domain outcomes or requiring data migration.

Update the M3 how-to incrementally: default foreground attributes on the HTTP span, root UUID on the interaction span, copy-and-search example, custom key, and a separately identified background fixture/example. Retain the separate Jaeger and metrics scripts and unified Petclinic launcher.

## Open Questions

No product decision blocks implementation. Assign the main-side Jira ticket/branch before implementation; CAUSEWAY-3975/4068 are provenance. Implementation must prove filter ordering against the pinned Boot baseline and check how agent Quartz instrumentation exposes the job entry span. If either requires a contract change, update this plan before expanding scope.
