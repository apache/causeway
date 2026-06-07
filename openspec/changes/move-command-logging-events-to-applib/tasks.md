## 1. Core Applib Events

- [ ] 1.1 Add `PauseCommandLoggingEvent` to core applib under the command service API package with JavaDoc describing scoped command-log persistence suppression.
- [ ] 1.2 Add `ResumeCommandLoggingEvent` to core applib under the command service API package with JavaDoc describing matching resume behavior and nested pause semantics.
- [ ] 1.3 Remove the command-log applib `PauseLoggingEvent` and `ResumeLoggingEvent` classes.

## 2. Command Log Listener Simplification

- [ ] 2.1 Update `CommandLogPauseStateListener` to import and listen for the core applib `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent` classes.
- [ ] 2.2 Remove listener methods and imports for fixture-specific initial fixture installing and installed events.
- [ ] 2.3 Verify `CommandLogPauseState` and `CommandSubscriberForCommandLog` continue to use the existing depth-counted pause state behavior.

## 3. Fixture Script Integration

- [ ] 3.1 Update `InitialFixtureScriptsInstaller` to publish `PauseCommandLoggingEvent` before invoking the configured initial fixture script.
- [ ] 3.2 Update `InitialFixtureScriptsInstaller` to publish `ResumeCommandLoggingEvent` in the existing `finally` path.
- [ ] 3.3 Remove fixture-specific `InitialFixtureScriptsInstallingEvent` and `InitialFixtureScriptsInstalledEvent` classes.
- [ ] 3.4 Ensure testing fixtures applib depends only on core applib for command logging pause and resume events, not on command-log applib.

## 4. Tests and Verification

- [ ] 4.1 Update command-log integration tests to use `PauseCommandLoggingEvent` and `ResumeCommandLoggingEvent`.
- [ ] 4.2 Update initial fixture installer tests to verify direct publication of the core applib pause and resume command logging events.
- [ ] 4.3 Run focused tests for testing fixtures applib and command-log persistence that cover pause/resume and initial fixture installation.
- [ ] 4.4 Run `openspec validate move-command-logging-events-to-applib --strict` and fix any proposal/spec/task issues.
