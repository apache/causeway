## Context

`CommandExportManager` lists commands after a baseline and exports selected `ReplayableCommand` rows in timestamp order.
Known-target validation treats menu-service roots and earlier command results as the only ways for selected action targets and reference parameters to be reachable in an exported sequence.
When a developer records an action first and only later records the finder or navigation action that explains the missing target or parameter, selecting both commands still fails because the explanatory command is later in export order.
The manager already exposes collection-associated actions such as `exportSelected` and `makeSelectedExportable`, so the reordering feature should fit the same UI model and operate on selected collection rows.

## Goals / Non-Goals

**Goals:**

- Add a prototyping-only `CommandExportManager` action that moves one or more selected commands relative to another command at or after the baseline.
- Exclude moved commands from the target choices so a command cannot be moved after itself.
- Preserve the selected commands as a contiguous block and preserve their original internal timing gaps.
- Retimestamp moved commands so the first moved command occurs 10ms after the chosen target command.
- Let existing export sorting and known-target validation naturally observe the repaired order.

**Non-Goals:**

- Do not infer missing finder or navigation commands automatically.
- Do not change YAML export format or replay import semantics.
- Do not relax known-target or reference-parameter validation rules.
- Do not reorder commands before the export manager baseline.

## Decisions

### Model command movement as a collection-associated action

Add a `CommandExportManager_moveCommands` mixin or equivalent action contribution associated with the `notYetExported` collection.
The action should accept `List<ReplayableCommand> selected` and a single `ReplayableCommand target` parameter.
This follows the existing `exportSelected` and `makeSelectedExportable` conventions and keeps the UI affordance near the exportable command list.

Alternative considered: add a per-command move action on `ReplayableCommand`.
That would make multi-command movement awkward and would not naturally express exclusion of selected rows from target choices.

### Use baseline-bounded target choices

The target choices should come from `CommandExportManager.getNotYetExported()` or the same repository query used by that collection, filtered to remove selected commands.
This ensures choices are commands since the baseline and visible in the current export context.
The action should require a non-empty selection and a non-null target.
It should reject a target that is not part of the baseline-bounded command set or that is one of the selected commands, even if a caller bypasses UI choices.

Alternative considered: allow any command log entry as the move target.
That could accidentally move commands outside the export repair context and make pagination or baseline behavior surprising.

### Retimestamp the selected block in one ordered pass

Resolve selected `ReplayableCommand` instances to their `CommandLogEntry` rows and sort them by their original timestamps before mutating any timestamps.
Let `baseTimestamp` be the chosen target command timestamp plus 10ms.
Set the first moved entry to `baseTimestamp`.
For each following moved entry, add the original time gap between that entry and the previous selected entry to the prior new timestamp.
If an original gap is null or negative because of equal or inconsistent timestamps, use a minimum 10ms increment to preserve deterministic ordering.

Alternative considered: space every moved command by a fixed 10ms.
That would be simpler, but it would discard recorded spacing that can help humans understand the original interaction flow.

### Persist by updating command log timestamps

Use the existing mutable `CommandLogEntry#setTimestamp` API so existing repository ordering, collection display, export sorting, and validator logic continue to work without separate order metadata.
After the action executes, return the same `CommandExportManager` and rely on page refresh to show changed ordering, consistent with export actions that mutate replay state.

Alternative considered: add an explicit export-order column or per-export override.
That would require schema changes and extra export logic for a repair workflow that is intentionally scoped to command-log maintenance.

## Risks / Trade-offs

- [Risk] Retimestamping command log entries changes historical recording metadata.
  → Mitigate by keeping the action prototyping-only, recording-suppressed, and explicit in its description.
- [Risk] Moving commands after a target near following commands could create timestamp collisions with commands not selected for movement.
  → Mitigate by applying deterministic increments and relying on existing timestamp sort; add tests around adjacent commands.
- [Risk] Pagination could hide a valid target command that is after the baseline but outside the current limit.
  → Mitigate initially by using the visible baseline-bounded collection choices and document that the baseline/limit can be adjusted before moving.
- [Risk] Moving commands can make other selected commands invalid if their required known participants move after them.
  → Mitigate by not bypassing export validation; users can move again until validation succeeds.
