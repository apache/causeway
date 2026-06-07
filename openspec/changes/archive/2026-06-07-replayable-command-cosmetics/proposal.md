## Why

`ReplayableCommand` now shows target, parameter, and result information through the `Participants` table.
The older target summary fields and `openTarget` action duplicate that information and make the page busier than necessary.

## What Changes

- Remove the `targetType` and `targetId` properties from `ReplayableCommand`.
- Remove the `openTarget` action from `ReplayableCommand`.
- Remove the target field set entries from the `ReplayableCommand` fallback layout.
- Keep the `Participants` table as the place to inspect and navigate target information.
- Keep command member, replay state, DTO, and participant behaviour unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `replayable-command-remappings`: Clarifies that replayable command target inspection is provided through participants, not the legacy target summary properties or action.

## Impact

- Affects `ReplayableCommand` in `extensions/core/commandlog/applib`.
- Affects `ReplayableCommand.layout.fallback.xml` in the same package.
- Affects focused command log applib tests if any assert the removed target summary members.
- No persistence schema, replay mapping SPI, or participant table changes are expected.
