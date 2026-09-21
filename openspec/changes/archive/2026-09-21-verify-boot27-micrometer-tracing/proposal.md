## Why

Apache Causeway 2.x uses Spring Boot 2.7.18 and its managed Micrometer 1.9 generation, while the Causeway 4 tracing implementation relies on Micrometer Observation and Tracing facilities supplied by newer Spring Boot generations.
Before changing production framework code, the project needs an executable compatibility proof that a separately versioned Micrometer tracing stack can join the trace context owned by an OpenTelemetry Java agent without replacing Boot's managed Micrometer Core or creating a second OpenTelemetry SDK.

## What Changes

- Add an isolated compatibility proof for Micrometer Observation and Tracing on the existing Spring Boot 2.7 and Java 11 baseline.
- Pin and document a compatible set of Micrometer Observation, Micrometer Tracing, Micrometer OpenTelemetry bridge, and OpenTelemetry API versions without overriding Boot's managed `micrometer-core` version.
- Wire the Micrometer OpenTelemetry bridge to the agent-provided `GlobalOpenTelemetry` instance.
- Verify that a custom Micrometer span around a JDBC operation and the agent-generated JDBC span belong to one trace with the expected parent-child relationship.
- Verify safe no-op behavior when tracing is disabled or the OpenTelemetry Java agent is absent.
- Record the validated dependency and wiring decisions for the subsequent production backport phases.
- Make no production Causeway interaction, action, Wicket, transaction, or persistence instrumentation changes in this phase.

## Capabilities

### New Capabilities

- `boot27-micrometer-tracing-compatibility`: Defines the compatibility evidence required before Micrometer-based tracing is integrated into Causeway 2.x production modules.

### Modified Capabilities

None.

## Impact

The change affects only compatibility-test or spike code, its test dependencies, and supporting documentation.
It evaluates new Micrometer Observation, Micrometer Tracing, Micrometer OpenTelemetry bridge, and OpenTelemetry API dependencies while retaining Spring Boot 2.7.18's managed Micrometer Core 1.9.17.
It must not add an application-owned OpenTelemetry SDK or exporter, change public Causeway APIs, or alter production request and interaction behavior.
The result is a go/no-go decision and a pinned integration recipe for the later framework-substrate and semantic-instrumentation changes described in `openspec/planned-changes/micrometer-tracing-backport.md`.
