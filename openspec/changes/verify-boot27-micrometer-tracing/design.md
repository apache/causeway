## Context

Causeway 4 instruments framework operations through Micrometer Observation and relies on modern Spring Boot auto-configuration to connect observations to OpenTelemetry.
This maintenance branch uses Spring Boot 2.7.18, Java 11, and Boot-managed Micrometer Core 1.9.17, so it has neither the required Observation generation nor the newer tracing auto-configuration.

Micrometer Observation is distributed as a separate artifact from Micrometer 1.10 onward, and Micrometer Tracing 1.0 uses that API.
The initial compatibility hypothesis is that `micrometer-observation` 1.10.x and `micrometer-tracing` 1.0.x can coexist with Boot-managed `micrometer-core` 1.9.17 when the global Micrometer version is not overridden.
The transcript motivating this change identifies `micrometer-observation` 1.10.13 and `micrometer-tracing` 1.0.12 as an example aligned pair, but the spike must verify and record the complete resolved dependency set rather than assuming compatibility.

The intended deployment uses the OpenTelemetry Java agent for Servlet and JDBC instrumentation and for OTLP export.
Causeway's future semantic observations must join the agent's process-wide context so that agent-created JDBC spans become descendants of Causeway-created spans.

This phase creates evidence only.
It does not change production Causeway instrumentation.

## Goals / Non-Goals

**Goals:**

- Prove that Micrometer Observation and Tracing can run on the repository's Spring Boot 2.7.18 and Java 11 baseline without replacing Boot-managed Micrometer Core 1.9.17.
- Prove that a Micrometer observation backed by `micrometer-tracing-bridge-otel` can use the Java agent's `GlobalOpenTelemetry` context.
- Prove that a JDBC span created by Java-agent instrumentation is a direct or expected descendant of the custom Micrometer span surrounding the JDBC operation.
- Produce reproducible evidence containing the selected versions, resolved dependency graph, execution command, and observed trace identifiers.
- Verify that the same compatibility harness remains safe when launched without the Java agent.

**Non-Goals:**

- Add production observation configuration or framework instrumentation.
- Add root-interaction, action, Wicket, transaction, property-edit, execution-publishing, or JPA semantic spans.
- Introduce Spring Cloud Sleuth.
- Override Boot's `micrometer.version` or upgrade `micrometer-core` globally.
- Construct an application-owned `OpenTelemetrySdk` or configure an application-owned OTLP exporter.
- Establish final production sampling, baggage, privacy, or dynamic-enable policies.

## Decisions

### Keep the proof isolated from production modules

The compatibility harness will live in regression-test or equivalent test-only code and will use test-scoped dependencies.
No production Causeway module will acquire Micrometer Observation, Micrometer Tracing, bridge, SDK, or exporter dependencies during this phase.

This isolates dependency experiments from the decommissioned maintenance branch's production artifacts and makes a failed compatibility result cheap to remove.

**Alternative considered:** add the dependencies directly to `core/config` and validate them through a production bean.
This is rejected for the spike because it changes published production artifacts before compatibility has been demonstrated.

### Retain Boot-managed Micrometer Core

The harness will add explicit, mutually compatible Observation and Tracing artifacts while leaving Boot's Micrometer Core dependency management unchanged.
It will not set or override the global `micrometer.version` property.

The initial candidate line is Micrometer Observation 1.10.x with Micrometer Tracing 1.0.x.
The implementation must inspect the resolved tree and pin all versions required to make the result reproducible.

**Alternative considered:** upgrade the complete Micrometer family to 1.10.x.
This is rejected because Spring Boot 2.7.18 was built and tested against Micrometer Core 1.9.17.

### Build the bridge from GlobalOpenTelemetry

The harness will construct Micrometer's OpenTelemetry-backed tracer from `GlobalOpenTelemetry.getTracer(...)` and install the corresponding tracing observation handler in an `ObservationRegistry`.
It will not call `OpenTelemetrySdk.builder()` or otherwise create another SDK.

