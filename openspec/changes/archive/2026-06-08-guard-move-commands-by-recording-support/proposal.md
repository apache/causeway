## Why

Command movement in `CommandExportManager` is only useful for repairing recordings that rely on command-log recording support and dotted-path validation.
When recording support is disabled, moving commands changes historical command timestamps without helping export validation, so the action should not be available.

## What Changes

- Disable the `CommandExportManager` move commands action when command-log recording support is `DISABLED`.
- Keep the recording-support check in action disablement so the move action is greyed out with a clear reason.
- Keep existing move behavior unchanged when command-log recording support is `ENABLED`.

## Capabilities

### New Capabilities

### Modified Capabilities

- `command-export-command-reordering`: Requires command movement to be available only when command-log recording support is enabled.

## Impact

- Affects `CommandExportManager_moveCommands` and its validation/disablement behavior.
- Requires focused tests for recording support enabled and disabled action disablement cases.
- Does not change export YAML, replay import, or known-target validation behavior.
