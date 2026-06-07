## 1. Replay State Predicate

- [ ] 1.1 Add or update a replay-state predicate that returns true only for `PENDING`, `OK`, and `FAILED`.
- [ ] 1.2 Update `ReplayableCommand#disableReplayOrRetry()` to use the new allowed-state predicate and report a clear allowed-states message.
- [ ] 1.3 Ensure `ReplayableCommand#tryReplayOrRetry()` continues to guard direct invocation through `disableReplayOrRetry()`.

## 2. Action Disablement

- [ ] 2.1 Update `ReplayableCommand_replayOrRetry#disableAct()` to delegate to `ReplayableCommand#disableReplayOrRetry()`.
- [ ] 2.2 Confirm the action remains enabled for `PENDING`, `OK`, and `FAILED` replay states.
- [ ] 2.3 Confirm the action is disabled for `UNDEFINED`, `EXPORTED`, and `EXCLUDED` replay states.

## 3. Tests

- [ ] 3.1 Add focused tests for `ReplayableCommand#disableReplayOrRetry()` across all replay states.
- [ ] 3.2 Add or update tests proving `ReplayableCommand_replayOrRetry#disableAct()` delegates to the replayable command guard.
- [ ] 3.3 Add or update tests proving direct replay/retry invocation is still guarded when replay state is `UNDEFINED`.

## 4. Validation

- [ ] 4.1 Run the focused command log applib replayable command tests.
- [ ] 4.2 Run `openspec status --change "disable-undefined-replay-or-retry"` and confirm the change is apply-ready.
