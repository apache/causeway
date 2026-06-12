## Why

A newly recorded command has replay state `UNDEFINED`, meaning it has not been exported/imported into the replay workflow yet.
Enabling `ReplayableCommand#replayOrRetry` for that state suggests the command can be replayed immediately, but replay/retry should only be available for commands that are pending replay, have replayed successfully, or have failed and can be retried.

## What Changes

- Disable `ReplayableCommand#replayOrRetry` when the replay state is `UNDEFINED`.
- Keep `ReplayableCommand#replayOrRetry` enabled when replay state is `PENDING`, `OK`, or `FAILED`.
- Keep the existing behavior that the replay action is unavailable for non-replayable states such as `EXPORTED` or `EXCLUDED`.
- Update action disablement tests to cover the newly recorded `UNDEFINED` state.

## Capabilities

### New Capabilities

- `replayable-command-actions`: Defines replayable command action availability and state-driven enablement.

### Modified Capabilities

- None.

## Impact

- Affects `ReplayableCommand_replayOrRetry` and the underlying `ReplayableCommand` replay/retry guard logic in `extensions/core/commandlog/applib`.
- Affects tests for replayable command action enablement.
- Does not change command recording, export, import, or replay execution semantics.
