## Context

Petclinic and the tracing fixture duplicate an OtelTracer/ObservationRegistry bridge. Manual exclusions select the agent as trace owner. Core configuration already selects the single or primary registry and tracer.

## Goals / Non-Goals

Goals: configuration-only adoption, one trace owner, preserved Boot defaults and Micrometer metrics, tested production bridge.
Non-goals: downloading or attaching the agent automatically; span duration filtering; changing instrumentation boundaries.

## Decisions

Use a guarded Boot auto-configuration with optional Micrometer OTel bridge dependencies. With both profiles active, create missing tracer and registry beans; the tracer wraps GlobalOpenTelemetry and creates no SDK or exporter. An application registry retains responsibility for its handlers. The existing fallback registry is limited to observation without agent, avoiding early duplicate registration.

Use an EnvironmentPostProcessor after ConfigData to merge the supported baseline's tracing/WebMVC exclusions whenever the agent profile is active, including when Causeway observations are inactive. This prevents a competing Boot trace owner independently of Causeway activation. Disable Boot OTEL environment mapping and trace export for this profile, retaining Micrometer metrics despite OTEL_METRICS_EXPORTER=none. Preserve unrelated application exclusions. A missing bridge dependency with both profiles active must give an actionable error.

Use real agent and Boot exported-trace fixtures without application bridge copies or agent exclusions to verify ownership, ancestry, classification and inactive behavior.

## Risks / Trade-offs

- Boot class names evolve → pin exclusions to current baseline and validate real Boot startup.
- Agent profile without an attached agent → bridge uses the global API (possibly no-op); documentation makes attachment explicit and no SDK is constructed.
- HTTP observation exclusion removes Boot HTTP request meters → document this; JVM and Causeway Micrometer meters remain available.
- Custom registry handlers can target another pipeline → preserve customization and document responsibility to share the selected tracer.

## Migration Plan

Delete app-owned bridge and manual agent exclusions; retain the BOM-managed bridge dependency, attach the agent and activate observation,agent. To return to Boot mode remove attachment and agent profile. Existing explicit exclusions remain compatible.
