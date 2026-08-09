# Fourth-opinion triage — Batch 4 (CAUSEWAY-4023..4042, later June/July OpenSpec era)

Independent clean-room reconciliation. SOURCE = maintenance-branch (v2, `.../causeway/ecp`), TARGET = main (v4, `.../causeway/main`). Merge-base `65d64cd85b7`.

**Cross-cutting context (critical):** Almost the entire batch lives in the `extensions/core/commandlog` command export/replay subsystem. Main contains a **more-evolved** version of this exact subsystem. The v2 design used separate `CommandExportManager_<verb>` mixins; main consolidated these into a `CommandManager` family (`CommandManager`, `CommandManager_<verb>`, `CommandManagerAbstract` eliminated by merge) plus a newer **participants** model (`ReplayableCommandParticipant`, `ReplayableCommandParticipantTracker`, `CommandKnownParticipantsValidator`, `knownParticipants`). None of the ticket numbers appear in main's git history — the behaviour was forward-ported (unattributed, or via reconciliation tickets CAUSEWAY-3910/3923), NOT cherry-picked. So verdicts are on **behaviour/semantics**, accounting for renames; and main sometimes chose a *different* successor design that supersedes the v2 approach (or, in a few cases, deliberately rejected it).

Renames to keep in mind: `CommandExportManager_*`→`CommandManager_*`; `CommandExportManagerMovementSupport`→`CommandManagerMovementSupport`; `CommandExportKnownTargetValidator`→`CommandKnownParticipantsValidator`; `ReplayableCommand#isExportable`→`isKnownParticipants`; `spi/RefData`→`api/applib/.../domain/RefData`.

## 1. Summary table

| Ticket | Classification | One-line intent | Verdict |
|---|---|---|---|
| 4023 | BEHAVIOURAL | Preserve recorded DTO/result during replay sync; recording-support export guard | PRESENT |
| 4024 | BEHAVIOURAL | Add moveCommands action + squash timings + DTO deep-copy timestamp | PRESENT |
| 4025 | BEHAVIOURAL (simplification) | Single `commandsInSequence` list, remove mode-toggle/dual collections | PRESENT (via successor `CommandManager`) |
| 4026 | BEHAVIOURAL (+docs) | Exclude recorded commands, EXCLUDED state, exportability flag, validation | **PARTIAL** |
| 4027 | BEHAVIOURAL | Restore/unexclude excluded commands | PRESENT |
| 4028 | BEHAVIOURAL | Delete excluded commands | PRESENT |
| 4029 | BEHAVIOURAL | Directional up/down move, autoselect exportable, remove make-selected-exportable | **MISSING** (intentional divergence) |
| 4030 | BEHAVIOURAL | Singleton-list command result capture + validation message | PRESENT |
| 4031 | BEHAVIOURAL (+docs) | Replayable command next/previous navigation, getHasResult | PRESENT |
| 4032 | BACKPORT-FROM-MAIN | Refdata replay-arg SPI + default marker service | PRESENT |
| 4033 | BACKPORT-FROM-MAIN | Suppress useless safe commands + record property edits | PRESENT |
| 4034 | BEHAVIOURAL (mixed) | Delete replay mappings; RefData on secman entities; validator/guard/view-model | **MIXED — PARTIAL** |
| 4037 | BEHAVIOURAL | Make `saveForReplay` idempotent | **MISSING** |
| 4038 | BEHAVIOURAL (mixed) | Navigate-to param ordering, REFERENCES_PARENT hiding, replay-arg padding, target | **MIXED — PARTIAL** |
| 4039 | BEHAVIOURAL (large refactor) | Consolidate export/replay managers; POST_PROCESS nav synthesis; isolate mixin events | **MOSTLY PRESENT — 1 MISSING strand** |
| 4042 | BEHAVIOURAL (large, mixed) | openTarget refinement, AppFeat RefData, replay-error handling, config advisor | **PARTIALLY PRESENT** |

