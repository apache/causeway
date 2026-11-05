## 1. Subscriber observation

- [ ] 1.1 Wrap both enabled audit subscriber delegations with the existing observation integration and static operation/context/module metadata.
- [ ] 1.2 Add focused tests for individual/bulk delegation, parentage, exact private metadata, disabled/no-op behavior, original failures and scope cleanup.

## 2. Exported tracing regressions

- [ ] 2.1 Add the audit applib test dependency and exercise the actual subscriber in the existing Boot/agent child-JVM fixture with controlled JDBC work.
- [ ] 2.2 Verify exported audit parentage, JDBC descendants, aggregation, static metadata, failure followed by success and inactive/absent-exporter modes.

## 3. Documentation and verification

- [ ] 3.1 Document callback granularity, names, privacy, natural ancestry and activation in the observability guide and update roadmap step 8a.
- [ ] 3.2 Run focused audit tests, the complete tracing/JPA regression suite, documentation rendering and strict OpenSpec validation; record commands, versions, results and fixture limitations.
