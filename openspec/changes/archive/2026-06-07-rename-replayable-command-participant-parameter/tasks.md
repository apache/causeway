## 1. Participant API Rename

- [x] 1.1 Rename the `ReplayableCommandParticipant` object-valued `parameter` property to `argument` while leaving `parameterName` unchanged.
- [x] 1.2 Update participant construction and object lookup code so parameter participants expose resolved objects through `argument`.
- [x] 1.3 Remove or update in-repository references to the old `parameter` property name.

## 2. Layout Metadata

- [x] 2.1 Update fallback column order metadata to use `argument` between `target` and `result`.
- [x] 2.2 Update any fallback layout metadata or property names that reference `parameter` as the object-valued participant property.

## 3. Tests and Validation

- [x] 3.1 Update focused replayable command participant tests to assert `argument` and preserve `parameterName`.
- [x] 3.2 Run the command log applib focused test suite for replayable command mapping.
- [x] 3.3 Run OpenSpec validation for the change.
