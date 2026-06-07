## Why

The replayable command participants UI currently exposes a reference parameter object through a property named `parameter`, which is easy to confuse with the existing `parameterName` metadata.
Renaming the object-valued property to `argument` makes the row model clearer while preserving `parameterName` as the recorded command parameter label.

## What Changes

- Rename the `ReplayableCommandParticipant` object-valued `parameter` property to `argument`.
- Update participant layout and column-order metadata so target, argument, and result object columns appear in the intended order.
- Update tests and any references that expect the old `parameter` property name.
- Preserve the `parameterName` property and its behaviour unchanged.
- **BREAKING**: Consumers using the `ReplayableCommandParticipant#parameter` property must switch to `argument`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Rename the reference parameter object property exposed by replayable command participants from `parameter` to `argument` while preserving `parameterName`.

## Impact

- Affects the command log applib replay view model `ReplayableCommandParticipant`.
- Affects fallback layout and column-order metadata for replayable command participants.
- Affects focused tests that assert participant property names and layout order.
- Does not affect replay mapping SPI signatures or persistence schema.
