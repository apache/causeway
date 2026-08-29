## 1. Restore test-source compilation

- [ ] 1.1 Replace `RestEasyConfiguration` in `RestEndpointService` with the current `RestfulPathProvider` contract while preserving context-path and trailing-slash URI assembly.
- [ ] 1.2 Migrate `EntityScan`, `LocalServerPort`, and any other stale test imports to current Spring Boot and Causeway APIs.
- [ ] 1.3 Run forced test compilation with `-Dmaven.test.skip=false` and resolve every remaining compile error without excluding sources or weakening compiler checks.

## 2. Restore integration behavior

- [ ] 2.1 Run the focused execution-outbox REST client integration suite and diagnose application-context, endpoint, authentication, persistence, transaction, serialization, and cleanup failures.
- [ ] 2.2 Fix deterministic runtime regressions while preserving the existing outbox recording, delivery, acknowledgment, ordering, and authorization assertions.
- [ ] 2.3 Add or refine focused endpoint-path and repeated-run coverage where needed to protect configured-path composition and fixture isolation.

## 3. Re-enable default lifecycle coverage

- [ ] 3.1 Remove the module-level `maven.test.skip` property and stale TODO after focused compilation and tests pass.
- [ ] 3.2 Verify the module's default `test`, `verify`, install, and RAT lifecycles execute the restored suite successfully without command-line skip overrides.
- [ ] 3.3 Run the applicable parent-reactor build, strict OpenSpec validation, IntelliJ compilation or inspections, and diff checks to confirm the module can no longer hide stale test sources.
