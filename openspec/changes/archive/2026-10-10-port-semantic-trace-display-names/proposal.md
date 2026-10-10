## Why

Foreground traces currently identify HTTP routes even when Causeway knows the action invoked, prompt opened or object type viewed. Forward-port CAUSEWAY-4063 now that classification, semantic member identity and Wicket observations are available, so operators can recognize domain work directly in Jaeger trace lists.

## What Changes

- Select a bounded, case-preserving foreground entry-span display from trusted nominations: `act` over `prompt` over `view`, first candidate winning equal-priority ties.
- Retain full static identity in `causeway.action.id` or `causeway.object.type`, together with `causeway.trace.name`; preserve HTTP attributes and span ancestry.
- Nominate eligible top-level actions and Wicket page/prompt outcomes, independently of Wicket observation admission.
- Support both Boot-managed and agent-managed tracing through the existing shared tracer, with deterministic request cleanup and unchanged names when no candidate exists.
- Integrate verification guidance into the M3 how-to and observability guide; update roadmap step 7.

## Capabilities

### New Capabilities

- `semantic-trace-display-names`: Foreground entry naming, prioritized nominations, static identity and tracing-owner compatibility.

### Modified Capabilities

- `observation-execution-classification`: Allow semantic naming alongside foreground classification without changing its scope or ancestry.
- `wicket-region-observations`: Permit entry-name nominations and integrate their operator guidance while preserving region admission and lifecycles.

## Impact

Core configuration/observation integration, webapp foreground filter, runtime member executor, Wicket descriptors/behaviors, focused and exported-trace regression tests, and existing telemetry documentation. Source anchor: maintenance commit `e17c3088ac2`; planning baseline: local `81047dc9830` on `CAUSEWAY-4059-v4`. No new applib API, configuration switch, SDK, exporter or telemetry launcher option. Exported foreground span display names change when a semantic candidate exists; stable observation operation names and physical invocation identifiers remain intact. Background naming, application-defined nominations, other-viewer page/prompt instrumentation and general filtering are outside scope.
