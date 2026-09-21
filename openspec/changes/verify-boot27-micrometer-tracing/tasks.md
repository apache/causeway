## 1. Establish the Compatibility Fixture

- [ ] 1.1 Select the smallest existing regression-test module, or create a dedicated test-only module, that can run a Java 11 Spring Boot 2.7.18 application with a real JDBC operation.
- [ ] 1.2 Add test-scoped, explicitly pinned candidate dependencies for Micrometer Observation, Micrometer Tracing, the Micrometer OpenTelemetry bridge, the OpenTelemetry API, and the OpenTelemetry Java agent without overriding `micrometer.version`.
- [ ] 1.3 Capture and review the resolved dependency tree, confirming that `micrometer-core` remains at Spring Boot 2.7.18's managed version and documenting every required exclusion or alignment decision.

## 2. Build the Agent Interoperability Harness

- [ ] 2.1 Add a Java 11-compatible child-process fixture that performs a real JDBC operation inside a custom Micrometer observation.
- [ ] 2.2 Configure the fixture's `ObservationRegistry` and tracing observation handler with a Micrometer `OtelTracer` built from `GlobalOpenTelemetry.getTracer(...)` without constructing an `OpenTelemetrySdk`.
- [ ] 2.3 Add a parent-process test harness that launches the fixture with the pinned OpenTelemetry Java agent and captures the custom and agent-generated JDBC spans through the agent-owned exporter.
- [ ] 2.4 Add span parsing and assertions that verify the custom and JDBC spans share a trace identifier and that the JDBC span names the custom span as its parent or expected semantic ancestor.

## 3. Verify Safe Degradation and Build Compatibility

- [ ] 3.1 Launch the same fixture without `-javaagent` and verify that startup and JDBC execution complete without an application-owned SDK or exporter.
- [ ] 3.2 Run the compatibility tests using the repository's Java 11 compilation target and relevant regression-test Maven profile.
- [ ] 3.3 Run dependency convergence and targeted module verification, resolving failures without changing Spring Boot's managed Micrometer Core version.

## 4. Record the Gate Result

- [ ] 4.1 Record the validated Java, Spring Boot, Micrometer, OpenTelemetry bridge, and Java-agent versions together with the resolved dependency graph and reproduction commands.
- [ ] 4.2 Record the custom-span and JDBC-span trace identifiers and parentage evidence, or document the failed compatibility gate if the expected relationship cannot be demonstrated.
- [ ] 4.3 Summarize the go/no-go result and the pinned integration recipe that the planned Boot 2.7 observation-substrate change must use.
- [ ] 4.4 Validate the completed OpenSpec change and confirm that no production Causeway module or public API was changed by the compatibility phase.
