## Context

OpenTelemetry has no trace-name field distinct from its spans.
Jaeger and comparable backends conventionally label a trace using its root span's operation name.

For foreground Causeway requests, the OpenTelemetry Java agent creates and owns the root HTTP server span before Causeway's servlet filter runs.
Causeway currently classifies that span with `causeway.execution.mode=foreground`, then creates `causeway.root.interaction` and more specific action, prompt, preparation, and render descendants.
Those descendants now have useful names such as `act demo.Customer#update`, but the trace list still shows a transport name such as `GET /wicket/...`.

The existing `CausewayForegroundTraceFilter` is the earliest and latest Causeway-controlled boundary inside the agent-owned server span.
It can capture `Span.current()` before delegating and update that same span after downstream Causeway instrumentation has nominated the request's primary semantic outcome.

The supported baseline is OpenTelemetry Java agent 1.31.0.
The agent must continue to own span creation, context propagation, HTTP and JDBC instrumentation, sampling, and OTLP export.

## Goals / Non-Goals

**Goals:**

- Give eligible foreground entry spans bounded domain-facing operation names that telemetry backends can use as trace display names.
- Select one name deterministically from action invocation, action prompt, and entity view evidence collected during the request.
- Preserve standard HTTP attributes, Causeway execution-mode classification, parentage, sampling, and export.
- Preserve full canonical Causeway identifiers as attributes when display names are compacted or truncated.
- Keep all state request-local, transient, and safely cleaned up on successful and failed requests.
- Remain harmless without the Java agent or with a non-recording current span.
- Prove final-name behavior against agent 1.31.0 before treating in-process renaming as supported.

**Non-Goals:**

- Introduce a fictional OpenTelemetry trace-name field.
- Replace the agent-owned HTTP root with a Causeway-created root span.
- Rename `causeway.root.interaction` or any existing Causeway child observation for the purpose of trace-list presentation.
- Rename JDBC, outbound HTTP, background Quartz, or unrelated automatic spans.
- Remove or alter standard HTTP method, route, target, status, or error attributes.
- Expose semantic trace naming as an application-facing API.
- Include object-instance identifiers, values, arguments, titles, bookmarks, users, tenants, localized labels, or arbitrary URL data.
- Guarantee identical trace-list presentation in every telemetry backend.

## Decisions

### Rename the agent-owned foreground entry span in place

`CausewayForegroundTraceFilter` will capture `Span.current()` before invoking the filter chain and will call `Span.updateName(...)` in its `finally` path after downstream processing has completed.
The update occurs while the captured span is still active and before the Java agent ends it.

This is preferred to adding another Causeway root because an additional span would remain beneath the HTTP span and would not change the backend's trace-list label.
It is preferred to an application-owned span processor or exporter because those would violate the single telemetry-owner architecture.

The implementation will check that the captured span is recording before installing active nomination state or mutating it.
No-agent and non-recording cases retain current behavior.

### Coordinate nominations through an internal request scope

Add an internal semantic trace-name coordinator shared by core webapp, runtime action instrumentation, and Wicket instrumentation.
The filter opens a request scope containing the captured entry span and a selected candidate, and closes that scope in `finally` even when downstream processing throws.

The coordinator will use a stack-safe thread-local scope because runtime action instrumentation must not depend on servlet APIs and Wicket request attributes are not available at every nomination point.
Nested filter dispatches will restore the previous scope rather than overwriting it.
Asynchronous servlet continuation and cross-thread propagation are out of scope.

Only framework-internal callers can nominate candidates.
No applib service or arbitrary string-based application hook will be introduced.

### Use explicit candidate kinds and first-wins ties

Candidates have fixed priorities:

1. `ACTION` produces `act <logical-member-identifier>`.
2. `PROMPT` produces `prompt <logical-member-identifier>`.
3. `VIEW` produces `view <logical-type-name>`.

A higher-priority nomination replaces a lower-priority selection.
The first nomination at an equal priority remains selected, making repeated nominations deterministic by request execution order and preventing later rendering from overwriting the primary operation.

