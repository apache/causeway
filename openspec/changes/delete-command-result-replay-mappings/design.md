## Context

`CommandLogMenu` currently exposes command-log finder actions, replay result mapping finders, and prototype-only `exportManager` and `replayManager` actions under the Activity menu.
Replay result mappings are queried through `CommandReplayResultMappingRepository`, but that repository does not currently expose a bulk delete operation for operators who need to reset replay mapping state.
The existing replay mapping finder actions are hidden when no repository bean is available, because mapping persistence is optional and persistence-module-specific.
The requested menu order is a UI layout concern and can be addressed with `@ActionLayout(sequence)` on the existing menu action classes.

## Goals / Non-Goals

**Goals:**

- Provide a destructive, confirmation-protected menu action that deletes every persisted `CommandReplayResultMapping` record.
- Reuse the existing optional repository wiring so the delete action only appears when replay mapping persistence is present.
- Add a repository-level bulk delete method that can be implemented once in the applib abstract repository and inherited by JDO and JPA repositories.
- Reorder the export, replay, mapping finder, and mapping delete menu actions using action layout sequences.
- Cover the new behavior with focused unit tests and repository tests where practical.

**Non-Goals:**

- Do not change command replay mapping SPI signatures.
- Do not change the replay result mapping entity schema.
- Do not delete command log entries or replay manager state.
- Do not add a partial-delete or filtered-delete workflow.
- Do not expose this maintenance action outside the existing command log menu.

## Decisions

### Add `removeAll()` to `CommandReplayResultMappingRepository`

The repository interface will gain a `removeAll()` method so menu code does not need to know about concrete persistence entity classes.
The abstract repository can implement this by delegating to `RepositoryService.removeAll(entityClass)`, which lets existing JDO and JPA concrete repositories inherit the behavior.

Alternative considered: implement deletion directly in the menu by looking up concrete repositories.
That would couple the applib menu to persistence-specific classes and would not follow the existing repository abstraction.

### Use `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE` on the menu action

The delete-all action will use an idempotent are-you-sure semantic because deleting an already-empty mapping table leaves the system in the same state but remains destructive when mappings exist.
The action will return `void`, because returning the menu service is not a valid action result type for this service.
The action will use `MessageService` to report how many mappings were deleted.

Alternative considered: use `SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE`.
That would also force confirmation, but the operation is naturally idempotent at the aggregate level.

### Hide rather than disable when the mapping repository is absent

The new action will follow the existing replay result mapping finder actions and hide itself when `commandReplayResultMappingRepository` is empty.
This keeps the menu clean for applications that do not include replay mapping persistence.

Alternative considered: always show the action and disable it when no repository is available.
That would expose a maintenance action that cannot work in applications without persisted mappings.

### Re-sequence the existing actions instead of restructuring the menu

The export manager, replay manager, mapping finder actions, and delete action will be ordered by `@ActionLayout(sequence)` values.
This satisfies the requested order without changing service composition or action names.

Alternative considered: move actions into separate menu services or rename actions.
That would be broader and riskier than necessary for a decommissioning branch.

## Risks / Trade-offs

- Bulk delete behavior may be used accidentally if the confirmation semantic is not rendered by a custom viewer.
  Mitigation: use the explicit `ARE_YOU_SURE` semantic and keep the action prototype or maintenance-oriented as appropriate.
- Repository bulk delete semantics may differ slightly between JDO and JPA providers.
  Mitigation: rely on `RepositoryService.removeAll(entityClass)`, which is already used by command log repository maintenance code.
- Menu ordering tests can be brittle if they assert exact sequence values.
  Mitigation: test the relative sequence/order only where existing test support makes it practical.
