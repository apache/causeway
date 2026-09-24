## 1. Agent Compatibility Evidence Gate

- [x] 1.1 Extend the agent-backed tracing fixture with a controlled foreground-filter `Span.updateName(...)` experiment and verify the exported final server-span name under OpenTelemetry Java agent 1.31.0.
- [x] 1.2 Verify in the same experiment that HTTP method, route, target, status, execution-mode classification, trace identity, span identity, and child parentage remain unchanged.
- [x] 1.3 Record the evidence-gate result; proceed with in-process renaming if the update survives, otherwise constrain implementation to bounded semantic attributes and document the collector-transform or supported-agent-extension fallback.

## 2. Request-Local Selection Infrastructure

- [x] 2.1 Add internal candidate kinds for action, prompt, and view with explicit priorities and canonical identifier metadata.
- [x] 2.2 Implement a stack-safe request-local coordinator that opens and closes nomination scopes, replaces lower-priority candidates, and retains the first equal-priority candidate.
- [x] 2.3 Add contextual-name builders for `act`, `prompt`, and `view` candidates using the existing case-preserving 50-character full-name, namespace-free, and truncation policy.
- [x] 2.4 Add unit tests for candidate priority, equal-priority determinism, nested-scope restoration, failure cleanup, bounded names, casing, and canonical metadata.

## 3. Foreground Entry-Span Integration

- [x] 3.1 Update `CausewayForegroundTraceFilter` to capture the current recording span, classify it as foreground, open nomination state before delegation, and finalize and clean up state in `finally`.
- [x] 3.2 Apply the selected name and bounded `causeway.trace.name` to the captured entry span, together with complete `causeway.action.id` or `causeway.object.type` metadata as applicable.
- [x] 3.3 Preserve downstream exceptions unchanged and make no-agent, non-recording-span, and no-candidate paths harmless no-ops that retain the agent-provided operation name.
- [x] 3.4 Extend core webapp tests for successful requests, failed requests, no candidate, non-recording spans, nested dispatch scopes, mutation timing, and unchanged classification behavior.

## 4. Trusted Candidate Nomination

- [x] 4.1 Nominate action candidates from runtime action instrumentation only when the domain-facing action matches the current top-level interaction command.
- [x] 4.2 Exclude mixed-in property and collection access, framework helper invocations, pass-through work, and nested wrapper actions from primary action nomination.
- [x] 4.3 Nominate prompt candidates from the enclosing Wicket action-prompt instrumentation using the complete domain-facing action identifier.
- [x] 4.4 Nominate view candidates from Wicket entity-page instrumentation using the complete logical object type, while excluding ambiguous Ajax region rendering.
- [x] 4.5 Add focused runtime and Wicket tests for declared and mixed-in top-level actions, calculated associations, nested wrapper invocations, prompt-only requests, view-only requests, Ajax requests, and combined action-response rendering.

## 5. End-to-End Regression Coverage

- [x] 5.1 Extend the tracing compatibility fixture and collector assertions for exported `act`, `prompt`, and `view` entry-span names and their canonical Causeway attributes.
- [x] 5.2 Add compatibility scenarios proving action-over-prompt-over-view priority, first-wins ties, and preservation of the original route name for unsupported requests.
- [x] 5.3 Assert that renamed entry spans retain standard HTTP attributes, `causeway.execution.mode=foreground`, existing trace parentage, and JDBC descendants.
- [x] 5.4 Verify failed requests still export the selected name and error telemetry while preserving application failure behavior.
- [x] 5.5 Verify startup and request execution without the Java agent remain safe and do not create an application-owned telemetry SDK, sampler, processor, registry, or exporter.

## 6. Operations Documentation and Verification

- [x] 6.1 Update the tracing operations guide with semantic trace-list examples, candidate priority, eligibility rules, backend limitations, and unchanged-route fallback behavior.
- [x] 6.2 Document the operationally breaking change to server operation-name grouping and advise route-oriented dashboards and alerts to use standard HTTP route and method attributes.
- [x] 6.3 Document the validated agent baseline and, if required by the evidence gate, the bounded `causeway.trace.name` collector-transform or supported-agent-extension fallback.
- [x] 6.4 Run focused core config, core runtime services, core webapp, Wicket, and tracing compatibility tests on the supported Java and Maven toolchain.
- [x] 6.5 Run strict OpenSpec validation and confirm every semantic trace-display-name scenario has automated coverage or an explicit operational verification step.
