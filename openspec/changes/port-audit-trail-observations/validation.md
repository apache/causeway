# Validation: audit-trail observation forward port

## Provenance and environment

Roadmap step 8a ports maintenance implementation `7961910a089` and the archived `add-audit-trail-write-span` design recovered at `1a9910c3e77^`.
Planning commit: `e2aa21510b3` on `CAUSEWAY-4059-audit-v4`.
The branch starts from the preceding rebased Wicket/semantic-name tranche at `4efb0a79fa4`, based on recorded `origin/main` `958fc795e7c`.

Validation on 2026-11-05 uses Java 25.0.4-tem and Maven 3.9.13.
The upstream Boot dependency baseline is 4.2.0-RC1, with managed Micrometer 1.18.0-RC1, Micrometer tracing 1.8.0-RC1 and OpenTelemetry 1.67.0; the regression module pins Java agent 2.33.1.
No production dependencies or tracing configuration were added.
IntelliJ build was unavailable because this worktree is not an open IDE project; the Maven reactor compiled the affected modules instead.

## Build and tests

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl extensions/security/audittrail/applib -am \
  -Dtest=EntityPropertyChangeSubscriberForAuditTrailObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -f regressiontests/tracing-compatibility/pom.xml test
```

The focused reactor build/install passed all five subscriber tests with no failures or skips.
Coverage includes both callback overloads, one observation per callback without per-entry delegation, exact static metadata, action/background parentage, synchronous child parentage, original failures from both overloads, restoration to a current parent, subsequent success, disabled audit persistence and no-op delegation/failure propagation.

The complete tracing/JPA suite passed all seventeen tests with no failures or skips: fifteen compatibility tests and two JPA policy tests.
The new audit export case runs under Boot and agent ownership and verifies four aggregate observations across an individual and bulk callback on each of two requests, exactly one failing audit, natural root-interaction parentage, distinct request traces, JDBC descendants, static metadata and absence of audit payload sentinels.
The failing bulk write is followed by a successful request on the same HTTP worker, and the fixture checks restoration of the previous current observation on each path.
Disabled audit is exercised under Boot, agent, inactive and absent-exporter modes; enabled audit is additionally exercised under inactive and absent-exporter modes.
Inactive agent tracing retains automatic JDBC spans without Causeway audit spans.

## Documentation and specification

The observability guide documents operation/display names, callback granularity, current-context ancestry, privacy, failure propagation and activation.
It rendered successfully with the installed Asciidoctor 2.0.26 Ruby core extracted from cached AsciidoctorJ 3.0.1, including its data/stylesheets directory.
`Asciidoctor.convert_file` used safe mode with an output path inside the repository (`target/audit-observability.html`); the output contains the new audit section.
The initial temporary-output attempt was rejected by safe-mode path confinement; rendering inside the repository resolved it.

Strict `openspec validate port-audit-trail-observations --strict` and `git diff --check` passed.
The new capability contains four requirements; no existing capability requirements are replaced.

## Limits

The export fixture invokes the actual audit subscriber and production interaction/action services, with a mocked repository executing real H2 table creation and inserts.
It does not establish a complete audit entity ORM deployment or test transaction commit/rollback through a real audit repository.
The production repository methods, publisher callback selection, batching and transaction code are unchanged.
Agent mode uses real automatic JDBC instrumentation; Boot mode explicitly observes the fixture SQL, consistent with the existing tracing regression suite.
No full repository reactor suite, browser session or complete Antora site build was performed.
