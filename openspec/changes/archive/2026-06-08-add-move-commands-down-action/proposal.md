## Why

`CommandExportManager_moveCommands` currently represents only the existing upward command movement workflow, but its generic name makes the available direction ambiguous.
Renaming it to `moveCommandsUp` and adding `moveCommandsDown` makes command ordering repairs explicit in both directions.

## What Changes

- Rename the current `CommandExportManager_moveCommands` action to `CommandExportManager_moveCommandsUp` while preserving its existing upward retimestamping behavior.
- Add a complementary `CommandExportManager_moveCommandsDown` action that moves selected commands later in the export ordering.
- Factor shared selection validation, target choices, replay-state checks, timestamp updates, and timing-gap handling into helper code or a shared superclass.
- Keep command-log recording support guards and active command collection constraints for both movement directions.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-export-command-reordering`: Command export manager movement actions become direction-specific, with upward and downward movement supported from the active command list.

## Impact

- Affects command-log applib action classes under `dom/replay`, especially the existing move action, module registration, and tests.
- May affect generated metadata or layout expectations that refer to `moveCommands`.
- No persistence, YAML export/import, or external dependency changes are expected.
