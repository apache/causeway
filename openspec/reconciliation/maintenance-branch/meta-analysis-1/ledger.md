# Meta-analysis pass 1 — authoritative consolidated discrepancy ledger

Canonical, reconciled view of the remaining `maintenance-branch → main` discrepancies,
merging the second/third/fourth opinions and settling their disagreements against code.

- **MAIN** (target, v4): `/Users/danhaywood/repos/github/apache/causeway/main` @ `a150e41682d`
- **MAINTENANCE** (authoritative, v2): `/Users/danhaywood/repos/github/apache/causeway/ecp` @ `1683383878939`
- Cross-reference key: `2nd`=second-opinion Gn/Dn, `3rd`=third-opinion Fn, `4th`=fourth-opinion Gn.
- **Verified** = confirmed here by reading both worktrees' source (evidence quoted).

Status legend: **GAP** (maintenance behaviour missing/incomplete in main) ·
**DIVERGENCE** (present but semantically different — may be intentional) ·
**SUPERSET/POLICY** (main retains extra legacy behaviour maintenance deliberately removed) ·
**NON-ISSUE** (raised by an opinion but resolved as equivalent/adaptation).

---

## A. First-tier remediation — unanimous or code-confirmed high impact

| ID | Discrepancy | 2nd | 3rd | 4th | Agreement | Severity | Verdict |
|---|---|---|---|---|---|---|---|
| **MA-1** | **Mixin domain-event facet isolation** | G1 | F2 | G1 | Unanimous | **HIGH** | **GAP (verified)** |
| **MA-2** | **`saveForReplay` idempotency** | G2 | F3 | G2 | Unanimous | MED-HIGH | **GAP (verified)** |
| **MA-3** | **Replay-failure batch continuation** | G3 | F4 | (G6 partial; wrongly deemed present) | Unanimous in substance | **HIGH** | **GAP (verified — 4th erred)** |
| **MA-5** | **Synthetic collection-navigation replay compatibility** | (said PRESENT) | F1 | G8/G9/G11 | Disputed → resolved | **HIGH** | **GAP (verified — 3rd correct)** |

### MA-1 — Mixin domain-event facet isolation *(the one that matters)*
When a single mixin (action/property/collection) is contributed to **multiple** mixee types
with differing `@DomainObject(...DomainEvent=...)` defaults, main mutates the **shared** facet
on the mixin's faceted method, so the last mixee processed wins for all of them
(cross-mixee event-type pollution). Maintenance installs a **per-mixee object-type-specific
overlay facet** plus a mixee-specific `ActionInvocationFacetForAction`, and execution routes
through the local invocation facet.
- **main** `core/metamodel/.../postprocessors/members/SynthesizeDomainEventsForMixinPostProcessor.java:53,64,75` calls `facet->facet.initWithMixee(objectSpecification)` (shared mutation); **0 hits** for `createObjectTypeSpecific*` in `core/metamodel`; `spec/impl/ObjectActionMixedIn.java:179-181` is an unconditional `mixinAction.executeInternal(...)` with no local-facet branch.
- **ecp** `SynthesizeDomainEventsForMixinPostProcessor.java:57,86-116,126-141` uses `createObjectTypeSpecificForMixin(...)` + `addFacet(...)` + `installMixeeSpecificActionInvocationFacet(...)`; `specimpl/ObjectActionMixedIn.java:197-207` routes via `hasLocalActionInvocationFacet() ? this.executeInternal(...) : mixinAction.executeInternal(...)`. v2 commit `d5cdc5da369` (CAUSEWAY-4039).
- **Why it matters:** general **metamodel** correctness, not command-log — affects any app with a shared mixin over several object types. Outside the command-log scope of the first analysis, which is why all three opinions rank it top. **Forward-port.**

### MA-2 — `saveForReplay` idempotency
Re-importing/re-saving a command whose interaction-id already exists creates a **duplicate**
`CommandLogEntry` on main (and, since the JPA PK is the interaction-id, is liable to fail
persistence on re-import). Maintenance looks up `findByInteractionId` and returns the
existing entry.
- **main** `extensions/core/commandlog/applib/.../dom/CommandLogEntryRepositoryAbstract.java:337-346` — unconditional create + persist.
- **ecp** `:267-284` — `findByInteractionId(...)` guard + early return. v2 commit `4bc7b2c9f25` (CAUSEWAY-4037).
- Neither main import caller (unified or legacy) pre-checks. Small, self-contained backport.

