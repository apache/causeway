## Context

`CommandExportManager` now exposes `excludedCommands` for command log entries that have been removed from the active export sequence by setting replay state to `EXCLUDED`.
Users can also restore excluded commands to `UNDEFINED` if they need them again.

Some excluded commands are known noise and should be removed from the command log entirely to simplify the export manager view.
Deletion is destructive, so it should be available only from the already-excluded collection and should reject stale or active-command selections.

## Goals / Non-Goals

**Goals:**

- Provide a `deleteCommands` action associated with `excludedCommands`.
- Support deleting one or more selected excluded commands in a single invocation.
- Use `choicesFrom = "excludedCommands"` and explicit selected-command choices sourced from `getExcludedCommands()`.
- Disable the action when there are no excluded commands.
- Validate direct invocation so only current baseline-bounded excluded commands can be deleted.
- Remove selected backing `CommandLogEntry` entities through the repository service.

**Non-Goals:**

- Do not delete active `UNDEFINED` or `EXPORTED` commands from the export manager.
- Do not add deletion from the active `commands` collection.
- Do not add undo support for deleted command log entries.
- Do not change replay-manager deletion semantics for imported replay commands.

## Decisions

1. Delete only from `excludedCommands`.

The action will be associated with the excluded collection and use that collection for choices and validation.
Alternative considered: delete any command selected from either export-manager collection.
That alternative was rejected because deletion is destructive and should require a deliberate prior exclusion step.

2. Implement as a `CommandExportManager_deleteCommands` mixin.

The action name requested by the user is `deleteCommands`, and a mixin keeps behavior consistent with existing export-manager actions.
The mixin will return the current `CommandExportManager` so the page refreshes after deletion.
Alternative considered: reuse `ReplayableCommand_delete` on each row.
That alternative was rejected because this workflow needs multi-select deletion associated with the manager's `excludedCommands` collection.

3. Use explicit choices in addition to `choicesFrom`.

The action annotation will declare `choicesFrom = "excludedCommands"`, and the mixin will also provide `choicesSelected()` returning `commandExportManager.getExcludedCommands()`.
This follows existing command export manager mixin patterns and avoids metadata validation gaps.

## Risks / Trade-offs

- Deletion is irreversible → Limit choices and direct invocation validation to currently excluded commands only.
- Stale selected rows can occur after refresh or concurrent changes → Recompute `excludedCommands` in validation before deleting.
- Repository removal details may vary by persistence implementation → Use the existing `RepositoryService#remove` path already used by replayable-command deletion.
