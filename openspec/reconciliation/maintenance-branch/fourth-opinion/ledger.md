# Fourth-opinion reconciliation audit — maintenance-branch → main

**Author:** independent fourth-opinion pass (clean-room).
**Date:** 2026-08-07.
**Question:** every *genuine behavioural change* made on `maintenance-branch` (Causeway v2) that was forward-ported to `main` (Causeway v4) — is it faithfully present in main, or are there gaps/errors?

## Independence / clean-room statement

- This analysis was derived **only** from git and the two working trees. I did **not** read anything under
  `openspec/reconciliation/maintenance-branch/` (the first/second/third-opinion `ledger.md`, `dependency-graph.md`,
  `README.md`, `second-opinion/`, `third-opinion/`). No prior analysis informed the universe, the classifications, or the verdicts.
- Worktrees:
  - **MAINTENANCE (authoritative, v2):** `/…/causeway/ecp` @ `maintenance-branch` (HEAD `1683383878939`).
  - **MAIN (target, v4):** `/…/causeway/main` @ `main` (HEAD `a150e41682d`).
  - **merge-base:** `65d64cd85b7` (2025-04-27).
- The maintenance branch's own `openspec/specs` and `openspec/changes/archive` were treated as acceptable *intent* material
  but were **not** used to decide which commits to inspect.

## Method

1. **Derived the universe from git**: `65d64cd85b7..maintenance-branch` = **481 commits** (459 non-merge, 22 merges),
   2025-07-22 → 2026-07-13. Organised into **57 `CAUSEWAY-####` tickets** + **26 loose/other-tracker commits**
   (`JDOJPA-300`, `TURNDP-184`, `[Causeway-3998]`, `[v2]` items, `${revision}` version churn, docs). Raw list:
   `raw/all-commits.txt`, `raw/tickets.txt`.
2. **Triage (fan-out, 5 agents)** covering *every* commit from the beginning of the branch, classifying each ticket as
   BEHAVIOURAL / BACKPORT-FROM-MAIN / V2-INFRA / DOCS-STYLE, with a first-pass presence check in main.
   → `raw/triage-T1…T5.md`.
3. **Deep adversarial verification (fan-out, 6 agents)** on the heavily-evolved command-log/replay subsystem and the big
   tickets (4039, 4042, 4038, 4034, 4026, 4029), each trying to *prove the change missing or wrong* in main, accounting
   for the known cosmetic renames. → `raw/verify-*.md`.
4. **Hand-verification (me)** of every non-PRESENT finding against the exact v2 diff **and** main's current source. Findings
   below are marked **[hand]** (I personally read both sides) or **[agent]** (subagent-verified; corroborated, not
   independently re-opened line-by-line).

**Key structural fact** (discovered, not assumed): the command-log/replay work was **not cherry-picked** — the v2 ticket
numbers (4013, 4026, 4029, 4034, 4037, 4038, 4039, 4042) **do not appear in main's history at all**. Main re-implemented the
behaviour independently, largely under **CAUSEWAY-3910**, with substantial renaming/consolidation
(`CommandExportManager`+`CommandReplayManager` → `CommandManager` family; `isExportable` → `isKnownParticipants`;
`isReplayable` → `isDoOp`; `EXPORTABLE` replay-state removed; nav-action synthesis moved to
`SyntheticNavigationActionFactory`). Verdicts were therefore made by **symbol/behaviour**, never by ticket id.

**Ignored (per instructions, not gaps):** Jakarta (`javax`→`jakarta`), record-style config, JDO-adapter removal, pure
renames/repackaging, formatting/import order.

## Bottom line

The forward-port is **largely faithful**. The overwhelming majority of behavioural changes are present (often as a cleaner or
stronger variant), or are backports-from-main / v2-only infra correctly excluded. However, I found **one substantive gap and a
cluster of smaller ones**, almost all concentrated in two areas: the **metamodel mixin domain-event handling** and the
**command-log replay** extension.

**12 gaps/divergences** are listed below. The single one I would not ship without is **G1**.

---

## Confirmed gaps (behaviour present in v2, absent/incomplete in main)

