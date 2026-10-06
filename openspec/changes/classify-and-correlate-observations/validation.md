# Classification and correlation validation

Validated 2026-10-06 on `CAUSEWAY-4068-v4` (CAUSEWAY-4068). Planning checkpoint: `94a6d4011cb`. Initial implementation was committed as `670607d175f`; archival remains pending. The fixed-key follow-up is recorded separately below.

## Toolchain

- Eclipse Temurin Java 25.0.4, Maven 3.9.13, macOS aarch64.
- Checkout BOM: Spring Boot 4.2.0-M1, Micrometer Observation 1.18.0-M1, Micrometer Tracing 1.8.0-M1, OpenTelemetry API/SDK 1.64.0.
- OpenTelemetry Java agent 2.31.1; servlet fixture uses Tomcat 11.0.24.
- All Maven commands below used `JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem`. Dependencies were available offline. Socket tests ran with permission to bind local ports.

## Initial implementation validation (before fixed-key simplification)

```sh
mvn -o -pl core/webapp,core/runtimeservices,extensions/core/commandlog/applib,viewers/wicket/viewer -am \
  -Dtest=CausewayTraceClassifierTest,CausewayObservationAutoConfigurationTest,InteractionServiceObservationTest,CausewayForegroundTraceFilterTest,RunBackgroundCommandsJobTraceClassificationTest,TelemetryRequestCycleTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
```

Passed: 16 configuration tests, 7 interaction tests, 3 servlet-filter tests, 2 Wicket lifecycle tests and the original 2 job tests. After adding the third job test, reran that module:

```sh
mvn -o -f extensions/core/commandlog/applib/pom.xml \
  -Dtest=RunBackgroundCommandsJobTraceClassificationTest test
```

Passed: all 3 job tests. Total distinct focused cases: **31**.

```sh
mvn -o -f regressiontests/tracing-compatibility/pom.xml test
```

Passed: **9** tests (7 exported compatibility tests and 2 JPA policy tests). The new `servletAndQuartzEntryClassificationAndCorrelation` case launches four child JVMs: Boot and agent, each with default and custom attribute keys. Each exports four HTTP requests (failure followed by success on a single servlet worker) and a real Quartz invocation of the production background job. Assertions cover:

- Foreground on each HTTP entry, never its security-like or interaction children; exactly one framework root per request and correct ancestry.
- Background on the supplied Boot job entry or actual agent Quartz span, with no additional classifier-created span.
- Production command executor replacement of interaction identity from a command DTO; final replay UUID on the root interaction.
- Six root interactions: four requests, pending-command lookup and replay. Five distinct traces, with the two job interactions sharing the job trace.
- Root-only correlation, valid UUIDs, unchanged identity omission, custom key replacing the default, and one failed request interaction.
- Existing agent/Boot/JPA export, filtering, inactive-profile and no-exporter checks remain passing.

```sh
mvn -o -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am \
  -Pobservation -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true package
```

Passed: Petclinic and its webcomponents reactor package with the shared agent tracer. Java tests were skipped for this packaging check; the reactor's configured JavaScript checks also ran successfully.

Generated `core/config/target/classes/META-INF/spring-configuration-metadata.json` contains `causeway.execution.mode.key`, type String, default `causeway.execution.mode`. Binding tests cover default, override, empty/whitespace rejection, inactive profile, missing tracer/span and single/primary/ambiguous tracers.

Both changed AsciiDoc guides were converted to HTML with the installed Antora Asciidoctor processor. All 7 YAML examples were parsed with `js-yaml`. The documented agent configuration mirrors the compiled sample. `git diff --check` and `openspec validate classify-and-correlate-observations --strict` passed.

## Initial implementation findings

Boot's `WebMvcObservationAutoConfiguration` registers its filter at `HIGHEST_PRECEDENCE + 1`; the new filter uses `+ 2`. The agent configuration excludes that Boot auto-configuration, preventing a second HTTP observation from becoming the classifier's current span. JVM and Causeway Micrometer metrics remain available; Boot HTTP request observation meters are consequently absent in this agent configuration. Both guides explain the trade-off.

`CausewayExecutionPolicy` follows the existing standalone observation-policy record pattern. Root correlation is refreshed immediately before observation closure, including when replay or cleanup fails. Tests include deterministic ordinary IDs, nested/reused layers, a real command-executor publishing failure after identity replacement, transaction cleanup failure and subsequent work without leaked context.

The first new servlet fixture run exposed an import-order issue in the fixture: its fallback registry was registered before the external agent registry configuration. Importing the application bridge before Causeway's configuration resolved this; all final runs pass. The existing and sample bridge code expose the same tracer used by their observation handler, without constructing an SDK.

## Scope and limitations

- The servlet fixture uses real Tomcat instrumentation with representative Wicket, GraphQL, REST and static paths and controlled business work; it does not boot all real viewers. Existing Wicket lifecycle tests also pass.
- The job fixture uses the production job and command executor, but mocks command persistence and domain dispatch. It does not prove full persisted-command execution, deadlock retries or every Quartz deployment. Those algorithms were not changed.
- Async/error redispatch behavior is covered by filter tests, not a full asynchronous servlet export scenario.
- Petclinic was packaged; live browser, Jaeger and Prometheus/Grafana checks were not repeated in this run. The M3 guide provides the manual steps and distinguishes the fixture's private OTLP collector from local Jaeger.
- A full repository regression build was not run. No dependency versions, domain schemas, telemetry-service scripts or launcher selection flags changed.

## Fixed-key simplification (2026-10-06)

User review removed the key override entirely. `CausewayTraceClassifier.EXECUTION_MODE_KEY` is always `causeway.execution.mode`; `CausewayExecutionPolicy` and its binding/registration are deleted. The preceding sections record the original implementation's validation, not the current configuration contract.

```sh
mvn -o -pl core/config,core/webapp,extensions/core/commandlog/applib -am \
  -Dtest=CausewayTraceClassifierTest,CausewayObservationAutoConfigurationTest,CausewayForegroundTraceFilterTest,RunBackgroundCommandsJobTraceClassificationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true -Dmaven.javadoc.skip=true clean install
```

Passed: 21 tests (15 config, 3 servlet filter, 3 background job). Clean rebuild confirmed the deleted policy class and all `causeway.execution` configuration metadata are absent. Both operator guides render with Asciidoctor; the OpenSpec change passes strict validation and the diff passes whitespace checks. Toolchain is unchanged from above.

```sh
mvn -o -f regressiontests/tracing-compatibility/pom.xml test
```

Passed: 9 tracing/JPA tests. The entry-point test now runs one child JVM per trace owner (Boot and agent), asserting the fixed key for both servlet and Quartz entry spans. Total follow-up verification: 30 passing Java tests. This follow-up is recorded in the commit containing this validation update.
