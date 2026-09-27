# Implementation evidence

## Baseline

- Refreshed `origin/main`: `351eae721d91366b1c4444131f0fcbf85faa323c` (2026-09-27).
- Worktree: `main`; branch: `CAUSEWAY-3975-observation-foundation`.
- Planning checkpoint: `13f98006d08`.
- No target AGENTS.md or SESSION_HANDOFF.md found. The previous maintenance instructions were explicitly withdrawn by the user.
- Maven 3.9.13, Java 25.0.3, compiler release 17, Boot 4.2.0-M1, Micrometer Observation 1.18.0-M1, Tracing 1.8.0-M1, OpenTelemetry 1.64.0.
- Baseline reactor build through core/config passed before production changes.

## Inventory and reconciliation

| Boundary | Main implementation | Treatment |
|---|---|---|
| Root/nested interactions | `InteractionServiceDefault`, `InteractionLayerStack` | Preserve names and layers; retain closure ownership, record failures and preserve the primary work exception during cleanup. |
| Action/property execution | `MemberExecutorServiceDefault` | Preserve existing observation sites and execution behavior. |
| Action/property substeps and events | `ActionExecutor`, `PropertyModifier` | Preserve existing observation sites. |
| Transactions | `TransactionServiceSpring` | Preserve creation/completion boundaries. Replace the shared closure across manager iterations with one per manager, closed in reverse order. This does not claim general multi-manager transaction support. |
| Execution publishing | `ExecutionPublisherDefault` | Preserve subscriber observation and name. |
| Metamodel initialization | `MetamodelInitializer` | Preserve initialization/listener observation sites. |
| JPA operations | `JpaEntityFacet` | Preserve operation names and 2ms threshold; delegate threshold selection to integration so agent mode can explicitly disable filtering. |

The closure remains in commons. The existing Exception overload and constructor taking an ObservationRegistry remain available. No applib API changes or main instrumentation removal were required.

## Wiring decision and observed behavior

Boot remains the default owner. The inactive Causeway integration no longer introduces a competing no-op ObservationRegistry bean. Active configuration resolves a supplied single/primary registry, or uses the fallback.

Agent mode uses an explicitly supplied application registry with an OtelTracer against GlobalOpenTelemetry; the fixture excludes Boot SDK/tracing auto-configurations. No production dependency was added for an agent-specific registry. Application wiring and exclusions are documented in the user guide.

Agent 2.31.1 and protocol collector 1.11.0-alpha are test-only dependencies. The fixture exports through the actual agent/Boot pipelines, not a substitute recording tracer. Its non-telemetry runtime collaborators are mocked and HTTP is served by the JDK HttpServer, so servlet/viewer-specific coverage remains for later features.

The target tracing handler preserves casing and full existing names. Maintenance's old handler normalization assumptions do not apply automatically to main; re-evaluate the need for its specialized naming handler during 4062.

Threshold composition was reproduced with the helper and the JPA provider pattern. Returning the delegate bypassed the wrapper's stop policy. Fluent/start methods now retain wrapper identity. A deterministic clock tests durations below, equal to and above the threshold. Boot exports short failures and suppresses short successes. Agent mode explicitly disables duration filtering and exports both.

## Checks

- Focused commons/config tests: 6 lifecycle, 7 registry configuration, 5 threshold tests passed.
- Runtime tests: nested interactions, work Error plus transaction cleanup failure, subsequent clean request, and per-manager observation cleanup passed; existing MemberExecutorServiceDefault tests passed.
- Affected runtime and JPA integration reactor compilation/install passed.
- Real exported-trace fixture: all 4 tests passed (agent ancestry/no duplicates, Boot export/discard, inactive profile with agent, active without agent/exporter).
- Selected property/wrapper/JPA publishing regression tests passed: PropertyInteractionTest (2), WrapperInteractionTest (4), JpaExecutionPublishingTest (6), JpaPropertySinglePublishingTest (10), for 22 tests.

## Limits retained for follow-up

Metadata/privacy policy and semantic naming changes remain roadmap step 1b and later tickets. Duration filtering can suppress parents while retaining descendants. The fixture's action uses the production pass-through execution path; metamodel rule-checking and viewer-specific tracing remain separate coverage. No deployment, archive, or implementation commit is part of apply.

## Reproducible validation commands

Run with `JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.3-tem` (or a compatible main build JDK):

```sh
mvn -pl core/runtimeservices,persistence/jpa/integration -am -Dtest=ObservationClosureTest,CausewayObservationAutoConfigurationTest,ObservationWithTimeThresholdTest,InteractionServiceObservationTest,TransactionObservationTest,MemberExecutorServiceDefaultTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.javadoc.skip=true -Dmaven.source.skip=true install
mvn -f regressiontests/tracing-compatibility/pom.xml test
mvn -Dmodule-regressiontests -pl regressiontests/interact,regressiontests/publishing-jpa -am -Dtest=PropertyInteractionTest,JpaPropertySinglePublishingTest,JpaExecutionPublishingTest,WrapperInteractionTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.javadoc.skip=true -Dmaven.source.skip=true test
openspec validate reconcile-main-observation-foundation --strict
```

The focused reactor run executes 31 tests, the exported-trace fixture executes 4, and the selected existing regression suites execute 22. Full-repository and viewer-specific suites were not run. Configuration metadata parses successfully and `git diff --check` is clean. Final review found no maintenance dependency pins, unrelated workspace artifacts, or new production dependencies.

## Archive checkpoint

On 2026-09-27 the focused reactor build and 31 tests, followed by all 4 exported-trace fixture tests, passed again using the installed Java 25.0.4. The original Java 25.0.3 installation had been replaced, so the archive check used 25.0.4. All 19 implementation tasks were complete; the six requirements were synced to `openspec/specs/main-observation-foundation/spec.md`. Implementation commit: `299dcdc2234`; PR: https://github.com/apache/causeway/pull/3814.
