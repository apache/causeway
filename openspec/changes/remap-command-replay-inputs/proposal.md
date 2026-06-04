## Why

Command recordings can be replayed later against an environment whose object identifiers have moved on since the recording was captured.
Replay needs an application SPI to remap command targets and action parameters before execution, while still reporting recorded-to-actual result mappings after execution.

## What Changes

- Extend and likely rename the existing replay result mapping SPI so it represents command replay mapping rather than only result notifications.
- Before executing a replayed command, ask the SPI whether the command target identifier should be remapped.
- Before executing a replayed action command, ask the SPI whether any reference-valued action parameter should be remapped, for example a parameter represented by `type: "reference"` with a `reference.type` and `reference.id`.
- Continue notifying the SPI about recorded-to-actual result bookmark mappings after successful replay.
- Keep replay behavior unchanged when no SPI implementation is registered or when the SPI returns no remapping.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-replay-result-mapping`: Extends the replay mapping SPI from result-only notification to bidirectional replay mapping support for target identifiers, reference-valued action parameters, and result bookmarks.

## Impact

- Affects the command log applib SPI introduced for replay result mapping.
- Affects `ReplayableCommand` or the replay preparation path before `CommandExecutorService#executeCommand(...)` is called.
- Affects command DTO cloning or mutation logic for replay-time remapping of targets and reference-valued action parameters.
- Adds tests for target remapping, reference action parameter remapping, no-op behavior, and continued result mapping notification.
