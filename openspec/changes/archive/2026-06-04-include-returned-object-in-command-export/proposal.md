## Why

Command export YAML currently serializes each selected `CommandLogEntry` through its underlying `CommandDto`, which includes the command target but does not expose the persisted returned object from `CommandLogEntry#getResult()` in the exported data.
Including the returned object makes exported commands easier to inspect, correlate, and replay-aware tooling can reason about action results without querying the original command log entry.

## What Changes

- Extend exported command YAML to include a returned object entry for each command when `CommandLogEntry#getResult()` is present.
- Represent the returned object with the same bookmark shape used for the target: `logicalTypeName` and `id`.
- Preserve existing export behavior for commands with no returned object by omitting the returned object entry or leaving it null, as appropriate for the existing YAML serialization conventions.
- Keep import/replay compatibility so existing command import continues to consume the command DTO data required for replay.

## Capabilities

### New Capabilities
- `command-export-yaml-result`: Exported command YAML records the returned object bookmark alongside existing command information.

### Modified Capabilities

## Impact

- Affected code is expected in the command log replay/export package, especially `CommandExportManager_exportSelected` and supporting DTO/YAML transformation code.
- Tests should cover YAML output for commands with and without `CommandLogEntry#getResult()`.
- The generated YAML file structure changes additively, with no breaking change expected for existing command replay imports.
