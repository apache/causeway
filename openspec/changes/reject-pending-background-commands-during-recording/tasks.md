## 1. Test Coverage

- [ ] 1.1 Add an integration test for recording support `ENABLED` where a foreground action schedules a background command and that scheduling action completes normally.
- [ ] 1.2 Add an integration test for recording support `ENABLED` where a subsequent foreground action attempted while the background command is still pending raises a clear wait-for-background-commands exception.
- [ ] 1.3 Add an integration test for recording support `ENABLED` where the end-user waits for the background command to execute and commit, then a subsequent foreground action completes normally.
- [ ] 1.4 Add an integration test for recording support `DISABLED` where a subsequent foreground action remains allowed even while earlier background commands are pending.
- [ ] 1.5 Ensure the tests exercise both JDO and JPA command-log persistence variants or shared abstract coverage used by both modules.

## 2. Core Implementation

- [ ] 2.1 Add a recording-support enabled check to `CommandSubscriberForCommandLog` that is separate from command-log persistence enablement.
- [ ] 2.2 Before accepting a new foreground command for recording, query `CommandLogEntryRepository#findBackgroundAndNotYetStarted()` when recording support is enabled.
- [ ] 2.3 Ensure the guard does not reject the original foreground command solely because it schedules background commands.
- [ ] 2.4 Throw a runtime exception for a subsequent foreground command when the pending background-command list is non-empty, with a message instructing the user to wait until pending background commands have executed and committed before continuing.
- [ ] 2.5 Keep existing behavior unchanged when command-log persistence is disabled, command-log pause state is active, or recording support is disabled.

## 3. Verification

- [ ] 3.1 Run the targeted command-log applib/JDO/JPA test set that covers background command scheduling and command recording.
- [ ] 3.2 Run `openspec status --change "reject-pending-background-commands-during-recording"` and confirm the change remains apply-ready.
