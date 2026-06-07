## 1. Participant API Rename

- [ ] 1.1 Rename the `ReplayableCommandParticipant` object-valued `parameter` property to `argument` while leaving `parameterName` unchanged.
- [ ] 1.2 Update participant construction and object lookup code so parameter participants expose resolved objects through `argument`.
- [ ] 1.3 Remove or update in-repository references to the old `parameter` property name.

## 2. Layout Metadata

- [ ] 2.1 Update fallback column order metadata to use `argument` between `target` and `result`.
- [ ] 2.2 Update any fallback layout metadata or property names that reference `parameter` as the object-valued participant property.

## 3. Tests and Validation

- [ ] 3.1 Update focused replayable command participant tests to assert `argument` and preserve `parameterName`.
- [ ] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [ ] 3.3 Run OpenSpec validation for the change.
