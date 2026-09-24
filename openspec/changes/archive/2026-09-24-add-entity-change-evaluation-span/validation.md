# Validation Evidence

## Source Trace Motivation

The supplied trace showed repeated Java-agent `SELECT codaproxy.CodaTax` spans after command persistence and before `write audit trail`.
Debugger evidence located those calls in `EntityChangeTrackerDefault.changedRecords()` while `PropertyChangeRecord.withPostValueSetToCurrentElseUnknown()` obtained current values from JDO-derived properties.
The implemented boundary groups that work without moving audit persistence beneath it.

## Focused Tracker Tests

The Java 25 reactor compiled Java 11 release output and passed all nine `EntityChangeTrackerDefaultObservationTest` tests.
The tests cover one or multiple candidates, empty candidate suppression, candidates with no publishable difference, one observation across concurrent-modification retry, current parentage, synchronous child work, handled unknown-value behavior, escaping failure recording and rethrow, no-op registry behavior, scope cleanup, and static metadata privacy.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn -pl persistence/commons -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dtest=EntityChangeTrackerDefaultObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

## Persistence Compatibility

The active JPA property-change publication suites passed 20 tests across bulk and individual publication.
The JDO and JPA audit-trail integration suites each passed three tests and exercised property-change-driven audit persistence.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn -Dmodule-regressiontests \
  -pl regressiontests/publishing-jpa,extensions/security/audittrail/persistence-jdo,extensions/security/audittrail/persistence-jpa -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dtest=JpaPropertyBulkPublishingTest,JpaPropertySinglePublishingTest,AuditTrail_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

The older JDO publishing tests under `regressiontests/incubating` are excluded from the reactor.
Temporarily selecting that already-excluded module confirmed that its existing POM cannot enter the build because `causeway-tooling-model4adoc` has no managed dependency version.
No repository change was retained from that validation attempt.

## Java-Agent Compatibility

Both `MicrometerTracingCompatibilityTest` tests passed with the Java 25 reactor producing Java 11 release output and the fixture running on Java 11 with OpenTelemetry Java agent 1.31.0.
The no-agent test also passed and retained no framework-owned tracing SDK or exporter behavior.

The exported representative trace contained one aggregate evaluation observation with two Java-agent-owned JDBC children followed by a sibling audit observation:

[source,text]
----
act causeway.TracingFixture#executeJdbc
├── evaluate property changes
│   ├── SELECT trace_probe
│   └── SELECT trace_probe
└── write audit trail
    ├── CREATE audit_probe
    ├── INSERT audit_probe
    └── INSERT audit_probe
----

The fixture reported `entityChange=evaluate property changes entityChangeJdbc=2 audit=write audit trail auditInserts=3`.
It verified one evaluation span, direct JDBC ancestry, sibling evaluation and audit parentage, and static bean and module metadata without private change attributes.

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

The observation uses stable name `causeway.entitychange.evaluate`, contextual name `evaluate property changes`, and only static `causeway.bean` and `causeway.module` metadata.
No entity type, property identifier, target, bookmark, pre-value, post-value, user, tenant, transaction or interaction identifier, sequence number, candidate or changed count, or retry count is attached.
Strict OpenSpec validation, the targeted privacy scan, and `git diff --check` complete final validation.
