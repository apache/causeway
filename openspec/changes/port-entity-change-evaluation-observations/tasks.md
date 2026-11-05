## 1. Tracker observation

- [ ] 1.1 Inject the existing observation integration, wrap nonempty evaluation including retry, and update explicit tracker construction sites.
- [ ] 1.2 Add focused tests for aggregate/empty/unchanged candidates, retry, handled and escaping failures, natural parentage, static metadata and no-op behavior.
- [ ] 1.3 Verify public lazy evaluation/memoization and publication-after-evaluation ordering.

## 2. Exported trace regression

- [ ] 2.1 Exercise the actual tracker with controlled records and JDBC work through the existing child-JVM fixture.
- [ ] 2.2 Assert Boot/agent evaluation and audit sibling ancestry, JDBC descendants, private metadata, failure cleanup, empty candidates and inactive/absent-exporter behavior.

## 3. Documentation and validation

- [ ] 3.1 Document evaluation granularity, retry, privacy and publication boundaries, and update roadmap step 8b.
- [ ] 3.2 Run the focused persistence build/tests, complete tracing/JPA regressions, documentation rendering and strict change validation; record evidence and limitations.
