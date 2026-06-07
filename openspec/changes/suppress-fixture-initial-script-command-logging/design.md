## Context

Command-log persistence is handled by `CommandSubscriberForCommandLog`, which creates or syncs `CommandLogEntry` rows as published commands move through ready, started, and completed states.
Existing command recording suppression is target-based and prevents helper objects from being recorded when command-log recording support is enabled.
Initial fixture scripts are installed by `InitialFixtureScriptsInstaller` after the post-metamodel event and are executed through `FixtureScripts#run(...)`.
That programmatic path can invoke domain actions while the command-log extension is active, causing startup fixture activity to appear as normal recorded commands.

The command-log applib module already depends on the testing fixtures applib module, so command-log code can listen to fixture lifecycle events without adding a reverse dependency from testing fixtures to command-log.
If pause and resume event classes are placed in command-log applib, fixture code must not import them unless the module dependency direction is intentionally changed.

## Goals / Non-Goals

**Goals:**

- Provide a scoped command-log pause mechanism that prevents `CommandSubscriberForCommandLog` from creating or syncing command log entries while the scope is active.
- Ensure command logging is resumed reliably after the scoped work completes, including exceptional fixture script execution.
- Suppress commands produced by configured initial fixture scripts during application startup.
- Keep normal command logging unchanged outside the paused scope.

**Non-Goals:**

- Do not disable command publishing globally or change command DTO construction.
- Do not suppress audit trail, execution publishing, entity change publishing, or transaction behavior.
- Do not change fixture script execution ordering, transaction boundaries, or multiple-execution strategy.
- Do not remove existing target-based command recording suppression.

## Decisions

### Use a command-log-local pause state checked by the subscriber

Add an internal command-log service that tracks whether logging is currently paused, and have `CommandSubscriberForCommandLog` return before create or sync work when the state is paused.
This keeps the suppression close to persistence and avoids changing lower-level command creation or publishing behavior.

Alternative considered: disable command publishing facets during fixture execution.
That would be broader, harder to scope, and could affect unrelated subscribers that still need to see command activity.

### Represent pause and resume as application events with an internal listener

Add command-log application events such as `PauseLoggingEvent` and `ResumeLoggingEvent`, plus a command-log listener that updates the pause state.
Use depth-counted pause state rather than a single boolean so nested pause scopes cannot resume logging too early.

Alternative considered: expose imperative methods on a command-log pause service and inject that service into fixture code.
Events keep the coupling looser for callers that can publish events, and the internal listener keeps the subscriber independent of event delivery details.

### Bridge initial fixture execution without introducing a reverse module dependency

Because testing fixtures applib should not depend on command-log applib, prefer one of these implementation shapes during apply.
The lowest-coupling option is to have the initial fixture installation path publish fixture lifecycle events around the configured initial script and have command-log applib translate those fixture events into pause and resume state.
If the module dependency direction is deliberately changed or an event type is moved to a shared module, `FixtureScripts` can instead publish the command-log `PauseLoggingEvent` and `ResumeLoggingEvent` directly around initial script execution.

Alternative considered: call the existing UI-oriented `runFixtureScript(...)` lifecycle from the initial installer.
That risks changing semantics for non-initial programmatic fixture runs and may suppress more fixture activity than requested.

### Resume in finally blocks and tolerate missing starts

The initial fixture path must publish resume in a `finally` block or use an equivalent scope helper.
The pause state listener should also guard against unmatched resume events by never allowing its depth counter to become negative.

Alternative considered: rely on normal successful completion of fixture scripts.
That would leave logging paused after a failed startup fixture script and make subsequent diagnostics harder.

## Risks / Trade-offs

- [Risk] If pause and resume events are delivered through transaction-scoped or asynchronous event infrastructure, the subscriber might observe stale state.
  → Mitigation: use the existing synchronous application event mechanism used by fixture lifecycle events, and test pause effects around command subscriber calls.
- [Risk] A global pause state could affect concurrent interactions if startup fixture execution overlaps user requests.
  → Mitigation: initial fixture installation occurs during startup before normal user traffic, and depth-counted state keeps nested scopes safe.
- [Risk] Suppressing `onReady` but later receiving `onStarted` or `onCompleted` could find no command log entry.
  → Mitigation: make all subscriber phases no-op while paused and keep existing optional lookup behavior after resume.
- [Risk] Listening to broad fixture lifecycle events could suppress user-invoked fixture script commands, not only initial startup fixtures.
  → Mitigation: prefer an initial-fixture-specific event or scoped helper when implementing the bridge, and add tests that document the intended scope.