## 2. BEHAVIOURAL tickets — detail

### CAUSEWAY-4023 — PRESENT
- Intent (spec `command-replay-result-mapping`): during lifecycle sync of a replayed command, update execution timings but do NOT overwrite recorded command DTO/target/member/result/exception. Plus guard known-target validation behind `recordingSupport.isEnabled()`.
- Key commits: 01f51258 (preserve DTOs), a130e08b (metadata sync), a3f8fe27 (export validation guard).
- Essential behaviour: `CommandLogEntry.sync(Command)` sets `startedAt`/`completedAt` then early-returns when replay state is active, skipping DTO/result writes; `CommandSubscriberForCommandLog` collapsed to call `sync()`.
- Evidence in main: `extensions/core/commandlog/applib/.../dom/CommandLogEntry.java:159-177` — `sync` sets timings then `if(replayState != null && !replayState.isExportable() && !replayState.isExported()) return;`. Guard predicate broader on main ({PENDING,OK,FAILED,EXCLUDED} vs v2 {PENDING,OK,FAILED}) but the load-bearing behaviour is equivalent. Recording-support guard present via `CommandKnownParticipantsValidator` wiring.
- Verdict: PRESENT (renamed guard, same semantics).

### CAUSEWAY-4024 — PRESENT
- Intent (spec `command-export-command-reordering`): "move commands" action retimestamps selected commands after a target, optional squash-timings (1s increments), updating timestamp inside a DEEP COPY of the DTO (not the shared recorded DTO). Late commit 63b57a98 fixes "update the timestamp properly within the DTO".
- Key commits: 34781cd9 (squash timings), e72acaf4 (move commands action), 63b57a98 (timestamp-in-DTO fix).
- Essential behaviour: `CommandExportManager_moveCommands` (`squashTimings`, `SQUASH_GAP_MILLIS=1000`), `CommandExportManagerMovementSupport.updateCommandDtoTimestamp` via new `CommandDtoUtils.copy(CommandDto)` JAXB deep-copy.
- Evidence in main: `dom/replay/CommandManager_moveCommands.java:59-89` (squashTimings param+default); `CommandManagerMovementSupport.java:33` `MINIMUM_GAP=Duration.ofSeconds(1)`, `:167-168` `CommandDtoUtils.copy(commandDto)` then `copy.setTimestamp(...)`; `api/applib/.../util/schema/CommandDtoUtils.java:270` `copy(...)` deep-copy.
- Verdict: PRESENT (renamed classes; deep-copy + squash both present).

### CAUSEWAY-4025 — PRESENT (via successor)
- Intent (spec `command-export-manager-command-list`): collapse dual `notYetExported`/`exported` collections + `Mode` enum + `toggleMode` into a single baseline-bounded `commandsInSequence`.
- Key commits: 5847584e (simplify), aec56668 (proposal).
- Essential behaviour: single `commandsInSequence` collection; delete `CommandExportManager_toggleMode`; `ReplayState` tweaks.
- Evidence in main: `dom/replay/CommandManager.java:109 getCommandsInSequence()` — the single baseline-bounded collection consumed by move/exclude/exportSequence/replay mixins; layout `CommandManager.layout.fallback.xml:28`. No `toggleMode`/`enum Mode` anywhere. NB: main did NOT delete the legacy `CommandExportManager.java` (still has dual `getNotYetExported()`/`getExported()` at :143/:236) — a cleanup divergence, not a behaviour gap.
- Verdict: PRESENT (successor `CommandManager.commandsInSequence` embodies the simplification; nothing to port).

