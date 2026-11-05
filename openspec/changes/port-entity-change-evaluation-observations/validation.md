# Validation: entity-change evaluation observation forward port

## Provenance and environment

Roadmap step 8b ports maintenance implementation `02786054574` and the archived `add-entity-change-evaluation-span` design at `1a9910c3e77^`.
Planning commit: `faafbe9850a` on `CAUSEWAY-4059-evaluation-v4`.
The branch is stacked on audit PR #3897 at `65f4f38cd1c`, after the preceding Wicket/semantic-name PR #3896 merged.

Validation on 2026-11-05 uses Java 25.0.4-tem and Maven 3.9.13.
The inherited baseline is Boot 4.2.0-RC1, Micrometer 1.18.0-RC1, Micrometer tracing 1.8.0-RC1, OpenTelemetry 1.67.0 and Java agent 2.33.1.
No production dependencies or new tracing configuration were added.
IntelliJ compilation was unavailable because this worktree is not an open IDE project; Maven compiled the affected modules.

## Commands and results

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl persistence/commons -am \
  -Dtest=EntityChangeTrackerDefaultObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -f regressiontests/tracing-compatibility/pom.xml test
openspec validate port-entity-change-evaluation-observations --strict
git diff --check
```

The focused reactor build/install passed eight tracker tests without failures or skips.
Tests cover aggregate changed/unchanged filtering, exact static metadata, empty-candidate bypass, defensive-copy retry inside one observation, handled unknown values, identical escaping failures, prior-scope restoration and subsequent success, action/background parentage, synchronous descendants, no-op results/failures and lazy public access followed by publication outside evaluation.
The initial maintenance-style test doubles failed because v4's `ManagedObject` is sealed; real entity/value wrappers with mocked specifications and facets resolved that incompatibility.
No production behavior was changed to accommodate testing.

The complete tracing/JPA suite passed nineteen tests without failures or skips: seventeen compatibility tests and two JPA policy tests.
New exported-trace coverage runs the actual tracker's public `beforeCommit()` and lazy memoized access under Boot and agent ownership, with two real property-change records and H2 SQL in controlled accessors.
It verifies one evaluation per request, JDBC descendants, static metadata without payload sentinels, natural root-interaction parentage, distinct request traces and evaluation ending before sibling audit observations begin.
The audit subscriber runs through a controlled publisher callback, not inside the evaluation observation.
Both audit-failure and evaluation-failure requests are followed by successful requests on the same HTTP worker, with explicit scope-restoration checks.
Empty candidates produce no evaluation spans under both owners; inactive-profile and absent-exporter modes preserve execution without exported evaluation spans.

The observability guide rendered successfully with Asciidoctor 2.0.26 Ruby core and data files from cached AsciidoctorJ 3.0.1.
Safe-mode conversion wrote `target/evaluation-observability.html`, containing the new evaluation section.
Strict change validation and whitespace checks passed.

## Limits

Property records and managed-object wrappers are real, while the metamodel, accessors, comparison service, interaction-tracker provider and publisher collaborators are controlled.
Records are seeded reflectively so the fixture exercises the actual evaluation/lifecycle boundary without requiring a complete domain metamodel and ORM bootstrap.
The fixture does not establish JDO/JPA enlistment, database transaction commit/rollback or a complete audit entity ORM deployment.
The production enlistment, evaluation traversal/retry, memoization, clearing, transaction, publisher and repository code is unchanged apart from the observation wrapper and its injection.
Agent mode uses automatic JDBC instrumentation; Boot mode explicitly observes the fixture SQL as in the existing regression suite.
No full repository reactor suite, browser session or complete Antora site build was performed.
