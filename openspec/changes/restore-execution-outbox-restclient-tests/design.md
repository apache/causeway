## Context

`RestEasyConfiguration` was removed by `d241d3d918bc` on 7 July 2025 in favor of the Restful Objects configuration exposed through `CausewayConfiguration` and `RestfulPathProvider`.
The execution-outbox REST client test helper retained the removed type, while its integration test also retained obsolete Spring Boot imports for `EntityScan` and `LocalServerPort`.

The module has declared `<maven.test.skip>true</maven.test.skip>` since 3 July 2025.
That property skips test-source compilation as well as execution, so normal `mvn clean install` builds report success while the disabled sources do not compile.
Forcing `-Dmaven.test.skip=false test-compile` currently exposes these failures before any runtime test diagnosis can occur.

## Goals / Non-Goals

**Goals:**

- Compile the integration-test sources against the current Causeway and Spring Boot APIs.
- Derive the REST endpoint from the same canonical Restful Objects base-path configuration used by production infrastructure.
- Restore the existing integration suite to ordinary Maven and reactor participation.
- Diagnose and fix runtime failures revealed after compilation without weakening the assertions.

**Non-Goals:**

- Changing the production `OutboxClient` API or wire protocol.
- Restoring `RestEasyConfiguration` or introducing a RESTEasy-specific compatibility layer.
- Changing the default `/restful` endpoint, web context-path rules, authentication policy, or outbox persistence semantics.
- Replacing the current Restful Objects viewer implementation.

## Decisions

### Use `RestfulPathProvider` as the endpoint-path boundary

`RestEndpointService` will depend on the current `org.apache.causeway.core.config.applib.RestfulPathProvider` rather than reading removed RESTEasy configuration.
It will use `getRestfulPath().orElse("")`, combine that path with `WebAppContextPath`, and retain the existing trailing slash expected by `OutboxClient`.

This follows the migration used by `TemplateResourceServlet` in the removal commit and keeps the test helper aligned with the canonical Restful Objects base path.
Injecting the provider is preferred to recreating removed configuration structure or hard-coding `/restful`.

### Migrate test annotations to current Spring Boot packages

`EntityScan` will come from `org.springframework.boot.persistence.autoconfigure` and `LocalServerPort` from `org.springframework.boot.test.web.server`.
No compatibility reflection or duplicate annotations will be introduced.
These packages are already used by active modules in the repository.

### Separate compile restoration from runtime restoration

Implementation will first run test compilation with `-Dmaven.test.skip=false` while the POM suppression still exists.
After all sources compile, the integration suite will run explicitly and any runtime failures will be diagnosed against the original assertions and fixture lifecycle.
The skip property and its stale TODO will be removed only after focused tests pass.

This ordering distinguishes source migration failures from application-context, endpoint, transaction, or delivery failures.

### Re-enable tests through ordinary Maven lifecycle configuration

The module-level `maven.test.skip` property will be removed rather than replaced with another default skip mechanism.
Developers may still use standard command-line skip flags for local builds, but repository defaults must compile and execute the suite.
Module `test`, `verify`, and root-reactor builds will therefore regain coverage.

## Risks / Trade-offs

- [Risk] Compiling the sources may reveal additional platform migrations beyond the three known stale imports.
  → Mitigation: force `test-compile` first and resolve each error using current APIs already established elsewhere in the repository.
- [Risk] Runtime failures may have been the original reason for suppressing the suite.
  → Mitigation: preserve assertions, inspect server and client behavior, and fix deterministic setup or production regressions instead of reinstating a skip.
- [Risk] Re-enabling integration tests increases reactor duration.
  → Mitigation: keep the suite focused, reuse its current application context, and verify that it has bounded deterministic waits and cleanup.
- [Risk] Endpoint path assembly could introduce missing or duplicate slashes.
  → Mitigation: preserve `WebAppContextPath.prependContextPath` and the existing single trailing-slash client contract, with focused assertions where practical.

## Migration Plan

1. Update current API imports and endpoint-path injection.
2. Force test-source compilation while overriding the existing skip.
3. Run and repair the focused integration suite.
4. Remove `maven.test.skip` and its TODO.
5. Verify module test, install, RAT, and applicable reactor behavior with default settings.

Rollback consists of reverting the source and POM changes together; restoring a default test skip is not an acceptable completed state.

## Open Questions

None at proposal time.
Runtime failures discovered after compilation will be treated as implementation findings and will update the design only if they require a change in scope or behavior.
