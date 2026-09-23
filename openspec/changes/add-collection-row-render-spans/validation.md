# Validation Evidence

## Source Trace Analysis

The Jaeger export for trace `6cfdb024d9142ff660699111909cf5ad` contained 1,777 spans.
The `render collection roles` span lasted 2,071.814 ms and had no SQL descendants or temporally overlapping SQL spans.
The page preparation span lasted 2,500.768 ms and contained 1,679 SQL descendants with 748.151 ms of aggregate JDBC duration.
Role preparation included one collection query followed by sixteen per-role `FixedAssetRole` and `Party` queries between 1,998.607 ms and 2,022.745 ms from the trace start.
Those sixteen JDBC spans totalled 6.551 ms, confirming an N-plus-one during preparation while also proving that it did not account for the later two-second markup-rendering interval.

## Focused Unit and Wicket Lifecycle Tests

The focused Java 25 reactor run compiled with Java 11 release output and passed 30 tests.

- `CausewayObservationNamingTest`: 7 tests.
- `WicketRenderObservationTest`: 19 tests.
- `CausewayAjaxDataTableObservationTest`: 4 tests.

The tests cover bounded collection and row preparation and render descriptors, preparation-to-render phase separation, collection-to-row-to-cell parentage, failure cleanup, no-op behavior, serialization, current-page row bounds, standalone-table exclusion, parented table property and action inclusion, and forbidden instance identity.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn -pl viewers/wicket/ui-test -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dtest=CausewayObservationNamingTest,WicketRenderObservationTest,CausewayAjaxDataTableObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

## Java-Agent Compatibility

The Java 25 reactor compiled Java 11 release output, then launched the fixture on Java 11 with OpenTelemetry Java agent 1.31.0.
Both `MicrometerTracingCompatibilityTest` tests passed.
The exported trace confirmed the following ancestry:

[source,text]
----
GET /trace
└── causeway.root.interaction
    ├── prepare causeway.TracingFixture
    │   └── prepare collection roles
    │       └── prepare row causeway.TracingRole
    │           └── SELECT causeway-tracing
    ├── render causeway.TracingFixture
    │   └── render collection roles
    │       └── render row causeway.TracingRole
    │           ├── render property name
    │           └── render action update
    └── act causeway.TracingFixture#executeJdbc
        └── JDBC
----

The fixture also passed without the Java agent and exported no framework-owned SDK dependency behavior.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn -Dmodule-regressiontests \
  -pl regressiontests/tracing-compatibility -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dcauseway.tracing.fixture.java.home=$HOME/.sdkman/candidates/java/11.0.29-tem \
  -Dtest=MicrometerTracingCompatibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

## Specification and Privacy Checks

`openspec validate add-collection-row-render-spans --strict` passed.
`git diff --check` passed.
A focused source scan found no row index, bookmark, primary-key, object-identifier, or custom row-identity telemetry.
Canonical `causeway.object.type`, `causeway.collection.id`, `causeway.property.id`, and `causeway.action.id` attributes remain complete while contextual names remain bounded to 50 characters.
