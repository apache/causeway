## Why

`ReplayableCommandParticipant` currently serializes bookmark details into its view-model memento, making generated URLs hard to read and duplicating information that can be derived from the owning replayable command.
Using a compact role-based memento keeps URLs readable while ensuring bookmark values remain calculated from the command log entry as source of truth.

## What Changes

- Change `ReplayableCommandParticipant` mementos to use readable role-based forms:
  - `[commandInteractionId]--target`
  - `[commandInteractionId]--parameter--[parameterName]`
  - `[commandInteractionId]--result`
- Stop serializing recorded and actual bookmark values in the participant memento.
- Rehydrate participant bookmark data by looking up the owning command log entry by command interaction id.
- Inject `CommandLogEntryRepository` into the view model constructor if needed to support memento rehydration.
- Preserve participant display behaviour for recorded bookmarks, actual bookmarks, object links, title, parent link, and role-specific visibility.
- **BREAKING**: Existing URLs that use the old pipe-delimited participant memento format will no longer be the canonical participant URL format.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Simplify replayable command participant mementos and derive bookmark data from the owning replayable command.

## Impact

- Affects the command log applib replay participant view model memento format and constructor dependencies.
- Affects participant derivation and tests around memento round-tripping.
- Does not affect replay mapping SPI signatures or persistence schema.
