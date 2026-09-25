## Why

Operators can correlate Causeway command, execution-log, and audit records through the interaction ID, but exported traces do not currently expose that identifier.
Adding the interaction ID to the root interaction span allows an operator to locate the containing trace directly from a known interaction ID, including when using Jaeger's in-memory storage.

## What Changes

- Add the current Causeway interaction ID to each `causeway.root.interaction` observation as the high-cardinality `causeway.interaction.id` span attribute.
- Keep the attribute on the root interaction span rather than copying it to the agent-owned HTTP entry span or every descendant span.
- Preserve current span names, parentage, behavior without active observation, and failure handling.
- Extend tracing verification and operational documentation to cover interaction-ID correlation and Jaeger tag search.

## Capabilities

### New Capabilities

- `interaction-trace-correlation`: Defines how a top-level Causeway interaction exposes its interaction ID for trace correlation.

### Modified Capabilities

None.

## Impact

The change affects root interaction observation creation in `core/runtimeservices`, its unit tests, the OpenTelemetry Java-agent compatibility regression, and the Micrometer tracing operations runbook.
It adds no dependency, public API, exporter, or telemetry SDK and does not alter the existing opt-in `observation` profile.
The UUID-valued attribute is intentionally high-cardinality and may increase telemetry storage or indexing costs when traces are retained.
