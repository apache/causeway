# Fourth-opinion triage — Batch 3 (CAUSEWAY-4010 .. 4022)

Clean-room reconciliation audit. MAINTENANCE=ecp/maintenance-branch (v2, source), MAIN=main/main (v4, target).
Merge-base 65d64cd85b7. All tickets in this batch are OpenSpec-era and cluster almost
entirely in the `extensions/core/commandlog` extension plus the synthetic
"selector"/"navigate" actions in `core/metamodel`.

Overarching finding: MAIN is substantially AHEAD of the v2 endpoint for the entire
commandlog + synthetic-navigation area. The v2 `CommandExportManager`/`CommandReplayManager`
pair was merged/renamed into `CommandManager` (+ `HasLimit`/`HasBaseline` paging mixins),
and the "selector" vocabulary was renamed to "navigate" (CAUSEWAY-4021). Nearly every
behavioural change in this batch is present in MAIN, usually in an evolved/renamed form.

## 1. Summary table

| Ticket | Classification | One-line intent | Verdict |
|--------|---------------|-----------------|---------|
| 4010 | BEHAVIOURAL (mixed; ~2/3 openspec docs) | Replay mapping SPI (input remap + result observe), in-mem/persistent listeners, conflict handling+config, import rework, YAML export of returnedObject | PRESENT |
| 4011 | BEHAVIOURAL | Export/replay manager paging, before-timestamp finders, batch-size paging, `--` memento delimiter | PRESENT (evolved) |
| 4012 | BEHAVIOURAL (mixed) | Config-gated synthetic "selector" actions on parented collections + supporting command-log export/publishing (RecordingSupport enum) | PRESENT (renamed to "navigate") |
| 4013 | BEHAVIOURAL | ReplayableCommand.getDto() shows Export DTO incl. result metadata | **MISSING** |
| 4014 | BEHAVIOURAL (mixed) | Replay-result mapping table + finders + participant view-models & refinements | PRESENT |
| 4015 | BEHAVIOURAL | Suppress initial-fixture command logging via Pause/Resume events (moved to applib) | PRESENT |
| 4016 | BEHAVIOURAL | Guard replay + reject foreground command recording while background commands pending | PRESENT |
| 4017 | BEHAVIOURAL | Validate known action targets + parameters on export (reachability) | PRESENT (renamed) |
| 4018 | BEHAVIOURAL | Suppress command recording for replay tooling objects (CommandRecordingSuppressed) | PRESENT |
| 4019 | BEHAVIOURAL | Add "navigate-to reference" synthetic action (scalar reference navigation) | PRESENT |
| 4020 | BEHAVIOURAL | Remove synthetic-navigation target parameter | PRESENT |
| 4021 | DOCS/STYLE + rename | Rename selector action -> navigate (pure repackaging/rename) | PRESENT (SKIP as rename) |
| 4022 | BEHAVIOURAL | Disable replay/retry unless PENDING/OK/FAILED (i.e. disable for UNDEFINED) | PRESENT (renamed method) |

Counts: 11 BEHAVIOURAL-present, 1 BEHAVIOURAL-MISSING (4013), 1 rename/DOCS-STYLE (4021, effectively part of 4012).

## 2. Per-ticket BEHAVIOURAL detail

### CAUSEWAY-4010 — replay mapping machinery — PRESENT
~33 commits, mostly OpenSpec propose/archive docs; ~12 touch code. 7 sub-parts, all in MAIN:
- SPI `CommandReplayMappingListener.lookup/onReplayResult` — PRESENT (`main/.../applib/spi/CommandReplayMappingListener.java`); input remapping in `ResultRemappingService.remapped(CommandDto)` (`.../dom/replay/ResultRemappingService.java`).
- In-memory listener `CommandReplayMappingListenerInMemory` — PRESENT (same conditional wiring, matchIfMissing=true).
- Conflict handling+config: `OnConflictPolicy{THROW_EXCEPTION,LOG_AND_CONTINUE}`, `StorageStrategy{IN_MEMORY,PERSISTENT}`, config `causeway.extensions.command-log.replay-result-mapping.*` — PRESENT (`CausewayConfiguration.java` ~3696-3717); identical conflict message.
- Import rework: `CommandDtoUtils.fromYamlForReplay`, `ImportedCommandDto`, `failIfYamlListRoot` — PRESENT, tested.
- YAML export of returned object: `toYamlExport`/`CommandExportDto`/`BookmarkDto` — PRESENT; wire fields `result`/`type`/`id` match final v2 state (`returnedObject` was intermediate only).
- Persistent listener + applib/JPA entities + menu + delete mixin — PRESENT (JDO entity absent = expected per IGNORE).
- CommandLogEntry result-state preservation (23751ef/a81f15a): PRESENT (equivalent) — MAIN `sync()` uses early-return guard `!isExportable() && !isExported()` protecting result; `init()` uses `copyOver(...RESULT...)` so no null-clobber when result key absent. Different implementation, same intent. (2nd-opinion agent flagged PARTIAL; on inspection intent is met.)

