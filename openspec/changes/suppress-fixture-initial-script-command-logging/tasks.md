## 1. Command Log Pause State

- [ ] 1.1 Add command-log applib event types for pausing and resuming command log persistence, with API documentation describing their scoped use.
- [ ] 1.2 Add an internal command-log pause state service that tracks nested pause depth and exposes whether command logging is currently paused.
- [ ] 1.3 Add an internal command-log listener that reacts to pause and resume events and updates the pause state service.
- [ ] 1.4 Register any new command-log listener or support service in `CausewayModuleExtCommandLogApplib` if component scanning does not already include it.

## 2. Subscriber Suppression

- [ ] 2.1 Update `CommandSubscriberForCommandLog#onReady` to return without creating or comparing command log entries while command logging is paused.
- [ ] 2.2 Update `CommandSubscriberForCommandLog#onStarted` and `#onCompleted` to return without syncing command log entries while command logging is paused.
- [ ] 2.3 Add unit tests for paused ready, started, and completed notifications.
- [ ] 2.4 Add unit tests proving resumed and nested pause scopes restore normal command-log persistence only after all active pauses are resumed.

## 3. Initial Fixture Script Integration

- [ ] 3.1 Identify the lowest-coupling bridge from initial fixture installation to command-log pause state, respecting the existing dependency from command-log applib to testing fixtures applib.
- [ ] 3.2 Update the initial fixture script execution path to enter a command-log pause scope before running the configured initial script and to resume in a `finally` path.
- [ ] 3.3 Ensure the initial fixture script still executes through the existing `FixtureScripts` domain logic and transaction behavior.
- [ ] 3.4 Add tests proving commands produced by the configured initial fixture script are not persisted as command log entries.
- [ ] 3.5 Add tests proving command logging resumes after successful and failed initial fixture script execution.

## 4. Verification

- [ ] 4.1 Run focused command-log applib tests covering the new pause state and subscriber behavior.
- [ ] 4.2 Run focused testing fixtures applib or integration tests covering initial fixture installation behavior.
- [ ] 4.3 Run `openspec validate suppress-fixture-initial-script-command-logging --strict` and fix any proposal/spec/task issues.
