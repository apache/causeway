# Validation Evidence

## Source Trace Motivation

The supplied trace screenshot showed `act InvoiceSummary#invoiceAll` followed by many Java-agent JDBC children, including repeated `SELECT codaproxy.CodaTax` operations and `INSERT isisaudit.AuditEntryWithArchive` operations.
The database spans were individually visible but had no semantic aggregate identifying their total cost as audit-trail writing.
The implemented subscriber boundary groups audit-owned persistence while retaining the automatic JDBC detail.

## Focused Subscriber Tests

The Java 25 reactor compiled Java 11 release output and passed all six `EntityPropertyChangeSubscriberForAuditTrailObservationTest` tests.
The tests verify individual and bulk callbacks, one observation per bulk callback, current action parentage, synchronous child-operation parentage, success cleanup, failure recording and rethrow, disabled audit behavior, no-op registry behavior, and static metadata privacy.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn -pl extensions/security/audittrail/applib -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dtest=EntityPropertyChangeSubscriberForAuditTrailObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

## Audit Persistence Compatibility

The existing JDO and JPA audit-trail integration suites each passed three tests.
They verify created, updated, and deleted audit entries and preserve their existing contents and transaction behavior.

The command was:

[source,shell]
----
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.3-tem \
mvn \
  -pl extensions/security/audittrail/persistence-jdo,extensions/security/audittrail/persistence-jpa -am \
  -Drevision=2.2.0-SNAPSHOT \
  -Dmaven.compiler.release=11 \
  -Dmaven.compiler.proc=full \
  -Dmaven.source.skip=true \
  -Dtest=AuditTrail_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

## Java-Agent Compatibility

Both `MicrometerTracingCompatibilityTest` tests passed with the Java 25 reactor producing Java 11 release output and the fixture running on Java 11 with OpenTelemetry Java agent 1.31.0.
The exported trace contained one aggregate audit observation and three Java-agent-owned JDBC children:

[source,text]
----
act causeway.TracingFixture#executeJdbc
└── write audit trail
    ├── CREATE audit probe
    ├── INSERT audit probe
    └── INSERT audit probe
----

The fixture reported `audit=write audit trail auditInserts=3` and verified that the audit span carried only static bean and module metadata.
It also passed without the Java agent and retained no framework-owned SDK or exporter behavior.

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

The observation uses stable name `causeway.audittrail.write`, contextual name `write audit trail`, and only static `causeway.bean` and `causeway.module` metadata.
No property identifier, target, bookmark, old or new value, interaction or transaction identifier, sequence, user, tenant, or entry count is attached.
Strict OpenSpec validation and `git diff --check` complete the final validation.
