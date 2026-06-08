## Context

Replay input remapping replaces recorded target and reference parameter bookmarks with actual bookmarks before command execution.
The imported `CommandLogEntry` command DTO is also used for audit, participant inspection, retry, and future mapping lookup.
If replay remapping writes replacements into that stored DTO, replay loses the original recorded input values and the UI can no longer show recorded versus actual bookmarks accurately.

The replay flow already has a narrow remapping point immediately before `CommandExecutorService#executeCommand(...)`.
That is the right place to ensure remapping works on an isolated execution DTO rather than the persisted command DTO.

The command subscriber also receives lifecycle callbacks for the in-memory `Command` created during replay execution.
By the time `CommandSubscriberForCommandLog#onStarted(...)` or `onCompleted(...)` sees that command, its DTO can already contain replay remappings.
Replay entries therefore need lifecycle sync that preserves recorded command data while still recording execution timing metadata.

## Goals / Non-Goals

**Goals:**

- Ensure target remapping does not mutate `CommandLogEntry#getCommandDto()`.
- Ensure reference parameter remapping does not mutate `CommandLogEntry#getCommandDto()`.
- Ensure replay execution still receives remapped target and reference parameter values.
- Preserve recorded DTO values for replayable command participants, inspection, and retry.
- Add focused tests that fail if replay remapping mutates the stored DTO.

**Non-Goals:**

- Do not change command replay mapping SPI method signatures.
- Do not change command export YAML shape.
- Do not change replay result notification behavior.
- Do not add persistence schema changes.

## Decisions

### Remap only a defensive command DTO copy

The replay flow should create a defensive copy of the recorded command DTO before remapping targets or reference parameters.
All bookmark replacement writes should apply to that copy only.
The copied DTO is then passed to command execution.

Alternative considered: remap the stored DTO and restore it afterward.
That would be fragile around exceptions, transaction rollback boundaries, and future code paths that inspect the DTO during replay.

### Keep recorded and execution DTO responsibilities separate

The stored command DTO should remain the recorded input contract.
The remapped execution DTO should be treated as a transient execution artifact.
Replayable participant display should continue deriving recorded bookmarks from the stored DTO and actual bookmarks from mapping lookup or replay success state.

Alternative considered: persist both recorded and effective replay DTOs.
That would add data model complexity before there is a requirement to retain the effective execution shape.

### Subscriber lifecycle sync preserves replay entries

For replay entries, command subscriber `onStarted(...)` and `onCompleted(...)` should not call full `CommandLogEntry#sync(Command)` because that copies the in-memory command DTO back into the persisted entry.
Instead, replay entries should sync only execution metadata such as started and completed timestamps.
Normal recorded commands should continue using full sync.

Alternative considered: skip only `onStarted(...)` for replay entries.
That is insufficient because `onCompleted(...)` can see the same remapped in-memory command DTO and overwrite the recorded DTO later in the lifecycle.

### Test target and parameter mutation independently

Target and reference parameter remapping mutate different nested DTO structures.
Tests should assert that replay execution receives replacements and that the original target and parameter references remain unchanged on the command log entry after replay mapping.

Alternative considered: rely on participant display tests only.
Those tests can miss direct DTO mutation if they use cached or mocked participant data.

## Risks / Trade-offs

- [Risk] The defensive copy utility might be shallow for some command DTO nested structures.
  → Mitigate with tests that inspect nested targets and reference parameters after replay remapping.
- [Risk] Copying command DTOs on every replay adds overhead.
  → Mitigate by copying only during replay execution, where command execution dominates cost.
- [Risk] Existing tests may have assumed remapping mutates the stored DTO.
  → Mitigate by updating tests to assert recorded versus execution DTO separation.

## Migration Plan

No schema migration is expected.
Existing command log entries remain unchanged.
After deployment, replay will preserve recorded command DTO values even when mapping replaces targets or reference parameters at execution time.
Rollback by reverting the defensive-copy/remapping change if unexpected replay execution regressions appear.

## Open Questions

- Should replay diagnostics expose the transient remapped execution DTO directly, or are participant actual bookmark values sufficient?
