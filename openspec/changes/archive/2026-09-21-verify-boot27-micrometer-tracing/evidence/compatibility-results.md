# Boot 2.7 Micrometer Tracing Compatibility Results

## Gate Result

The compatibility gate passes.
A Micrometer observation created through `micrometer-tracing-bridge-otel` joined the OpenTelemetry Java agent's global context, and an agent-instrumented H2 JDBC span was exported as a direct child of the Micrometer span.
The same fixture completed successfully without the Java agent.
The child fixture starts a non-web Spring Boot 2.7.18 application before creating the observation and executing JDBC work.

## Validated Environment

- Java runtime: Eclipse Temurin 11.0.29+7.
- Java compilation release: 11.
- Maven: 3.9.13.
- Spring Boot: 2.7.18.
- Micrometer Core: 1.9.17, retained from Spring Boot dependency management.
- Micrometer Observation: 1.10.13.
- Micrometer Tracing: 1.0.12.
- Micrometer OpenTelemetry bridge: 1.0.12.
- OpenTelemetry API: 1.19.0.
- OpenTelemetry Java agent: 1.31.0.
- OTLP protobuf model used only by the parent test collector: 1.0.0-alpha.

## Dependency Result

The module does not override `micrometer.version`.
Maven Enforcer dependency convergence passes.
The resolved relevant dependency graph is:

[source,text]
----
io.micrometer:micrometer-core:1.9.17:test
io.micrometer:micrometer-observation:1.10.13:test
+- io.micrometer:micrometer-commons:1.10.13:test
io.micrometer:micrometer-tracing:1.0.12:test
+- io.micrometer:context-propagation:1.0.6:test
io.micrometer:micrometer-tracing-bridge-otel:1.0.12:test
+- io.opentelemetry:opentelemetry-extension-aws:1.19.0:test
+- io.opentelemetry:opentelemetry-semconv:1.19.0-alpha:test
+- io.opentelemetry:opentelemetry-extension-trace-propagators:1.19.0:test
io.opentelemetry:opentelemetry-api:1.19.0:test
+- io.opentelemetry:opentelemetry-context:1.19.0:test
io.opentelemetry.javaagent:opentelemetry-javaagent:1.31.0:test
io.opentelemetry.proto:opentelemetry-proto:1.0.0-alpha:test
----

The bridge's transitive OpenTelemetry SDK artifacts are explicitly excluded.
The child fixture constructs no `OpenTelemetrySdk` and configures no application-owned exporter.
The Java agent owns the SDK and OTLP exporter used by the agent-enabled process.

## Span Parentage Evidence

A representative successful reactor run produced:

[source,text]
----
traceId=a07c6b6a4f5d845ec8a1e92bf99b341d
customSpanId=293efc65278bf22c
jdbcSpanId=1a0b97937c96a33b
jdbcParentSpanId=293efc65278bf22c
jdbcName=causeway-tracing
----

The JDBC span's parent identifier equals the custom Micrometer span identifier, and both spans have the same trace identifier.
The assertion is performed against OTLP protobuf received by a local test-only HTTP collector rather than inferred from logging text.

## Reproduction

Run the compatibility module in the project reactor with:

[source,bash]
----
mvn -pl regressiontests/tracing-compatibility -am test \
    -Dmodule-regressiontests \
    -Drevision=2.2.0-SNAPSHOT \
    -Dmaven.compiler.release=11 \
    -Dmaven.compiler.proc=full
----

Inspect the relevant resolved dependency graph with:

[source,bash]
----
mvn -f regressiontests/tracing-compatibility/pom.xml dependency:tree \
    -Drevision=2.2.0-SNAPSHOT \
    -Dscope=test \
    '-Dincludes=io.micrometer:*,io.opentelemetry:*,io.opentelemetry.javaagent:*,io.opentelemetry.proto:*,com.google.protobuf:*'
----

## Integration Recipe for the Next Phase

- Retain Boot-managed Micrometer Core 1.9.17.
- Add Micrometer Observation 1.10.13 and Micrometer Tracing 1.0.12 without overriding the global Micrometer version.
- Use `micrometer-tracing-bridge-otel` 1.0.12 with OpenTelemetry API 1.19.0.
- Exclude bridge-transitive OpenTelemetry SDK artifacts because the Java agent owns the SDK.
- Construct `OtelTracer` from `GlobalOpenTelemetry.getTracer(...)` through `OtelCurrentTraceContext` and `OtelBaggageManager`.
- Register `DefaultTracingObservationHandler` with an `ObservationRegistry`.
- Let the Java agent own automatic JDBC and HTTP instrumentation and trace exporting.
- Preserve no-agent behavior as a safe no-op path.

No production Causeway module or public API was changed by this compatibility phase.
