## Why

Replayable command participants can expose actual argument objects for reference parameters, but the command view currently has no direct action for opening one of those argument objects.
Adding an `openArgument` action makes argument inspection consistent with the existing `openTarget` action while keeping navigation associated with the participants table.

## What Changes

- Add a command-level `openArgument` mixin associated with the `participants` collection.
- Set the `openArgument` action layout sequence to `2`.
- Disable `openArgument` when the replayable command has no parameter participants.
- Add an `openArgument` action parameter named `parameterName`.
- Populate `parameterName` choices from participant parameter names where role is `PARAMETER`.
- Default `parameterName` to the only available parameter name when exactly one parameter participant exists, otherwise leave the default empty.
- Validate `parameterName` so the action cannot proceed when the selected parameter participant has no actual bookmark for its argument.
- Open the selected parameter participant's actual argument object when available.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Add an `openArgument` participants-associated action for navigating to actual argument objects from parameter participants.

## Impact

- Affects the command log applib replay UI and mixin registration.
- Affects `ReplayableCommand` participant navigation tests.
- Does not affect replay mapping SPI signatures or persistence schema.
