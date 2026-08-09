# CAUSEWAY-4042 — Fourth-opinion clean-room verification

Source (authoritative v2): `ecp` @ `maintenance-branch`
Target (v4): `main` @ `main`

Overall: the forward-port is INCOMPLETE. The two most substantive user-facing strands (openTarget recorded/actual distinction; AppFeat->RefData) and two smaller behaviours (typed replay-error prefix; queryResultsCache clearing) are absent from main. The config-property/service-check strand is fully present. Some divergences are legitimate design differences.

---

## Strand 1 — ReplayableCommand_openTarget RECORDED vs ACTUAL target — MISSING

v2 (8efd1dd6772) reworks the openTarget mixin to distinguish recorded vs actual target:
- adds `ReplayableCommand.getActualTarget()` (resolves via `CommandReplayResultMappingRepository.findByRecordedBookmark(...).getActualBookmark()`), injects `CommandReplayResultMappingRepository`.
- rewrites `ReplayableCommand_openTarget` with a `TargetType {RECORDED, ACTUAL}` enum, `act(TargetType)`, `choicesTargetType()`, `defaultTargetType()`, and a `domainObject(...)` helper.
- `ReplayableCommand_openTargetTR` delegates `act(TargetType)`, `choicesTargetType()`, `defaultTargetType()`.
- `ReplayableCommand.layout.fallback.xml` adds `<cpt:property id="actualTarget"/>`.
- 102fbb50cd3 registers the mixin as `ReplayableCommand.openTarget.class` in `CausewayModuleExtCommandLogApplib` and removes `honorSystemEnvironment()` / `NAMESPACE_REPLAY_PRIMARY` from that module class.

main evidence (all ABSENT):
- `.../dom/replay/ReplayableCommand_openTarget.java:52-65` — plain `act()` returning `commandLogEntry().map(CommandLogEntry::getTarget).flatMap(bookmarkService::lookup)`. No `TargetType` enum, no `choicesTargetType`/`defaultTargetType`, no `getActualTarget`.
- `.../dom/replay/ReplayableCommand_openTargetTR.java:54-65` — same plain `act()`; no TargetType delegation.
- `.../dom/replay/ReplayableCommand.java` — grep for `getActualTarget|CommandReplayResultMapping|actualTarget|TargetType` = no matches.
- `.../dom/replay/ReplayableCommand.layout.fallback.xml` — no `actualTarget` property (grep = no match).
- `CausewayModuleExtCommandLogApplib.java:184-190` still contains `NAMESPACE_REPLAY_PRIMARY` and `honorSystemEnvironment()` (which 102fbb50cd3 deleted); no `ReplayableCommand.openTarget.class` registration.

Verdict: MISSING. main carries a different/older openTarget implementation; the recorded/actual feature was not forward-ported. 8ed3c8c4e5d (the disableAct isEmpty->isPresent fix) is moot since the whole method differs.

---

## Strand 2 — AppFeat implements marker RefData — MISSING

v2 (6075f978367) changes `ApplicationFeatureChoices.AppFeat` `implements ViewModel` -> `implements ViewModel, RefData` (import `org.apache.causeway.applib.domain.RefData`), "so can replay addPermission".

main evidence:
- `extensions/security/secman/applib/src/main/java/org/apache/causeway/extensions/secman/applib/feature/api/ApplicationFeatureChoices.java:121-124`:
  ```
  public static class AppFeat
  implements
      Comparable<AppFeat>,
      ViewModel {
  ```
  Only `Comparable<AppFeat>, ViewModel` — RefData is NOT in the implements clause; no `RefData` import.
- `RefData` type exists in main at `api/applib/src/main/java/org/apache/causeway/applib/domain/RefData.java`, so the port was possible but not done.

Verdict: MISSING. addPermission replay support (the stated purpose) is not enabled for AppFeat in main.

---

## Strand 3 — Replay-error handling (no rollback, typed prefix, clear cache in new txn) — PARTIAL

Three sub-behaviours from 3f128a7a791 / 622ed00ba0c / 9a9d4444d17:

(a) Don't rollback on execution failure; record failure in a NEW transaction; return success — PRESENT.
- main `.../dom/replay/ReplayableCommand.java:509-529`: `tryReplay` runs `executeCommand` in `callTransactional(REQUIRES_NEW,...)`; on failure it runs `runTransactional(Propagation.REQUIRES_NEW, () -> onReplayError(ex))` (separate new transaction) then `invalidateCachedRecord()` and returns `tryResultBookmark`. Matches the intent (failure captured, outer flow not rolled back). Implemented with `.accept(...)` rather than v2's `.mapFailureToSuccess(...)`, but functionally equivalent for this concern.

(b) Typed replay-error message prefix ("Disabled: " / "Invalid: ") — MISSING.
- v2 3f128a7a791 `onReplayError`: prefixes based on `HiddenException`/`DisabledException`/`InvalidException` and calls `entry.saveAnalysis(prefix + ex.getMessage())`.
- main `.../dom/replay/ReplayableCommand.java:561-564`: `onReplayError` does `entry.saveAnalysis(ex.toString())` — no instanceof checks, no typed prefix, uses `toString()` not `getMessage()`. grep for `Disabled: |Invalid: |getReasonAsString` in this file = no matches.

