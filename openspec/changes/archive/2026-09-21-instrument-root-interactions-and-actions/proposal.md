## Why

The Boot 2.7 observation substrate can export framework-owned spans through the OpenTelemetry Java agent, but Causeway does not yet describe its own interaction or action lifecycle in those traces.
Adding a deliberately narrow semantic layer now provides useful application-level trace structure while limiting backport risk and avoiding broad instrumentation of the maintenance branch.

## What Changes

- Create one observation for each top-level Causeway interaction using the existing `InteractionServiceDefault` lifecycle.
- Create one child observation around each `MemberExecutorServiceDefault.invokeAction(...)` execution.
- Use stable observation names and explicit low-cardinality tags for the action identifier and initiation mode.
- Record failures and close observation scopes reliably on successful, exceptional, and defensive cleanup paths.
- Preserve inactive-profile no-op behavior and active-without-agent safe degradation.
- Extend automated coverage to verify semantic span creation, cleanup, and parentage through the real OpenTelemetry Java agent.
- Keep nested interactions, property edits, Wicket-specific spans, transactions, execution publishing, persistence semantics, sensitive values, and argument capture outside this change.

## Capabilities

### New Capabilities

- `root-interaction-action-observability`: Defines profile-gated semantic observations for top-level Causeway interactions and action invocations, including stable metadata, lifecycle safety, privacy boundaries, and trace parentage.

### Modified Capabilities

None.

## Impact

- `core/runtimeservices` gains observation integration in `InteractionServiceDefault` and `MemberExecutorServiceDefault`.
- Existing constructor wiring and focused runtime-service tests require updates for the observation dependency.
- The tracing compatibility regression harness gains semantic parentage coverage using the production instrumentation.
- No public applib API, persistence schema, exporter configuration, or application-owned OpenTelemetry SDK is introduced.
