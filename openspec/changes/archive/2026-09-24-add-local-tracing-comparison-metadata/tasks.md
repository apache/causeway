## 1. Comparison Metadata Resolution

- [x] 1.1 Extend `scripts/otel-local-env.sh` to consume optional `OTEL_SERVICE_ROLE` and `OTEL_SERVICE_VERSION` convenience inputs.
- [x] 1.2 Resolve an unset service version from the full `HEAD` commit SHA of the current working directory, while allowing an explicit version to take precedence.
- [x] 1.3 Detect tracked and untracked changes when the version is Git-derived and emit a dirty-worktree warning without modifying the SHA.
- [x] 1.4 Handle a missing Git repository or commit by warning and continuing without a synthesized service version.

## 2. Resource Attribute Composition

- [x] 2.1 Map resolved role and version values to `service.role` and `service.version` in `OTEL_RESOURCE_ATTRIBUTES`.
- [x] 2.2 Preserve unrelated existing resource attributes and deterministically replace conflicting `service.role` or `service.version` keys without duplicates.
- [x] 2.3 Extend helper completion output to show the resolved service name and any role and version values.

## 3. Regression Coverage

- [x] 3.1 Add focused shell coverage for shared service naming, baseline and candidate roles, explicit service-version precedence, and Git-derived full SHA behavior.
- [x] 3.2 Add coverage for clean, dirty, non-Git, and existing-resource-attribute cases, including conflict replacement and preservation of unrelated attributes.
- [x] 3.3 Verify that sourced-script failure and repeated sourcing behavior remain safe and that no application-owned telemetry component is introduced.

## 4. Operations Documentation and Verification

- [x] 4.1 Update `adoc/micrometer-tracing-operations.adoc` with baseline and candidate terminal examples using one service name and distinct roles.
- [x] 4.2 Document Git-derived version behavior, explicit overrides for externally built dependencies, dirty-worktree warnings, non-Git fallback, and preservation of platform resource attributes.
- [x] 4.3 Verify locally that Jaeger groups both processes under one service and filters traces by `service.role` and `service.version`.
- [x] 4.4 Run focused shell tests and strict OpenSpec validation.
