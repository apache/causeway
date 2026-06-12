## 1. SPI Refactor

- [x] 1.1 Rename `ReplayResultMappingListener` to a broader command replay mapping SPI name.
- [x] 1.2 Preserve the existing recorded-to-actual result mapping notification method under the renamed SPI.
- [x] 1.3 Add a target remapping method that receives the replay command context and recorded target bookmark, and returns an optional replacement target bookmark.
- [x] 1.4 Add a reference action parameter remapping method that receives the replay command context, parameter context, and current reference bookmark, and returns an optional replacement reference bookmark.
- [x] 1.5 Update module wiring and replay context references to use the renamed SPI.

## 2. Replay Input Remapping

- [x] 2.1 Add a utility or method to create a replay-time copy of the imported `CommandDto` before applying remaps.
- [x] 2.2 Apply target bookmark remapping to the replay-time command DTO copy before execution.
- [x] 2.3 Apply reference action parameter remapping to `ParamDto.reference` values on the replay-time command DTO copy before execution.
- [x] 2.4 Pass the remapped command DTO copy to `CommandExecutorService#executeCommand(...)`.
- [x] 2.5 Preserve the stored imported command DTO unchanged after remapping.
- [x] 2.6 Keep existing result mapping notification behavior after successful replay.

## 3. Tests

- [x] 3.1 Add tests that target bookmark remapping changes the command DTO supplied to replay execution.
- [x] 3.2 Add tests that missing target remapping leaves replay execution unchanged.
- [x] 3.3 Add tests that reference action parameter remapping changes the command DTO supplied to replay execution using a `type: "reference"` parameter with `reference.type` and `reference.id`.
- [x] 3.4 Add tests that missing reference action parameter remapping leaves replay execution unchanged.
- [x] 3.5 Add tests that remapping does not mutate the command DTO stored on the imported command log entry.
- [x] 3.6 Update existing result mapping tests for the renamed SPI.
- [x] 3.7 Run focused command replay tests.
