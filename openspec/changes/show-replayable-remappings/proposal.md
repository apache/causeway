## Why

Replayable command details currently show the recorded command DTO, but they do not make replay-time bookmark remappings visible at the point where the user reviews or replays the command.
Users need to see which target, reference parameter, or result bookmarks differed between the recorded command and the actual replay so that replay behaviour is auditable without manually comparing DTOs or separate mapping lists.

## What Changes

- Add a replayable command remappings collection that is populated on the fly from the command DTO, replay state, and replay mapping listeners or persisted mapping data.
- Represent each remapping row with a new `ReplayableCommandParticipant` view model.
- Display the owning command interaction id, participant role, target object, parameter name where applicable, result object, recorded bookmark, and actual bookmark.
- Resolve target and parameter bookmarks to local domain objects when possible, while still showing bookmark values when objects cannot be resolved.
- Resolve result bookmarks only when the command has replayed successfully.
- Add fallback layout metadata for `ReplayableCommandParticipant`.
- Update the fallback layout metadata for `ReplayableCommand` to include the remappings table.

## Capabilities

### New Capabilities
- `replayable-command-remappings`: Displays replay-time target, reference parameter, and result bookmark remappings from a replayable command.

### Modified Capabilities
- `command-replay-result-mapping`: Replayable command inspection surfaces the remappings that replay mapping SPI lookup and result notifications make relevant to a command.

## Impact

- Affects the command log applib replay UI under `extensions/core/commandlog/applib`.
- Adds a new view model and layout metadata alongside `ReplayableCommand`.
- Updates `ReplayableCommand.layout.fallback.xml` to expose the collection.
- Adds or updates unit tests for parsing command DTO participants, looking up actual bookmarks, and rendering remapping rows.
- No persistence schema or public replay mapping SPI changes are expected.
