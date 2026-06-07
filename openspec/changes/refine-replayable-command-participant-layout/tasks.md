## 1. Participant View Model

- [ ] 1.1 Add or refine the `ReplayableCommandParticipant` title.
- [ ] 1.2 Add a derived `replayableCommand` property that links to the owning `ReplayableCommand`.
- [ ] 1.3 Hide `owningInteractionId` from the object view while preserving the memento.
- [ ] 1.4 Ensure `logicalTypeName` is available for metadata display.
- [ ] 1.5 Hide `target` unless role is `TARGET`, hide `argument` unless role is `PARAMETER`, and hide `result` unless role is `RESULT`.

## 2. Layout Metadata

- [ ] 2.1 Update `ReplayableCommandParticipant.layout.fallback.xml` with the requested three-column layout.
- [ ] 2.2 Put replayable command, role, and parameter name in a first-column `general` fieldset with width `4`.
- [ ] 2.3 Put logical type name in a `metadata` tab or fieldset in the same first-column tab group.
- [ ] 2.4 Put recorded bookmark, target, and argument in the second column.
- [ ] 2.5 Put actual bookmark and result in the third column.

## 3. Tests and Validation

- [ ] 3.1 Add focused tests for title, parent link, hidden properties, role-specific visibility, and layout metadata.
- [ ] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [ ] 3.3 Run OpenSpec validation for the change.