### CAUSEWAY-4026 — PARTIAL
- Intent (spec `command-export-command-exclusion`, `replayable-command-exportability`): add `ReplayState.EXCLUDED`; `excludedCommands` collection + `excludeCommands` action (autoselect non-exportable); per-`ReplayableCommand` `getExportable()` property; relax known-target validator to cover property-edit (non-action) commands; reword validation messages.
- Key commits: 7fabedde (exclude from export manager), 5889abc5 (property-edit export targets), 980a4fb3 (validation messages), 6630069d (exportability).
- Evidence in main:
  - EXCLUDED state + excluded collection: PRESENT — `CommandManager.getExcluded()` (`CommandManager.java:117`; filter `getReplayState() != ReplayState.EXCLUDED` at :111/:181); `CommandManager_excludeCommands.java:63` sets `ReplayState.EXCLUDED`.
  - Validator relaxation + messages: PRESENT — `CommandKnownParticipantsValidator` validates any entry with non-null `commandDto`; messages reworded (`:172/:175` "...is unknown for command export; select a prior navigation or finder action...").
  - excludeCommands autoselect: renamed predicate — main's `defaultSelected()` (:89) uses `!command.isKnownParticipants()` vs v2 `getExportable()==FALSE`. Same intent.
  - Per-command `getExportable()` property on `ReplayableCommand`: **MISSING** — `grep getExportable` on main's `ReplayableCommand.java`/`CommandExportManager.java` = nothing. Main handles export eligibility via collections + `isKnownParticipants` instead.
- Verdict: PARTIAL — core exclusion behaviour present under `CommandManager`; the v2 `getExportable` property is absent (superseded by `isKnownParticipants`, arguably equivalent — flag for confirmation).

### CAUSEWAY-4027 — PRESENT
- Intent: `CommandExportManager_unexcludeCommands` resets selected EXCLUDED commands to UNDEFINED, restoring them.
- Key commit: d1b44cb8.
- Evidence in main: `CommandManager_unexcludeCommands.java` present, registered `CausewayModuleExtCommandLogApplib.java:132`, `choicesFrom="excluded"` (:37), guards non-EXCLUDED (:90,:97-98).
- Verdict: PRESENT (renamed).

### CAUSEWAY-4028 — PRESENT
- Intent: `CommandExportManager_deleteCommands` permanently deletes selected EXCLUDED entries.
- Key commit: 2a70c164.
- Evidence in main: `CommandManager_deleteCommands.java` present, registered :133, `choicesFrom="excluded"` (:35).
- Verdict: PRESENT (renamed).

### CAUSEWAY-4029 — MISSING (intentional divergence)
- Intent (spec `command-export-command-reordering`, `replayable-command-actions`): (a) split move into directional `moveCommandsUp`/`moveCommandsDown` (shared `MovementSupport` + `Direction`); (b) autoselect exportable commands as export default (`exportSelected.defaultSelected()` filtering `getExportable()==TRUE`); (c) REMOVE `CommandExportManager_makeSelectedExportable`; (d) movement icons.
- Key commits: 817817206 (directional actions), 2932b6ae (autoselect), ea438afc (remove make-selected-exportable), efd9cbe0 (icons).
- Evidence in main:
  - Directional up/down: **MISSING** — main has single `CommandManager_moveCommands` (target-after + squash); `CommandManagerMovementSupport` has NO `Direction`. Main's `ReplayableCommandPresentationTest.java:126` explicitly asserts layout `doesNotContain("moveCommandsUp","moveCommandsDown")` — deliberate rejection.
  - Autoselect-exportable on export: **MISSING** — `CommandExportManager.exportSelected` (:167+) has no `defaultSelected()`.
  - Remove make-selected-exportable: **CONTRADICTED** — main still has `CommandExportManager.makeSelectedExportable` (:270, registered :114) and `ReplayableCommand_makeExportable` (registered :103).
- Verdict: MISSING — all three v2 changes absent; main kept an intentional alternative UX. Low-risk (cosmetic/UX), but genuinely not forward-ported.

### CAUSEWAY-4030 — PRESENT
- Intent: when a command returns a singleton list (size-1 collection), capture the single element as the entity result (so it can be a known export target); improve unknown-target validation message.
- Key commits: e1027241 (capture results), c69fe6dd (validation).
- Evidence in main: `core/runtimeservices/.../executor/MemberExecutorServiceDefault.java:360-385` — identical `singletonResultCandidate` unpacking a size-1 `PackedManagedObject`. Reworded message at `CommandKnownParticipantsValidator.java:172,175` matches v2 verbatim.
- Verdict: PRESENT (both strands; validator renamed).

