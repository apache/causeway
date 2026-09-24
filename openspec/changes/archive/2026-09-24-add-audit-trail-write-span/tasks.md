## 1. Audit Observation Boundary

- [x] 1.1 Confirm the audit-trail applib's compile-time access to `CausewayObservationIntegration` and add only an existing-module dependency declaration if the current provided runtime dependency is insufficient.
- [x] 1.2 Add stable `causeway.audittrail.write` and contextual `write audit trail` observation construction using the audit-trail extension's static module metadata.
- [x] 1.3 Wrap enabled individual subscriber persistence in one observation without changing repository invocation, transaction, or exception behavior.
- [x] 1.4 Wrap enabled bulk subscriber persistence in one aggregate observation without creating nested or per-entry framework observations.
- [x] 1.5 Keep the enablement check outside the observation and preserve inactive-profile execution through the existing no-op integration.

## 2. Lifecycle, Privacy, and Persistence Tests

- [x] 2.1 Add focused subscriber tests proving that individual and bulk repository callbacks execute with `causeway.audittrail.write` current and that a bulk callback produces exactly one observation.
- [x] 2.2 Add parentage tests proving the audit observation inherits an existing action or interaction observation and synchronous simulated database work inherits the audit observation.
- [x] 2.3 Add success and failure lifecycle tests proving scopes close, the original failure is rethrown, and no observation remains current.
- [x] 2.4 Add disabled-extension and no-op-registry tests proving repository behavior and exported telemetry remain unchanged in inactive modes.
- [x] 2.5 Verify exported names and attributes contain only static bean and module metadata and no audit target, property, value, identity, user, tenant, sequence, transaction, or count data.
- [x] 2.6 Run the existing JDO and JPA audit-trail persistence tests to confirm entry contents, ordering, bulk behavior, and transaction semantics remain unchanged.

## 3. Java-Agent Compatibility and Documentation

- [x] 3.1 Extend the Java-agent compatibility fixture with representative audit persistence and assert `action -> write audit trail -> JDBC` ancestry while HTTP and JDBC spans remain agent-owned and unduplicated.
- [x] 3.2 Verify a bulk fixture creates one audit observation around multiple automatic JDBC operations rather than one framework observation per entry.
- [x] 3.3 Update the tracing operations guide with the audit hierarchy, stable and contextual names, single-versus-bulk behavior, natural non-action parentage, activation requirements, and privacy exclusions.
- [x] 3.4 Record representative trace evidence showing audit queries and inserts grouped beneath `write audit trail` and separated from domain SQL.
- [x] 3.5 Run focused extension and compatibility test suites, run `git diff --check`, validate the OpenSpec change strictly, and record the commands and results in `validation.md`.
