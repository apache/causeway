## 1. SPI Refactor

- [ ] 1.1 Rename `ReplayResultMappingListener` to a broader command replay mapping SPI name.
- [ ] 1.2 Preserve the existing recorded-to-actual result mapping notification method under the renamed SPI.
- [ ] 1.3 Add a target remapping method that receives the replay command context and recorded target bookmark, and returns an optional replacement target bookmark.
- [ ] 1.4 Add a reference action parameter remapping method that receives the replay command context, parameter context, and current reference bookmark, and returns an optional replacement reference bookmark.
- [ ] 1.5 Update module wiring and replay context references to use the renamed SPI.

## 2. Replay Input Remapping

- [ ] 2.1 Add a utility or method to create a replay-time copy of the imported `CommandDto` before applying remaps.
- [ ] 2.2 Apply target bookmark remapping to the replay-time command DTO copy before execution.
- [ ] 2.3 Apply reference action parameter remapping to `ParamDto.reference` values on the replay-time command DTO copy before execution.
- [ ] 2.4 Pass the remapped command DTO copy to `CommandExecutorService#executeCommand(...)`.
- [ ] 2.5 Preserve the stored imported command DTO unchanged after remapping.
- [ ] 2.6 Keep existing result mapping notification behavior after successful replay.

## 3. Tests

- [ ] 3.1 Add tests that target bookmark remapping changes the command DTO supplied to replay execution.
- [ ] 3.2 Add tests that missing target remapping leaves replay execution unchanged.
- [ ] 3.3 Add tests that reference action parameter remapping changes the command DTO supplied to replay execution using a `type: "reference"` parameter with `reference.type` and `reference.id`.
- [ ] 3.4 Add tests that missing reference action parameter remapping leaves replay execution unchanged.
- [ ] 3.5 Add tests that remapping does not mutate the command DTO stored on the imported command log entry.
- [ ] 3.6 Update existing result mapping tests for the renamed SPI.
- [ ] 3.7 Run focused command replay tests.
