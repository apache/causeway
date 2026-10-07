## Why

Dropping a short JPA parent while retaining independently exported JDBC children fragments trace trees. The policy also differs between Boot-managed and agent-managed export, so it does not provide coherent database-operation filtering.

## What Changes

- **BREAKING:** Remove `causeway.observation.jpa-duration-threshold`, the duration wrapper and the now-empty observation policy configuration/API.
- Retain successful and failed Causeway JPA spans regardless of duration, subject to normal sampling and export; retain existing names, attributes and ancestry.
- Remove filtering examples and agent-only threshold overrides from the guides and Petclinic launcher.
- Preserve explicit discard behavior used by transaction instrumentation; it is independent of duration filtering.
- Track possible general duration filtering for all span types in `openspec/planned-changes/general-span-duration-filtering.md`, including descendant handling and Boot/agent consistency. No replacement filtering is implemented here.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `main-observation-foundation`: remove the duration-wrapper lifecycle contract while preserving other observation guarantees.
- `observation-policy`: replace JPA threshold configuration with duration-independent span retention and update operator guidance.

## Impact

`core/config`, `persistence/jpa/integration`, runtime observation tests, tracing compatibility fixtures, the Petclinic launcher/configuration and the M3/user observability guides. No new dependency, exporter, SDK, persistence schema or span-name migration. Existing user identity, root UUID and execution-mode attributes remain automatic.
