## 1. Participant Availability

- [ ] 1.1 Add participant-resolution tests for `UNDEFINED` target and reference-argument availability from recorded bookmarks.
- [ ] 1.2 Add participant-resolution tests for `EXPORTED` target and reference-argument availability from recorded bookmarks.
- [ ] 1.3 Add participant-resolution tests proving domain service targets are available regardless of replay state.
- [ ] 1.4 Update `ReplayableCommand` and `ReplayableCommandParticipant` bookmark resolution so the new availability rules pass without changing replay state.

## 2. Command Navigation

- [ ] 2.1 Identify or add repository/query support to find adjacent `CommandLogEntry` records using the replayable command list ordering.
- [ ] 2.2 Add `previous` and `next` actions to `ReplayableCommand` that return adjacent `ReplayableCommand` instances with the same replay context.
- [ ] 2.3 Add disable methods for the first-command and last-command navigation boundaries.
- [ ] 2.4 Add tests for previous, next, boundary disablement, and non-mutating navigation behavior.

## 3. Result Presence and Table Layout

- [ ] 3.1 Add a derived Boolean `hasResult` property to `ReplayableCommand` based on `CommandLogEntry#getResult()`.
- [ ] 3.2 Set property layout metadata so `hasResult` appears in tables before exportability.
- [ ] 3.3 Add tests or metadata assertions for `hasResult` true, `hasResult` false, unresolved-result behavior, and column ordering relative to exportability.

## 4. Verification

- [ ] 4.1 Run the focused command-log applib tests for replayable command participant, navigation, result-presence, and exportability behavior.
- [ ] 4.2 Run the relevant Maven module test suite or project validation command used for command-log extension changes.
- [ ] 4.3 Run `openspec status --change improve-replayable-command-navigation` and confirm the change remains apply-ready.
