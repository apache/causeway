# boot27-micrometer-tracing-operations Specification

## Purpose
TBD - created by archiving change document-boot27-micrometer-tracing-operations. Update Purpose after archive.
## Requirements

### Requirement: Provide a repository-local one-page runbook
The repository SHALL contain a concise developer runbook under `adoc/` covering local evaluation, Docker image preparation, and Azure Container Apps operation.
The runbook MUST remain independent of Antora and published website navigation.
It SHALL identify Spring Boot 2.7.18, Java 11, and OpenTelemetry Java agent 1.31.0 as the validated baseline and SHALL direct version changes through compatibility verification.

#### Scenario: Find maintenance-branch tracing instructions
- **WHEN** a maintenance-branch developer needs to evaluate or deploy tracing
- **THEN** a single repository-local document provides the local, image, and Azure Container Apps procedures without requiring website documentation

#### Scenario: Evaluate an agent upgrade
- **WHEN** the team wants to use an OpenTelemetry Java agent version other than 1.31.0
- **THEN** the runbook directs the team to re-run Java 11 compatibility and semantic parentage verification before deployment

### Requirement: Explain local evaluation with Jaeger
The runbook SHALL provide copyable steps to obtain the validated Java agent, run a pinned Jaeger all-in-one container, start an application with the agent and `observation` profile, generate an HTTP-triggered action with JDBC work, and inspect the trace in Jaeger.
The local example SHALL configure traces through OTLP HTTP/protobuf, disable metrics and logs exporters, use a local-only always-on sampling policy, and assign an explicit service name.

#### Scenario: Try tracing on a laptop
- **WHEN** a developer follows the local procedure on a machine with Java 11 and Docker
- **THEN** Jaeger receives a trace from the application and exposes it through the local UI

#### Scenario: Inspect semantic ancestry
- **WHEN** the developer invokes an HTTP action that performs JDBC work
- **THEN** the runbook enables the developer to find the ordered ancestry `HTTP → causeway.root.interaction → causeway.action.invocation → JDBC`, allowing additional agent-created intermediary spans

### Requirement: Specify the Docker image change
The runbook SHALL show how to add the validated Java agent JAR to a stable path in the application image.
The image instructions MUST leave agent attachment, Spring-profile activation, OTLP destination, service name, credentials, and sampling as runtime configuration.
The runbook SHALL state that the application container requires no additional inbound port for OTLP trace export.

#### Scenario: Build one image for multiple environments
- **WHEN** the team builds the application image with the Java agent JAR included but no environment-specific telemetry settings baked in
- **THEN** the same image can run with tracing disabled locally or be activated by Azure Container Apps runtime configuration

#### Scenario: Roll back tracing
- **WHEN** operators remove the `-javaagent` option and `observation` profile or revert to an earlier Container App revision
- **THEN** the application runs without Causeway semantic tracing and requires no application code change

### Requirement: Document Azure Container Apps runtime configuration
The runbook SHALL separate settings applied to the Container App revision from settings applied to the Azure Container Apps managed environment.
The Container App settings SHALL cover `JAVA_TOOL_OPTIONS`, inclusion of the `observation` Spring profile, `OTEL_SERVICE_NAME`, traces exporter selection, disabling metrics and logs export for this slice, and an explicit production sampling policy.
The runbook SHALL warn that changing Container App environment variables creates a new revision and that existing Spring profiles must be preserved when adding `observation`.

#### Scenario: Activate an application revision
- **WHEN** operators deploy an image containing the agent JAR and apply the documented Container App environment settings
- **THEN** the JVM attaches the Java agent, Causeway semantic observation is active, the service is identifiable, and trace volume follows the configured production sampler

#### Scenario: Preserve existing application profiles
- **WHEN** the application already uses one or more active Spring profiles
- **THEN** the runbook directs operators to add `observation` without replacing the existing profile set

### Requirement: Explain the Azure managed-agent collector path
The runbook SHALL distinguish the in-process OpenTelemetry Java agent from the Azure Container Apps managed OpenTelemetry agent.
It SHALL explain that the Java agent instruments the JVM while the Azure managed agent receives and routes OTLP data at the managed-environment level.
For the managed-agent path, the runbook SHALL describe destination and trace-routing configuration on the managed environment and SHALL identify the automatically injected `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_PROTOCOL=grpc`, and `OTEL_RESOURCE_ATTRIBUTES` values.
It MUST warn against overriding those injected values unless intentionally bypassing the managed agent.

#### Scenario: Export through the Azure managed agent
- **WHEN** the managed Container Apps environment has OpenTelemetry trace routing configured to Application Insights or another supported destination
- **THEN** the Java agent exports over the Azure-injected gRPC OTLP endpoint and the managed agent forwards the traces to that destination

#### Scenario: Avoid confusing the two agents
- **WHEN** the Azure managed OpenTelemetry agent is enabled
- **THEN** the runbook states that the application image still requires the OpenTelemetry Java agent JAR for JVM instrumentation

### Requirement: Support a direct external OTLP path
The runbook SHALL describe direct export to an external OTLP collector when the Azure managed OpenTelemetry agent is not used.
That path SHALL identify endpoint, protocol, and authentication configuration and SHALL direct sensitive values to Azure Container Apps secret references rather than the image or plain repository content.

#### Scenario: Use an external collector
- **WHEN** the application does not use the Azure managed OpenTelemetry agent
- **THEN** the Container App revision supplies the external OTLP endpoint, supported protocol, and any required secret-backed authentication settings

### Requirement: Preserve telemetry ownership and privacy boundaries
The runbook SHALL state that the OpenTelemetry Java agent owns the SDK, automatic HTTP and JDBC instrumentation, sampling, and export.
It MUST warn against adding an application-owned SDK, Spring Boot 4 OpenTelemetry starter, Micrometer OTLP registry, or duplicate in-process exporter.
It SHALL document the `causeway.root.interaction` and `causeway.action.invocation` span names and the `causeway.action.id` and `causeway.execution.initiatedBy` attributes.
It MUST state that Causeway semantic instrumentation does not capture action arguments, results, target instance identifiers, user names, tenancy tokens, or other instance-specific application data.

#### Scenario: Prepare application dependencies
- **WHEN** the team enables tracing through the documented Java-agent path
- **THEN** it does not add another telemetry SDK or exporter to the application

#### Scenario: Review emitted semantic data
- **WHEN** the team reviews a Causeway action span
- **THEN** the runbook identifies the bounded metadata that is emitted and the instance-specific values that are deliberately excluded

### Requirement: Provide concise verification and troubleshooting
The runbook SHALL provide checks for Java and agent compatibility, agent attachment, Spring profile activation, service naming, endpoint and protocol, exporter selection, sampling, collector reachability, absent spans, duplicate spans, and unexpected parentage.
The checks SHALL cover both the Azure managed-agent path and direct external OTLP export.

#### Scenario: Automatic spans exist but semantic spans are absent
- **WHEN** HTTP or JDBC spans are exported but Causeway semantic spans are missing
- **THEN** the runbook directs the developer to verify activation of the `observation` profile

#### Scenario: No spans are exported in Azure
- **WHEN** no application traces reach the configured destination
- **THEN** the runbook directs the operator to verify Java-agent attachment, sampling, Azure-injected or explicitly configured OTLP settings, managed-environment trace routing, credentials, and destination reachability
