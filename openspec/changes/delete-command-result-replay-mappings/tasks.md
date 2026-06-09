## 1. Repository Support

- [ ] 1.1 Add `removeAll()` to `CommandReplayResultMappingRepository`.
- [ ] 1.2 Implement `removeAll()` in `CommandReplayResultMappingRepositoryAbstract` using the repository service and the configured entity class.
- [ ] 1.3 Update fake repository implementations in existing tests so they compile and can verify deletion behavior.

## 2. Command Log Menu Action

- [ ] 2.1 Add a new `deleteReplayResultMappings` action to `CommandLogMenu` with `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE`.
- [ ] 2.2 Hide the delete action when the optional replay result mapping repository is absent.
- [ ] 2.3 Invoke the repository `removeAll()` method from the new action without affecting command log entries.

## 3. Menu Ordering

- [ ] 3.1 Update `@ActionLayout(sequence)` values so `exportManager` appears before `replayManager`.
- [ ] 3.2 Update replay result mapping finder action sequences so they appear after `replayManager`.
- [ ] 3.3 Assign the delete action a sequence after the replay result mapping finder actions.

## 4. Verification

- [ ] 4.1 Add or update `CommandLogMenu` tests for delete action visibility and repository invocation.
- [ ] 4.2 Add or update tests that verify the delete action uses the are-you-sure idempotent semantic.
- [ ] 4.3 Add or update tests that verify the relative action ordering for export, replay, finder, and delete actions where existing test utilities support it.
- [ ] 4.4 Run the focused command log applib tests and fix regressions.
