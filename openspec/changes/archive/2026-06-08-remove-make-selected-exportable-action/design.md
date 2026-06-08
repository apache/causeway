## Context

`CommandExportManager` now exposes a single active `commands` collection with row-level exportability state, explicit export, exclusion, and movement actions.
`CommandExportManager_makeSelectedExportable` predates the current workflow and sets selected command replay state back to `UNDEFINED` through `ReplayableCommand.makeExportable()`.
That collection action overlaps with older state-management behavior and is no longer part of the desired command export manager surface.

## Goals / Non-Goals

**Goals:**

- Remove the obsolete `CommandExportManager_makeSelectedExportable` collection action from the command export manager UI and module registration.
- Remove tests, metadata expectations, or layout references that exist only for that collection action.
- Keep the current command export, exclusion, unexclusion, deletion, movement, and row-level exportability behavior intact.

**Non-Goals:**

- Do not remove `ReplayableCommand_makeExportable` unless implementation proves it is unused and obsolete outside this manager action.
- Do not change how exportability is computed.
- Do not change command replay states during export except through existing supported actions.
- Do not change YAML export or import formats.

## Decisions

1. Delete the manager-level action contribution.

   Remove `CommandExportManager_makeSelectedExportable` and unregister it from `CausewayModuleExtCommandLogApplib`.
   This removes the collection action from the metamodel instead of merely disabling or hiding it.

2. Preserve per-command behavior unless separately proven obsolete.

   The request specifically targets the `CommandExportManager` collection action.
   Keeping `ReplayableCommand_makeExportable` avoids a broader behavior removal unless tests and metadata show it is exclusively tied to the obsolete workflow.

3. Update metadata and tests to assert absence where appropriate.

   Existing generated metadata or layout expectations may list action contributions.
   Focused tests should verify the obsolete action is no longer registered or exposed while avoiding brittle UI-only assertions when module registration coverage is sufficient.

## Risks / Trade-offs

- [Risk] Removing the action class may break generated metadata tests that assert exact action lists.
  → Mitigation: update those expectations to remove only the obsolete action.
- [Risk] Users who still relied on the older workflow lose a shortcut to reset replay state from the manager collection.
  → Mitigation: this is intentional because exclusion, unexclusion, movement, and exportability indicators define the current workflow.
- [Risk] `ReplayableCommand_makeExportable` may become unused dead code.
  → Mitigation: check references during implementation and remove it only if it is demonstrably obsolete beyond the requested manager action.
