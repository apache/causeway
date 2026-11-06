## 1. Public API and startup registry

- [x] 1.1 Add primer/registrar/registry APIs, immutable positional arguments, package exports and argument tests.
- [x] 1.2 Add and register the metamodel-backed startup registry with exact validation, immutable publication, failure closure and deterministic dispatch tests.

## 2. Action and view integration

- [x] 2.1 Invoke optional action primers after event argument adjustment, using current domain-facing identity/mixee targets and excluding pass-through/pseudo-getters.
- [x] 2.2 Test event-adjusted arguments, order, non-event/nested/mixin paths, unmatched/excluded paths and failure propagation through the real action executor.
- [x] 2.3 Add authorized initial/later DomainObjectPage priming and identity-based request admission without serialized state.
- [x] 2.4 Test actual request-cycle full/Ajax admission, authorization, callback failure/re-entry and fresh requests while preserving existing Wicket preparation behavior.

## 3. Documentation and validation

- [x] 3.1 Document registration, exact matching, immutable arguments, read-only/idempotent contracts and lifecycle limits; update roadmap 10a without adding 10b observations.
- [x] 3.2 Run focused builds/tests, full relevant Wicket regressions, documentation rendering and strict change validation; record evidence and limitations.
