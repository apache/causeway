## 1. Participant View Model

- [x] 1.1 Add or refine the `ReplayableCommandParticipant` title.
- [x] 1.2 Add a derived `replayableCommand` property that links to the owning `ReplayableCommand`.
- [x] 1.3 Hide `owningInteractionId` from the object view while preserving the memento.
- [x] 1.4 Ensure `logicalTypeName` is available for metadata display.
- [x] 1.5 Hide `target` unless role is `TARGET`, hide `argument` unless role is `PARAMETER`, and hide `result` unless role is `RESULT`.

## 2. Layout Metadata

- [x] 2.1 Update `ReplayableCommandParticipant.layout.fallback.xml` with the requested three-column layout.
- [x] 2.2 Put replayable command, role, and parameter name in a first-column `general` fieldset with width `4`.
- [x] 2.3 Put logical type name in a `metadata` tab or fieldset in the same first-column tab group.
- [x] 2.4 Put recorded bookmark, target, and argument in the second column.
- [x] 2.5 Put actual bookmark and result in the third column.

## 3. Tests and Validation

- [x] 3.1 Add focused tests for title, parent link, hidden properties, role-specific visibility, and layout metadata.
- [x] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [x] 3.3 Run OpenSpec validation for the change.
