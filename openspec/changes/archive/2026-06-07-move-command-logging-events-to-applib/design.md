## Context

The previous change introduced command-log-local `PauseLoggingEvent` and `ResumeLoggingEvent` classes in the command-log extension.
Because testing fixtures applib must not depend on the command-log extension, initial fixture script installation could not publish those events directly.
The workaround added fixture-specific initial-installing and initial-installed events that the command-log extension listened to alongside the pause and resume events.

That bridge works but adds unnecessary concepts.
The pause and resume intent is not command-log-extension-specific from the publisher's perspective: publishers only need to ask any installed command logging implementation to ignore commands in a scope.
Core applib is the common API surface available to fixture scripts and command-log implementations.

## Goals / Non-Goals

**Goals:**

- Move command logging pause and resume event types to core applib so they are available to all modules.
- Rename the events to `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent` to make their purpose explicit.
- Have initial fixture script installation publish those core applib events directly.
- Simplify the command-log listener to consume only the two core applib event types.
- Remove the fixture-specific initial fixture installing and installed bridge events.

**Non-Goals:**

- Do not change the pause-depth semantics or command subscriber behavior while paused.
- Do not add a hard dependency from testing fixtures applib to command-log applib.
- Do not introduce a new command logging service API beyond event classes.
- Do not change fixture script execution or transaction behavior.

## Decisions

### Place the events in core applib command service package

Add `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent` under the core applib command service API area, for example `org.apache.causeway.applib.services.command`.
This package already hosts command-related application APIs such as `Command`, `CommandExecutorService`, and the command recording suppression marker.

Alternative considered: place the events under a generic applib events package.
That would make the package less discoverable for command logging users and separate the events from related command service APIs.

### Rename the events rather than preserve aliases

Replace `PauseLoggingEvent` and `ResumeLoggingEvent` with `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent`.
The explicit names avoid confusion with audit, execution, application, or framework logging.

Alternative considered: keep deprecated aliases in command-log applib.
The current events are newly introduced in this branch of work, so aliases would preserve unnecessary API surface and keep the old coupling around.

### Publish core events directly from the initial fixture installer

Update `InitialFixtureScriptsInstaller` to post `PauseCommandLoggingEvent` before invoking `FixtureScripts#run(...)` and `ResumeCommandLoggingEvent` in a `finally` block.
This removes the need for fixture-specific bridge events while preserving the existing fixture script execution path.

Alternative considered: continue publishing fixture-specific events and translate them in command-log applib.
That keeps extra event classes and listener methods that exist only to work around event location.

### Keep the listener focused on pause state only

Update `CommandLogPauseStateListener` so it only listens to the core applib pause and resume events and updates `CommandLogPauseState`.
The listener no longer needs imports from testing fixtures applib for initial fixture event types.

Alternative considered: merge pause state and listener into `CommandSubscriberForCommandLog`.
Keeping listener state separate remains cleaner and easier to test.

## Risks / Trade-offs

- [Risk] Moving events to core applib exposes them as a broader API than the command-log extension-local classes.
  → Mitigation: name and JavaDoc them explicitly as command logging events and keep behavior implemented only by modules that choose to listen.
- [Risk] Existing tests or imports still reference old command-log event classes or fixture bridge events.
  → Mitigation: update all references and remove the old classes so stale imports fail at compile time.
- [Risk] Publishing pause events when no command-log extension is installed has no visible effect.
  → Mitigation: this is acceptable for application events; publishers do not need to know whether a listener is installed.