| # | Source (v2 commit) | Behaviour | Verdict | Severity | Conf. |
|---|---|---|---|---|---|
| **G1** | 4039 `d5cdc5da369` | **Isolate mixin domain-event facets** — install a *per-mixee* local overlay facet instead of shared-mutating one facet across all mixees | **MISSING** | **HIGH** | [hand] |
| **G2** | 4037 `4bc7b2c9f25` | **`saveForReplay` idempotent** — return existing entry if one already exists for the interactionId | **MISSING** | MED-HIGH | [hand] |
| **G3** | 4013 `f9c7562` | **`getDto()` shows the *export* DTO incl. recorded result** (not the raw command DTO) | **MISSING** | MED | [hand] |
| **G4** | 4042 `8efd1dd6772`/`102fbb50cd3` | **`openTarget` lets the user pick RECORDED vs ACTUAL target** (`TargetType` param, `getActualTarget()`) | **MISSING (UI)** | MED | [hand] |
| **G5** | 4042 `6075f978367` | **`AppFeat implements RefData`** marker (needed for addPermission replay) | **MISSING** | MED | [hand] |
| **G6** | 4042 `3f128a7a791` | **Typed replay-error message prefix** (`Hidden:`/`Disabled:`/`Invalid:`) | **MISSING** | LOW | [hand] |
| **G7** | 4042 `9a9d4444d17` | **Clear `queryResultsCache` in the new replay transaction** | **MISSING** | LOW | [hand] |
| **G8** | 4038 `7a8d749f2b2` | **Pad replay args (bind-by-id) for parented-collection navigation** | **MISSING** | MED | [agent] |
| **G9** | 4038 `db291cd28a5` | **Hide navigate-to params when the property is hidden `Where.REFERENCES_PARENT`** | **MISSING** (narrow) | LOW | [agent] |
| **G10** | 4038 `d80cda55d2f` | **Standalone `target` property/column on `ReplayableCommand`** | **MISSING** (poss. intentional) | LOW | [agent] |
| **G11** | 4038 `9937366e891` | **navigate-to param ordering by `byMemberOrderSequence`** | **DIVERGED** (grid case only) | LOW | [agent] |
| **G12** | loose `[v2]` `5f91fe27e5d` | **Disable collection summary view via env var** `causeway.viewer.wicket.summary-view-disabled` | **MISSING** | LOW / info | [hand] |

### G1 — mixin domain-event facet isolation *(the one that matters)*
- **v2** (`d5cdc5da369`, `SynthesizeDomainEventsForMixinPostProcessor` + `ActionDomainEventFacet` + `ObjectActionMixedIn`):
  replaces the shared `facet.initWithMixee(objectSpecification)` with per-mixee `initActionWithMixee/…Property…/…Collection…`
  that call `ActionDomainEventFacet.createObjectTypeSpecificForMixin(...)`, `addFacet(...)` a mixee-specific facet, and install
  a mixee-specific `ActionInvocationFacetForAction`; `ObjectActionMixedIn.execute` then routes through the local invocation
  facet.
- **main:** `core/metamodel/.../postprocessors/members/SynthesizeDomainEventsForMixinPostProcessor.java` still runs the **old
  shared-mutation code verbatim** (`facet->facet.initWithMixee(objectSpecification)` for action/property/collection).
  `createObjectTypeSpecific*` = **0 hits** in `core/metamodel`; `spec/impl/ObjectActionMixedIn.java:179` is an **unconditional**
  `mixinAction.executeInternal(...)` with no local-facet branch.
- **Impact:** when one mixin is contributed to multiple mixee types, the domain-event type is shared/last-writer-wins rather
  than isolated per mixee. This is a metamodel correctness fix, not cosmetic. **Recommend forward-porting.**
- *Note:* the sibling 4039 strands **did** land under CAUSEWAY-3910 and are PRESENT — POST_PROCESS-only navigation-action
  synthesis + re-entrancy/StackOverflow guard (`ObjectSpecificationDefault`/`SyntheticNavigationActionFactory`), view-model
  result recording (`MemberExecutorServiceDefault:385-396`), and the `ResultRemappingService` result-on-copy fix. Only the
  facet-isolation strand is missing.

### G2 — `saveForReplay` idempotency
- **v2** `CommandLogEntryRepositoryAbstract.saveForReplay` guards on `findByInteractionId(...)` and returns the existing entry.
- **main** `extensions/core/commandlog/applib/.../dom/CommandLogEntryRepositoryAbstract.java:337-346` unconditionally creates &
  persists a new entity — replaying the same command twice inserts duplicates. Clean, small backport.

