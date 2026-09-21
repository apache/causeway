# boot27-observation-substrate Specification

## Purpose
TBD - created by archiving change add-boot27-observation-substrate. Update Purpose after archive.
## Requirements
### Requirement: Preserve the validated dependency baseline
The production observation substrate SHALL retain Spring Boot 2.7.18's managed Micrometer Core 1.9.17 and SHALL use the Observation, Tracing, OpenTelemetry bridge, and OpenTelemetry API versions validated by the Boot 2.7 compatibility proof.
The substrate MUST NOT override the global `micrometer.version` property.

#### Scenario: Resolve production observation dependencies
- **WHEN** the affected production modules' dependency graph is resolved
- **THEN** Micrometer Core remains at 1.9.17, the validated observation and tracing artifacts resolve without convergence errors, and bridge-transitive OpenTelemetry SDK artifacts are absent

### Requirement: Keep Causeway observation inactive by default
The substrate SHALL provide a Causeway-owned no-op observation registry whenever the `observation` Spring profile is inactive.
An unrelated application-defined observation registry MUST NOT activate Causeway framework observations.

#### Scenario: Start an application without the observation profile
- **WHEN** a Causeway application starts without the `observation` Spring profile
- **THEN** the Causeway observation integration is available, reports no-op behavior, and does not construct the OpenTelemetry tracing bridge

#### Scenario: Create an observation while inactive
- **WHEN** framework code creates and executes an observation through the inactive Causeway integration
- **THEN** the observed operation executes normally without exporting a span or retaining a current observation

### Requirement: Activate agent-backed observation explicitly
The substrate SHALL create a real Causeway-owned observation registry when the `observation` Spring profile is active.
The active registry SHALL use Micrometer's OpenTelemetry bridge and an API tracer obtained from `GlobalOpenTelemetry`.

#### Scenario: Start an observed application with the Java agent
- **WHEN** a Causeway application starts with the `observation` profile and the OpenTelemetry Java agent attached
- **THEN** the Causeway registry uses the agent-installed global trace context and can export a framework-owned observation through the agent

### Requirement: Preserve single ownership of the telemetry runtime
The substrate MUST NOT construct an `OpenTelemetrySdk`, configure an application-owned trace exporter, or package the OpenTelemetry Java agent as a production runtime dependency.
The Java agent SHALL remain responsible for the SDK, automatic infrastructure instrumentation, sampling, and export.

#### Scenario: Inspect production telemetry wiring
- **WHEN** the production observation configuration and dependency graph are inspected
- **THEN** they contain no application-owned SDK construction, no application-owned exporter configuration, and no production Java-agent dependency

### Requirement: Degrade safely when the Java agent is absent
The active observation substrate SHALL allow application startup and observation execution when the `observation` profile is active but the OpenTelemetry Java agent is absent.

#### Scenario: Start an observed application without the Java agent
- **WHEN** a Causeway application starts with the `observation` profile and without `-javaagent`
- **THEN** startup succeeds and framework observations complete safely through the no-op global OpenTelemetry implementation

### Requirement: Provide consistent framework observation creation
The substrate SHALL expose one Causeway framework integration that creates unstarted observations from the profile-selected Causeway registry and that can report whether the selected registry is no-op.
The integration MUST remain internal framework infrastructure rather than a public applib service.

#### Scenario: Inject the framework integration
- **WHEN** a core framework component receives the Causeway observation integration
- **THEN** it can create an observation without performing optional bean lookup or depending directly on OpenTelemetry

### Requirement: Close observation lifecycles reliably
The substrate SHALL provide lifecycle support that starts an observation, opens its scope, records any reported failure, closes the scope before stopping the observation, and clears retained lifecycle state.
Cleanup SHALL be safe when invoked defensively after partial initialization or prior cleanup.

#### Scenario: Complete a successful observation
- **WHEN** a started framework observation completes successfully
- **THEN** its scope and observation are closed exactly once and the registry no longer exposes it as current

#### Scenario: Complete a failing observation
- **WHEN** framework work reports a `Throwable`
- **THEN** the active observation records the failure and cleanup closes its scope and stops it without leaving current observation state behind

#### Scenario: Clean up a partial lifecycle
- **WHEN** cleanup runs without a successfully started observation or runs again after cleanup
- **THEN** cleanup completes without throwing or corrupting the registry's current observation

### Requirement: Verify the production substrate through the real agent
The existing tracing compatibility harness SHALL exercise the production Causeway observation integration rather than duplicate its bridge construction.

#### Scenario: Export a production-substrate test observation
- **WHEN** the compatibility harness runs a framework-owned test observation with the Java agent attached
- **THEN** the observation and the agent-generated JDBC span share a trace and the JDBC span identifies the observation span as its parent or expected semantic ancestor
