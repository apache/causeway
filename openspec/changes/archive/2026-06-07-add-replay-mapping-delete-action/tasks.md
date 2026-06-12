## 1. Delete Mixin Implementation

- [x] 1.1 Create `CommandReplayResultMapping_delete` as a mixin contributed to `CommandReplayResultMapping`.
- [x] 1.2 Configure the action with `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE`, disabled command publishing, disabled execution publishing, and a clear cannot-be-undone description.
- [x] 1.3 Inject `RepositoryService` and remove the mixed-in `CommandReplayResultMapping` instance from persistence.
- [x] 1.4 Register the mixin in `CausewayModuleExtCommandLogApplib`.

## 2. Layout Metadata

- [x] 2.1 Update `CommandReplayResultMapping.layout.fallback.xml` to place the `delete` action explicitly.
- [x] 2.2 Ensure the layout keeps existing bookmark and command interaction id fields unchanged.

## 3. Tests and Validation

- [x] 3.1 Add unit tests for the delete mixin action semantics and publishing configuration.
- [x] 3.2 Add unit tests proving the delete mixin delegates removal of the mixed-in mapping to `RepositoryService`.
- [x] 3.3 Add a module-registration test or equivalent assertion that the command log applib module imports the delete mixin.
- [x] 3.4 Add or update layout metadata tests to verify the `delete` action is present.
- [x] 3.5 Run `openspec validate add-replay-mapping-delete-action --strict`.
- [x] 3.6 Run targeted Maven tests for the commandlog applib module.