(c) Clear query-results-cache in the new transaction, preserving message — MISSING (cache) / INTENTIONAL-DIVERGENCE (message).
- v2 9a9d4444d17 adds `queryResultsCache.onTransactionEnded()` inside both the success and error REQUIRES_NEW transactions, plus two-arg message-preserving exception constructors on Hidden/Disabled/Invalid/InteractionException.
- main: grep for `queryResultsCache|QueryResultsCache|onTransactionEnded` in `ReplayableCommand.java` and `ReplayContext.java` = NO matches. The cache-clearing is ABSENT.
- The two-arg exception constructors are ABSENT (Hidden/Disabled/InvalidException each have only the single `InteractionEvent` ctor — see `api/applib/.../wrapper/{Hidden,Disabled,Invalid}Exception.java:35/36`). HOWEVER main preserves the veto reason via a different design: `CommandExecutorServiceDefault.advised(event, consent)` at lines 337-341 sets the reason onto the event (`event.advised(reason, ...)`) so the single-arg exception reads it through `getReason()`. So message preservation IS achieved differently.

Verdict: PARTIAL. Non-rollback/new-txn behaviour present; typed prefix and queryResultsCache clearing missing; message-preservation achieved via divergent design.

---

## Strand 4 — New config property + service checks — PRESENT

v2 065ca38aede adds `CommandExecutorService.interactionAdvisorPolicy` (enum CHECK / CHECK_BUT_IGNORE / NO_CHECK, default NO_CHECK) to CausewayConfiguration, and adds the visibility/usability/validity checks (throwing Hidden/Disabled/InvalidException) in `CommandExecutorServiceDefault`. c1177675725 fixed the action-visibility target (headTarget -> targetAdapter).

main evidence (PRESENT):
- `core/config/src/main/java/org/apache/causeway/core/config/CausewayConfiguration.java:1848-1859`: `record CommandExecutorService(... InteractionAdvisorPolicy interactionAdvisorPolicy)` with `enum InteractionAdvisorPolicy { CHECK, CHECK_BUT_IGNORE, NO_CHECK }` (record-config style — an accepted divergence).
- `core/runtimeservices/.../CommandExecutorServiceDefault.java`:
  - reads policy via `causewayConfiguration.core().runtimeServices().commandExecutorService().interactionAdvisorPolicy()` (lines 218, 258).
  - `applyActionAdvisorPolicy` (272-302) and `applyPropertyAdvisorPolicy` (304-335): NO_CHECK early-return; CHECK path throws HiddenException/DisabledException/InvalidException on veto; CHECK_BUT_IGNORE evaluated implicitly (checks run, vetos not thrown).
  - visibility check uses `target`/`targetAdapter` (the c1177675725 fix), not headTarget.

Verdict: PRESENT (refactored into helper methods + record config; behaviour equivalent).

---

## Strand 5 — Paging cleanup (remove nextPage/previousPage, increase limit) — PARTIAL / INTENTIONAL-DIVERGENCE

v2 d87d39b81d9: deletes `CommandManager_nextPage.java` and `CommandManager_previousPage.java`, unregisters them, changes default limit `50 -> 100`, and `HasLimit_changeLimit.MAX_LIMIT 100 -> 320`; removes the +1/-1 page actions from `CommandManager.layout.fallback.xml`.

main evidence:
- `CommandManager_nextPage.java` / `CommandManager_previousPage.java` do NOT exist (find = no results); not registered in `CausewayModuleExtCommandLogApplib` (grep = no matches). Consistent with the removal end-state.
- Default limit: `CommandManager.java:54 DEFAULT_LIMIT = 100` (matches the 100 target; main uses a named constant rather than v2's literal).
- MAX_LIMIT 320: NOT present. main `HasLimit_changeLimit.java` has NO `MAX_LIMIT` constant at all; it validates via `validateNewLimit(newLimit) -> "Limit must be positive"` (line 50-52). So the 320 cap increase has no analogue.

Verdict: PARTIAL / INTENTIONAL-DIVERGENCE. End-state (no paging mixins, default 100) matches, but this is a structurally divergent CommandManager where the paging mixins and MAX_LIMIT cap never existed in the v2 form; the specific `MAX_LIMIT=320` change is absent.

---

## Summary of verdicts
1. openTarget recorded/actual — MISSING
2. AppFeat implements RefData — MISSING
3. Replay-error handling — PARTIAL (no-rollback/new-txn present; typed prefix + queryResultsCache clearing missing; message-preservation via divergent design)
4. Config property + service checks — PRESENT
5. Paging cleanup — PARTIAL / INTENTIONAL-DIVERGENCE (end-state matches; MAX_LIMIT=320 absent, no MAX_LIMIT concept in main)
