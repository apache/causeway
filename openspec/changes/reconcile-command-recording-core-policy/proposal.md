## Why

Causeway 4 lacks the maintenance-branch control contracts needed to enable command recording safely without recursively recording commandlog helper interactions or startup fixture activity.
This change establishes dependency-graph node C1 as the foundation for later safe-action, property-edit, synthetic-navigation, and replay work.

## What Changes

- Add an opt-in `causeway.extensions.command-log.recording-support` configuration property with `ENABLED` and `DISABLED` values and a default of `DISABLED`.
- Add a core applib marker that allows domain objects and view models to suppress recording-support command logging while preserving normal invocation behaviour.
- Ensure the normal execution and commandlog subscriber flow honours the suppression marker without creating a parallel logging mechanism.
- Add core applib pause and resume application events for temporarily suspending commandlog persistence.
- Make commandlog persistence pause-aware for ready, started, and completed command notifications, including nested pause scopes.
- Suppress commandlog persistence while an initial fixture script is installed and always resume it when the fixture scope exits.
- Mark the existing commandlog export, replay, entry, and replayable-command helper types that are present on `main` as recording-suppressed.
- Preserve existing Causeway 4 command-publishing behaviour while recording support is disabled.
- Defer safe-action publishing, property-edit publishing, synthetic actions, result metadata, replay mapping, and command-manager redesign to later reconciliation changes.

## Capabilities

### New Capabilities

- `command-recording-control`: Defines opt-in recording-support configuration, target-level recording suppression, nested commandlog persistence pause/resume, and startup fixture suppression.

### Modified Capabilities

None.

## Impact

The change affects public contracts in `api/applib`, immutable configuration records in `core/config`, command execution or publishing coordination in `core/runtimeservices`, initial fixture installation, and the commandlog extension subscriber and helper view models.
The new applib contracts must remain independent of the commandlog extension so applications and fixture infrastructure do not acquire a reverse dependency on commandlog.
The configuration and implementation must use the current Causeway 4 record-based configuration, Jakarta, and Spring Boot 4 architecture rather than copying the mutable Causeway 2 implementation.
The programme context and downstream dependencies are recorded in `openspec/reconciliation/maintenance-branch/`.
