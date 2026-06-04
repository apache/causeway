## Why

The default command replay mapping listener currently allows a recorded result bookmark to be overwritten with a later, different actual bookmark.
That can hide replay divergence and allow downstream commands to remap against an ambiguous original result.

Replay also currently handles replay execution and listener notification in separate transactions.
A listener failure during result mapping therefore cannot make the command replay itself fail, which prevents consistency checks from protecting the replay outcome.

## What Changes

- Update the default command replay mapping listener to reject conflicting result mappings for the same recorded result bookmark.
- Treat a repeated notification for the same recorded result and the same actual result as idempotent.
- Keep ignoring identity mappings where recorded and actual bookmarks are equal.
- Change replay transaction handling so replay execution, success bookkeeping, and result-mapping listener notification run in the same command transaction.
- Let result-mapping listener exceptions propagate so a failed consistency check causes command replay execution to fail.
- Continue not notifying result-mapping listeners when replay fails or when recorded or actual result bookmarks are unavailable.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `command-replay-result-mapping`: Result-mapping listener failures must fail the replayed command, and listener notification must occur in the same transaction as command execution.
- `default-command-replay-mapping-listener`: The default listener must reject conflicting actual bookmarks for a recorded result bookmark.

## Impact

- Affects command replay transaction flow in the command log applib replay implementation.
- Affects `CommandReplayMappingListenerDefault` behavior for repeated mappings.
- Affects tests for replay listener exception handling and default listener consistency.
- Existing applications with custom listeners that throw from result mapping will now cause replay command execution to fail instead of only logging a warning.