### CAUSEWAY-4031 — PRESENT
- Intent (spec `replayable-command-navigation`): add `ReplayableCommand_next`/`_previous` (SAFE nav to adjacent foreground command by timestamp), `getHasResult` property, domain-service target handling, columnOrder/layout cosmetics.
- Key commits: de0e81de (navigation), 66aafa88 (cosmetics).
- Evidence in main: `ReplayableCommand_next.java`/`_previous.java` present (registered :105/:106); `next()`/`previous()`/`adjacent()` at `ReplayableCommand.java:378-408` via `findForegroundBeforeTimestamp`/`findForegroundSinceTimestamp`, `SemanticsOf.SAFE`, seq 0.2/0.3 (matches v2, plus eligibility filter). `getHasResult` at :245. Cosmetics = DOCS/STYLE.
- Verdict: PRESENT.

### CAUSEWAY-4034 — MIXED / PARTIAL
- Intent: (a) delete replay mappings action; (b) `RefData` marker on secman entities (enable replay of security setup); (c) validator hack to skip `_navigate_to` in overloading check; (d) guard against duplicate synthetic actions; (e) support view models as command results.
- Key commits: 79031ec0 (delete mappings), 588a24ad (RefData for entities), 6f1fd269 (move RefData marker), bd05ada0 (view models as results), 067e3ba4 (validator hack), 6878cc51 (dup-synthetic guard).
- Evidence in main:
  - (a) delete replay mappings: PRESENT — `app/CommandLogMenu.java` `deleteReplayResultMappings`; `CommandReplayResultMappingRepository.java:51 removeAll()` + impl `...Abstract.java:96`.
  - (b) RefData on secman entities: PRESENT — `ApplicationUser.java:80`, `ApplicationRole.java:64`, `ApplicationTenancy.java:64`, `ApplicationPermission.java:93` all carry `RefData` (import `applib.domain.RefData`). Modelled as interfaces on main vs abstract classes on ecp — marker still applied.
  - (c) validator hack (skip `_navigate_to` in `ActionOverloadingValidator`): **MISSING** — `ActionOverloadingValidator.java:60` still unconditional `overloadedNames.add(...)`, no prefix skip. Likely moot because main's `SyntheticNavigationActionFactory` only installs synthetic actions when `ownerSpec == declaringType` (see d), so overload collisions may not arise — but not verified behaviourally. Assess, don't blind-port.
  - (d) duplicate-synthetic guard: PRESENT (evolved) — `SyntheticNavigationActionFactory.java:125,133` (`ownerSpec == collection/reference.getDeclaringType()`).
  - (e) view models as command results: **MISSING** — ecp made `ApplicationRoleManager` etc `implements ViewModel` with `viewModelMemento()`; main's `ApplicationRoleManager.java:41` is plain class, menu still `factory.viewModel(new ApplicationRoleManager())` (`ApplicationRoleMenu.java:87`). Different memento strategy — assess whether main supports view-model results another way.
- Verdict: MIXED — (a)(b)(d) PRESENT; (c) MISSING-but-likely-moot; (e) MISSING/superseded. Net PARTIAL.

### CAUSEWAY-4037 — MISSING
- Intent: make `saveForReplay` idempotent — guard by `findByInteractionId` and return existing entry rather than inserting a duplicate.
- Key commit: 4bc7b2c9 (single impl commit).
- Evidence in main: `dom/CommandLogEntryRepositoryAbstract.java:337-346` `saveForReplay` does `detachedEntity → init → persist` with NO idempotency check. `findByInteractionId` exists (:79) but is not called from `saveForReplay`.
- Verdict: MISSING — genuine, small, self-contained behavioural gap to forward-port.