Action instrumentation will nominate only the ordinary domain-facing action corresponding to the request's top-level interaction command.
It will not nominate mixed-in property or collection access, framework helper invocations, or later nested wrapper invocations as the request's primary action.
Wicket prompt instrumentation will nominate the prompted action, and entity-page instrumentation will nominate the viewed logical type.
An action therefore wins over prompt and view candidates produced while rendering the response to that action.

This explicit selector is preferred to last-writer-wins behavior because page rendering normally occurs after invocation and would otherwise hide the action that caused the response.

### Reuse bounded semantic naming and canonical attributes

Candidate display names will use the existing case-preserving `CausewayObservationNaming` policy and its 50-character maximum.
Action and prompt candidates will use the full logical member identifier, then retry without the logical type namespace, then truncate if necessary.
View candidates will use the full logical type, then its namespace-free form, then truncate if necessary.

The selected entry span will receive bounded `causeway.trace.name` equal to the selected display name.
Action and prompt candidates will also attach the complete canonical `causeway.action.id`, and view candidates will attach the complete canonical `causeway.object.type`.
These values describe static application structure and remain authoritative if the display name is compacted or truncated.

Existing standard HTTP attributes and `causeway.execution.mode=foreground` remain untouched.

### Gate support on an agent-backed exported-span test

The tracing compatibility regression will exercise view-only, prompt-only, action-invocation, priority, unchanged-route, and failed-request cases in child JVMs with agent 1.31.0 attached.
The collector output must prove that the exported HTTP server span retains the Causeway-selected final name after the filter returns.
It must also prove that HTTP attributes, trace identity, parentage, and execution-mode classification are unchanged.

If the agent overwrites the name after the filter returns, the in-process rename will not be shipped as effective behavior.
Causeway will still emit bounded `causeway.trace.name` and canonical attributes, and the operations guide will document a collector transform or supported Java-agent extension as the deployment-specific fallback.
This fallback is less portable, so the supported in-process path remains contingent on the evidence gate.

## Risks / Trade-offs

- [Risk] The Java agent may update the server span name after Causeway's filter returns. → Make the exported final name an agent-backed compatibility gate and retain attribute-plus-collector transformation as the fallback.
- [Risk] Renaming changes backend operation-name grouping and can affect dashboards or alerts. → Document the behavior as operationally breaking and direct route-based monitoring to standard HTTP route attributes.
- [Risk] A request can produce several legitimate semantic operations. → Use explicit priority and first-wins tie handling, and test combined action, prompt, and page-render flows.
- [Risk] A thread-local scope can leak across reused servlet threads. → Use an `AutoCloseable` stack scope and unconditional `finally` cleanup, with failure and nested-dispatch tests.
- [Risk] Internal action activity could incorrectly label a page-view request as an action. → Restrict action nomination to the top-level command identity and exclude mixed-in associations and framework or nested helper invocations.
- [Risk] Ajax requests can have no unambiguous page outcome. → Leave the original route name unless an actual selected action or unambiguous prompt nomination exists.
- [Trade-off] Semantic names reduce immediate route visibility in the operation-name column. → Preserve every standard HTTP attribute for filtering and diagnosis.
- [Trade-off] Background traces retain different root naming behavior. → Keep background naming explicitly out of scope rather than presenting a partial abstraction as universal.

## Migration Plan

Deploy the change first in an environment where trace operation-name dashboards and alerts have been inventoried.
Update transport-oriented grouping to use standard HTTP route and method attributes where semantic entry names would otherwise break it.
Verify representative view, prompt, action, Ajax, and unmatched requests against the configured backend before broad rollout.

Rollback requires only deployment of the previous framework version or disabling the semantic rename integration if an implementation guard is provided.
No persisted application data or telemetry backend schema migration is required.

## Open Questions

- The evidence gate must confirm whether agent 1.31.0 preserves `Span.updateName(...)` after the servlet filter returns; this determines whether in-process renaming or collector-side transformation is the supported deployment path.
- The implementation spike must confirm the most reliable test for matching an action invocation to the current top-level command across declared and mixed-in actions without nominating nested wrapper work.
