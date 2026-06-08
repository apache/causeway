## 1. Test Coverage

- [ ] 1.1 Add tests for selected replay where an earlier selected command creates a pending background command and the replay loop stops before later selected commands execute.
- [ ] 1.2 Add tests for selected replay where no background command is pending and the loop continues through selected commands as before.
- [ ] 1.3 Add tests showing selected replay can continue after pending background commands have executed and committed.
- [ ] 1.4 Add tests for individual `ReplayableCommand#replayOrRetry` disablement while pending background commands exist.
- [ ] 1.5 Add tests for direct individual replay invocation or helper method behavior so pending background work cannot be bypassed outside UI disablement.
- [ ] 1.6 Ensure replay/background tests cover the shared command-log applib behavior through JDO and JPA persistence where integration fixtures are required.

## 2. Pending Background Replay Guard

- [ ] 2.1 Introduce a reusable replay guard/helper that checks `CommandLogEntryRepository#findBackgroundAndNotYetStarted()` and returns a consistent wait-for-background-commands message.
- [ ] 2.2 Wire the guard into `ReplayableCommand` or its `replayOrRetry` mixin so `disableAct` disables replay while background commands are pending.
- [ ] 2.3 Ensure direct replay attempts do not execute a command while the replay pending-background guard is active.
- [ ] 2.4 Wire the guard into replay manager single-next style actions so replay cannot proceed through manager entry points while background commands are pending.

## 3. Selected Replay Loop

- [ ] 3.1 Add a guard check before selected replay starts so existing pending background commands prevent the selected loop from running.
- [ ] 3.2 Add a guard check after each selected command replay attempt and stop the loop when the just-replayed command leaves pending background commands.
- [ ] 3.3 Preserve existing stop-on-replay-failure behavior and replay-state updates for commands already replayed before the pause.
- [ ] 3.4 Ensure selected replay uses the same user-facing wait message as single-command replay disablement.

## 4. Verification

- [ ] 4.1 Run targeted command-log replay/background tests for the affected modules.
- [ ] 4.2 Run `openspec validate "prevent-replay-with-pending-background-commands" --strict`.
- [ ] 4.3 Run `openspec status --change "prevent-replay-with-pending-background-commands"` and confirm the change is apply-ready.