### CAUSEWAY-4038 — MIXED / PARTIAL
- Intent: (a) `LayoutOrderFacetForParentedCollectionNavigation` deferred nav-param ordering; (b) `CommandExecutorServiceDefault` replay-arg padding for parented-collection navigation; (c) hide navigate-to params when property hidden `Where.REFERENCES_PARENT`; (d) surface `target` property/column for `ReplayableCommand`.
- Key commits: 8c28ac01 (impl), 0953577 (ordering+bool), db291cd2 (REFERENCES_PARENT hide), 9937366 (rework ordering), 7a8d749f (pad replay args), d80cda55 (surface target).
- Evidence in main:
  - (a) LayoutOrderFacet: PRESENT — `core/metamodel/.../facets/actions/synthetic/LayoutOrderFacetForParentedCollectionNavigation.java`, installed `SyntheticNavigationActionFactory.java:163`.
  - (b) replay-arg padding: **MISSING** — `CommandExecutorServiceDefault.java:213/441` uses plain `argAdaptersFor(actionDto)`; no `argAdaptersForParentedCollectionNavigation`, no pad-missing-filters logic.
  - (c) REFERENCES_PARENT hiding: **MISSING** — main's `SyntheticNavigationActionFactory.filterPropertiesOf` (214-244) has no hidden-facet/`REFERENCES_PARENT` check. Uses a different `streamAssociationsForColumnRendering` path — assess whether it already excludes parent-referencing props.
  - (d) surface `target`: PARTIAL/MISSING — no `getTarget()` property and no `target` column in main's fallback; main instead surfaces targets as `Role.TARGET` participants (`addTargetParticipants`, :283). Standalone target property/column absent; decide if participants supersede.
- Verdict: MIXED PARTIAL — (a) present; (b)(c) missing; (d) different representation.

### CAUSEWAY-4039 — MOSTLY PRESENT (one MISSING strand)
- Intent: major refactor — merge `CommandExportManager`+`CommandReplayManager` into `CommandManager` (via abstract, then merged); move injected services into `ReplayContext`; rename `isExportable`→`isKnownParticipants`, `isReplayable`→`isDoOp`; `ReplayableCommandParticipantTracker`; remove `EXPORTABLE` state; add 'export' + `@Import` + `ResultRemappingService`; record view-model results; **remove INLINE navigation-action synthesis → POST_PROCESS only**; re-entrancy/StackOverflow guard; **isolate mixin domain event facets**.
- ~44 commits (all dated 2026-06-30, squashed range). Representative: 0d2aaed3 (rename managers/stub abstract), 486796c1 (merge into CommandManager), e320575 (isKnownParticipants), bf8aab45 (isDoOp), 68ba09f0 (participant tracker), 359de942 (POST_PROCESS synthesis), 13c67c8b (remove INLINE), 25b0b7ee (re-entrancy guard), d5cdc5da (isolate mixin domain event facets).
- Strand verdicts vs main:
  - Consolidation into `CommandManager` (+ abstract eliminated): PRESENT — `dom/replay/CommandManager.java:49`.
  - `ReplayContext` holds injected services: PRESENT — `ReplayContext.java:44` record (RepositoryService, InteractionService, TransactionService, CommandExecutorService, ClockService, ResultRemappingService, BookmarkService, refdata svcs).
  - `isKnownParticipants` (was isExportable): PRESENT — `ReplayableCommand.java:257` delegating to tracker.
  - `isDoOp` (was isReplayable): PARTIAL — main keeps `ReplayState.isReplayable()` (`:115/:555`); no `isDoOp` token. Semantic equivalent, rename not applied (cosmetic).
  - `ReplayableCommandParticipantTracker`: PRESENT — interface `:27`, field `:95`, `CommandManager` implements it.
  - Remove EXPORTABLE state: PRESENT — `dom/ReplayState.java` has no `EXPORTABLE`.
  - 'export'/remapping/`ResultRemappingService`: PRESENT — `CommandManager_exportSequence.java:52,67-73` builds `CommandExportDto`+`remapResults`; `ResultRemappingService.java:49` wired via ReplayContext.
  - Record view-model result: PRESENT (semantic) — `MemberExecutorServiceDefault.java:388-395` records bookmark for view models too.
  - POST_PROCESS-only nav synthesis + INLINE removal + re-entrancy guard: PRESENT (semantic, different impl) — `SynthesizeNavigationActionsPostProcessor.java:50-52`; gated in `ObjectSpecificationDefault.java:243-246` by `recordingSupport().isEnabled()`; no INLINE config enum; idempotent guard via `existingSyntheticActionIds` (:249-268).
  - **Isolate mixin domain event facets (d5cdc5da): MISSING** — main's `SynthesizeDomainEventsForMixinPostProcessor.java:53,64,75` still mutates the shared mixin facet via `facet.initWithMixee(...)`; no local-overlay isolation / `ObjectActionMixedIn` local-invocation-facet path. Mixin domain-event defaults from one mixee can still pollute other mixees.
  - Divergence (not a gap per se): main RETAINS live `CommandExportManager`+`CommandReplayManager` alongside `CommandManager` (all `@Import`ed, three menu entries `CommandLogMenu.java:97-315`), whereas ecp end-state deleted both. Cleanup divergence — confirm intent.
