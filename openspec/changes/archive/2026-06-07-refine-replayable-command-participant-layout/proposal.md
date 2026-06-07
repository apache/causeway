## Why

`ReplayableCommandParticipant` currently presents technical identifiers and role-specific object fields without enough layout guidance.
A clearer title, parent link, and role-aware fallback layout will make participant details easier to inspect from the participants table.

## What Changes

- Add or refine fallback layout metadata for `ReplayableCommandParticipant`.
- Add a useful title for `ReplayableCommandParticipant` instances.
- Add a derived `replayableCommand` property that links each participant back to its owning `ReplayableCommand`.
- Hide `owningInteractionId` from the object view while retaining it as memento data.
- Expose `logicalTypeName` only in a metadata tab or fieldset.
- Arrange participant details in three columns.
- Put `replayableCommand`, `role`, and `parameterName` in a first-column `general` fieldset with width `4`.
- Put `logicalTypeName` in a `metadata` tab or fieldset in the same first-column tab group.
- Put `recordedBookmark`, `target`, and `argument` in the second column.
- Hide `target` unless the participant role is `TARGET`.
- Hide `argument` unless the participant role is `PARAMETER`.
- Put `actualBookmark` and `result` in the third column.
- Hide `result` unless the participant role is `RESULT`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `replayable-command-remappings`: Refine replayable command participant details layout, title, parent link, and role-specific property visibility.

## Impact

- Affects the command log applib replay participant view model and fallback layout metadata.
- Affects focused participant layout, title, parent-link, and visibility tests.
- Does not affect participant derivation, bookmark mapping, replay mapping SPI signatures, or persistence schema.
