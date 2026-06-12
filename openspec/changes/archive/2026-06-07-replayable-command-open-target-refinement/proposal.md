## Why

The participants table is now the primary place to inspect replayable command targets, but users still need a convenient action on the command page to open the actual target when one is available.
Reintroducing `openTarget` as a participants-associated action keeps navigation close to the participant data without restoring the removed target summary fields.

## What Changes

- Reintroduce the `ReplayableCommand_openTarget` mixin.
- Associate the action with the `participants` collection.
- Set the action layout sequence to `1`.
- Open the actual target represented by the target participant.
- Disable the action when no actual target is available.
- Do not reintroduce `targetType`, `targetId`, or table-row `openTargetTR` actions.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `replayable-command-remappings`: Refines replayable command target navigation so `openTarget` is available as a participants-associated action only when an actual target can be opened.

## Impact

- Affects `ReplayableCommand` and its mixins in `extensions/core/commandlog/applib`.
- Affects module imports in `CausewayModuleExtCommandLogApplib`.
- Affects focused command log applib tests for participant-derived target actuals and action enablement.
- No persistence schema, replay mapping SPI, or participant table column changes are expected.
