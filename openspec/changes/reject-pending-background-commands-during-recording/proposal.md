## Why

Command replay export can currently allow a user to continue recording new commands while background commands from an earlier action are still pending execution.
This produces exported command sequences that can fail later during replay because the replaying user advances before required asynchronous work has reached a durable state.

## What Changes

- Add a guard to command-log recording support that detects pending background commands when the end-user attempts to execute a subsequent recorded command.
- Throw a clear exception while recording support is enabled if a subsequent command is attempted before earlier background commands have executed and committed.
- Allow the action that schedules background commands to complete normally, so the end-user can wait for the background work and then continue recording.
- Leave normal command logging behavior unchanged when recording support is disabled.
- Preserve existing export and replay manager behavior except that recorded command sequences cannot silently continue past uncommitted background work.

## Capabilities

### New Capabilities
- `command-recording-background-completion`: Ensures recorded command sequences cannot advance to a subsequent command while earlier background commands remain pending execution.

### Modified Capabilities

## Impact

- Affects the command-log recording listener and command-log repository/query support used to identify pending background commands before new recorded commands proceed.
- Affects both JDO and JPA command-log persistence modules if pending-command lookup is persistence-specific.
- Adds regression coverage around command recording support, background command scheduling, subsequent command attempts, and disabled recording mode.
- No new dependencies are expected.