This allows Micrometer-created spans to share the context and exporter installed by the Java agent.
It also models the boundary intended for the later production integration: Causeway code uses Micrometer while Boot 2.7-specific integration code understands the OpenTelemetry bridge.

**Alternative considered:** use the OpenTelemetry API directly from Causeway instrumentation.
This is rejected because it would diverge from the Causeway 4 Micrometer abstraction and make a later Spring Boot upgrade harder.

**Alternative considered:** configure a standalone OpenTelemetry SDK and exporter inside the test application.
This is rejected because it would not prove interoperability with the Java agent and could conceal a split-context or duplicate-exporter problem.

### Use a forked JVM with the real Java agent

The compatibility test will launch a small Java 11 child process with the actual OpenTelemetry Java agent, create a Micrometer observation around a real JDBC operation, and capture exported spans.
The preferred initial capture mechanism is the agent's logging trace exporter because it avoids requiring Docker, an external collector, or an application-owned SDK.
The test will identify the custom and JDBC spans and compare their trace and parent identifiers.

The same helper process will also be launched without `-javaagent` to verify that the wiring degrades safely rather than preventing startup or JDBC execution.

**Alternative considered:** create OTel spans directly in a unit test to simulate JDBC instrumentation.
This is rejected as the sole proof because it would not exercise the Java agent's instrumentation and context behavior.

**Alternative considered:** require Jaeger, Tempo, or an OpenTelemetry Collector through Testcontainers.
This is deferred because it would add Docker and external-image requirements to a narrow compatibility spike.

### Treat the result as a gate for later phases

A successful build alone is insufficient.
The phase succeeds only when the evidence demonstrates both dependency compatibility and the expected span parentage.
If the bridge and agent produce disconnected traces, subsequent production integration must not proceed until the context-sharing design is revised.

## Risks / Trade-offs

- **[Risk] Micrometer 1.10-era support artifacts may pull versions that conflict with Boot-managed Micrometer Core 1.9.17.** → Inspect and record the complete dependency tree, prohibit the global Micrometer override, and treat convergence failures as a no-go result rather than forcing exclusions without explanation.
- **[Risk] Micrometer Tracing bridge constructor signatures differ across patch versions.** → Pin one verified version set and keep all version-specific construction inside the isolated harness and, later, the Boot 2.7 integration boundary.
- **[Risk] A modern Java agent may use a different OpenTelemetry API generation from the bridge.** → Pin and report the agent version, execute the actual forked-JVM test, and rely on observed parentage rather than API compatibility assumptions.
- **[Risk] The logging exporter format may be inconvenient or unstable to parse.** → Keep span capture behind a test helper; if necessary, replace it with a local OTLP capture endpoint without changing the compatibility requirement.
- **[Risk] A no-agent run can return a no-op global implementation and therefore cannot prove export.** → Use it only to prove safe degradation; require the agent-enabled run for parentage evidence.
- **[Trade-off] The spike does not prove the complete future HTTP-to-JDBC trace.** → It proves the critical Micrometer-to-agent JDBC boundary first; later phases will add production interaction/action observations and end-to-end HTTP coverage.

## Migration Plan

1. Add only test-scoped dependency and harness changes.
2. Run dependency convergence and the compatibility test on Java 11.
3. Record the selected versions, commands, and span-parentage evidence.
4. If the proof succeeds, use its pinned recipe as input to the planned observation-substrate change.
5. If it fails, remove the isolated harness or retain it as failure evidence and reconsider the bridge, agent version, or lower-level Micrometer Tracing approach.

Rollback consists of removing the test-only harness and dependencies because this phase changes no production runtime behavior or public API.

## Open Questions

- Which exact OpenTelemetry Java agent version provides the best supported intersection with the selected Micrometer Tracing 1.0.x bridge while retaining Java 11 compatibility?
- Does the agent's logging exporter expose sufficiently stable trace and parent identifiers for an automated assertion, or will a small local OTLP capture endpoint be required?
- Which existing regression-test module provides the smallest reliable Spring Boot 2.7 and JDBC fixture, or is a dedicated compatibility-test module clearer?
