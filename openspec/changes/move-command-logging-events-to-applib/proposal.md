## Why

The command logging pause and resume events currently live in the command-log extension, so fixture code cannot use them directly without introducing an undesirable dependency from testing fixtures to command-log.
Moving the events to core applib gives any module a stable, shared application-event API for scoped command-log suppression and lets fixture scripts use the same events that the command-log listener consumes.

## What Changes

- Move the pause and resume command logging events from command-log applib to core applib.
- Rename `PauseLoggingEvent` to `PauseCommandLoggingEvent` and `ResumeLoggingEvent` to `ResumeCommandLoggingEvent`.
- Update initial fixture script installation to publish the core applib pause and resume events directly, rather than fixture-specific installing and installed events.
- Simplify the command-log pause/resume listener so it only listens for the core applib pause and resume command logging events.
- Remove the fixture-specific initial script installing and installed events introduced only to bridge command-log module dependencies.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-recording-suppression`: Clarifies that scoped command-log pause and resume use the core applib `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent` API.

## Impact

- Affects core applib by adding command logging pause and resume event classes under the command service API area.
- Affects command-log applib by changing imports, removing extension-local event classes, and simplifying the pause state listener.
- Affects testing fixtures applib by publishing core applib command logging pause and resume events around initial fixture script installation.
- Affects tests that refer to the old command-log-local event names or fixture-specific bridge events.
