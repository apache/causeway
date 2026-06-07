## 1. Open Target Mixin

- [ ] 1.1 Add `ReplayableCommand_openTarget` mixin back to the replay package.
- [ ] 1.2 Configure the mixin action layout with `associateWith = "participants"` and `sequence = "1"`.
- [ ] 1.3 Implement the action to open the actual target object from the target participant.
- [ ] 1.4 Disable the action when no actual target object is available.
- [ ] 1.5 Register the mixin in `CausewayModuleExtCommandLogApplib`.

## 2. Tests and Validation

- [ ] 2.1 Add or update tests for opening the actual target object.
- [ ] 2.2 Add or update tests for disabling the action when no actual target is available.
- [ ] 2.3 Run focused command log applib tests for replayable command mapping and participant behaviour.
- [ ] 2.4 Run `openspec validate replayable-command-open-target-refinement --strict`.
