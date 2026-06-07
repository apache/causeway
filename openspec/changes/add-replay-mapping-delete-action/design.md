## Context

`CommandReplayResultMapping` is an abstract applib entity with concrete JDO and JPA persistence implementations.
Existing replay mapping maintenance is list-and-find oriented; deletion is currently only possible indirectly through repository or database access.
Causeway supports object-contributed actions as mixins, and the command log module already registers replay-related mixins through `CausewayModuleExtCommandLogApplib`.

## Goals / Non-Goals

**Goals:**
- Add a delete action contributed to `CommandReplayResultMapping` as a mixin.
- Mark the action as `IDEMPOTENT_ARE_YOU_SURE` to require confirmation while preserving idempotent semantics.
- Delete the current mapping through `RepositoryService` so the action works for both JDO and JPA concrete entity types.
- Place the action in `CommandReplayResultMapping.layout.fallback.xml`.

**Non-Goals:**
- Do not add bulk delete actions to `CommandLogMenu`.
- Do not add new repository finder methods.
- Do not change replay conflict policy or automatic mapping creation behavior.

## Decisions

- Implement deletion as an applib mixin named `CommandReplayResultMapping_delete`.
  This keeps the abstract mapping entity focused on state and follows the existing command log pattern for replay object actions.
  The alternative was to add an action method directly to the abstract entity, but that would mix UI behavior into the entity contract.

- Inject `RepositoryService` into the mixin and call `removeAndFlush` or the repository-service removal API used elsewhere in the codebase.
  This avoids persistence-module-specific dependencies and works against the concrete entity instance supplied to the mixin.
  The alternative was to extend `CommandReplayResultMappingRepository`, but deletion of a known object does not require a custom repository contract.

- Use `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE` with command and execution publishing disabled.
  The operation is destructive and should require user confirmation, but repeated user intent against the same object should not have additional semantic effect beyond the object being gone.
  The alternative was `NON_IDEMPOTENT_ARE_YOU_SURE`, but the user specifically requested an idempotent action.

- Register the mixin in `CausewayModuleExtCommandLogApplib` and place `delete` in the fallback layout.
  Registration makes the action available consistently, while layout placement prevents it from relying on unreferenced action defaults.
  The alternative was to rely on `unreferencedActions`, but explicit placement is clearer for a destructive operation.

## Risks / Trade-offs

- Deleting a mapping can affect later replay input remapping → Require `ARE_YOU_SURE` confirmation and describe that the operation cannot be undone.
- Layout action id must match the contributed action id → Use the conventional mixin suffix `_delete`, which contributes action id `delete`.
- Repository service removal API naming may differ across framework versions → Follow existing project usage and validate with targeted applib tests.
