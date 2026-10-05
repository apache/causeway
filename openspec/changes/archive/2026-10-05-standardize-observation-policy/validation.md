# CAUSEWAY-4096 implementation evidence

## Baseline and scope

Implemented on `CAUSEWAY-4096`, based on main `da8c9a98bb7` (viewer documentation archive after foundation merge `74d44c060ce`). Proposal checkpoint: `5390171e070`.

The typed `CausewayObservationPolicy` binds once through Boot configuration properties, using the repository's immutable-record/default-value conventions. It is registered separately from the large root configuration so the existing observation auto-configuration can still be used independently. The existing registry constructors and duration-filtering accessor remain available. The no-argument policy defaults match auto-configuration.

Existing JPA, member and publishing observation boundaries remain in place. Their dynamic operation names now use the design's fixed mapping, with static metadata in contextual names/attributes. Interaction identity attributes require independent opt-ins. Default duration filtering is off; explicit filtering uses the configurable JPA threshold. Duration comparison also handles thresholds too large to represent as nanoseconds, avoiding a cleanup-time overflow.

No new production dependencies, exporters, SDKs, samplers or observation boundaries were added. The existing test-only tracing module now depends on JPA integration to exercise the production facet.

## Validation results

All checks passed using Java 25.0.4-tem and Maven 3.9.13, compiler release 17. The fixture retains main's Boot 4.2.0-M1, Micrometer Observation 1.18.0-M1, Tracing 1.8.0-M1 and OpenTelemetry 1.64.0 baseline, with agent 2.31.1 and protocol collector 1.11.0-alpha.

- Focused reactor build/install: 43 tests (6 observation lifecycle, 11 configuration/registry, 8 duration policy, 4 interaction, 1 transaction, 10 existing member-service tests, 3 member/publishing policy tests).
- Tracing compatibility module: 8 tests (2 real JPA facet policy tests, 6 exported-trace compatibility tests). The identity export test launches both Boot and agent modes.
- Existing interaction/publishing regression suites: 22 tests (PropertyInteractionTest 2, WrapperInteractionTest 4, JpaExecutionPublishingTest 6, JpaPropertySinglePublishingTest 10).
- The Boot discard test was subsequently strengthened and rerun successfully to assert that suppressed JPA parents leave independently exported JDBC children with unresolved parent IDs.
- Antora preview build: passed. The rendered user guide contains the CAUSEWAY-4096 migration section, with no diagnostics referencing observability.adoc. Existing unrelated documentation diagnostics remain.
- Strict OpenSpec validation and `git diff --check`: passed. Configuration metadata is valid JSON.

The focused tests cover configuration defaults and opt-ins, inactive activation, invalid/negative/zero thresholds, fluent wrapper behavior, threshold equality, large durations, identity combinations/empty values, stable member and subscriber naming, and unchanged physical execution. The real JPA facet tests cover all affected JPA names, multiple bookmarks and query arguments, sentinel-value exclusion, configured threshold use and short failure retention.

## Export evidence and limits

The child JVM fixture uses the production interaction service, member executor and JPA facet, with mocked non-telemetry collaborators and a controlled EntityManager backend. Persistence invokes real H2 SQL. Agent mode supplies automatic HTTP/JDBC spans; Boot mode explicitly wraps that SQL call in a fixture observation. This is not a full ORM/servlet/viewer instrumentation test.

Both modes verify exported contextual names, a single JPA span beneath each action, parent/child trace IDs, identity omission by default and explicit identity export. The default-retention ancestry tests use a generous configured JPA threshold (`1d`) with filtering disabled, avoiding timing-sensitive assertions around 2 ms; binding and deterministic unit tests separately verify the 2 ms default and exact duration boundaries. Agent mode includes both the documented explicit disable configuration and default-disabled policy coverage.

Explicit Boot filtering drops short successful JPA parents while their JDBC children remain. Default-disabled filtering retains these parents. This does not guarantee completeness under external sampling/export filtering, and the agent exporter still does not consume Spring's discard predicate. No collector buffering or ancestry-aware suppression is claimed.

At the initial implementation checkpoint, the full repository suite and viewer-specific telemetry tests were not run; selected existing regression suites cover property, wrapper and JPA publishing behavior. Later semantic mixin names, interaction correlation, trace-root naming and collection budgets remain separate roadmap work.

## Reproducible commands

Set `JAVA_HOME` to an installed supported build JDK (here `/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem`). From the repository root:

```sh
mvn -pl core/runtimeservices,persistence/jpa/integration -am -Dtest=ObservationClosureTest,CausewayObservationAutoConfigurationTest,ObservationWithTimeThresholdTest,InteractionServiceObservationTest,TransactionObservationTest,MemberExecutorServiceDefaultTest,MemberObservationPolicyTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.javadoc.skip=true -Dmaven.source.skip=true install
mvn -f regressiontests/tracing-compatibility/pom.xml test
mvn -Dmodule-regressiontests -pl regressiontests/interact,regressiontests/publishing-jpa -am -Dtest=PropertyInteractionTest,JpaPropertySinglePublishingTest,JpaExecutionPublishingTest,WrapperInteractionTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.javadoc.skip=true -Dmaven.source.skip=true test
bash ./preview.sh -A
openspec validate standardize-observation-policy --strict
git diff --check
```

## Local verification follow-up (2026-10-05)

Added independent ephemeral Jaeger and Prometheus/Grafana helpers and a single Petclinic `run-with-telemetry.sh` launcher with Boot-managed and optional agent-managed tracing. Micrometer exports metrics in both modes. The sample agent profile disables Boot's OTEL environment mapping so the agent's `OTEL_METRICS_EXPORTER=none` does not disable Micrometer metrics. The M3 how-to and sample README document the runnable setup and scenario expectations. These sample-only dependencies do not change core SDK ownership.

Manual testing exposed a Wicket scope-ordering defect: Wicket ends request listeners in reverse registration order, so the former stop handler closed the outer scope before the interaction. Closure now runs in the start listener's end callback, after interaction cleanup; the stop listener still captures metrics before cleanup. `TelemetryRequestCycleTest` reproduces both successful and failed request scope restoration failures against the old implementation and passes with the fix. The Wicket viewer module test run reports 19 tests, 0 failures/errors, 3 skipped.

Additional checks passed: offline observation-profile Petclinic reactor package (tests skipped); launcher shell syntax and all 12 supported mode/scenario combinations using stubs; actual Boot environment-postprocessor verification that agent settings retain Micrometer metrics; Docker Compose configuration validation; M3 guide and sample README AsciiDoc conversion; internal guide links; whitespace checks. Live Docker access was unavailable to the assistant. The user manually verified the local services, corrected Jaeger v3 readiness endpoint, Grafana after disabling plugin auto-updates, newly nested Wicket/GraphQL traces, and identity/filtering scenarios, and accepted the result. The full repository suite was not rerun.

Follow-up commands:

```sh
mvn -o -f viewers/wicket/viewer/pom.xml test
mvn -o -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pobservation -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true package
./scripts/metrics-local.sh config
```

Archive checkpoint: repeated the observation-profile Petclinic package and Wicket module tests successfully on 2026-10-05; guide rendering, shell syntax and strict change validation passed. Git staging was blocked by the session's read-only Git metadata (`.git/index.lock`: Operation not permitted), so no pre-archive or archive commit could be created by the assistant. Spec sync and the archive move were completed in the working tree for the user to commit locally.
