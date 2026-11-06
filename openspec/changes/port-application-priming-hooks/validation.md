# Validation: application priming hooks forward port

## Provenance and environment

Roadmap step 10a ports maintenance `19d2a36d2f9` and the archived `add-application-priming-hooks` design recoverable at `1a9910c3e77^`.
Planning commit: `46b372b7a51` on `CAUSEWAY-4066-priming-v4`, stacked on application-span PR #3911 at `643411a9787`.
Primer observations from maintenance `473454f2447` are deliberately excluded and remain roadmap 10b.

Validation on 2026-11-06 uses Java 25.0.4-tem and Maven 3.9.13 with the inherited Boot 4.2.0-RC1/Wicket 11.0.0 baseline.
The metamodel module adds BOM-managed JUnit/Mockito test dependencies because it did not previously include a test framework; no production dependency is added.
Importing internal test support would introduce a circular dependency, so the focused metamodel tests use direct test-scoped libraries.
IntelliJ compilation is unavailable because this worktree is not an open IDE project; Maven compiled the affected reactor modules.

## Commands and focused results

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl api/applib,core/metamodel,core/runtimeservices,viewers/wicket/ui-test,viewers/wicket/viewer -am \
  -Dtest=ActionArgumentsTest,PrimingRegistryDefaultTest,ActionExecutorPrimingTest,DomainObjectPagePrimingTest,MemberExecutorServiceDefaultTest,MemberObservationPolicyTest,InteractionProviderTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -pl viewers/wicket/ui-test,viewers/wicket/viewer \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
```

The focused build/install passed fifty-three tests without failures or skips.
New coverage comprises three argument tests, eight registry tests, seven real action-executor tests and seven Wicket priming tests.
Existing interaction-provider and member-executor/policy regressions contribute twenty-eight further tests.

Argument tests cover shallow defensive snapshots, nulls, single-slot arrays, list immutability, typed diagnostics and invalid access.
Registry tests cover multiple deterministic callbacks, exact class/action validation including contributed actions, no hierarchy matching, closed registration before/after loading, closure after startup failure, no partial snapshot publication, unmatched/no-registrar behavior and identical callback failures.
The implementation atomically publishes both immutable lookup maps through one volatile snapshot after validation succeeds.

Action tests use the real record-based executor and actual reflection invocation with controlled event/metamodel collaborators.
They verify priming before eventless methods, executing-event adjusted arguments, executed-event ordering, contributed names and mixee targets, preserved physical identifiers, nested ordinary execution, excluded pass-through/synthetic facets, absent services and identical primer failures preventing method invocation.

Wicket tests use real WicketTester full/Ajax request cycles and the production shared page boundary.
They verify once-per-page/request identity admission, later request reset, distinct-but-equal page objects, authorization before service lookup, absent services, identical callback failures without same-request retry, and the real DomainObjectPage new-request hook with controlled page/model collaborators.
An initial-hook test invokes the actual page initializer and deliberately stops at the next layout metadata access, proving that priming occurs first without bootstrapping the full domain UI.
No new callback state is retained in page instance fields.

## Broader Wicket regression

The complete Wicket UI/viewer suite passed 118 tests total: 113 executed and five existing skips, with no failures or errors.
This includes existing preparation/rendering, collection observations, prompts, serialization and request-cycle cleanup coverage alongside the new view hooks.

## Documentation and specification

The new user guide page explains registrar examples, exact class/action keys, contributed targets, effective arguments, read-only/idempotent/concurrent callback contracts, failure transparency and full/Ajax request lifetimes.
It is linked from user guide navigation and explicitly states that dedicated primer spans are not part of this feature.
It rendered successfully with Asciidoctor 2.0.26 core/data from cached AsciidoctorJ 3.0.1, using safe-mode output `target/application-priming-hooks.html`.
Strict `openspec validate port-application-priming-hooks --strict` and `git diff --check` passed.
Six requirements are additive; existing authorization, invocation identity and observation requirements are retained.

## Limits

Tests use real managed-object wrappers, executor/reflection logic and Wicket request cycles, but metamodel specifications, service registry, domain-event helper, ORM-related collaborators and page/model services are controlled.
The initializer test intentionally stops before full layout construction, while existing Wicket regressions exercise normal component rendering and serialization.
No full application metamodel bootstrap, live database priming query, full domain-page browser session or complete Antora build was performed.
Read-only, independently idempotent and concurrent-safe callback behavior is an application responsibility, not enforced through new transactions or async work.