### MA-3 — Replay-failure batch continuation (and exception-field population)
A failed command replay must record `FAILED` in a `REQUIRES_NEW` transaction and then let a
bounded/multiple replay **continue to the next command**. On main a failure **halts the
whole batch**.
- Both branches record the failure in a new transaction — but only maintenance then maps
  the failure to success. **ecp** `ReplayableCommand.tryReplay` `:691-698` uses
  `mapFailureToSuccess(ex -> { ...REQUIRES_NEW: queryResultsCache.onTransactionEnded(); onReplayError(ex); ... return null; })` → returns a **Success**. **main** `ReplayableCommand.java:520-528` uses `tryResultBookmark.accept(ex -> ...REQUIRES_NEW onReplayError(ex), __ -> {})` then `return tryResultBookmark` — `Failure.accept(...)` returns the Failure **unchanged**.
- The batch loop is **identical** on both sides and stops on `isFailure()`: main `CommandManager_replayOrRetryMultiple.java:85-92`, ecp `:64-70`. So main stops on the first failure; maintenance continues.
- **Secondary:** on failure main sets only the reason, not the exception. **main** `CommandLogEntry.saveAnalysis` `:643-644` (no `setException`) vs **ecp** `:745-747` (`setReplayStateFailureReason` **and** `setException(analysis)`). v2 commits `3f128a7a791`, `622ed00ba0c`, `0d5ee322b17`, `9a9d4444d17` (CAUSEWAY-4042).
- **Disagreement resolved:** the fourth opinion cited main `:511,522` and concluded the behaviour was present; it saw the `REQUIRES_NEW` error handling but missed that `accept(...)` (not `mapFailureToSuccess`) leaves the `Try` a Failure. Second and third are correct.

### MA-5 — Synthetic collection-navigation replay compatibility
Two distinct sub-defects break replay of maintenance-recorded **collection**-navigation
commands on main:
- **(a) Action-id mismatch.** Maintenance uses `__causeway_navigate_to_one_of_<collectionId>` for collection navigation and `__causeway_navigate_to_<referenceId>` for scalar references (**ecp** `ObjectSpecificationAbstract.java:1055,1190-1191` collections; `:1242,1293-1294` references). Main uses a single `SyntheticNavigationActionFactory.ACTION_ID_PREFIX = "__causeway_navigate_to_"` for **both** (**main** `SyntheticNavigationActionFactory.java:69,155,183`) — no `one_of_` infix. Main's replay does an exact lookup and throws `"Unknown action '%s'"` on a miss (**main** `CommandExecutorServiceDefault.java:372-374,402-407`). ⇒ a maintenance-recorded collection-nav DTO names an id main never synthesizes.
- **(b) Argument reconstruction.** Main reconstructs action args **positionally** with no compatibility branch (**main** `CommandExecutorServiceDefault.java:441-447`). Maintenance dispatches on the collection-nav prefix and binds args **by parameter id / friendly name, padding missing filter params** (**ecp** `CommandExecutorServiceDefault.java:110,438,452-477`). ⇒ even with the id repaired, adding/removing/reordering synthetic filter columns breaks positional replay. v2 commits `067e3ba4565`, `7a8d749f2b2` (CAUSEWAY-4038).
- **Disagreement resolved:** third opinion (F1, HIGH) is correct on both counts; fourth's "id collision is moot" answers intra-type uniqueness, not the cross-version prefix change; second's "PRESENT" understated it. **Forward-port.**

---

## B. Second-tier — command-log presentation, projection & reference data

| ID | Discrepancy | 2nd | 3rd | 4th | Agreement | Severity | Verdict |
|---|---|---|---|---|---|---|---|
| **MA-4** | **`AppFeat implements RefData`** (SecMan permission-feature reference data) | G4 | F8 | G5 | Unanimous | MED | **GAP (verified)** |
| **MA-6** | **`getDto()` returns export DTO incl. recorded result** (not raw CommandDto) | — | F7(a) | G3 | Majority | MED | **GAP (verified)** |
| **MA-7** | **`actualBookmarkFor` fallback for `UNDEFINED` as well as `OK`** | — | F7(b) | — | Single (3rd) | MED | **GAP (verified)** |
| **MA-8** | **Replayable title from full recorded bookmark** (not truncated `targetId` @10 chars) | — | F7(c) | — | Single (3rd) | LOW | **GAP (verified)** |
| **MA-9** | **`openTarget` RECORDED-vs-ACTUAL UI + top-level `getTarget`/`getActualTarget` properties** | D1 | F5 | G4+G10 | Unanimous | MED | **GAP + dead code (verified)** |
| **MA-10** | **Per-command export actions** `ReplayableCommand_export`/`_exportTR` | — | F6 | — | Single (3rd) | MED | **GAP (verified)** |

