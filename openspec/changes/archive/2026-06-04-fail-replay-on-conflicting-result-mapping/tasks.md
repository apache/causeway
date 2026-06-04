## 1. Default Listener Consistency

- [x] 1.1 Update `CommandReplayMappingListenerDefault#onReplayResultMapped(...)` to check for an existing non-identity mapping before writing.
- [x] 1.2 Throw an exception when a recorded result bookmark is already mapped to a different actual bookmark.
- [x] 1.3 Preserve idempotent behavior when a repeated notification uses the same actual bookmark.
- [x] 1.4 Preserve no-op behavior when recorded and actual bookmarks are equal.

## 2. Replay Transaction Handling

- [x] 2.1 Refactor `ReplayableCommand#tryReplay(...)` so command execution and successful result-mapping notification happen inside the same `REQUIRES_NEW` transaction.
- [x] 2.2 Preserve separate failure-analysis persistence after the failed command transaction rolls back.
- [x] 2.3 Remove exception swallowing for result-mapping notification failures so listener exceptions propagate from the command transaction.
- [x] 2.4 Keep target and reference parameter remapping exception handling unchanged.

## 3. Tests

- [x] 3.1 Update default listener tests to cover conflict rejection, idempotent duplicate notification, and unchanged mapping after conflict.
- [x] 3.2 Update replay mapping tests to verify result-mapping listener exceptions fail replay execution.
- [x] 3.3 Add or update transaction-service test stubbing to verify successful listener notification happens in the same transaction as command execution.
- [x] 3.4 Verify missing recorded or actual result bookmarks still skip result-mapping notification.

## 4. Verification

- [x] 4.1 Run the command log applib tests that cover `CommandReplayMappingListenerDefaultTest` and `ReplayableCommandMappingTest`.
- [x] 4.2 Run `openspec validate fail-replay-on-conflicting-result-mapping --type change --strict`.
