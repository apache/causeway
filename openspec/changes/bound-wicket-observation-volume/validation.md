# Validation

## Implementation evidence

The Wicket configuration model exposes `NONE`, `PAGE`, `REGIONS`, `ROWS`, and `MEMBERS` through `causeway.viewer.wicket.observation.detail` with compatibility default `MEMBERS`.
The configuration model exposes `causeway.viewer.wicket.observation.max-spans-per-request` with compatibility default `0`, and both configuration binding and the coordinator reject negative values.
The request-cycle coordinator owns immutable request policy, admitted observation count, structural reservation, active emitted ancestors, suppression counts, and collection aggregates.
A positive request maximum reserves ten percent of capacity for page and region observations, bounded to at least one and at most sixteen, without exceeding the absolute maximum.
Suppressed observations open no scope, so admitted descendants naturally join the nearest emitted Wicket or non-Wicket ancestor.
Collection preparation and rendering accumulate fixed-key visible-row count, total duration, maximum duration, logical-cell count, and suppressed-child count without adding observations.
Aggregate timing uses `System.nanoTime()` in production and an injected monotonic supplier in focused tests.
Request completion, request failure, component detach, and render failure close or discard active transient state.
The serializable behaviors retain only static descriptors because admissions, timers, scopes, counters, and aggregates are transient request metadata.
The `causeway.wicket.collection.initialize` descriptor and `EntityCollectionPanel` construction wrapper were removed.
Collection UI construction remains unchanged and now inherits page preparation or the nearest natural context.

## Volume evidence

A representative synthetic entity-page candidate tree with one fieldset, one parented collection, one visible row, two page members, and two row members emits 0 Causeway Wicket observations at `NONE`, 3 at `PAGE`, 10 at `REGIONS`, 12 at `ROWS`, and 16 at `MEMBERS` with an unlimited budget.
The equivalent Ajax collection subtree without a page boundary emits 0 at `NONE` and `PAGE`, 5 at `REGIONS`, 6 at `ROWS`, and 8 at `MEMBERS`.
The focused finite-budget scenario presents 14 eligible candidates to a maximum of 10, emits exactly 10, suppresses four detailed candidates, and retains the late collection structural observation.
Java-agent HTTP, JDBC, interaction, entity-change, audit, and application observations do not enter the Wicket coordinator and therefore do not consume this count.

## Collection-initialization evidence

The supplied representative traces showed sibling collection-initialization spans generally contributing only hundreds of microseconds to a few milliseconds each.
Their broad repetition amplified collection-heavy traces without sufficiently distinguishing useful work from page preparation.
After removal, construction-time JDBC work remains beneath `prepare <logicalTypeName>` when page preparation is active, while later provider and visible-row work remains beneath `prepare collection <collectionId>`.

## Commands and results

The Java 25 reactor compiled Java 11 release output successfully for `viewers/wicket/ui` and its required modules.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -pl viewers/wicket/ui -am -DskipTests -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 4 install
----

The focused Wicket observation, Ajax table, and configuration tests passed.
`WicketRenderObservationTest` ran 27 tests with no failures before the final combined run, including detail mapping, hard-budget reservation, suppression metadata, aggregate monotonic timing, no-detail behavior, hierarchy, Ajax, failures, cleanup, privacy, and serialization coverage.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -pl viewers/wicket/ui-test -am -Dtest=WicketRenderObservationTest,CausewayAjaxDataTableObservationTest,CausewayObservationConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

The complete Wicket UI test reactor passed.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -pl viewers/wicket/ui-test -am -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

The Java-agent compatibility suite passed both tests using the Java 25 reactor and Java 11 child fixture.
It verified Java-agent-owned HTTP and JDBC spans, semantic parentage, construction-time JDBC inheritance by page preparation, and absence of collection initialization.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -Dmodule-regressiontests -pl :causeway-regressiontests-tracing-compatibility -am -Dtest=MicrometerTracingCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=false -Dcauseway.tracing.fixture.java.home=$HOME/.sdkman/candidates/java/11.0.29-tem -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

`git diff --check` passed.
Strict OpenSpec validation passed with all four required artifacts complete and all 39 implementation tasks marked complete.

[source,bash]
----
git diff --check
openspec validate bound-wicket-observation-volume --strict
openspec status --change bound-wicket-observation-volume
----
