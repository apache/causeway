## 1. Rename the CommandReplayMappingListener SPI

- [ ] 1.1 Rename `CommandReplayMappingListener#remap(CommandLogEntry, Bookmark)` to `lookup(CommandLogEntry, Bookmark)` and update its JavaDoc to describe bookmark lookup rather than remapping.
- [ ] 1.2 Rename `CommandReplayMappingListener#onReplayResultMapped(Bookmark, Bookmark, CommandLogEntry)` to `onReplayResult(Bookmark, Bookmark, CommandLogEntry)` and update its JavaDoc to describe replay result observation.
- [ ] 1.3 Update all replay execution call sites in `ReplayableCommand` to call `lookup(...)` before execution and `onReplayResult(...)` after successful replay.

## 2. Store every replay result notification

- [ ] 2.1 Update `CommandReplayMappingListenerInMemory` so `onReplayResult(...)` stores the first actual bookmark for a recorded result bookmark even when the recorded and actual bookmarks are equal.
- [ ] 2.2 Update `CommandReplayMappingListenerPersistent` so `onReplayResult(...)` persists the first actual bookmark for a recorded result bookmark even when the recorded and actual bookmarks are equal.
- [ ] 2.3 Preserve idempotent repeated notifications and configured conflict handling when an existing recorded bookmark is later notified with a different actual bookmark.

## 3. Update tests and documentation references

- [ ] 3.1 Update `ReplayableCommandMappingTest` mocks and verifications to use `lookup(...)` and `onReplayResult(...)`.
- [ ] 3.2 Update in-memory listener tests to expect identity replay results to be stored and returned by `lookup(...)`.
- [ ] 3.3 Update persistent listener tests to expect identity replay results to be persisted and returned by `lookup(...)`.
- [ ] 3.4 Search the command log extension for old method names and update comments, JavaDocs, and test names that refer to `remap(...)` or `onReplayResultMapped(...)`.

## 4. Validate

- [ ] 4.1 Run the command log applib tests covering `ReplayableCommandMappingTest`, `CommandReplayMappingListenerInMemoryTest`, and `CommandReplayMappingListenerPersistentTest`.
- [ ] 4.2 Run OpenSpec validation for `record-all-replay-results` and fix any spec or task issues.
