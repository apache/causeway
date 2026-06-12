## Context

`CommandExportManager_moveCommands` currently retimestamps selected commands as a contiguous block after a target command.
The first moved command is placed 10ms after the target, and subsequent moved commands preserve the original elapsed time between selected commands.
This preserves replay timing, but it can create a moved block whose duration exceeds the available space before the target command's successor.
The requested change adds an explicit user choice to squash those internal timings when the user needs the moved block to fit into a tight gap.

## Goals / Non-Goals

**Goals:**

- Add a checkbox parameter to the move action for enabling timing squash per invocation.
- Keep the existing preserve-gap behavior as the default when the checkbox is not selected.
- When squashing is enabled, assign moved command timestamps at deterministic 1 second increments while preserving selected command order.
- Continue updating both `CommandLogEntry.timestamp` and the timestamp inside the associated command DTO.
- Cover both default and squash behavior with command-log applib tests.

**Non-Goals:**

- Automatically calculate whether the moved commands fit between the target and successor.
- Retimestamp commands that are neither selected nor the target command.
- Change target-command selection, validation rules, export validation, or replay mapping behavior.
- Add a new configuration property or persistent user preference for the checkbox value.

## Decisions

- Add a boolean action parameter rather than a new action.
  This keeps the move-command workflow in one place and lets the user choose the timing policy at the moment of moving commands.
  The alternative was a second squash-specific action, but that would duplicate choices, validation, and UI affordances.

- Default the checkbox to `false`.
  This preserves current behavior for existing users and tests unless they explicitly opt into squashing.
  The alternative was to always squash, but that would remove the existing timing-preservation behavior.

- Represent the timing policy in the move retimestamping helper.
  The current code already centralizes the timestamp rewrite in `moveAfter`, so adding a policy flag there keeps validation and DTO update behavior unchanged.
  The alternative was to split preserve-gap and squash code paths at the action level, but that would make it easier for future changes to update one path and not the other.

- Use a dedicated 1 second gap constant for squash increments.
  This keeps the existing 10ms minimum-gap behavior for non-squashed moves while making squashed moves easier to inspect and reliably ordered.
  The alternative was a configurable increment, but the requirement is fixed at 1 second and configuration would add unnecessary surface area.

## Risks / Trade-offs

- [Risk] Adding a boolean parameter changes the action signature and may affect callers that invoke the action programmatically.
  → Mitigation: Keep validation behavior unchanged and update tests to exercise the new signature.

- [Risk] Squashed timings still may not fit if the available target gap is smaller than 1 second times the moved command count.
  → Mitigation: The checkbox provides deterministic compression but does not promise a fit check; the scope explicitly avoids successor-gap validation.

- [Risk] Users might not understand the difference between preserving and squashing timings.
  → Mitigation: Give the parameter a clear label and description explaining that original timing differences are discarded.
