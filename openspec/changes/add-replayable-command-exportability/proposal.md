## Why

Command export validation currently runs only after the user attempts an export, so users must discover sequence problems through a failed action.
Showing each replayable command's exportability in the export manager list gives earlier feedback and helps users reorder or include prerequisite commands before exporting.

## What Changes

- Add a nullable Boolean exportability property to `ReplayableCommand`.
- Compute the property when replayable commands are created for `CommandExportManager#getCommands()` by sharing the current export manager through `Scratchpad`.
- Reuse the existing baseline-bounded known-target and reference-parameter validation rules so the per-command indicator matches export behavior.
- Keep replayable commands created outside the export manager context with an unknown exportability value.
- No breaking changes.

## Capabilities

### New Capabilities
- `replayable-command-exportability`: Indicates whether a replayable command is currently exportable in the command export manager context.

### Modified Capabilities
- `command-export-known-targets`: Reuses existing export target and reference-parameter validation semantics to compute per-command exportability before export.

## Impact

- Affects `CommandExportManager`, `ReplayableCommand`, and the command export validation helper classes in `extensions/core/commandlog/applib`.
- Uses the request-scoped `Scratchpad` service to pass export-manager context while constructing replayable commands.
- Adds or updates unit tests for per-command exportability values in valid, invalid, and non-export-manager contexts.
