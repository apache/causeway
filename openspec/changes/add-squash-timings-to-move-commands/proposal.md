## Why

Moving a group of commands currently preserves the original timing gaps between the selected commands.
When those gaps are large, the moved block can extend beyond the gap between the target command and its successor, making the replay order ambiguous or disrupting commands that should remain after the moved block.

## What Changes

- Add a checkbox parameter to the `CommandExportManager_moveCommands` action that lets the user choose whether to squash timings while moving commands.
- When squash timings is selected, retimestamp moved commands as a contiguous sequence starting 10ms after the target command, with each moved command exactly 10ms after the preceding moved command.
- Preserve the existing timing-gap behavior when squash timings is not selected.
- Keep validation, target choices, selected command ordering, and non-selected command timestamps unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-export-command-reordering`: Adds an optional timing-squash mode to command movement so moved commands can fit into a narrow target gap.

## Impact

- Affects `CommandExportManager_moveCommands` action parameters and retimestamping logic.
- Affects command-log applib tests covering command movement behavior.
- No new dependencies or external APIs are expected.
