## Context

`CommandExportManager` exposes a single active `commands` collection for commands at or after the export baseline.
Each `ReplayableCommand` in that collection can expose a nullable `exportable` property when the command is constructed in export-manager context.
`CommandExportManager_exportSelected` already uses `choicesFrom = "commands"` and an explicit `choicesSelected()` member support method so the action parameter can select from the active command collection.
The action currently has filename defaults but no default for `selected`, so the user must select exportable rows manually.

## Goals / Non-Goals

**Goals:**

- Default the `selected` parameter for `CommandExportManager_exportSelected` to active commands whose computed exportability is `true`.
- Preserve the existing full active-command choices so users can override the default selection.
- Reuse existing exportability computation and export validation rather than introducing a second definition of exportability.

**Non-Goals:**

- Do not change the command export YAML format.
- Do not change the rules that decide whether a command is exportable.
- Do not change replay state until export succeeds.
- Do not hide non-exportable commands from the active `commands` collection or from the export action choices.

## Decisions

1. Add `defaultSelected()` to `CommandExportManager_exportSelected`.

   The default method will call `commandExportManager.getCommands()` and filter commands where `Boolean.TRUE.equals(command.getExportable())`.
   This aligns with Causeway multi-select parameter conventions and mirrors the existing `CommandExportManager_excludeCommands.defaultSelected()` pattern.

2. Keep `choicesSelected()` returning `commandExportManager.getCommands()`.

   The selectable universe remains the full active command collection, including already exported or non-exportable commands.
   Validation continues to protect execution when a user overrides defaults with an invalid selection.

3. Treat `null` exportability as not selected by default.

   In export-manager context the property is expected to be non-null for active commands, but filtering only `Boolean.TRUE` avoids selecting commands when exportability cannot be computed.
   This is safer than falling back to validation-only behavior because it avoids surprising default exports.

## Risks / Trade-offs

- [Risk] Computing defaults calls `getCommands()` and may compute exportability for the collection before the page renders.
  → Mitigation: this reuses the same collection and computation already needed for rendering and validation.
- [Risk] An empty default selection is possible when no active command is exportable.
  → Mitigation: existing validation still reports that at least one command must be selected if the action is invoked empty.
- [Risk] Users might assume non-selected commands are hidden rather than merely not defaulted.
  → Mitigation: keep choices as the full active collection and rely on the visible `exportable` property to explain the default.
