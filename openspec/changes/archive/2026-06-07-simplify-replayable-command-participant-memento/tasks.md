## 1. Memento Format

- [x] 1.1 Change `ReplayableCommandParticipant#viewModelMemento()` to emit `[commandInteractionId]--target` for target participants.
- [x] 1.2 Change `ReplayableCommandParticipant#viewModelMemento()` to emit `[commandInteractionId]--parameter--[parameterName]` for parameter participants.
- [x] 1.3 Change `ReplayableCommandParticipant#viewModelMemento()` to emit `[commandInteractionId]--result` for result participants.
- [x] 1.4 Ensure participant mementos do not include recorded bookmark or actual bookmark values.

## 2. Memento Rehydration

- [x] 2.1 Keep the framework-facing view model constructor in the form `String memento` first, injected services after.
- [x] 2.2 Inject `CommandLogEntryRepository` or another minimal collaborator needed to find the owning command log entry by interaction id.
- [x] 2.3 Reconstruct target, parameter, and result participants from the owning replayable command's derived participant data.
- [x] 2.4 Preserve recorded bookmark, actual bookmark, object link, title, parent link, and role-specific visibility behaviour after reconstruction.
- [x] 2.5 Parse parameter mementos without losing parameter names that contain delimiter-like text.

## 3. Tests and Validation

- [x] 3.1 Add focused tests for target, parameter, and result memento strings.
- [x] 3.2 Add focused tests that reconstruct participants from mementos and verify derived bookmark values.
- [x] 3.3 Add focused test coverage for framework-compatible constructor ordering.
- [x] 3.4 Run the command log applib focused test suite for replayable command mapping.
- [x] 3.5 Run OpenSpec validation for the change.
