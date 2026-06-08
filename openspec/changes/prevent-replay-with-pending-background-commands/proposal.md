## Why

Replay users can currently continue replaying commands while background commands created by an earlier replayed action are still pending execution.
This can make replay advance past durable asynchronous effects that the original recording user had to wait for, causing later replayed commands to run against incomplete state.

## What Changes

- Stop multi-command replay after a replayed command creates one or more pending background commands.
- Require the replay user to wait until those background commands have executed and committed before continuing replay.
- Disable single-command `replayOrRetry` while pending background commands from earlier replayed work still exist.
- Reuse the existing pending background command detection used by recording support.
- Leave recording-side pending-background behavior unchanged.

## Capabilities

### New Capabilities
- `command-replay-background-completion`: Ensures replay cannot advance past pending background commands created by replayed actions.

### Modified Capabilities
- `replayable-command-actions`: The single-command replay-or-retry action is disabled while pending background commands must be completed first.

## Impact

- Affects `CommandReplayManager` selected replay flow and `ReplayableCommand#replayOrRetry` disablement.
- Uses command-log repository pending-background queries, with implementation likely in command-log applib and covered by JDO/JPA integration tests.
- Adds regression tests for selected replay stopping, single replay disablement, and replay continuation after pending background commands complete.
- No new dependencies are expected.
