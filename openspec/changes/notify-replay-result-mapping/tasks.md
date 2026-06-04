## 1. SPI Definition

- [ ] 1.1 Add a replay result mapping SPI interface that accepts the recorded result bookmark, actual result bookmark, and replayed `CommandLogEntry` context.
- [ ] 1.2 Add a no-op/default SPI implementation or optional injection strategy so replay works when applications do not provide an implementation.
- [ ] 1.3 Register the SPI or default implementation with the command log applib module if required.

## 2. Replay Integration

- [ ] 2.1 Update `ReplayableCommand#tryReplay` success handling to retain the actual replay result bookmark.
- [ ] 2.2 Read the recorded result bookmark from the imported replay `CommandLogEntry`.
- [ ] 2.3 Notify the SPI after successful replay when both recorded and actual result bookmarks are non-null.
- [ ] 2.4 Do not notify the SPI for failed replay attempts, missing recorded bookmarks, or missing actual bookmarks.
- [ ] 2.5 Keep existing replay state and analysis updates unchanged.

## 3. Tests

- [ ] 3.1 Add tests that a successful replay with different recorded and actual result bookmarks notifies the SPI with both values.
- [ ] 3.2 Add tests that a successful replay with equal recorded and actual result bookmarks still notifies the SPI.
- [ ] 3.3 Add tests that failed replay attempts do not notify the SPI.
- [ ] 3.4 Add tests that missing recorded or actual result bookmarks do not notify the SPI.
- [ ] 3.5 Run focused command replay tests.
