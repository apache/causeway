## Why

The Boot 2.7 observation substrate and initial semantic spans are implemented, but maintenance-branch developers do not yet have a concise runbook for trying them locally or deploying them to Azure Container Apps.
The material will not be published as website documentation, so the team needs a repository-local one-page guide focused on the actual development and deployment workflow.

## What Changes

- Add a repository-local one-page runbook for downloading the validated OpenTelemetry Java agent, running Jaeger locally, starting an application, and inspecting traces.
- Document the Docker image change needed to include the agent JAR at a stable path without baking environment-specific exporter configuration into the image.
- Document Azure Container Apps runtime settings for attaching the Java agent, activating the `observation` profile, naming the service, selecting traces-only export, and configuring sampling.
- Explain how the Azure Container Apps managed OpenTelemetry agent acts as the OTLP collector and injects its endpoint, protocol, and resource attributes when configured at the managed-environment level.
- Describe the alternative of exporting directly to an external OTLP endpoint when the managed OpenTelemetry agent is not used.
- Distinguish the in-process OpenTelemetry Java agent from the Azure Container Apps managed OpenTelemetry agent and preserve Java-agent ownership of the SDK and automatic instrumentation.
- Include expected span names, privacy boundaries, verification checks, and concise troubleshooting guidance.

## Capabilities

### New Capabilities
- `boot27-micrometer-tracing-operations`: A repository-local runbook for local evaluation, Docker image preparation, and Azure Container Apps runtime configuration of the Boot 2.7 tracing slice.

### Modified Capabilities

None.

## Impact

The change adds a single developer-facing AsciiDoc file under `adoc/` and does not affect Antora navigation or published website content.
It changes no production code, public API, dependency, or runtime default.
The intended readers are maintenance-branch developers and the team responsible for the application's Docker image and Azure Container Apps environment.
