# Validation: application primer observations forward port

## Provenance and environment

Roadmap 10b ports maintenance `473454f2447` and the archived `observe-application-primers` design recoverable at `1a9910c3e77^`.
Planning commit: `7caf9eaff9e` on `CAUSEWAY-4066-primer-observations-v4`, stacked on priming-hook PR #3912 at `3b2fc42522a`.
V4's atomic immutable registration snapshots and existing action/view admission hooks are retained.
Canonical action metadata now includes resolved metamodel parameter signatures rather than maintenance's placeholder `()`.

Validation on 2026-11-06 uses Java 25.0.4-tem, Maven 3.9.13, inherited Boot 4.2.0-RC1 and Wicket 11.0.0, with Java agent 2.33.1 in the tracing module.
No public API, dependency, configuration or tracing pipeline was added.
IntelliJ compilation was unavailable because this worktree is not an open IDE project; Maven compiled the affected reactor modules.

## Commands

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl api/applib,core/metamodel,core/runtimeservices,viewers/wicket/ui-test,viewers/wicket/viewer -am \
  -Dtest=ActionArgumentsTest,PrimingRegistryDefaultTest,PrimingRegistryDefaultObservationTest,ActionExecutorPrimingTest,DomainObjectPagePrimingTest,MemberExecutorServiceDefaultTest,MemberObservationPolicyTest,InteractionProviderTest,ObservationClosureTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -pl viewers/wicket/ui-test,viewers/wicket/viewer \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
mvn -B -f regressiontests/tracing-compatibility/pom.xml test
openspec validate port-application-primer-observations --strict
git diff --check
```

## Focused and Wicket results

The focused build/install passed sixty-six tests without failures or skips.
Seven new registry observation tests cover one callback scope each, deterministic sequential siblings without an aggregate, current-parent restoration and synchronous descendants, exact static metadata, complete signatures, contributed identity, bounded case-preserving names, frozen metadata, runtime/error fail-fast behavior, subsequent success, unmatched lookups without integration resolution, absent/no-op behavior and secondary telemetry failures suppressed on the primary callback failure.
The fifty-three prior priming/identity/hook regressions and six shared closure tests also passed.

The complete Wicket UI/viewer suite passed 118 total tests: 113 executed with five existing skips and no failures or errors.
Existing authorization, request-local page identity admission, initial/later/Ajax preparation, failure handling, observation rendering and serialization regressions remain passing.

## Exported trace evidence

The complete tracing/JPA suite passed twenty-three tests without failures or skips: twenty-one compatibility tests and two JPA policy tests.
Two new tests exercise Boot/agent ownership and inactive/absent-exporter modes with both action-primer and view-primer failure scenarios.
The fixture uses a real finalized registry with two callbacks per action/view key and actual H2 SQL in each callback.
It verifies exact callback counts in every mode, including inactive modes, so failure short-circuiting and request-local admission are checked independently of export.

Export assertions cover distinct per-callback spans, identical static sibling metadata, complete canonical attributes, current action/request ancestry, JDBC descendants, sequential non-overlapping siblings, view priming ending before sibling preparation, and an initial failure followed by a successful request on the same HTTP worker.
The fixture explicitly checks observation restoration on both action/view dispatch paths and retains the existing Wicket cleanup checks.
Inactive agent tracing still exports automatic JDBC work without Causeway primer spans; absent-exporter mode exports no spans.

View dispatch uses the production authorization/admission boundary with a controlled visible model before real Wicket component preparation; Ajax dispatch uses the same request guard on the fixture's page identity.
Action registry dispatch occurs within the existing observed reflected action before its controlled persistence work.
The before-domain-method integration hook itself is established independently by the retained 10a real-executor tests, rather than claiming a complete domain action metamodel/ORM bootstrap in this export fixture.

## Documentation and specification

The observability guide documents operation/display names, complete signatures, privacy, one-span-per-callback volume, natural ownership, failure cleanup and inactive behavior.
The priming guide now links to that explanation instead of stating there are no dedicated observations.
Both pages rendered successfully with Asciidoctor 2.0.26 core/data from cached AsciidoctorJ 3.0.1, using safe-mode outputs under `target/primer-observations-*.html`.
Strict change validation and whitespace checks passed.
Six additive requirements preserve the existing priming-hook and observation contracts.

## Limits

Metamodel specifications, registrar discovery, visible page models and non-telemetry collaborators are controlled test doubles.
Tests exercise real registration validation, callback wrappers, request admission, Wicket preparation/Ajax lifecycles and H2 JDBC, not a full ORM-backed application, security provider or browser deployment.
Agent mode owns automatic JDBC instrumentation; Boot mode explicitly observes fixture SQL as in the existing suite.
No full repository reactor suite or complete Antora site build was performed.
