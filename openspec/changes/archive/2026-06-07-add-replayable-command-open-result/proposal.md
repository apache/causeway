## Why

Replayable command participants can show an actual result object after successful replay, but the command view has no direct participants-associated action to open it.
Adding an `openResult` action completes the target, argument, and result navigation set on the participants table.

## What Changes

- Add a command-level `openResult` mixin associated with the `participants` collection.
- Set the `openResult` action layout sequence to `3`.
- Disable `openResult` when no actual result object is available.
- Open the actual result object when a result participant has one available.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Add an `openResult` participants-associated action for navigating to an actual result object from result participants.

## Impact

- Affects the command log applib replay UI and mixin registration.
- Affects `ReplayableCommand` participant navigation tests.
- Does not affect replay mapping SPI signatures or persistence schema.