### G3 — `getDto()` export DTO with recorded result
- **v2** `ReplayableCommand.getDto()` → `CommandDtoUtils.CommandExportDto.of(commandDto, result)`, `describedAs = "Export DTO …"`.
- **main** `extensions/core/commandlog/applib/.../dom/replay/ReplayableCommand.java:364-368` still maps the **raw** `CommandDto`;
  `"Export DTO"` describedAs = 0 hits. The `CommandExportDto` infrastructure **is** present in main (used by
  `CommandManager_exportSequence`, `ResultRemappingService`), so this is a display gap, cheap to fix.

### G4/G5/G6/G7 — CAUSEWAY-4042 replay refinements
- **G4:** main `ReplayableCommand_openTarget.act()` is a plain no-arg action opening `getTarget()`; no `TargetType{RECORDED,ACTUAL}`
  param, no `getActualTarget()`, mixin not registered with the recorded/actual choice. The *data* model for recorded-vs-actual
  (`CommandReplayResultMapping` `FIND_BY_RECORDED/ACTUAL_BOOKMARK`, `getTargetType()`) is present — only the UI affordance is missing.
- **G5:** main `ApplicationFeatureChoices.AppFeat implements Comparable<AppFeat>, ViewModel` only (0 `RefData` refs).
- **G6:** main `ReplayableCommand.onReplayError:561-563` = `saveAnalysis(ex.toString())` — no `Hidden:/Disabled:/Invalid:` prefix.
  (The larger "don't rollback on execution failure, handle error, return success in a `REQUIRES_NEW` transaction" behaviour **is**
  present — `ReplayableCommand.java:511,522`.)
- **G7:** no `queryResultsCache` reference anywhere in the replay path (`ReplayableCommand`/`ReplayContext`); the v2 cache-clear on
  the new replay transaction has no analogue.
- 4042 items that **are** present: config `CommandExecutorService.interactionAdvisorPolicy` (`CausewayConfiguration:1848-1859` +
  enforcement in `CommandExecutorServiceDefault:272-335`, incl. the visibility-check fix `c1177675725`); paging cleanup / default
  limit 100. Minor divergence: v2 `MAX_LIMIT=320` cap has no analogue (main validates "must be positive").

### G8–G11 — CAUSEWAY-4038 navigate-to / replay-arg strands
Main's synthesis was refactored into `core/metamodel/.../spec/impl/SyntheticNavigationActionFactory.java`; column/param selection
flows through `_MembersAsColumns`. Present: the deferred `LayoutOrderFacetForParentedCollectionNavigation` (byte-identical, wired at
`SyntheticNavigationActionFactory:163`). Gaps: **G8** replay-arg padding (main `CommandExecutorServiceDefault:213`
`argAdaptersFor(actionDto)` is positional — legacy DTOs replayed against a changed nav-param set bind positionally, not padded
by id); **G9** the extra `Where.REFERENCES_PARENT` hidden-property filter; **G10** the standalone `target` property/column; **G11**
`byMemberOrderSequence` param ordering (main uses grid-occurrence order — differs only when the element type declares a grid).

### G12 — collection summary-view disable
- **v2** `[v2]`-tagged: `CollectionContentsAsSummaryFactory` reads `System.getenv("causeway.viewer.wicket.summary-view-disabled")`
  and returns `DOES_NOT_APPLY` when set.
- **main** `viewers/.../collection/present/summary/CollectionContentsAsSummaryFactory.java` has no such short-circuit; the property
  name = 0 hits repo-wide.
- **Assessment:** a `[v2]` stop-gap using `getenv` (not a real config property). Very likely an intentional v2-only escape hatch,
  **not** a forward-port failure — surfaced here for completeness; low priority.

---

## Divergences that are NOT gaps (main is a superset / intentionally different)

- **CAUSEWAY-4026/4029 — `makeSelectedExportable` removal not applied.** v2 deleted the legacy `CommandExportManager`,
  `makeExportable`, `makeSelectedExportable`; main forward-ported the new `CommandManager` model **but kept the legacy classes
  too** (main = superset). The live export path uses `CommandManager` + `isKnownParticipants`, so the extra code is dormant.
  Low-risk stale legacy; side-effect: main's `getRecordedOrReplayed` query still includes the `EXPORTED` state. Not a
  missing-behaviour gap.
