## 1. Action Parameter

- [x] 1.1 Add a boolean checkbox parameter to `CommandExportManager_moveCommands.act` for enabling timing squash.
- [x] 1.2 Add parameter layout text that explains selected command timing gaps are discarded when squash is enabled.
- [x] 1.3 Provide a default value of `false` for the squash parameter so existing behavior remains the default.

## 2. Retimestamping Logic

- [x] 2.1 Pass the squash flag into the move retimestamping helper.
- [x] 2.2 Keep the current original-gap-preserving behavior when squash is `false`.
- [x] 2.3 When squash is `true`, assign moved commands timestamps at 1 second increments after the target command.
- [x] 2.4 Ensure command DTO timestamps are updated for both preserve-gap and squash modes.

## 3. Tests

- [x] 3.1 Update existing move-command tests to call the new action signature with squash disabled.
- [x] 3.2 Add a test showing multiple moved commands squash large original internal gaps to 1 second increments.
- [x] 3.3 Add a test showing squash mode updates the command DTO timestamp.
- [x] 3.4 Run the relevant command-log applib test class or module tests.

## 4. Verification

- [x] 4.1 Run `openspec status --change add-squash-timings-to-move-commands` and confirm the change remains apply-ready.
