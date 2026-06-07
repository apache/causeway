## Why

Command recording support is intended to capture user work against application domain objects, not framework inspection and management interactions with command export and replay helper objects.
When users interact with `CommandReplayManager`, `CommandExportManager`, `ReplayableCommand`, `ReplayableCommandParticipant`, or `CommandLogEntry` during a recording session, those interactions can pollute the recording with commands that replay or export the recording machinery itself.

## What Changes

- Add a marker interface for objects whose interactions must be ignored by command recording support.
- Suppress persistence of command log entries for actions whose target object implements the marker interface.
- Suppress adding synthetic selector actions at all when the collection owner type implements the marker interface.
- Mark command export and replay management objects and related view models/entities as ignored by command recording support.

## Capabilities

### New Capabilities

- `command-recording-suppression`: Defines the marker-based opt-out behavior for command recording support.

### Modified Capabilities

- `parented-collection-selector-actions`: Excludes marked owner types from synthetic selector action creation.
- `safe-action-command-publishing`: Clarifies that only synthesized selector actions for unmarked owner types participate in safe action command publishing.

## Impact

- Affects command log recording support and the action command publishing path.
- Affects synthetic parented collection selector action creation and recording behavior.
- Affects command log extension APIs by introducing a marker interface.
- Affects `CommandReplayManager`, `CommandExportManager`, `ReplayableCommand`, `ReplayableCommandParticipant`, and `CommandLogEntry` by having them opt out of recording support.
