## 1. ReplayableCommand Result Display

- [ ] 1.1 Inspect current ReplayableCommand properties and layout to choose where the recorded result display belongs.
- [ ] 1.2 Add a read-only ReplayableCommand property that obtains `CommandLogEntry#getResult()` and renders it as exported `result` metadata with `type` and `id` fields.
- [ ] 1.3 Ensure the property is absent or empty when no result bookmark is recorded and never emits `returnedObject`.
- [ ] 1.4 Ensure the property displays bookmark metadata without resolving the bookmark to a domain object.

## 2. Tests and Validation

- [ ] 2.1 Add focused tests for ReplayableCommand result display with a recorded result bookmark.
- [ ] 2.2 Add focused tests for no recorded result and unresolved-result behavior.
- [ ] 2.3 Run the relevant command-log applib tests.
- [ ] 2.4 Run `openspec validate show-replayable-command-result --strict`.