- **CAUSEWAY-4029 up/down move.** The directional up/down actions were themselves superseded by a single *move-after-target*
  action (`CommandManager_moveCommands` + `CommandManagerMovementSupport`) on **both** branches — not a gap.
- **CAUSEWAY-4034 `_navigate_to` validator skip.** Main's `ActionOverloadingValidator` has no explicit skip, but main's synthetic
  action ids are suffixed with the association id (`__causeway_navigate_to_<assocId>`, unique per type) and the factory throws on
  true collisions — so overloading cannot fire. The v2 "hack" is moot in main by design.

## Areas confirmed PRESENT / correctly adapted (no gaps)

- **CAUSEWAY-4034 (all strands):** view-model command results, duplicate-synthetic-action guard (stronger in main), `RefData` on
  `ApplicationPermission/Role/Tenancy/User`, delete-replay-mappings action.
- **CAUSEWAY-4026/4029 (bar the note above):** exportability = `isKnownParticipants` (equivalent predicate), exclude/unexclude,
  property-edit-target validation + messages, autoselect-exportable (as a filter).
- **T3 (4010–4022):** command-log endpoint consolidation into `CommandManager` (paging `HasLimit`/`HasBaseline`), validator rename
  `CommandExportKnownTargetValidator`→`CommandKnownParticipantsValidator`, `isReplayOrRetryEnabled`→`isReplayable`,
  CommandLogEntry result preservation — all present. (4013 is the sole T3 gap = G3.)
- **T2 (3969–3997):** nine are backports-from-main (main is the source: `JpaWeavingSafeguard`, `QualifiedFacet`/`FacetRanking`,
  `JavaScriptRedirect`/`OriginRewrite`, `CommandReplayManager`, `Object_patchLayout`, `ColumnOrderPatchingFacet`);
  3972 deadlock CAS fix, 3995 layout-cache-clear, 3989 export-filter negation, 4002 `ServiceRegistryDefault.select()` NPE guard
  — all present.
- **T1 (3883–3970):** 3899 (null-guard + `Empty`), 3942+3968 (`_persistence_` static-weaving exclusion via `_ClassCache`),
  3945 (`setCacheDuration(ZERO)`), 3950 (file-input preview config) — present; remainder backport-from-main / infra / docs.
- **Loose:** `JDOJPA-300` (replay in own `REQUIRES_NEW` transaction — present, slightly stronger), `TURNDP-184` "update only"
  (`ENABLED_FOR_UPDATES_ONLY` + granular facet flags + `EntityChangeTrackerDefault` guards — present).

## Skips (justified)

- **BACKPORT-FROM-MAIN** (change originated in v4, so present in main by construction): 3883, 3891, 3950, 3955, 3957, 3958, 3969,
  3973, 3976, 3983, 3985, 3989, 3996, 3998, 3997, 4032, 4033. (Spot-checked present by symbol.)
- **V2-INFRA / release plumbing** (v2-only, no forward-port expected): `${revision}` rename churn (`0632e377b20` and its
  revert/reapply chain), CI/deployment docs, `86d43d03786` "decommission replay incubator" (main still wires the `commandreplay`
  incubator — do **not** port), 3951/3956/3952/2445/3970, 4002's two CI commits.
- **DOCS/STYLE**: changelog/README/agent-instruction/cosmetic commits; `bbab91cdb7b` disables a v2-only parser test whose fixture
  doesn't exist in main.

## Confidence & caveats

- G1–G7 and G12 were **hand-verified by me** against both the exact v2 diff and main's current source (file:line cited above).
- G8–G11 are **subagent-verified** (second, adversarial pass) and corroborated by an independent re-run; I did not personally
  re-open every line, so treat their *severity* as approximate. They are all LOW–MED and localised to nav-action replay edge cases.
- Detail and raw evidence for every ticket: `raw/triage-T1…T5.md` and `raw/verify-{4039,4042,4038,4034,4026-4029}.md`.

## Recommendation

Forward-port **G1** (mixin domain-event facet isolation) — it is a genuine metamodel correctness fix and the highest-value miss.
**G2** (idempotent `saveForReplay`) and **G3** (export-DTO display) are cheap, self-contained backports worth doing.
**G4/G5** are small, user-visible replay-console features. **G6/G7/G8–G11/G12** are low-severity/edge-case; port opportunistically
or accept as intentional divergence after a quick product decision (especially G10 and G12, which look deliberate).