### MA-4 — `AppFeat implements RefData`
`AppFeat` (the reference view-model for permission-feature choices) is not marked `RefData`
on main, so commands whose target/reference-parameter is an `AppFeat` bookmark (e.g.
`addPermission` replay) are treated as unknown export participants.
- **main** `extensions/security/secman/applib/.../ApplicationFeatureChoices.java:121-124` — `implements Comparable<AppFeat>, ViewModel` only.
- **ecp** `:125-128` — `... , ViewModel, RefData`. v2 commit `6075f978367` (CAUSEWAY-4042).
- Note: main **does** already apply `RefData` to `ApplicationPermission/Role/Tenancy/User` — the first reconcile deliberately scoped ref-data opt-ins to those four; the later `AppFeat` opt-in was simply not carried over.

### MA-6 — `getDto()` export DTO with recorded result
- **main** `ReplayableCommand.java:365-373` renders the raw `CommandDto` (result envelope + result bookmark omitted from displayed YAML).
- **ecp** `:394-404` builds `CommandDtoUtils.CommandExportDto.of(commandDto, result)`. The `CommandExportDto` infra already exists in main (used by `CommandManager_exportSequence`), so this is a cheap display fix. v2 commit `f9c7562` (CAUSEWAY-4013).

### MA-7 — `actualBookmarkFor` fallback
- **main** `ReplayableCommand.java:349-355` falls back to the recorded bookmark only when `replayState == OK`.
- **ecp** `:491-499` falls back when `isExecutedOk()` = `UNDEFINED || OK` (`ReplayState.java:114-116`). ⇒ on main, recorded-side `UNDEFINED` participants lose their actual-bookmark link without explicit remapping.

### MA-8 — Replayable title
- **main** `ReplayableCommand.java:152-155,202-208` builds the title from `getTargetType()+":"+getTargetId()`, and `getTargetId()` ellipsifies to 10 chars.
- **ecp** `:191-199,371-375` uses the complete recorded target bookmark (`Bookmark::stringify`, untruncated).

### MA-9 — `openTarget` recorded-vs-actual + target properties (and dead files)
- **main** has no top-level `@Property getTarget()`/`getActualTarget()` (only `@Programmatic getTargetType()`/`getTargetId()` at `ReplayableCommand.java:192-209`). The source files `ReplayableCommand_openTarget.java` and `ReplayableCommand_openTargetTR.java` **exist but are inert** — not imported/registered in `CausewayModuleExtCommandLogApplib`. Main's `ReplayableCommandPresentationTest` asserts they are unregistered.
- **ecp** exposes `getTarget()`/`getActualTarget()` (`ReplayableCommand.java:250-289`) and imports both `openTarget` mixins with the `TargetType{RECORDED,ACTUAL}` choice (`CausewayModuleExtCommandLogApplib.java:63-64,101-102`). v2 commits `d80cda55d2f` (top-level target property/column), `8efd1dd6772`/`102fbb50cd3` (recorded/actual UI) (CAUSEWAY-4038/4042).
- **Action:** decide whether parity with the final maintenance UI is required; **either way delete the two dead files** in main. (The recorded-vs-actual *data* model — `CommandReplayResultMapping FIND_BY_RECORDED/ACTUAL_BOOKMARK` — is already present; only the UI affordance and top-level properties are missing.)

### MA-10 — Per-command export actions
- **ecp** registers per-command YAML export mixins `ReplayableCommand_export` (row → `Clob`, non-state-mutating) and `ReplayableCommand_exportTR` (`CausewayModuleExtCommandLogApplib.java:61-62,109-110`). v2 commits `73ee2fc20ae`, `6c0e6406b724`.
- **main** has neither source file; only the bulk `CommandManager_exportSequence` exists. ⇒ the per-command form/table-row export surface is absent (not replaced by the bulk action).

---

## C. Config / API-surface gaps

