## Why

Initial fixture scripts run automatically during application startup to seed an application before users begin normal work.
When command-log recording support is enabled, commands produced while the initial script runs can pollute the command log with setup activity that is not user work and should not be exported or replayed as part of an application recording.

## What Changes

- Add a command-log applib mechanism to temporarily pause and resume command log persistence through published application events.
- Provide internal command-log listeners that react to pause and resume events and cause `CommandSubscriberForCommandLog` to ignore commands while logging is paused.
- Update the fixture initial script installer path so it emits pause and resume logging events around the configured initial script execution.
- Preserve normal command logging outside the paused scope and ensure logging resumes even when initial fixture script execution fails.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-recording-suppression`: Extends command recording suppression from target-based suppression to scoped event-based pause and resume of command-log persistence.

## Impact

- Affects `extensions/core/commandlog/applib` by adding pause and resume event types plus internal subscriber state used by command log persistence.
- Affects `CommandSubscriberForCommandLog` so it skips create and sync work while logging is paused.
- Affects `testing/fixtures/applib` so `InitialFixtureScriptsInstaller` or `FixtureScripts` publishes the pause and resume events around initial script execution when command-log applib is present.
- Adds tests covering pause and resume behavior and the initial fixture script startup path.
