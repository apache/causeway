## Context

`CommandExportManager_moveCommands` retimestamps selected command log entries so a developer can repair an export sequence before running known-target validation.
That repair is meaningful when command-log recording support is enabled, because export validates that action targets and reference parameters are reachable from roots or earlier results.
When recording support is disabled, export skips dotted-path validation, so command movement is unnecessary and can mutate command history without a matching benefit.

## Goals / Non-Goals

**Goals:**

- Disable command movement from `CommandExportManager` when command-log recording support is disabled.
- Keep the recording-support check in action disablement only, because disablement is evaluated before validation and greys out the action with a prompt reason.
- Preserve existing selection, target-choice, and retimestamp behavior when recording support is enabled.

**Non-Goals:**

- Do not change export validation rules for targets or reference parameters.
- Do not change command recording suppression behavior for export/replay helper view models.
- Do not change the move action's timestamp adjustment algorithm.

## Decisions

### Reuse command-log recording support configuration in the move action

Inject or otherwise access `CausewayConfiguration` from `CommandExportManager_moveCommands`, matching the existing `CommandExportManager_exportSelected` pattern.
Treat the action as available only when `causewayConfiguration.getExtensions().getCommandLog().getRecordingSupport().isEnabled()` is true.
Use the helper in `disableAct()` only, because the framework evaluates disablement before validation and uses that result to grey out the action with a clear reason.

Alternative considered: hide the action entirely when recording support is disabled.
Disablement is preferable because it gives users a clear reason and matches existing action guard patterns in the replay/export UI.

## Risks / Trade-offs

- [Risk] Tests that instantiate the move action without configuration may start seeing the action as disabled.
  → Mitigate by injecting or setting enabled recording support in tests that exercise normal move behavior.
- [Risk] A user with recording support disabled might still want to manually reorder commands for readability.
  → Mitigate by keeping this action scoped to export repair; manual history editing without validation benefit remains out of scope.
