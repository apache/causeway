## Why

Command export can reject otherwise useful recordings when an action target or reference parameter was obtained by a finder or navigation command that occurred later in the recording than the selected action that needs it.
Developers need an in-product way to repair the baseline-bounded command order so retrospective finder or navigation commands can establish dotted-path participants before dependent commands are exported.

## What Changes

- Add `moveCommand`/`moveCommands` support on `CommandExportManager` so one or more selected commands can be moved relative to another command after the export baseline.
- Provide command choices for the move target from commands at or after the baseline, excluding commands selected for movement.
- Move the selected commands as a contiguous block after the target command while preserving their original relative timing gaps.
- Retimestamp the first moved command to the target command timestamp plus 10ms, then retimestamp subsequent moved commands using their original inter-command timing offsets.
- Ensure command export ordering and known-target validation use the adjusted timestamps after a move.

## Capabilities

### New Capabilities

- `command-export-command-reordering`: Covers reordering baseline-bounded command log entries from `CommandExportManager` so exportable sequences can be repaired before YAML export.

### Modified Capabilities

- `command-export-known-targets`: Clarifies that reordered command timestamps can make retrospective finder or navigation results available to later selected command targets and reference parameters.

## Impact

- Affects `CommandExportManager`, its action contributions, layout/action metadata, and any command-log retimestamping services used by the replay/export UI.
- Affects export validation tests around target and reference parameter reachability because command order can now be intentionally changed before export.
- May require focused integration or unit tests for multi-command movement, target choices, timestamp preservation, and validation behavior after movement.
