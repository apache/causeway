## Context

Maintenance `e17c3088ac2` introduces an internal OpenTelemetry-current-span nomination stack, wraps its servlet foreground filter and adds executor and Wicket nominations. Main instead classifies entries using an injected Micrometer tracer, Jakarta OncePerRequestFilter and the shared Boot/agent integration. Its Wicket descriptors and request coordinator already enforce detail/budgets with attempt-all cleanup. Main's domain-facing identifiers differ from physical mixin invocation identifiers.

## Goals / Non-Goals

**Goals:** Recognizable foreground entry displays under both tracing owners; deterministic priority and cleanup; bounded static metadata; Wicket nominations independent of span admission; integrated runnable guidance.

**Non-Goals:** Public nomination APIs, new configuration, background renaming, new spans, other-viewer page/prompt hooks, arbitrary asynchronous nomination propagation, filtering or application-defined spans.

## Decisions

### Capture the entry through the shared tracer

Introduce an internal naming coordinator using the same resolved tracer as classification. Capture the existing entry when the foreground filter starts, before security/viewer children, and apply the selected name and tags before that entry ends. Do not read the then-current child at nomination/close time. The maintenance direct OpenTelemetry approach would unnecessarily couple naming to a particular owner; the existing bridge supports both setups. Do not add an SDK, registry, exporter or specialized handler unless export evidence demonstrates a concrete necessity.

Use request-scoped, thread-confined nested state with idempotent reverse-order close and removal before applying updates. A missing tracing context is a no-op. Keep OncePerRequestFilter redispatch behavior; synchronous work within the initial dispatch is the supported nomination boundary. Preserve original request failures and prevent stale state on reused worker threads, including cleanup failures.

### Deterministic semantic selection

Keep maintenance priority ACTION (act) > PROMPT (prompt) > VIEW (view), first wins ties. Format through CausewayObservationNaming's case-preserving 50-character compaction. Keep display signatures omitted, full canonical signatures/types in attributes, and the selected bounded display in causeway.trace.name. Use static logical metadata only; no argument values, bookmarks, titles, localized labels or instance IDs. Unselected/no-candidate entries retain their supplied names. HTTP route/method/status, execution classification, trace/span identity and ancestry remain intact; observation operation categories are unchanged.

### Nominate domain work at trusted boundaries

After preparing command identity, nominate a real top-level action only when its domain-facing identity matches the current interaction command and it is not a mixed-in property/collection association. Preserve contributed action names and physical invocation identity. Helpers, calculated access and unmatched nested actions must not take over. Characterize main's executor paths and canonical signatures rather than copying maintenance's synthetic empty signature indiscriminately.

Wicket PAGE renders nominate view and enclosing ACTION_PROMPT renders nominate prompt. Ajax-only subregions do not invent a view outcome. Nominate before coordinator admission so NONE detail and exhausted budgets do not alter request names; do not force model resolution or invent rendering callbacks to obtain a candidate. Serialized descriptors remain static and hold no live nomination state.

### Verify exported behavior and integrate documentation

Extend production Boot/agent compatibility fixtures with action, prompt, view, no-candidate, priority, suppressed-region and failure-followed-by-success cases. Assert actual entry names and identity/HTTP attributes and parentage; agent-owned servlet spans may differ in shape from Boot spans. Use focused unit tests for exhaustive selection/cleanup edges. Update existing how-to and observability content directly: recognizable names, static attributes, relevant Petclinic operations and route-based grouping, retaining Boot-first then agent instructions. No before/after table or new launcher scenarios.

## Risks / Trade-offs

- Final framework instrumentation may overwrite an early rename → verify final OTLP exports with both owners and adjust application timing through the existing bridge.
- Incorrect action eligibility may expose helper outcomes → characterize command matching, contributed actions and association wrappers with focused regression cases.
- Observation suppression may remove nominations → nominate independently before admission and test NONE plus exhausted budgets.
- Thread-local leaks may join unrelated requests → close/remove on every exit, test nested and failed requests on reused workers.
- Operation-name grouping changes → explain standard HTTP attributes for route grouping; keep complete static identity for compact-name collisions.
- Async continuation work occurs outside the synchronous nomination scope → retain redispatch safety and document scope; do not propagate mutable naming state across threads.

## Migration Plan

No application code or configuration migration. Deploy with the existing observation profile and tracing owner; semantic candidates automatically provide names. Saved views grouped by entry operation can use HTTP route/method attributes when route grouping is desired. Reverting this change restores supplied entry names without changing domain behavior.

## Open Questions

No blocking product decisions. Implementation must confirm final-name retention and canonical action signature availability on main using the specified exported regressions.
