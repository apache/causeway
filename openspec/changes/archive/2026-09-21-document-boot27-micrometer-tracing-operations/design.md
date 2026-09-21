## Context

The maintenance branch provides an opt-in observation substrate and semantic observations for root interactions and action invocations.
The validated deployment model uses OpenTelemetry Java agent 1.31.0 with Spring Boot 2.7.18 and Java 11, while the Java agent owns the SDK, HTTP and JDBC instrumentation, sampling, and export.
The target deployment platform is Azure Container Apps, whose optional managed OpenTelemetry agent is a collector and routing service rather than a replacement for the in-process Java instrumentation agent.
The team needs concise repository-local guidance rather than material intended for the Causeway website.

## Goals / Non-Goals

**Goals:**

- Provide a one-page runbook under `adoc/` for local evaluation with Jaeger.
- Specify the minimal Docker image change needed to package the Java agent.
- Specify application-level and managed-environment-level Azure Container Apps configuration.
- Clearly distinguish the OpenTelemetry Java agent from the Azure Container Apps managed OpenTelemetry agent.
- Provide enough verification and troubleshooting information for the maintenance team to prove the semantic trace ancestry.

**Non-Goals:**

- Add the guide to Antora or any published website navigation.
- Add or change production code, dependencies, runtime defaults, or public APIs.
- Provide a complete Azure infrastructure template for a specific application or observability vendor.
- Document an application-owned OpenTelemetry SDK or duplicate exporter.
- Promise semantic spans beyond root interactions and action invocations.

## Decisions

### Keep one repository-local runbook

The implementation will add `adoc/micrometer-tracing-operations.adoc` as a concise one-page runbook.
It will use three primary sections matching the team's workflow: local laptop, Docker image, and Azure Container Apps runtime.
This location follows the repository's existing internal build and operational notes without implying that the file is part of the Antora website.

An Antora page was rejected because this branch's guidance will not be published on the website.
A release-note page was rejected because the runbook must remain easy for the maintenance team to find after the release date.

### Package the Java agent in the image but configure it at runtime

The Docker example will copy the validated agent JAR to a stable path such as `/opt/opentelemetry/opentelemetry-javaagent.jar`.
The image will not bake the OTLP endpoint, service name, sampling rate, or Spring profile into an environment-specific layer.
The runtime platform will attach the JAR through `JAVA_TOOL_OPTIONS` and supply all deployment-specific settings.
No additional listening port is required in the application container for trace export.

Downloading the agent at container startup was rejected because it adds network availability and artifact-integrity risks to every replica start.
Baking all telemetry settings into the image was rejected because the same image must move safely between local and Azure environments.

### Use Jaeger all-in-one for local evaluation

The local procedure will run a pinned Jaeger all-in-one container exposing OTLP HTTP on 4318 and the UI on 16686.
The application will use the validated Java agent with traces exported over `http/protobuf`, metrics and logs exporters disabled, always-on sampling for local testing, and the `observation` profile active.
The procedure will tell the developer to invoke an HTTP action that performs JDBC work and then inspect the expected semantic ancestry in Jaeger.

### Treat the two agents as separate layers

The OpenTelemetry Java agent runs inside the application JVM and supplies bytecode instrumentation, the SDK, sampling, and OTLP export.
The Azure Container Apps managed OpenTelemetry agent runs at the managed-environment level and receives OTLP data before routing it to Application Insights or another configured destination.
When the managed agent is enabled, Azure automatically injects `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_PROTOCOL=grpc`, and `OTEL_RESOURCE_ATTRIBUTES`; the application must not override those values unless deliberately bypassing the managed agent.
The Java agent JAR is still required because the managed Azure agent does not instrument the application JVM.

### Keep application and environment settings explicit

The Container App revision will set `JAVA_TOOL_OPTIONS`, include the `observation` Spring profile, set `OTEL_SERVICE_NAME`, select the OTLP traces exporter, disable metrics and logs export for this tracing-only slice, and set a production sampling policy.
The managed Container Apps environment will configure the managed OpenTelemetry destination and trace routing when that option is used.
If the managed agent is not used, the Container App will instead set the OTLP endpoint, protocol, and any authentication headers needed by the external collector, using secret references for sensitive values.
The guide will note that changing Container App environment variables creates a new revision.

### Preserve the narrow semantic and privacy contract

The runbook will list `causeway.root.interaction` and `causeway.action.invocation`, plus `causeway.action.id` and `causeway.execution.initiatedBy`.
It will show the ordered ancestry `HTTP → causeway.root.interaction → causeway.action.invocation → JDBC` while allowing automatic intermediary spans.
It will state that Causeway semantic instrumentation does not capture arguments, results, target identifiers, user names, tenancy tokens, or other instance-specific application data.

## Risks / Trade-offs

- [Risk] Developers confuse the Java agent with the Azure managed agent → Use separate definitions and state that both are needed when Azure provides the collector path.
- [Risk] Azure managed OpenTelemetry behavior changes → Link to the Microsoft documentation and state the currently relevant injected variables and gRPC constraint rather than copying a full infrastructure template.
- [Risk] Agent or Jaeger examples silently drift → Pin example versions and direct upgrades through the Java 11 regression test.
- [Risk] `SPRING_PROFILES_ACTIVE` replaces application-specific profiles → Show either appending `observation` to the existing active profile list or using the application's established profile composition mechanism.
- [Risk] Production always-on sampling creates excessive volume → Use always-on only for local evaluation and require an explicit production sampler and rate.
- [Trade-off] A one-page runbook cannot cover every backend → Keep Azure managed-agent and direct-OTLP paths concrete while referring backend-specific destination setup to the platform owner.

## Migration Plan

No migration is required because tracing remains opt-in.
The same application image can be rolled out first with the agent JAR present but unattached, then activated in a new Container App revision through runtime settings.
Rollback removes the `observation` profile and `-javaagent` option or reverts to the prior revision.

## Open Questions

None.
