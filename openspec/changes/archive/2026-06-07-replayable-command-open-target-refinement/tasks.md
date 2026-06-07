## 1. Open Target Mixin

- [x] 1.1 Add `ReplayableCommand_openTarget` mixin back to the replay package.
- [x] 1.2 Configure the mixin action layout with `associateWith = "participants"` and `sequence = "1"`.
- [x] 1.3 Implement the action to open the actual target object from the target participant.
- [x] 1.4 Disable the action when no actual target object is available.
- [x] 1.5 Register the mixin in `CausewayModuleExtCommandLogApplib`.

## 2. Tests and Validation

- [x] 2.1 Add or update tests for opening the actual target object.
- [x] 2.2 Add or update tests for disabling the action when no actual target is available.
- [x] 2.3 Run focused command log applib tests for replayable command mapping and participant behaviour.
- [x] 2.4 Run `openspec validate replayable-command-open-target-refinement --strict`.
