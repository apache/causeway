## 1. Public API and naming

- [ ] 1.1 Add applib service, standalone NOOP helper, package documentation and module export with unchanged throwable semantics.
- [ ] 1.2 Add suffix validation and suffix-preserving 50-character names with focused boundary tests.

## 2. Runtime service

- [ ] 2.1 Implement and register the default service using the existing integration, closure and current execution's domain-facing identity.
- [ ] 2.2 Test results, nesting, identity/restoration, mixin invoked identity preservation, inactive behavior, failures and telemetry cleanup failures; verify Spring registration.

## 3. Exported traces and documentation

- [ ] 3.1 Extend real action fixtures to verify Boot/agent exported application regions, nested JDBC ancestry, current declared/mixin identities and failure-followed-by-success cleanup.
- [ ] 3.2 Verify fallback naming, long suffixes and inactive/absent-exporter modes without changing framework span behavior.
- [ ] 3.3 Document usage, privacy, validation, checked exceptions and synchronous limits; update roadmap step 9.
- [ ] 3.4 Run applicable focused builds/tests, full tracing regressions, documentation rendering and strict change validation; record evidence and limitations.
