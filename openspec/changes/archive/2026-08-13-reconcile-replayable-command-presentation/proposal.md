> **Execution note:** several self-contained presentation fixes reusing existing infrastructure; **`medium`
> reasoning effort** is sufficient.

## Why

Child change 6 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancies **MA-6, MA-7, MA-8,
MA-9**; third-opinion F7a/F7b/F7c/F5, fourth-opinion G3/G4/G10, second-opinion D1). Four `ReplayableCommand`
presentation behaviours from final maintenance are missing on `main`. Product decision **D-A** is to port the
recorded-vs-actual target UI to parity. (`ReplayableCommand` is byte-identical between the audited head and
current HEAD, so all anchors hold.)

- **MA-6 — displayed DTO omits the recorded result.** `getDto()` renders the raw `CommandDto`
  (`ReplayableCommand.java:365-374`, `YamlUtils.toStringUtf8(commandDto, …)`); maintenance renders a
  `CommandExportDto.of(commandDto, result)` so the displayed YAML carries the result envelope and result
  bookmark. The `CommandExportDto` infrastructure already exists on `main` (used by `CommandManager_exportSequence`).
- **MA-7 — `actualBookmarkFor` falls back only for `OK`.** `ReplayableCommand.java:345-356` returns the recorded
  bookmark as the actual only when `replayState == ReplayState.OK`. Maintenance treats a recorded-only command
  (replay state `UNDEFINED`) as executed-ok too, so recorded-side `UNDEFINED` target/parameter/result rows keep
  their actual-bookmark link. `ReplayState` on `main` has **no** `isExecutedOk()` method.
- **MA-8 — title truncates the target id.** `title()` (`:152-155`) builds `getTargetType() + ":" + getTargetId()`,
  and `getTargetId()` (`:201-209`) ellipsifies the id to 10 chars (`_Strings.ellipsifyAtEnd(id, 10, "...")`).
  Maintenance titles use the full recorded target bookmark.
- **MA-9 — no recorded-vs-actual target UI.** There are no top-level `@Property getTarget()`/`getActualTarget()`.
  The source files `ReplayableCommand_openTarget` and `ReplayableCommand_openTargetTR` **exist and work** (they
  open the recorded target via `CommandLogEntry::getTarget`) but are **not registered** in
  `CausewayModuleExtCommandLogApplib`, and neither offers a RECORDED/ACTUAL choice. The recorded-vs-actual data
  model partially exists (`CommandReplayResultMapping.Nq.FIND_BY_RECORDED_BOOKMARK` / `FIND_BY_ACTUAL_BOOKMARK`),
  but there is no `getTargetType()` on the mapping.

## What Changes

- **MA-6:** `getDto()` renders `CommandDtoUtils.CommandExportDto.of(commandDto, result)` (result envelope +
  bookmark included), reusing the existing export-DTO infrastructure.
- **MA-7:** add `ReplayState.isExecutedOk()` returning true for `UNDEFINED` or `OK`, and use it in
  `actualBookmarkFor` (both the result-role gate and the unmapped fallback) so recorded-only `UNDEFINED`
  participants expose their recorded bookmark as the actual.
- **MA-8:** build the replayable title from the full recorded target bookmark (untruncated); keep the truncated
  `getTargetId()` for any column/table use that intentionally wants a short id.
- **MA-9 (D-A):** add top-level `@Property getTarget()` and `getActualTarget()` projections; **enhance and
  register** `ReplayableCommand_openTarget` / `_openTargetTR` with a `TargetType {RECORDED, ACTUAL}` parameter,
  opening the recorded or the actual target, with the action disabled + user feedback when the chosen target
  cannot be resolved.
- No change to replay execution, eligibility, mapping SPI, persistence, or the participant-row model.

## Capabilities

### Modified Capabilities

- `replayable-command-projection`: the participant actual-bookmark fallback covers `UNDEFINED` as well as `OK`
  (MA-7); the displayed DTO includes the recorded result (MA-6); the title uses the full recorded target (MA-8);
  and the command exposes recorded and actual target projections with a recorded-vs-actual open action (MA-9).

## Impact

- Affects commandlog applib `ReplayableCommand` (`getDto`, `actualBookmarkFor`, `title`, new target properties),
  `ReplayState` (`isExecutedOk`), the two `openTarget` mixins, and `CausewayModuleExtCommandLogApplib`
  registration.
- MA-9 is a product-decision port (D-A); MA-6/7/8 are cheap, self-contained presentation fixes.
- Requires coverage: displayed DTO contains the result envelope; `UNDEFINED` participants expose recorded actual
  bookmarks; title is untruncated; `openTarget` offers RECORDED/ACTUAL and is disabled with feedback when the
  target does not resolve; `ReplayableCommandPresentationTest` updated so the openTarget mixins are now registered.
