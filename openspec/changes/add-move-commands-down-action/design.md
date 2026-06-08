## Context

`CommandExportManager_moveCommands` is currently the only collection action for retimestamping selected active commands relative to a target command.
It retimestamps selected commands as a contiguous block after the target timestamp, optionally preserving original gaps or squashing timings.
In practice this supports the existing upward workflow, but the generic action name does not make the direction explicit and there is no companion action for moving selected commands downward in the export order.

## Goals / Non-Goals

**Goals:**

- Rename the existing manager-level movement action to `CommandExportManager_moveCommandsUp` and expose it as `moveCommandsUp`.
- Add `CommandExportManager_moveCommandsDown` and expose it as `moveCommandsDown`.
- Share common movement validation, target choice, active command lookup, command-log-entry extraction, timestamp update, and timing-gap logic.
- Preserve the existing squash-timings parameter semantics for both directions.

**Non-Goals:**

- Do not change the active `commands` collection rules.
- Do not change known-target validation rules or exportability computation.
- Do not change YAML export/import formats.
- Do not change replay states while moving commands.

## Decisions

1. Use direction-specific mixin classes for the user-facing actions.

   `CommandExportManager_moveCommandsUp` will replace the current generic action and keep the current user workflow.
   `CommandExportManager_moveCommandsDown` will be a separate action contribution so action names, labels, descriptions, and tests are explicit.

2. Factor shared behavior into implementation support code.

   Prefer a package-private helper or abstract superclass in `dom/replay` that owns validation, choices, command extraction, timestamp updates, and common constants.
   The two user-facing mixins should delegate direction-specific retimestamping decisions rather than duplicating the current implementation.

3. Model movement direction in terms of target placement.

   Upward movement places the selected block immediately after a target command that is before the first selected command.
   Downward movement places the selected block immediately after a target command that is after the last selected command.
   This makes choosing the next command after the selected block move the block down by one position rather than leaving it visually unchanged.
   The downward algorithm should preserve selected order, support squash timings, and use deterministic minimum gaps.

4. Filter target choices by movement direction.

   `moveCommandsUp` should only offer targets before the first selected command because later targets would move the block down or keep it in place.
   `moveCommandsDown` should only offer targets after the last selected command because earlier targets would move the block up or keep it in place.
   Direct invocation validation should enforce the same directional constraint as the choices.

5. Keep validation constraints symmetric.

   Both actions require command-log recording support to be enabled.
   Both actions operate only on active commands at or after the export baseline.
   Both actions exclude selected commands from target choices and reject missing selection, missing target, target-in-selection, excluded commands, commands outside the active set, and targets on the wrong side of the selected block.

## Risks / Trade-offs

- [Risk] Renaming the action may require updates to generated metadata, layouts, tests, or bookmarks that reference `moveCommands`.
  → Mitigation: search all source and test references and update only manager-level movement references.
- [Risk] Downward retimestamping could collide with commands between the moved block and target when the available gap is small.
  → Mitigation: use deterministic minimum spacing and rely on existing command ordering semantics; add focused tests for representative timestamp layouts.
- [Risk] Sharing behavior through an abstract superclass may make framework member-support discovery confusing if helper methods are inherited unexpectedly.
  → Mitigation: keep framework-facing `@MemberSupport` methods in concrete mixins when clearer, and delegate internal logic to a package-private helper if inheritance creates ambiguity.
