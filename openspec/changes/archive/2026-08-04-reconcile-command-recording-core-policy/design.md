## Context

The maintenance branch introduced command recording as an opt-in mode that later enables safe-action, property-edit, and synthetic-navigation commands to participate in the normal command publishing pipeline.
Causeway 4 currently has command publishing and commandlog persistence, but it has no recording-support switch, no target-level suppression contract, and no scoped mechanism to pause commandlog persistence.

This change implements dependency-graph node C1 from `openspec/reconciliation/maintenance-branch/dependency-graph.md`.
It deliberately establishes control contracts before any later change broadens command publishing eligibility.

The maintenance implementation uses mutable configuration classes, `javax` APIs, and the Causeway 2 member-execution structure.
The Causeway 4 implementation must use immutable configuration records, Jakarta APIs, the current event bus, and the current `InteractionCarrier`-based member executor.

## Goals / Non-Goals

**Goals:**

- Introduce the disabled-by-default recording-support configuration contract.
- Provide a core applib marker that prevents marked helper interactions from becoming recorded commands.
- Provide extension-independent pause and resume event contracts.
- Maintain nested, application-wide commandlog pause state.
- Make all commandlog subscriber lifecycle callbacks honour pause state.
- Suppress initial fixture-script command persistence with guaranteed resume behaviour.
- Mark the commandlog helper types currently present on `main` as recording-suppressed.
- Preserve ordinary member execution when command recording is suppressed.

**Non-Goals:**

- Enable command publishing for safe actions.
- Enable command publishing for property edits that are not currently publishable.
- Generate selector or navigation actions.
- Capture new result shapes or export result metadata.
- Add replay mapping.
- Replace `CommandExportManager` and `CommandReplayManager` with the maintenance `CommandManager`.
- Add recording or replay background-completion guards.

## Decisions

### Keep recording support under commandlog extension configuration

Add `RecordingSupport recordingSupport` to the Causeway 4 `CausewayConfiguration.Extensions.CommandLog` record with `@DefaultValue("DISABLED")`.
The enum has `ENABLED` and `DISABLED` values and an `isEnabled()` convenience method.

This preserves the maintenance external property `causeway.extensions.command-log.recording-support` and avoids introducing a second safe-action-specific property.
Although later behaviour affects core metamodel construction, the feature remains an opt-in commandlog capability.

An alternative was to place the switch under `causeway.core.runtime-services` because member execution consumes it.
That would break maintenance configuration compatibility and incorrectly present recording support as a core feature independent of commandlog.

### Keep public control contracts in core applib

Add `CommandRecordingSuppressed`, `PauseCommandLoggingEvent`, and `ResumeCommandLoggingEvent` under `org.apache.causeway.applib.services.command`.
The events use the current Causeway applib event base and carry their publisher as the source.

This allows domain types and fixture infrastructure to use the contracts without depending on the commandlog extension.
The commandlog extension remains responsible for interpreting pause and resume events.

An alternative was to define the contracts in commandlog applib.
That would create an undesirable reverse dependency from testing fixtures and application domain types to an optional persistence extension.

### Suppress command preparation at the normal execution seam

The current `MemberExecutorServiceDefault.prepareCommandForPublishing(...)` seam will check both the interaction owner and target for `CommandRecordingSuppressed` before advancing the command to the publishing-ready phase.
The invocation itself continues normally.

Checking both owner and target preserves maintenance semantics for contributed and mixin members where those objects can differ.
Placing the check in normal command preparation ensures future recording-aware facets cannot bypass suppression.

An alternative was to suppress only in `CommandSubscriberForCommandLog`.
That would allow other command subscribers to observe and act on a command that recording policy declared suppressed, and it would duplicate target-resolution logic outside member execution.

### Use application-wide nested pause depth

The commandlog extension will hold pause state in a singleton service backed by an `AtomicInteger`.
A Spring application-event listener increments the depth for each pause event and decrements it for each resume event.
An unmatched resume clamps the depth to zero.
The commandlog is paused whenever the depth is greater than zero.

This follows the authoritative maintenance behaviour and treats pause as a commandlog-persistence state for the application context rather than a thread-local or interaction-local state.
Atomic state allows events and command notifications to arrive on different threads without corrupting the nesting count.

An alternative was a thread-local pause scope.
That would fail when event publication and command persistence occur on different threads and would not match maintenance semantics.

### Check pause state at every subscriber lifecycle phase

`CommandSubscriberForCommandLog` will return without creating or synchronizing an entry when persistence is disabled or pause state is active.
The check applies independently to ready, started, and completed notifications.

This prevents a command created before a pause from being synchronized while paused and prevents a command first observed while paused from being created by a later phase.

### Pause initial fixture installation with try/finally

`InitialFixtureScriptsInstaller` will publish a pause event immediately before running the configured initial fixture script and publish the matching resume event from a `finally` block.
The installer depends only on core applib event contracts and `EventBusService`.
Fixture domain logic and exception propagation remain unchanged.

An alternative was to make the installer detect the commandlog extension directly.
That would couple testing fixtures to an optional extension and is therefore rejected.

### Mark current helper types without anticipating future classes

The existing `CommandExportManager`, `CommandReplayManager`, `ReplayableCommand`, and commandlog entry view/entity abstractions on `main` will implement the suppression marker where their public type hierarchy permits it.
Future `CommandManager` and `ReplayableCommandParticipant` types will implement the same marker when their respective reconciliation changes introduce them.

This prevents the first change from introducing placeholder types or prematurely redesigning the manager model.

## Risks / Trade-offs

- [Risk] Application-wide pause affects unrelated concurrent command interactions while a pause scope is active. → Mitigation: preserve maintenance semantics, keep pause scopes short, and guarantee fixture resume with `finally`.
- [Risk] A missing resume event can leave persistence paused. → Mitigation: document paired events, support nesting explicitly, and use `try/finally` in framework-owned scopes.
- [Risk] An unmatched resume may hide an event-pairing defect. → Mitigation: clamp at zero to preserve availability and cover the behaviour with unit tests; diagnostic logging can be considered during implementation.
- [Risk] Record-based configuration constructor changes can affect configuration tests and direct constructors. → Mitigation: use `@DefaultValue`, update focused construction tests, and retain the existing external property hierarchy.
- [Risk] Suppression in the core publishing seam affects every command subscriber, not only commandlog. → Mitigation: this matches the maintenance execution seam and is limited to types that deliberately implement the marker.
- [Risk] Existing and maintenance helper class names differ because the manager redesign has not yet been reconciled. → Mitigation: mark only current helper types now and record future marker adoption in downstream designs.

## Migration Plan

The configuration defaults to `DISABLED`, so applications that do not opt in retain existing recording eligibility.
The new events and marker are additive public contracts.
Commandlog helper interactions become suppressed once those helper types implement the marker.
Rollback consists of removing the configuration component, event listener and pause state, fixture event publication, marker checks, and helper marker declarations.
No persistent schema migration is required.

## Open Questions

No blocking design questions remain for C1.
The exact diagnostic policy for unmatched resume events can be decided during implementation without changing the specified behaviour.
