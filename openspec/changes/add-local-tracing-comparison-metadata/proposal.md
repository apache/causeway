## Why

Local regression comparisons need baseline and candidate processes to appear under the same Jaeger service while remaining independently searchable.
The current helper configures only `service.name`, so operators must either split one logical application into separate services or cannot reliably filter the two variants.

## What Changes

- Keep `OTEL_SERVICE_NAME` as the shared logical application name for both baseline and candidate processes.
- Introduce `OTEL_SERVICE_ROLE` as a helper-owned input for the low-cardinality `service.role` resource attribute, with expected regression values `baseline` and `candidate`.
- Introduce `OTEL_SERVICE_VERSION` as a helper-owned input for the standard `service.version` resource attribute.
- Default `OTEL_SERVICE_VERSION` to the current application's full Git commit SHA when the caller has not supplied a value and the working directory belongs to a Git repository.
- Preserve and extend any existing `OTEL_RESOURCE_ATTRIBUTES` rather than replacing environment or platform metadata.
- Report the resolved service name, role, and version when local tracing is enabled, and warn when a Git-derived version comes from a dirty worktree.
- Document the two-process Jaeger regression workflow, filtering examples, explicit-version override, and non-Git fallback.

## Capabilities

### New Capabilities
- `local-tracing-comparison-metadata`: Defines local tracing resource metadata for grouping baseline and candidate processes under one service and filtering them by role and version.

### Modified Capabilities

None.

## Impact

The change affects `scripts/otel-local-env.sh`, its shell-level regression coverage, and `adoc/micrometer-tracing-operations.adoc`.
It adds no application runtime dependency and does not change Causeway span creation, semantic span names, sampling, or export ownership.
`OTEL_SERVICE_ROLE` and `OTEL_SERVICE_VERSION` are convenience inputs consumed by the helper script; the Java agent receives their values through `OTEL_RESOURCE_ATTRIBUTES`.
