# boot27-micrometer-tracing-compatibility Specification

## Purpose
TBD - created by archiving change verify-boot27-micrometer-tracing. Update Purpose after archive.
## Requirements
### Requirement: Preserve the Spring Boot Micrometer baseline
The compatibility proof SHALL retain Spring Boot 2.7.18's managed Micrometer Core 1.9.17 while resolving an explicitly pinned and internally compatible Micrometer Observation and Tracing stack.
The proof MUST NOT override the global `micrometer.version` property.

#### Scenario: Resolve the compatibility dependency graph
- **WHEN** the compatibility module's dependency graph is resolved
- **THEN** `micrometer-core` resolves to the Spring Boot 2.7.18-managed version and the separately pinned Observation, Tracing, and OpenTelemetry bridge artifacts resolve without unexplained convergence conflicts

### Requirement: Use the Java agent's OpenTelemetry runtime
The compatibility proof SHALL construct the Micrometer OpenTelemetry bridge from the process-wide `GlobalOpenTelemetry` instance supplied by the OpenTelemetry Java agent.
It MUST NOT construct a second `OpenTelemetrySdk` or configure a second trace exporter in application code.

#### Scenario: Run with the OpenTelemetry Java agent
- **WHEN** the compatibility harness is launched with the pinned OpenTelemetry Java agent
- **THEN** Micrometer observations use the agent-owned OpenTelemetry context and exporter

### Requirement: Prove Micrometer-to-agent span parentage
The compatibility proof SHALL execute a real JDBC operation inside a custom Micrometer observation and SHALL verify that the custom span and agent-generated JDBC span belong to the same trace with the expected parent-child relationship.

#### Scenario: Observe a JDBC operation
- **WHEN** the agent-enabled compatibility harness executes JDBC work inside the custom Micrometer observation
- **THEN** the exported JDBC span has the same trace identifier as the custom span and identifies the custom span as its parent or expected semantic ancestor

### Requirement: Degrade safely without the Java agent
The compatibility harness SHALL remain executable when the OpenTelemetry Java agent is absent.
Absence of the agent MUST NOT prevent application startup or completion of the JDBC operation.

#### Scenario: Run without the OpenTelemetry Java agent
- **WHEN** the compatibility harness is launched without `-javaagent`
- **THEN** the harness starts and completes its JDBC operation without requiring an application-owned OpenTelemetry SDK or exporter

### Requirement: Produce reproducible compatibility evidence
The compatibility proof SHALL record the Java version, Spring Boot version, Micrometer versions, OpenTelemetry bridge and agent versions, resolved dependency graph, execution command, and span-parentage result.

#### Scenario: Review successful proof evidence
- **WHEN** the compatibility proof passes
- **THEN** a maintainer can reproduce the proof and identify the exact validated dependency and runtime configuration from the recorded evidence

#### Scenario: Review failed proof evidence
- **WHEN** dependency compatibility or span parentage cannot be demonstrated
- **THEN** the evidence identifies the failed gate and the project does not treat the tracing backport as ready for production integration
