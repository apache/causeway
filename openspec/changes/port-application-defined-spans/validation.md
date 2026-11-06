# Validation: application-defined span forward port

## Provenance and environment

Roadmap step 9 ports maintenance implementation `ea32b739d0f`, standalone NOOP helper `5da64c34b57` and final invoked/domain-facing identity fix `f66b8f17a89`.
Planning commit: `67c0e9c309e` on `CAUSEWAY-4065-v4`, stacked on evaluation PR #3898 at `a1d565d379d`.
V4 already contains the final action identity distinction and executor support from earlier ports; this change reuses them rather than replacing physical invocation identity.
The older maintenance delta spec predates that correction, so the final implementation and v4 identity tests determine this adaptation.

Validation on 2026-11-05/06 uses Java 25.0.4-tem and Maven 3.9.13.
The inherited baseline is Boot 4.2.0-RC1, Micrometer 1.18.0-RC1, Micrometer tracing 1.8.0-RC1, OpenTelemetry 1.67.0 and Java agent 2.33.1.
No production dependencies or new tracing configuration were added.
IntelliJ compilation was unavailable because this worktree is not an open IDE project; Maven compiled the affected modules.

## Build and focused tests

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl api/applib,core/config,core/runtimeservices -am \
  -Dtest=ApplicationSpanServiceTest,ApplicationSpanNamingTest,ApplicationSpanServiceDefaultTest,MemberExecutorServiceDefaultTest,MemberObservationPolicyTest,InteractionProviderTest,CausewayObservationNamingTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -f regressiontests/tracing-compatibility/pom.xml test
```

The focused build/install passed fifty tests without failures or skips: two public NOOP tests, ten interaction-provider tests, four application naming tests, five existing naming tests, eleven runtime service tests and eighteen member executor/policy tests.
Coverage includes exact results and invocation counts, null results, declared/mixin domain-facing identity with unchanged physical identity, nested current execution and observation restoration, non-action identity, fallback names, full canonical metadata, all accepted suffix lengths, invalid arguments before execution even when inactive, checked/runtime/error identity, secondary telemetry failures suppressed on the application failure, no-op behavior and Spring injection/explicit module registration.
The initial metadata expectation was corrected to use the existing runtime module value `runtimeservices`; production metadata policy was not changed.

## Exported traces

The complete tracing/JPA suite passed twenty-one tests without failures or skips: nineteen compatibility tests and two JPA policy tests.
The new application fixture exports eight application regions per active ownership mode, covering declared and contributed actions, nested load/process regions with JDBC descendants, failure followed by success on one HTTP worker, fallback naming and the longest accepted suffix.
It asserts static exact attribute keys, complete canonical member identity without signatures, preserved existing physical member metadata, display bounds, suffix preservation, trace identity and natural parentage.
Inactive-agent and absent-exporter modes still run the same work without exported application spans; inactive automatic JDBC spans remain visible.

The existing fixture dispatches pass-through actions, which deliberately do not create a current execution.
For application identity checks it now supplies controlled declared/mixin `ActionInvocation` instances through the real interaction carrier's push/pop lifecycle around the existing member-service dispatch.
Application work failures retain the dispatcher's existing `ReflectiveActionException` boundary outside the span service; the HTTP fixture recognizes that expected wrapper without changing production exception behavior.
Focused service tests separately prove that the application span service itself preserves identical original checked exceptions, runtime exceptions and errors.

## Documentation and specification

The observability guide documents injection and call/run usage, static suffix privacy, 46/50-character limits, domain-facing versus invoked identity, nesting, checked exceptions, synchronous lifetime and standalone/inactive behavior.
It rendered successfully with Asciidoctor 2.0.26 Ruby core and data from cached AsciidoctorJ 3.0.1, using safe-mode output `target/application-spans-observability.html`.
Strict `openspec validate port-application-defined-spans --strict` and `git diff --check` passed.
Seven new capability requirements are additive; existing action context and other observation requirements are retained.

## Limits

Exported tests use real interaction and member observation services, the real carrier lifecycle, reflection dispatch and the real JPA facet with H2 SQL; action metadata and non-telemetry collaborators are controlled.
They do not establish a complete application metamodel, wrapper service invocation, ORM-backed domain action or production SDK bootstrap outside the tested configurations.
Nested current execution identity has focused coverage; exported nesting is synchronous application-region nesting.
Agent mode instruments real JDBC automatically; Boot mode explicitly observes fixture SQL as in the existing compatibility suite.
No full repository reactor suite, browser session or complete Antora site build was performed.