- Verdict: MOSTLY PRESENT; the single genuine behavioural gap is **isolate mixin domain event facets** (MISSING).

### CAUSEWAY-4042 — PARTIALLY PRESENT
- Intent: multi-part — register `ReplayableCommand_openTarget`/`_openTargetTR` mixin; refine openTarget with RECORDED vs ACTUAL choice + `actualTarget`; `disableAct`; AppFeat implements `RefData` (enable addPermission replay); replay-error handling (don't rollback, return success, typed-message prefix); clear query cache in new txn; config advisor property + service checks; remove nextPage/previousPage + raise limit; columnOrder rename.
- ~19 commits. Representative: 102fbb50 (register openTarget mixin), 8efd1dd6 (refine openTarget/actualTarget), 8ed3c8c4 (disableAct), 6075f978 (AppFeat RefData), 3f128a79 (replay error → success), 622ed00b (don't throw away failure), 9a9d4444 (clear cache/preserve msg), 065ca38a (config property+checks), c1177675 (visibility fix), d87d39b8 (remove pages/raise limit), f941cd60 (columnOrder).
- Sub-part verdicts vs main:
  - (1) register `ReplayableCommand_openTarget`/`_openTargetTR` mixin: **MISSING** — `CausewayModuleExtCommandLogApplib.java:103-108` does NOT register `_openTarget` (file exists on disk, unregistered).
  - (2) refine openTarget RECORDED/ACTUAL + `actualTarget`/`getActualTarget`/`actualTargetBookmarkIfAny`: **MISSING** — main's `ReplayableCommand_openTarget.act()` takes no arg, resolves only recorded target; no `actualTarget` anywhere in commandlog.
  - (3) `disableAct` on `choicesTargetType().isEmpty()`: **MISSING** — main uses old `isEmpty() ? "No corresponding CommandLogEntry" : null`.
  - (4) AppFeat implements `RefData`: **MISSING** — `secman/.../feature/api/ApplicationFeatureChoices.java:121-124` `AppFeat` implements only `Comparable, ViewModel`, no RefData. (Blocks addPermission replay.)
  - (5) replay-error `mapFailureToSuccess` + typed-exception message prefix (`Disabled:`/`Invalid:`): **MISSING** — main's `ReplayableCommand.tryReplay` (:508) returns the failure; `onReplayError` (:566) does `saveAnalysis(ex.toString())` with no prefixing; no Hidden/Disabled/Invalid handling.
  - (6) 622ed00b `tryReplayCaptureOutcome` refactor: N/A — main uses older shape (coupled to 5).
  - (7) config `causeway.core.runtime-services.command-executor-service.interaction-advisor-policy` (enum CHECK/CHECK_BUT_IGNORE/NO_CHECK, default NO_CHECK) + advisor checks: **PRESENT** — `CausewayConfiguration.java:1853-1862`; enforcement `CommandExecutorServiceDefault.java:272-330` (`applyActionAdvisorPolicy`/`applyPropertyAdvisorPolicy`).
  - (8) visibility check uses `targetAdapter` (not `headTarget`): PRESENT — main never had the bug (`CommandExecutorServiceDefault` call site :220).
  - (9) clear cache in new txn + preserve message: PARTIAL — message preservation present via `event.advised(...)` (equivalent); but `queryResultsCache.onTransactionEnded()` in `tryReplay` is MISSING (no `queryResultsCache()` on main's `ReplayContext`).
  - (10) remove nextPage/previousPage + raise limit: PRESENT (different) — no page mixins in main; `CommandManager.DEFAULT_LIMIT=100` (:54); `HasLimit_changeLimit` has no MAX_LIMIT cap (more permissive than ecp 320).
  - (11) columnOrder `recordedTargetAbbreviated`/`actualTargetAbbreviated` rename: N/A — main's columnOrder redesigned (member/replayState/hasResult/knownParticipants); target columns gone.
- Verdict: PARTIALLY PRESENT — config-advisor infra (7), visibility (8), page/limit cleanup (10) present; openTarget refinement (1,2,3), AppFeat RefData (4), replay-error return-success + typed message (5), query-cache clearing (9-part) MISSING; (6,11) superseded by main's participants redesign.

## 3. SKIPPED tickets — justification

- **CAUSEWAY-4032** — BACKPORT-FROM-MAIN: refdata replay-arg SPI (`spi/CommandReplayReferenceDataService`, `...ForRefData`) fully present in main; the `RefData` marker was relocated to `api/applib/.../domain/RefData.java` (part of 4034's move). Registered `CausewayModuleExtCommandLogApplib.java:150`. Nothing to port.
- **CAUSEWAY-4033** — BACKPORT-FROM-MAIN: (a) suppress useless safe replayable commands — `ReplayableCommandEligibility.java:46-47` `!isSafeAction(...) || getResult()!=null` + `:65`; (b) record property edits during recording — `CommandPublishingFacetForPropertyAnnotation.java:67` gated on `recordingSupport().isEnabled()`, subclass `EnabledByRecordingSupport` `isEnabled()→true`. Both present (evolved). The third sub-part (proposal-only navigate-to improvements) is superseded by 4038.

## 4. Items needing forward-port / assessment (consolidated)

Genuine gaps / divergences flagged for the maintainer:
- **4037 MISSING** — `saveForReplay` idempotency guard (clean small backport).
- **4039 MISSING** — isolate mixin domain event facets (`SynthesizeDomainEventsForMixinPostProcessor` still shared-mutates via `initWithMixee`).
- **4042 MISSING** — openTarget RECORDED/ACTUAL refinement + registration (1,2,3); AppFeat `RefData` marker (4, blocks addPermission replay); replay-error return-success + typed message prefix (5); query-results-cache clearing in `tryReplay` (9-part).
- **4038 MISSING** — replay-arg padding for parented-collection nav (b); REFERENCES_PARENT hiding (c); standalone `target` property/column (d, or confirm participants supersede).
- **4034 MISSING/assess** — validator `_navigate_to` skip (c, likely moot); view-model command results (e, different strategy).
- **4029 MISSING** — directional up/down move + autoselect-exportable + removal of make-selected-exportable (intentional UX divergence; low risk).
- **4026 PARTIAL** — per-command `getExportable()` property (superseded by `isKnownParticipants`; confirm equivalence).
- **4039 divergence** — legacy `CommandExportManager`/`CommandReplayManager` retained alongside `CommandManager` (confirm intent). **4025 divergence** — old `CommandExportManager` not deleted.
