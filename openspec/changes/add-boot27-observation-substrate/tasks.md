## 1. Add the Validated Production Dependencies

- [ ] 1.1 Add centrally managed properties and dependency entries for Micrometer Observation 1.10.13, Micrometer Tracing and OpenTelemetry bridge 1.0.12, and OpenTelemetry API 1.19.0 without overriding `micrometer.version`.
- [ ] 1.2 Add the required dependencies to `core/config`, excluding bridge-transitive OpenTelemetry SDK artifacts and keeping the Java agent and OTLP collector dependencies test-only.
- [ ] 1.3 Update Java module declarations for the new production API usage and verify that Micrometer Core remains at Spring Boot 2.7.18's managed 1.9.17 version with dependency convergence passing.

## 2. Implement the Observation Substrate

- [ ] 2.1 Add profile-specific core configuration that supplies a qualified `ObservationRegistry.NOOP` when `observation` is inactive and a qualified real registry when it is active.
- [ ] 2.2 Configure the active registry with `OtelCurrentTraceContext`, `OtelBaggageManager`, an `OtelTracer` obtained from `GlobalOpenTelemetry.getTracer(...)`, and `DefaultTracingObservationHandler` without constructing an SDK or exporter.
- [ ] 2.3 Add the internal Causeway observation integration that exposes no-op status and consistent creation of unstarted observations or observation providers from the qualified registry.
- [ ] 2.4 Add Java 11-compatible lifecycle support for start, scope, `Throwable` recording, idempotent cleanup, and clearing retained state.
- [ ] 2.5 Import the observation configuration from `CausewayModuleCoreConfig` and document the internal contract for later framework instrumentation.

## 3. Verify Profile and Lifecycle Behavior

- [ ] 3.1 Add focused tests proving that the inactive profile selects the Causeway no-op registry, creates no tracing bridge, and ignores unrelated application registries.
- [ ] 3.2 Add focused tests proving that the active profile creates the real Causeway registry and starts safely without the Java agent.
- [ ] 3.3 Add lifecycle tests covering successful completion, recorded failures, partial initialization, repeated cleanup, and removal of the current observation from the registry.
- [ ] 3.4 Update the tracing compatibility child fixture to obtain and use the production Causeway integration instead of constructing duplicate Micrometer bridge wiring.
- [ ] 3.5 Run the real-agent compatibility test and verify that the production-substrate observation remains the parent or expected semantic ancestor of the agent-generated JDBC span.

## 4. Validate the Production Integration

- [ ] 4.1 Run targeted `core/config` tests and representative application-context startup tests with and without the `observation` profile.
- [ ] 4.2 Run the tracing compatibility module in the regression-test reactor on the Java 11 compilation target and capture the resolved dependency graph and span-parentage evidence.
- [ ] 4.3 Confirm that production source contains no `OpenTelemetrySdk` construction, exporter configuration, semantic framework spans, or public applib API changes.
- [ ] 4.4 Record the validated internal integration recipe for Phase 3 and validate the completed OpenSpec change.
