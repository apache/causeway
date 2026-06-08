## Context

Command-log recording support is opt-in through `causeway.extensions.command-log.recording-support` and defaults to disabled.
The metamodel already uses that flag to gate synthetic navigation actions that create dotted replay paths.
Command export known-target validation currently runs from `CommandExportManager_exportSelected` through `CommandExportKnownTargetValidator` without consulting the same flag.
That means the export action can require a dotted path for action targets and reference parameters even when the application has not opted into recording support.

## Goals / Non-Goals

**Goals:**

- Make dotted-path export validation apply only when command-log recording support is enabled.
- Preserve existing validation semantics, ordering, baseline handling, and messages when recording support is enabled.
- Preserve normal export behavior when recording support is disabled by bypassing known-target and known-reference-parameter checks.
- Add tests for both disabled and enabled recording support.

**Non-Goals:**

- Do not change command recording, command replay, YAML serialization, or replay mapping behavior.
- Do not change synthetic navigation action creation, which is already gated by recording support.
- Do not introduce new configuration properties or persistent metadata.

## Decisions

- Inject or otherwise pass the command-log recording support flag at the export action boundary before invoking known-target validation.
  This keeps `CommandExportKnownTargetValidator` focused on validating a selected sequence and keeps the configuration concern near the user-facing action that decides whether validation is required.

- Use a no-op validation path when recording support is disabled.
  This avoids duplicating target and parameter traversal logic and makes disabled support behave like command export did before dotted-path validation was introduced.

- Keep the validator behavior unchanged for enabled support.
  Enabled recording support is the mode that produces safe finder and navigation steps needed by replay, so it should continue to enforce the existing dotted-path contract.

- Alternative considered: move the configuration check into each participant extraction method.
  That was rejected because it scatters the opt-in policy through low-level helper methods and makes it easier for future participants to bypass the guard accidentally.

- Alternative considered: hide or disable the export manager when recording support is disabled.
  That was rejected because command export can still be useful outside recording-enabled dotted-path replay workflows, and the requested behavior is to guard dotted-path validation rather than remove export access.

## Risks / Trade-offs

- [Risk] Disabled recording support may allow exports that are not replayable as dotted-path recordings.
  → Mitigation: this is intentional because the replay-oriented dotted-path contract belongs to the opt-in recording support mode.

- [Risk] Future validation entry points might instantiate `CommandExportKnownTargetValidator` directly and miss the guard.
  → Mitigation: add tests at the export action validation surface and document the policy in the spec so future callers know when to apply it.

- [Risk] Configuration access could be awkward in focused unit tests.
  → Mitigation: pass a simple boolean or small policy supplier into the export action or validator factory so tests can cover both modes without full application bootstrapping.

## Migration Plan

No data migration is required.
Deploying the change relaxes export validation only for applications with recording support disabled.
Rollback restores the current unconditional dotted-path validation behavior.

## Open Questions

None.