### CAUSEWAY-4011 — export/replay paging & finders — PRESENT (evolved)
Key commits: a92ec645 (repo finders+manager), 31eabda (delimiter `|`->`--`), ef8902c/546677e (paging), 05ea2c79 (extract inner actions).
Essential behaviour: before-timestamp finders (`findForegroundBeforeTimestamp`, `FIND_FOREGROUND_BY_TIMESTAMP_BEFORE`), batch-size paging, memento delimiter `--`, extracted mixin actions.
MAIN: `CommandLogEntryRepositoryAbstract.findForegroundBeforeTimestamp` (line 410) + `Nq.FIND_FOREGROUND_BY_TIMESTAMP_BEFORE` (CommandLogEntry.java:149); DELIMITER `--` in `CommandManager.State` (line 209); paging via `HasLimit`/`HasLimit_changeLimit`/`HasBaseline`. The whole area reworked into `CommandManager`. Note: v2's batch-size overload signatures (`...AndCanBeExported(since, batchSizeIfAny)` + `allMatches(query,batchSize)` helper) are NOT literally present — MAIN paginates via HasLimit instead. Behaviour present; implementation diverged. Verdict PRESENT.

### CAUSEWAY-4012 — synthetic selector actions + recording infra — PRESENT (renamed "navigate")
~50 commits (half openspec). 16 code sub-parts, ALL present in MAIN under the CAUSEWAY-4021 rename
("selector"->"navigate"; `__causeway_select_from_`->`__causeway_navigate_to_`;
`ParentedCollectionSelector*`->`ParentedCollectionNavigation*`; factory moved to
`spec.impl.SyntheticNavigationActionFactory`). Highlights verified:
config-gate via `RecordingSupport{ENABLED,DISABLED}` enum + `causeway.extensions.command-log.recording-support`
(`CausewayConfiguration.java` ~3744); styling/layout/validation/empty-disable facets all present as
`*ParentedCollectionNavigation*`; partial match `.contains`; column-limit `streamAssociationsForColumnRendering`;
excluded metadata params incl `logicalTypeName`; export rename `getResult`/`BookmarkDto.getType`
(no `getReturnedObject`/`getLogicalTypeName` remain); safe-action publishing
`CommandPublishingFacetForActionFromConfiguration.SafeEnabledByRecordingSupport`.
MAIN is AHEAD (extended to scalar references and to properties). No gaps.

### CAUSEWAY-4013 — show Export DTO (with result) — **MISSING**
Commit f9c7562. v2 changed `ReplayableCommand.getDto()` from
`commandRecord().map(CommandRecord::commandDto)` (raw CommandDto YAML) to
`commandLogEntry().map(cle -> CommandDtoUtils.CommandExportDto.of(cle.getCommandDto(), cle.getResult()))`
so the displayed DTO includes the recorded **result** bookmark metadata; describedAs changed to
"**Export** DTO of the original (replayable) Command".
MAIN evidence (adversarial):
`main/.../dom/replay/ReplayableCommand.java:365-373` still uses the OLD form
`commandRecord().map(CommandRecord::commandDto).map(...YamlUtils.toStringUtf8(commandDto...))`
and describedAs is still `"DTO of the original (replayable) Command"` (line 364 — no "Export").
`grep "Export DTO of the original"` = 0 hits; `grep "CommandExportDto.of"` in `getDto()` = 0 hits
(the only `CommandExportDto.of` call is in `CommandManager_exportSequence.java:71`, a different path —
the file-export sequence, not the on-screen DTO property).
=> The on-screen replayable-command DTO in MAIN does NOT include result metadata. Behavioural gap. Verdict MISSING.

### CAUSEWAY-4014 — replay mapping table, finders, participants — PRESENT
~35 commits (mostly docs); ~15 code. All 8 sub-parts present in MAIN:
finders (`findChangedReplayResultMappings` etc + named queries), `commandInteractionId` capture
(field+annotation+JPA column), `CommandReplayResultMapping_delete` mixin (MAIN uses `repository.remove`,
equiv), `getParticipants()` + `ReplayableCommandParticipant` view-model, listener `lookup`/`onReplayResult`
+ role-aware `actualBookmarkFor`, net synthetic "open" actions match v2 net state
(`_openTarget`(+TR) kept, `_openArgument`/`_openResult` correctly absent), `parameterName`/`getArgument()`
rename, readable `record Memento` with `--target`/`--parameter--<name>`/`--result` format. MAIN evolved further.

