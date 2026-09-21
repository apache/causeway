## 1. Establish the Repository-Local Runbook

- [ ] 1.1 Add `adoc/micrometer-tracing-operations.adoc` with the standard license header and a concise scope statement for maintenance-branch developers.
- [ ] 1.2 Structure the one-page runbook around local laptop evaluation, Docker image changes, and Azure Container Apps runtime configuration without adding it to Antora navigation.
- [ ] 1.3 Cross-check every version, profile, span name, attribute, and exporter setting against the BOM, production substrate, and Java 11 regression fixture.

## 2. Document Local Laptop Evaluation

- [ ] 2.1 Add commands to download or locate OpenTelemetry Java agent 1.31.0 and run a pinned Jaeger all-in-one container with OTLP HTTP and UI ports.
- [ ] 2.2 Add a copyable Java 11 startup example using the `observation` profile, local service name, traces-only OTLP HTTP/protobuf export, and local always-on sampling.
- [ ] 2.3 Explain how to generate an HTTP-triggered action with JDBC work and find the expected semantic ancestry in the Jaeger UI.

## 3. Document the Docker Image Change

- [ ] 3.1 Add a Dockerfile fragment that copies the validated Java agent JAR to `/opt/opentelemetry/opentelemetry-javaagent.jar`.
- [ ] 3.2 State which settings remain runtime concerns, that no extra inbound application port is required, and why the agent should not be downloaded at replica startup.
- [ ] 3.3 Document tracing activation and rollback using the same image without application code changes.

## 4. Document Azure Container Apps Operation

- [ ] 4.1 Add the Container App revision settings for `JAVA_TOOL_OPTIONS`, preserving existing Spring profiles while adding `observation`, service naming, traces-only export, and production sampling.
- [ ] 4.2 Explain the separate roles of the in-process Java agent and Azure Container Apps managed OpenTelemetry agent.
- [ ] 4.3 Document the managed-environment destination and trace-routing responsibility plus Azure's injected OTLP endpoint, gRPC protocol, and resource attributes.
- [ ] 4.4 Document the direct external-OTLP alternative, including protocol, endpoint, authentication, secret references, and the creation of a new revision when environment variables change.

## 5. Document Boundaries and Troubleshooting

- [ ] 5.1 Document agent ownership of the SDK, instrumentation, sampling, and export, and warn against Boot 4 starters, application-owned SDKs, Micrometer OTLP registries, and duplicate exporters.
- [ ] 5.2 Document stable semantic names and attributes, ordered parentage with possible intermediary spans, privacy exclusions, and the deliberately narrow initial scope.
- [ ] 5.3 Add concise troubleshooting checks for local Jaeger, the Azure managed-agent path, and direct external OTLP export.
- [ ] 5.4 Validate AsciiDoc formatting and external links, run strict OpenSpec validation, and confirm that no Antora navigation, production code, public API, dependency, or runtime default changed.
