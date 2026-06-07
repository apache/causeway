## Why

Replay result mappings currently show the recorded and actual bookmarks but not the command interaction that first created the mapping.
Capturing the originating command interaction id improves auditability and makes it possible to trace a remapping back to the replayed command that produced it.

## What Changes

- Add the command interaction id that created a replay result mapping to the replay mapping data model.
- Persist the command interaction id on new `CommandReplayResultMapping` entities for both JDO and JPA persistence modules.
- Keep the originally stored command interaction id unchanged when replay reports a mapping that already exists.
- Extend the in-memory replay mapping data structure so it tracks the originating command interaction id alongside the actual bookmark.
- Update the replay result mapping layout metadata so the command interaction id is visible in the UI.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-replay-result-mapping`: Replay result mapping observations expose and retain the command interaction id that produced the mapping.
- `persistent-command-replay-mapping-listener`: Persistent replay result mappings store and display the command interaction id that first created the mapping.

## Impact

- Affects the command log applib replay mapping SPI implementations and replay result mapping repository contract.
- Affects the JDO and JPA command log persistence entity mappings and generated query classes if applicable.
- Affects replay result mapping fallback layout XML and column order metadata.
- Requires tests for new mapping creation, idempotent existing mappings, and UI/layout metadata.