| ID | Discrepancy | 2nd | 3rd | 4th | Agreement | Severity | Verdict |
|---|---|---|---|---|---|---|---|
| **MA-11** | **Malformed command YAML silently accepted as empty** (legacy importer) | — | F9 | — | Single (3rd) | MED | **GAP (verified)** |
| **MA-12** | **Wicket summary-view disable switch** `causeway.viewer.wicket.summary-view-disabled` | — | F10 | G12 | Majority | LOW | **DIVERGENCE — likely intentional** |
| **MA-13** | **Typed replay-error message prefix** (`Hidden:`/`Disabled:`/`Invalid:`) | (G3 partial) | (F4) | G6 | Majority | LOW | **GAP (verified)** |

### MA-11 — Malformed command YAML
- **main** `api/applib/.../CommandDtoUtils.java:178-183` — `fromYaml` does `YamlUtils.tryReadAsList(...).getValue().orElseGet(Collections::emptyList)`; a failed parse `Try` has no value ⇒ malformed input becomes an **empty list, silently**. (The strict `fromYamlForReplay`, `:195-233`, does throw.)
- **ecp** `:191-222` — `fromYaml` tries four representations and ends in `ifFailureFail()`, i.e. **throws** on unparseable input.
- **Reachability:** the swallowing path is live — main's **legacy** `CommandReplayManager.importCommands` (`:159`, `@Action(restrictTo=PROTOTYPING)`, file upload) calls `fromYaml`, so a malformed upload silently imports zero commands and reports success. The **new** `CommandManager_importCommands` mixin correctly uses strict `fromYamlForReplay`. v2 commit `b29b16aea96` and follow-ups. Fixing this pairs naturally with MA-15 (retiring the legacy manager).

### MA-12 — Wicket summary-view disable switch
- **ecp** `CollectionContentsAsSummaryFactory.java:60-69` reads `System.getenv("causeway.viewer.wicket.summary-view-disabled")` (case-insensitive `true`) and returns `DOES_NOT_APPLY`.
- **main** — no such short-circuit; property name has 0 hits repo-wide.
- **Assessment:** it is a `[v2]`-tagged `getenv` stop-gap, **not** a real config property — very likely an intentional v2-only escape hatch, not a forward-port failure. Third opinion rated it a MED gap; fourth (G12) rated it LOW/likely-intentional. **Side with the fourth: product decision, low priority.** If wanted on v4, re-introduce as a proper `causeway.viewer.wicket.*` configuration property rather than an env var.

### MA-13 — Typed replay-error prefix
- **main** `ReplayableCommand.java:561-563` records `saveAnalysis(ex.toString())` with no `Hidden:`/`Disabled:`/`Invalid:` classification prefix.
- **ecp** classifies the error with a typed prefix. v2 commit `3f128a7a791` (CAUSEWAY-4042). Low severity; naturally bundled with MA-3 (both touch the failure path and `saveAnalysis`).

---

## D. Superset / policy items — main retains behaviour maintenance removed

| ID | Discrepancy | 2nd | 3rd | 4th | Agreement | Severity | Verdict |
|---|---|---|---|---|---|---|---|
| **MA-15** | **Legacy export-state surface still registered & executable in main** | — | F11 | (noted, "not a gap") | Split | MED-LOW | **POLICY DECISION (verified present)** |
| **MA-16** | **Unified-manager page limit** — main default 100 / no cap vs v2 `MAX_LIMIT=320` cap | D2 | — | (noted) | Majority | LOW | **DIVERGENCE — confirm intended** |

### MA-15 — Legacy export-state surface
Maintenance deliberately **removed** `ReplayState.EXPORTED`, stopped export from mutating
state, and collapsed the separate export/replay managers into one `CommandManager` (v2
commits `1b5c7a4a45e`, `cce505a5fef2`, `486796c17b0f`). Main forward-ported the unified
`CommandManager` and non-mutating sequence export **but kept the legacy surface too**, and it
is **registered and executable** (not dead):
- **main** retains `ReplayState.EXPORTED` (`ReplayState.java:36`), `CommandExportManager` & `CommandReplayManager` (registered viewmodels, module `:91-92`), `ReplayableCommand_makeExportable` (module `:103`), and `CommandExportManager.makeSelectedExportable` (module `:114`). The action is `@Action(restrictTo=PROTOTYPING)`, enabled whenever the `exported` collection is non-empty, and **mutates `EXPORTED` entries back to `UNDEFINED`** (`CommandExportManager.java:270-287` → `ReplayableCommand.makeExportable:413-423`); export sets `EXPORTED` at `CommandExportManager.java:208`.
- **Assessment:** this is the inverse of a gap — *extra* legacy behaviour on main, not missing maintenance behaviour. Third opinion flags it as policy-sensitive (the live mutating action is not mere memento compatibility); fourth calls it a harmless dormant superset (but it is in fact reachable). **Requires an explicit decision:** either accept as a documented v4 compatibility policy, or retire the legacy managers/actions and drop `ReplayState.EXPORTED` to match maintenance. Retiring `CommandReplayManager` would also resolve MA-11.

