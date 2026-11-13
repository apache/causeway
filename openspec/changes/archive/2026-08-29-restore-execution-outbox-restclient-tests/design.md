## Context

`RestEasyConfiguration` was removed by `d241d3d918bc` on 7 July 2025 in favor of the Restful Objects configuration exposed through `CausewayConfiguration` and `RestfulPathProvider`.
The execution-outbox REST client test helper retained the removed type, while its integration test also retained obsolete Spring Boot imports for `EntityScan` and `LocalServerPort`.

The module has declared `<maven.test.skip>true</maven.test.skip>` since 3 July 2025.
That property skips test-source compilation as well as execution, so forced `-Dmaven.test.skip=false test-compile` is needed to verify source compatibility.
Restoring runtime execution of this dormant suite is explicitly outside the revised scope.

## Goals / Non-Goals

**Goals:**

- Compile the integration-test sources against current Causeway and Spring Boot APIs when compilation is forced.
- Derive the test helper's REST endpoint from the canonical Restful Objects base-path configuration contract.
- Resolve source-level migration failures without excluding or deleting the dormant tests.

**Non-Goals:**

- Re-enabling or repairing execution of the integration tests.
- Removing or replacing the module's existing `maven.test.skip` setting.
- Changing production `OutboxClient`, `OutboxEvents`, REST wire behavior, authentication, or outbox persistence semantics.
- Restoring `RestEasyConfiguration` or introducing a RESTEasy-specific compatibility layer.

## Decisions

### Use `RestfulPathProvider` as the endpoint-path boundary

`RestEndpointService` will depend on the current `org.apache.causeway.core.config.applib.RestfulPathProvider` rather than reading removed RESTEasy configuration.
It will use `getRestfulPath().orElse("")`, combine that path with `WebAppContextPath`, and retain the existing trailing slash expected by `OutboxClient`.

This follows the migration used by `TemplateResourceServlet` in the removal commit and avoids recreating removed configuration structure or hard-coding `/restful`.

### Migrate test annotations to current Spring Boot packages

`EntityScan` will come from `org.springframework.boot.persistence.autoconfigure` and `LocalServerPort` from `org.springframework.boot.test.web.server`.
No compatibility reflection or duplicate annotations will be introduced.

### Verify compilation without restoring execution

Implementation will run `test-compile` with `-Dmaven.test.skip=false` to prove that the dormant sources compile.
The module-level test suppression remains unchanged because runtime restoration has been deliberately deferred.
Runtime failures discovered while exploring the broader restoration are not addressed by this compilation-only change.

## Risks / Trade-offs

- [Risk] The dormant suite can develop runtime regressions while execution remains suppressed.
  → Mitigation: this change makes only source compatibility explicit; runtime restoration requires a separate, intentionally scoped change.
- [Risk] Future platform migrations can again become hidden by `maven.test.skip`.
  → Mitigation: maintainers can repeat the documented forced `test-compile` check when touching these sources.
- [Risk] Endpoint path assembly could regress without runtime coverage.
  → Mitigation: preserve the established `WebAppContextPath` composition and current `RestfulPathProvider` contract without behavioral refactoring.

## Migration Plan

1. Update current API imports and endpoint-path injection.
2. Force test-source compilation while overriding the existing skip.
3. Leave runtime execution and Maven lifecycle policy unchanged.

Rollback consists of reverting the test-source migration.
