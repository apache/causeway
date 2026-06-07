## Context

Command-log recording support currently enables command publishing for safe actions and synthetic parented collection selector actions when `causeway.extensions.command-log.recording-support` is enabled.
That is useful for recording application workflows, but it also records framework management interactions when users browse or operate command export and replay objects during the same session.
Those helper interactions are not meaningful replay input and can create recursive or noisy recordings.

The suppression decision needs to be available in core command execution and synthetic selector code, so the marker type cannot live only in the command-log extension implementation.
The export and replay helper objects live in the command-log extension and can opt in by implementing the marker interface.

## Goals / Non-Goals

**Goals:**

- Provide a marker interface that a domain object or view model can implement to opt out of command recording support.
- Ensure actions targeting marked objects are not persisted as command log entries through recording support.
- Ensure synthetic selector actions are not added at all for types that implement the marker interface.
- Mark command export and replay management objects and related command log entities/view models as ignored by recording support.
- Preserve normal command recording for all unmarked application objects.

**Non-Goals:**

- Do not change command export YAML format or command replay import format.
- Do not change replay mapping SPI contracts or persistence schemas.
- Do not suppress execution publishing or domain event behavior for marked objects unless existing behavior already does so.
- Do not add configuration properties for the ignore list.

## Decisions

### Add a public applib marker interface

Introduce a public marker interface in applib, for example under `org.apache.causeway.applib.services.command`, so both core command execution code and command-log extension classes can depend on it without reversing module dependencies.
The marker should be empty and express only that interactions with the implementing object are ignored by command recording support.

Alternative considered: place the marker in the command-log applib module.
That would keep the type close to the feature, but core runtime and metamodel code would need an undesirable dependency on the extension module.

### Gate command publishing at command preparation time

Update command publishing preparation for normal action and property execution to leave the command publishing phase unchanged when the interaction target implements the marker.
The command can still execute normally, but command subscribers should not receive a READY command to persist for that invocation.

Alternative considered: filter the entry inside the command-log subscriber.
That would protect command-log persistence only, but other command subscribers could still observe and persist ignored interactions.

### Gate synthetic selector action synthesis on the owner type

Update synthetic parented collection selector action creation to skip adding selector actions when the collection owner type implements the marker interface.
This ensures ignored command-log helper views do not expose synthetic selector actions in the first place, so there is no selector invocation to record and no selected child result to attach to a command.
The check should use the target type being introspected, not a runtime target instance, because synthetic actions are added to the metamodel before any particular object is invoked.

Alternative considered: synthesize the action and suppress command readiness or result capture during invocation.
That would still expose recording-only selector actions on ignored helper objects, which is unnecessary and could confuse users.

### Opt out known command export and replay helper types

Have `CommandReplayManager`, `CommandExportManager`, `ReplayableCommand`, `ReplayableCommandParticipant`, and command-log entry entity/view types implement the marker interface.
This makes the suppression explicit on the target object and keeps the core suppression logic generic.

Alternative considered: hard-code command-log logical type names in core.
That would couple core behavior to extension implementation details and would be harder for applications to reuse.

## Risks / Trade-offs

- Marker implemented too broadly → important application interactions could be excluded from recording.
  Mitigation: document that the marker is an explicit opt-out and apply it only to command-log helper types in this change.
- Existing tests may assume synthetic selector actions always set command results when recording support is enabled.
  Mitigation: add targeted tests for both marked and unmarked selector targets so existing positive behavior remains covered.
- Suppression by target does not cover commands whose target is unmarked but whose result is marked.
  Mitigation: this is intentional for the requested scope, which is about interactions with export and replay helper objects as targets.
