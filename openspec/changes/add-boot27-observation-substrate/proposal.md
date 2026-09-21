## Why

The completed Boot 2.7 compatibility spike proved that Micrometer Observation and Tracing can join the OpenTelemetry Java agent's context without replacing Spring Boot's managed Micrometer Core or creating a second telemetry runtime.
Causeway 2.x now needs a production observation substrate that exposes this validated integration safely, remains inactive by default, and gives later changes a stable framework-level API for semantic spans.

## What Changes

- Add the compatibility-tested Micrometer Observation, Micrometer Tracing, OpenTelemetry bridge, and OpenTelemetry API versions to managed production dependencies without overriding Spring Boot's global Micrometer version.
- Add Java 11-compatible Causeway observation integration and lifecycle support in core configuration.
- Provide a no-op `ObservationRegistry` when the `observation` Spring profile is inactive.
- Provide an agent-backed `ObservationRegistry` when the `observation` Spring profile is active, using `GlobalOpenTelemetry` rather than constructing an application-owned OpenTelemetry SDK or exporter.
- Exclude bridge-transitive OpenTelemetry SDK artifacts so the Java agent remains the only telemetry runtime and exporter.
- Verify successful startup and unchanged no-op behavior when observation is inactive or the Java agent is absent.
- Verify observation scope, failure, and cleanup behavior and agent-backed export of a framework-owned test observation.
- Do not yet instrument Causeway interactions, actions, Wicket requests, transactions, publishing, or persistence operations.

## Capabilities

### New Capabilities

- `boot27-observation-substrate`: Defines opt-in, agent-backed Micrometer observation infrastructure and safe inactive behavior for Causeway 2.x applications.

### Modified Capabilities

None.

## Impact

The change affects dependency management, core configuration, Java module declarations, and focused unit or integration tests.
Applications receive the Micrometer observation and tracing bridge classes required by the substrate, but tracing remains inactive unless the `observation` Spring profile is enabled.
The bridge uses the versions and exclusions validated by `boot27-micrometer-tracing-compatibility` and delegates SDK ownership, automatic HTTP and JDBC instrumentation, sampling, and export to the OpenTelemetry Java agent.
No public applib API, domain behavior, database schema, or existing semantic execution path changes in this phase.
The substrate becomes the prerequisite for the planned root-interaction and action instrumentation phase documented in `openspec/planned-changes/micrometer-tracing-backport.md`.
