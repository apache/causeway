## Why

The command export manager was designed around batch export, where exported commands are removed from the default working list by changing replay state from `UNDEFINED` to `EXPORTED`.
That model no longer fits the more fluid workflow needed to keep dotted paths valid and move commands into replayable order.

## What Changes

- Replace the export manager's mode-based command collections with a single `commands` collection containing all commands at or after the baseline.
- Remove the UI mode and toggle action that switch between not-yet-exported and exported commands.
- Continue updating replay state from `UNDEFINED` to `EXPORTED` when commands are exported, so users can see which commands have already been exported.
- Include replay state as a visible property in the unified `commands` list.
- **BREAKING**: Remove the old `notYetExported` and `exported` collections and their mode-driven navigation semantics without preserving backward compatibility.

## Capabilities

### New Capabilities
- `command-export-manager-command-list`: Defines the unified baseline-bounded command list, replay-state visibility, and removal of mode-based filtering.

### Modified Capabilities
- `command-export-command-reordering`: Align move-command selection and target choices with the unified baseline-bounded command list.
- `command-export-known-targets`: Clarify that export validation evaluates selected commands within the unified baseline-bounded command list rather than a replay-state-filtered list.

## Impact

- Affects `CommandExportManager` and related mixins under `extensions/core/commandlog/applib`.
- Affects fallback layout and column order resources for the export manager.
- Affects tests that assert mode behavior, collection names, command filtering, move choices, export selection, and replay-state display.
- No new dependencies are expected.
