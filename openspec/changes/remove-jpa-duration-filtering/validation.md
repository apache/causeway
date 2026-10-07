# Validation: remove JPA duration filtering

Validated 2026-10-07 on `CAUSEWAY-4068-v4`. Planning checkpoint: `81e1bc7ccde`. Previous classification/correlation archive: `3b52d532fe7`.

## Toolchain

Eclipse Temurin Java 25.0.4, Maven 3.9.13, macOS aarch64. Checkout BOM: Boot 4.2.0-M1, Micrometer Observation 1.18.0-M1 / Tracing 1.8.0-M1, OpenTelemetry 1.64.0, Java agent 2.31.1. Maven commands use `JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem`; required dependencies are cached offline. Export tests have local socket access.

## Commands and results

```sh
mvn -o -pl core/config,core/runtimeservices,core/webapp,persistence/jpa/integration,extensions/core/commandlog/applib -am \
  -Dtest=CausewayTraceClassifierTest,CausewayObservationAutoConfigurationTest,InteractionServiceObservationTest,TransactionObservationTest,CausewayForegroundTraceFilterTest,RunBackgroundCommandsJobTraceClassificationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true -Dmaven.javadoc.skip=true clean install
mvn -o -f regressiontests/tracing-compatibility/pom.xml test
mvn -o -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am \
  -Pobservation -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true package
```

Passed: 25 focused Java tests (11 configuration/classification, 8 interaction/transaction, 3 servlet-filter, 3 background job), 9 tracing/JPA tests and Petclinic packaging. The packaging invocation skips Java tests; the reactor's configured JavaScript checks also passed.

Export evidence covers both supported tracing owners, retained successful and failed JPA observations, real JDBC children, correct ancestry, automatic identity attributes, root UUID correlation, entry classification, inactive observations and no exporter. The obsolete-threshold test supplies the removed property with a one-day value and verifies no duration discard or missing JPA parents in Boot and agent modes. Fixture business/persistence collaborators remain controlled test doubles; actual child JVMs export to a private local OTLP receiver.

Clean rebuild verified that the threshold property is absent from generated metadata, and the policy and threshold-wrapper classes are absent from compiled output. Existing explicit discard support remains in the registry integration and Spring predicate because transaction instrumentation uses it independently of duration.

Shell syntax and mock-Maven invocations verify launcher profile/agent forwarding with no threshold setting in either mode. Both observability guides and the sample README render with Asciidoctor and their YAML examples parse. `git diff --check` and strict OpenSpec change validation pass. Full repository regressions and live browser/backend sessions were not repeated.

Archive checkpoint validation uses the same focused `test` goals (without clean/install) and the tracing suite after the implementation commit, as required by the one-shot workflow.