### MA-16 — Page limit
- **main** opens the manager at `DEFAULT_LIMIT = 100` and `validateNewLimit` enforces only `> 0`.
- **ecp** opens at `MAX_LIMIT = 320` and caps user input to `[1,320]`.
- Minor; main is internally consistent with its own spec. Confirm the lower default and the removal of the 320 cap are intended.

---

## E. Non-issues — raised by an opinion but resolved as equivalent/adaptation

| ID | Item | Raised by | Resolution |
|---|---|---|---|
| **MA-14** | **`queryResultsCache` clear in the new replay transaction** | 4th G7 (gap) vs 3rd (rejected) | **NON-ISSUE.** Maintenance clears the cache in the `REQUIRES_NEW` replay block (`ecp ReplayableCommand.java:692-693`); main has no analogue **because main does not cache replay lookups** — it queries `findByInteractionId` directly each time, so there is no stale cache to invalidate. Accept as a v4 adaptation (side with the third opinion). |
| — | 4038 `byMemberOrderSequence` param ordering (4th G11) | 4th | Sub-strand of MA-5; DIVERGED only when the element type declares a grid (main uses grid-occurrence order). LOW — fold into MA-5 remediation. |
| — | 4038 hide navigate-to param when property hidden `Where.REFERENCES_PARENT` (4th G9) | 4th | Narrow sub-strand of MA-5. LOW — fold into MA-5. |
| — | `CommandDtoUtils.toYaml`/`toMultiDocYaml` naming | 3rd (challenged) | v4 API naming/refinement; multi-doc behaviour available via `toMultiDocYaml`/`toYamlExport`. Not a gap. |
| — | 4029 up/down move superseded by single move-after-target action | 4th | Superseded on **both** branches (`CommandManager_moveCommands`). Not a gap. |
| — | 4034 `_navigate_to` validator skip | 4th | Moot on main: synthetic ids are suffixed with the association id and the factory throws on true collisions, so overloading cannot fire. Not a gap. |
| — | Pre-4010 groups (2445, 3891, 3945, 3951, 3956, 3958, 3970, 3976, etc.) | all | Backport-from-main or v2-only infra — present by origin or N/A. Absence from the first ledger is a documentation gap only, not a behavioural one. |

---

## F. Remediation summary

**Port now (behavioural correctness):**
- MA-1 mixin domain-event facet isolation — **highest value**; general metamodel.
- MA-2 `saveForReplay` idempotency.
- MA-3 replay-failure batch continuation (+ `setException` population; bundle MA-13 typed prefix).
- MA-5 synthetic collection-navigation replay (action-id `one_of_` infix + bind-by-id/padding args; bundle 4038 sub-strands G9/G11).
- MA-4 `AppFeat implements RefData`.
- MA-11 malformed-YAML strict parse on the legacy importer (or retire it via MA-15).

**Cheap, self-contained (port opportunistically):**
- MA-6 `getDto()` export DTO; MA-7 `actualBookmarkFor` UNDEFINED fallback; MA-8 untruncated title; MA-10 per-command export mixins.

**Product / policy decisions (no code obligation until decided):**
- MA-9 `openTarget` recorded-vs-actual UI parity — *and delete the two dead files regardless*.
- MA-15 legacy export-state surface — accept as policy or retire (retiring also fixes MA-11).
- MA-12 summary-view switch — re-introduce as a real config property or drop.
- MA-16 page-limit default/cap — confirm intended.

**Accept as-is:** MA-14 (`queryResultsCache`) and the other Section E items.

## Counts

- **11 real discrepancies** to remediate: MA-1, MA-2, MA-3, MA-4, MA-5, MA-6, MA-7, MA-8, MA-10, MA-11, MA-13.
- **1 gap that is also a policy question:** MA-9 (UI parity optional; dead-file deletion required).
- **3 divergence/policy items:** MA-12, MA-15, MA-16.
- **1 confirmed non-issue** (MA-14) plus the Section E challenged candidates.