### CAUSEWAY-4015 — suppress initial-fixture command logging — PRESENT
Commits 6eea7c4 (move events to applib), 22412e4 (suppression).
Essential: `PauseCommandLoggingEvent`/`ResumeCommandLoggingEvent` (applib `services.command`),
`CommandLogPauseState(Listener)`, `InitialFixtureScriptsInstaller` posts pause/resume.
MAIN: `testing/fixtures/applib/.../InitialFixtureScriptsInstaller.java:89` posts
`new PauseCommandLoggingEvent(this)`; `CommandLogPauseState`, `CommandLogPauseStateListener`,
`CommandSubscriberForCommandLog` all present with tests. PRESENT.

### CAUSEWAY-4016 — background-pending command guard — PRESENT
Commits fdfa56d (guard replay), af00a69 (reject recording).
Essential: `ReplayPendingBackgroundCommands`, repo `findBackgroundAndNotYetStarted`,
`CommandSubscriberForCommandLog` rejection message.
MAIN: `dom/replay/ReplayPendingBackgroundCommands.java` present; `CommandSubscriberForCommandLog.java:143-148`
rejects with "Cannot continue command-log recording while %,d background command(s) are pending execution.";
integ test `CommandBackgroundGate_IntegTest`. PRESENT.

### CAUSEWAY-4017 — known-target/parameter export validation — PRESENT (renamed)
Commits 9902240 (targets), 6f7ce4a (parameters).
v2 class `CommandExportKnownTargetValidator` with `exportRootPredicate`, `validate(baseline, entries)`,
reachability check + failure message "Target %s is unknown for command export...".
MAIN: renamed to `CommandKnownParticipantsValidator` (`.../dom/replay/CommandKnownParticipantsValidator.java`)
with `exportRootPredicate`, `validateParticipants`, `ReplayContext.isExportRoot`,
`CommandManager.validateKnownTargets`; message "Unknown participants (target and/or action args)".
Both target+parameter validation folded into "participants". Tests
`CommandKnownParticipantsValidatorTest`, `CommandManagerKnownParticipantsTest`. PRESENT.

### CAUSEWAY-4018 — suppress recording for replay tooling objects — PRESENT
Commit eadaa13. Essential: `@CommandRecordingSuppressed` marker + `MemberExecutorServiceDefault`
honouring it; applied to CommandLogEntry/ReplayableCommand/managers/participant.
MAIN: `CommandRecordingSuppressed` present in `core/runtimeservices/.../MemberExecutorServiceDefault.java`
(+ test), config, and applied across commandlog dom classes and `SyntheticNavigationActionFactory`. PRESENT.

### CAUSEWAY-4019 — scalar reference navigation synthetic action — PRESENT
Commit 0158e76. Essential facets: `ScalarReferenceNavigationFacet(Default)`,
`ActionInvocationFacetForScalarReferenceNavigation`, `DisabledFacetForNullScalarReferenceNavigation`,
`LayoutGroupFacetForScalarReferenceNavigation`.
MAIN: all present under `core/metamodel/.../facets/actions/synthetic/` + wired in
`SyntheticNavigationActionFactory`. PRESENT.

### CAUSEWAY-4020 — remove synthetic-navigation target parameter — PRESENT
Commit 441f1e1. Deletes `*FacetForParentedCollectionNavigationParent` (parent-target param) facets and
simplifies matching util.
MAIN: `grep FacetForParentedCollectionNavigationParent` in core/metamodel = 0 hits (correctly removed);
`ParentedCollectionNavigationMatchingUtil` present. PRESENT.

### CAUSEWAY-4022 — disable replay/retry for non-replayable states — PRESENT (renamed method)
Commit cee1887. v2 added `ReplayState.isReplayOrRetryEnabled()` = PENDING||OK||FAILED and wired it so
UNDEFINED/EXPORTED/EXCLUDED cannot be replayed/retried.
MAIN: `grep isReplayOrRetryEnabled` = 0 hits, BUT MAIN has `ReplayState.isReplayable()`
(`ReplayState.java:66-70`) with identical logic PENDING||OK||FAILED, wired via
`ReplayableCommand.canReplayOrRetry` -> `disableReplayOrRetry()` returning
"Cannot replay, unless PENDING, OK or FAILED". Same behaviour, renamed method. PRESENT.

## 3. Skipped / rename-only

### CAUSEWAY-4021 — rename selector -> navigate — SKIP (pure rename)
Commit f3450ea: pure repackaging/rename of `ParentedCollectionSelector*` ->
`ParentedCollectionNavigation*` and prefix `__causeway_select_from_` -> `__causeway_navigate_to_`.
Confirmed present in MAIN (`SyntheticNavigationActionFactory.java:69`). This is the IGNORE-listed
"pure rename" category; behaviour is covered by CAUSEWAY-4012. No independent gap.
