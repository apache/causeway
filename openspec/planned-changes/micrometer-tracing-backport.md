# Micrometer Tracing Backport Roadmap

## Objective

Backport a narrow, low-risk subset of Causeway 4's Micrometer-based tracing to the Spring Boot 2.7 maintenance branch.
The target trace initially covers an agent-created HTTP span, Causeway root-interaction and action spans, and an agent-created JDBC span.

```text
OpenTelemetry agent: HTTP request
└── Micrometer: Causeway root interaction
    └── Micrometer: action invocation
        └── OpenTelemetry agent: JDBC
```

The OpenTelemetry Java agent owns automatic HTTP and JDBC instrumentation, the OpenTelemetry SDK, sampling, and exporting.
Causeway owns only its semantic spans and must join the agent's process-wide trace context through `GlobalOpenTelemetry`.

## Guiding Constraints

- Retain Spring Boot 2.7.18 and its managed `micrometer-core` 1.9.17.
- Do not override Boot's global Micrometer version.
- Keep the implementation compatible with Java 11.
- Do not create an application-owned `OpenTelemetrySdk` or a second OTLP exporter.
- Keep tracing disabled or no-op unless explicitly activated.
- Avoid the broad interaction-model refactoring included in the Causeway 4 `CAUSEWAY-3975` commit series.
- Exclude sensitive or unbounded tags such as action arguments, usernames, tenancy tokens, and bookmarks from the initial slice.

## Phase 1: Verify Boot 2.7 Compatibility

**OpenSpec change:** `verify-boot27-micrometer-tracing`

Establish an executable compatibility proof before changing production framework behavior.
Pin a compatible Micrometer Observation, Micrometer Tracing, OpenTelemetry bridge, and OpenTelemetry API version set without replacing Boot's Micrometer Core.
Construct the Micrometer bridge from the Java agent's `GlobalOpenTelemetry` instance.
Prove that a custom Micrometer span becomes the parent of an agent-generated JDBC span.
Verify safe behavior when tracing or the Java agent is absent.

**Exit gate:** dependency convergence passes and the exported trace proves the expected custom-span-to-JDBC-span parentage.

## Phase 2: Add the Boot 2.7 Observation Substrate

Add Java 11-compatible production integration around `ObservationRegistry` and Micrometer Tracing.
Provide a no-op default and opt-in activation using the `observation` Spring profile, matching the Causeway 4 convention where practical.
Keep the OpenTelemetry-specific bridge wiring at the integration boundary so core instrumentation depends on Micrometer rather than directly on OpenTelemetry.
Add lifecycle and failure tests for observation scope creation and cleanup.

**Exit gate:** Causeway applications behave unchanged when observation is inactive, while an activated application can create and export a framework-owned test observation through the agent.

## Phase 3: Instrument Root Interactions and Actions

Create one observation for each top-level Causeway interaction using the existing `InteractionServiceDefault` lifecycle without refactoring the interaction model.
Create one child observation around `MemberExecutorServiceDefault.invokeAction(...)`.
Use stable, low-cardinality observation names and put the action identifier and initiation mode in explicit tags.
Record failures on the active observation and close scopes reliably on success and failure.
Do not add custom Wicket, transaction, property-edit, execution-publishing, or JPA semantic observations in this phase.

**Exit gate:** an integration trace demonstrates `HTTP → root interaction → action → JDBC` as one correctly parented trace.

## Phase 4: Document and Operationalize

Document Java-agent installation, `JAVA_TOOL_OPTIONS`, service naming, OTLP endpoint configuration, sampling, and the `observation` profile.
Document that the agent owns the SDK and exporter and that applications must not configure a duplicate in-process exporter.
Document the initial span names, tags, privacy policy, troubleshooting steps, and the behavior when the agent is absent.

**Exit gate:** an application team can enable, disable, and diagnose the narrow tracing slice without changing Causeway framework code.

## Deferred Extensions

The following remain separate decisions after the narrow slice is proven in production:

- Wicket request-cycle observations beyond the agent's Servlet span.
- Nested Causeway interaction observations.
- Transaction observations.
- Property-edit and execution-publishing observations.
- JPA semantic observations and duration-based filtering.
- Runtime enablement changes without an application restart.
- Baggage propagation.
- Metrics derived from the same observations.
- Additional exporters or an application-owned OpenTelemetry SDK.
