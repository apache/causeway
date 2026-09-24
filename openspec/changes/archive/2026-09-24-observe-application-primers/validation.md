# Validation

## Implementation evidence

`PrimingRegistryDefault` creates one `causeway.priming.action` observation around every matching action-primer callback and one `causeway.priming.view` observation around every matching view-primer callback.
The registry performs observation lookup only after a matching list is found and bypasses observation creation when integration is absent or no-op.
Each matching registration is observed separately, so multiple callbacks produce sequential siblings without an aggregate priming parent.
Action-primer observations use contextual name `prime action <logical-member-identifier>` and complete `causeway.object.type` and `causeway.action.id` attributes.
View-primer observations use contextual name `prime view <logical-object-type>` and the complete `causeway.object.type` attribute.
Names use the established case-preserving namespace fallback and deterministic 50-character bound.
Validated registration keys retain domain-facing target identity and do not expose a contributed-action implementation type.
Callback runtime exceptions and errors are recorded, observation scopes close, the original failure identity propagates, and later callbacks remain uninvoked under existing fail-fast behavior.
The public priming SPI, registration matching, callback order, Wicket request guard, and Java-agent ownership remain unchanged.

## Focused test evidence

`PrimingRegistryDefaultObservationTest` ran 10 tests with no failures.
Coverage includes zero, one, and multiple matching action and view primers, sibling parentage, action and view metadata, contributed-action identity, bounded names, runtime exceptions, errors, fail-fast ordering, absent integration, no-op integration, and privacy.
Existing `PrimingRegistryDefaultTest`, `ActionExecutorPrimingTest`, and `EntityPagePrimingTest` continue to cover registration order, action dispatch, association exclusion, and once-per-request Wicket view priming.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -pl viewers/wicket/ui-test -am -Dtest=PrimingRegistryDefaultTest,PrimingRegistryDefaultObservationTest,ActionExecutorPrimingTest,EntityPagePrimingTest -Dsurefire.failIfNoSpecifiedTests=false -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

The complete Wicket UI reactor and its upstream metamodel tests passed.
The assertion-error log lines in that suite are expected output from tests that deliberately exercise assertion failures; Maven reported `BUILD SUCCESS`.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -pl viewers/wicket/ui-test -am -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

## Java-agent compatibility evidence

The Java-agent compatibility suite ran 2 tests with no failures using the Java 25 reactor and Java 11 child fixture.
The fixture invokes the real priming registry for both action and view callbacks.
It verifies that the action primer is a child of action invocation, the view primer is a request-interaction child and sibling of later page preparation, and each primer owns its Java-agent-created JDBC descendant.
It also verifies canonical primer attributes, absence of primer implementation metadata, one HTTP entry span, and no duplicate JDBC instrumentation.

[source,bash]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem mvn -Dmodule-regressiontests -pl :causeway-regressiontests-tracing-compatibility -am -Dtest=MicrometerTracingCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=false -Dcauseway.tracing.fixture.java.home=$HOME/.sdkman/candidates/java/11.0.29-tem -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 1 test
----

## Final checks

`git diff --check` passed.
Strict OpenSpec validation passed with all four required artifacts complete and all 19 implementation tasks complete.

[source,bash]
----
git diff --check
openspec validate observe-application-primers --strict
openspec status --change observe-application-primers
----
