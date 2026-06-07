## Why

The participants table already exposes target, argument, and result participant objects as navigable links.
The separate `openTarget`, `openArgument`, and `openResult` command actions duplicate that built-in navigation and add unnecessary UI complexity.

## What Changes

- Remove the `ReplayableCommand_openTarget` mixin.
- Remove the `ReplayableCommand_openArgument` mixin.
- Remove the `ReplayableCommand_openResult` mixin.
- Remove module registrations and tests for those mixins.
- Keep the participants collection and participant row properties unchanged.
- **BREAKING**: Consumers invoking the command-level `openTarget`, `openArgument`, or `openResult` actions must navigate through the participant row links instead.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Remove the participants-associated open actions and rely on participant row links for target, argument, and result navigation.

## Impact

- Affects the command log applib replay UI mixin set and module registration.
- Affects focused replayable command participant navigation tests.
- Does not affect participant derivation, bookmark mapping, replay mapping SPI signatures, or persistence schema.
