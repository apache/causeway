## Context

`ReplayableCommand` is the read model over a recorded/replayed command. Current HEAD (identical to the audited
head):

```
// extensions/core/commandlog/applib/.../dom/replay/ReplayableCommand.java
:152  title() -> getTargetType() + ":" + getTargetId() + " #" + getMember() + timestamp
:201  getTargetId() -> …_Strings.ellipsifyAtEnd(id, 10, "...")            // truncated to 10 chars
:345  actualBookmarkFor(role, recordedBookmark, replayState):
        if (role == RESULT && replayState != OK) return empty;
        return resultRemappingService().lookup(recordedBookmark)
            .or(() -> replayState == OK ? Optional.of(recordedBookmark) : empty);
:365  getDto() -> YamlUtils.toStringUtf8(commandDto, …)                    // raw CommandDto
```

`ReplayState` has `isFailed`, `isReplayable`, `isOkOrExcluded`, etc., but **no** `isExecutedOk()`. The
`openTarget` mixins exist and open the recorded target (`ReplayableCommand_openTarget.act()` →
`commandLogEntry().map(CommandLogEntry::getTarget).flatMap(bookmarkService::lookup)`), and `_openTargetTR` is the
table-row variant; **neither is imported** by `CausewayModuleExtCommandLogApplib` (its registered
`ReplayableCommand_*` list omits both). `CommandManager_exportSequence` already builds
`CommandDtoUtils.CommandExportDto.of(entry.getCommandDto(), entry.getResult())`, so the export-DTO shape is
available for `getDto()`.

Maintenance (`ecp`, CAUSEWAY-4013/4038/4042) renders the export DTO in `getDto()`, uses `isExecutedOk()`
(`UNDEFINED || OK`) for the actual-bookmark fallback, builds the title from the full recorded bookmark, and
exposes top-level `getTarget()`/`getActualTarget()` plus a RECORDED/ACTUAL `openTarget`.

## Goals / Non-Goals

**Goals:**

- The displayed DTO carries the recorded result (MA-6).
- Recorded-only (`UNDEFINED`) participants keep their actual-bookmark link (MA-7).
- The title identifies the command by its full recorded target (MA-8).
- Users can open the recorded or the actual target, with clear disabling/feedback (MA-9 / D-A).

**Non-Goals:**

- No change to replay execution, eligibility, the replay-mapping SPI, or persistence.
- Not removing the participant-row model — the direct target projections are additive (maintenance retains both).
- Not deleting the `openTarget` source files (they are not dead — they are enhanced and registered).

## Decisions

### MA-6 — render the export DTO in `getDto()`

Change `getDto()` to build `CommandDtoUtils.CommandExportDto.of(commandDto, result)` and serialise that, so the
displayed YAML includes the result envelope and result bookmark, matching the exported form. Reuse the existing
`CommandExportDto`/`toYamlExport` infrastructure; no new serialisation path.

### MA-7 — add `ReplayState.isExecutedOk()` and use it in the fallback

Add `isExecutedOk()` returning true for `UNDEFINED` or `OK`, and replace the two `replayState == OK` checks in
`actualBookmarkFor` (the result-role gate and the unmapped fallback) with `isExecutedOk()`. Rationale: a
recorded-only command (never imported for replay, state `UNDEFINED`) has no separate replay environment, so its
recorded bookmark *is* its actual bookmark; `main` currently drops that link for `UNDEFINED`.

### MA-8 — title uses the full recorded target bookmark

Build the title from the complete recorded target bookmark (type + full id, e.g. `Bookmark::stringify`), not the
10-char-ellipsified `getTargetId()`. Keep `getTargetId()` as-is for any table-column use that intentionally wants
a short id.

### MA-9 (D-A) — enhance and register the recorded-vs-actual target UI

- Add top-level `@Property getTarget()` (recorded target bookmark/object) and `getActualTarget()` (the actual
  target via the result-remapping / actual-bookmark lookup, using `isExecutedOk()` semantics from MA-7).
- Extend `ReplayableCommand_openTarget` and `_openTargetTR` with a `TargetType { RECORDED, ACTUAL }` parameter
  and open the chosen target; disable the action with user feedback when the chosen target cannot be resolved.
- Register both mixins in `CausewayModuleExtCommandLogApplib`, and update `ReplayableCommandPresentationTest`
  (which currently asserts they are unregistered) to assert they are registered with the RECORDED/ACTUAL choice.
- The recorded-vs-actual data uses the existing `CommandReplayResultMapping` bookmark queries; no new persistent
  field is required (the "actual" target derives from the mapping/remapping lookup, as `getActualTarget()` does).

Rejected — the "delete the dead files" alternative: superseded by D-A (port to parity). The files are functional,
so they are extended and registered rather than removed.

## Acceptance evidence

- `getDto()` output contains the result envelope and result bookmark for a command with a recorded result.
- A recorded-only (`UNDEFINED`) command's target/parameter/result participants expose their recorded bookmarks as
  actual bookmarks; a pre-`OK` genuinely-pending command still exposes no actual bookmark for unmapped
  participants (existing scenarios preserved).
- The title contains the full (untruncated) recorded target id.
- `openTarget` offers RECORDED and ACTUAL, opens the correct target for each, and is disabled with a message when
  the chosen target does not resolve; both mixins are registered.
