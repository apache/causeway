# Final Reconciliation Plan — maintenance-branch → main

## Purpose

This is the master plan for **completing** the `maintenance-branch` → `main` forward-port
reconciliation. The first wave of reconciliation (recorded in [`ledger.md`](ledger.md)) ported
every capability the consolidated maintenance OpenSpec specs described, and archived thirteen
child changes. Three independent second/third/fourth opinions and two meta-analyses then audited
that work and found a **second wave of genuine discrepancies** — concentrated in maintenance work
that *post-dated* the consolidated specs (CAUSEWAY-4042, the CAUSEWAY-4039 tail, the CAUSEWAY-4037/4038
replay strands) and in general-metamodel / loose `[v2]` commits outside the command-log capability graph.

This plan freezes the **canonical discrepancy set**, records the **product decisions** taken to close it,
and defines the **sequence of child changes** that will finish the programme. It follows the programme
workflow in [`README.md`](README.md): one implementation-oriented OpenSpec change is active at a time, each
child change references this plan, and completed child changes are archived normally.

## Evidence base

| Source | Location | Method | Findings |
|---|---|---|---|
| First ledger | [`ledger.md`](ledger.md) | Semantic capability port driven by the 23 consolidated maintenance specs | 13 archived child changes |
| Second opinion | [`second-opinion/`](second-opinion/) | Re-derived 481-commit universe; per-capability reviewers vs main **code** | G1–G4 + D1–D2 |
| Third opinion | [`third-opinion/`](third-opinion/) | 8 clean-room subagents over all 481 commits; runtime JShell probe | F1–F11 |
| Fourth opinion | [`fourth-opinion/`](fourth-opinion/) | Git-derived universe; adversarial deep-verify; hand-verification | G1–G12 |
| Meta-analysis 1 | [`meta-analysis-1/`](meta-analysis-1/) | Cross-mapped the three opinions; settled disagreements against code | MA-1…MA-16 |
| Meta-analysis 2 | [`meta-analysis-2/`](meta-analysis-2/) | Independent second meta-analysis; claim-by-claim adjudication | A1–A9 + D1–D4 |

The two meta-analyses agree on the substance. This plan adopts **meta-analysis 1's `MA-*` identifiers** as
canonical (meta-analysis 2's `A*`/`D*` equivalents are shown for cross-reference).

## Re-anchoring to current HEAD (the PR #3697 / CAUSEWAY-4044 merge)

**All six audit documents were produced against `main` at `a150e41682d`.** Since then the reconciliation
work was replayed onto a newer `main`, and — critically — that newer `main` includes one merge that the
audits never saw:

> `851a7173754` — **Merge pull request #3697 (CAUSEWAY-4044: "Work towards immutable ObjectSpecification — part 1")**,
> together with follow-ups CAUSEWAY-4044 (facet-processor rework), CAUSEWAY-4045 (svg mime fix), and
> CAUSEWAY-4046 (multi-arg constructor support for mixins).

This merge reworked exactly the metamodel areas that two of the P0 findings touch: it removed
`SynthesizeNavigationActionsPostProcessor`, reworked `SyntheticNavigationActionFactory` and the member-catalog
path, and touched `MixinFacet` / `ActionOverloadingValidator`. Every finding below has therefore been
**re-verified against current HEAD (`42ca10925fb`)**, not the audited head:

- **Command-log, `api/applib`, and SecMan findings are byte-identical** between `a150e41682d` and HEAD
  (`git diff` = 0 lines for `CommandLogEntryRepositoryAbstract`, `ReplayableCommand`, `CommandDtoUtils`,
  `CommandExecutorServiceDefault`, and the SecMan `ApplicationFeatureChoices`). Their audit file:line anchors
  still hold **exactly**.
- **Metamodel findings persist but shifted.** MA-1 is unchanged (same lines). MA-5's action-id gap persists,
  but the surrounding factory was refactored by CAUSEWAY-4044, so its metamodel-side anchors moved (its
  runtimeservices-side anchors in `CommandExecutorServiceDefault` did **not** move).
- **New landscape.** CAUSEWAY-4044's immutability work and CAUSEWAY-4046's multi-arg-mixin support are v4-origin
  changes (no forward-port obligation) but they change the ground the MA-1 / MA-5 fixes land on. In particular
  the MA-1 remediation installs per-mixee facets at post-process time; this remains viable (`FacetHolder.addFacet`
  and `FacetUtil.addFacet` are still used by postprocessors on HEAD) but each child change MUST re-confirm the
  metamodel entry points it depends on before implementing.

**Rule for every child change:** cite current-HEAD file:line, and where a finding sits in the metamodel,
re-verify the anchor against HEAD rather than trusting the audit's `a150e41682d` line numbers.

## Canonical discrepancy set

Verdict legend: **GAP** = maintenance behaviour missing/incomplete on main · **DECISION** = product
decision required/taken · **NON-ISSUE** = raised by an opinion, resolved as equivalent/adaptation.

### First-tier (P0) — correctness

| ID | 2nd/3rd/4th | Discrepancy | Sev | Current-HEAD anchor (verified) | Child change |
|---|---|---|---|---|---|
| **MA-1** | G1 / F2 / G1 | **Mixin domain-event facet isolation** — a shared mixin over several differently-annotated mixee types mutates one shared facet (last-writer-wins) instead of installing a per-mixee overlay | HIGH | `SynthesizeDomainEventsForMixinPostProcessor.java:53,64,75` (shared `initWithMixee`); `createObjectTypeSpecific*` = 0 hits; `ObjectActionMixedIn.java:179` unconditional `mixinAction.executeInternal(...)` | `reconcile-mixin-domain-event-isolation` |
| **MA-5** | (PRESENT) / F1 / G8-G11 | **Synthetic collection-navigation replay compatibility** — (a) main uses one `__causeway_navigate_to_` prefix for both collections and references, no `one_of_` infix, so a maintenance-recorded collection-nav DTO throws "Unknown action"; (b) replay args are reconstructed positionally, not bound by id with padding | HIGH | `SyntheticNavigationActionFactory.java:75` (single `ACTION_ID_PREFIX`), `:120,169` (collection), `:128,196` (reference); `CommandExecutorServiceDefault.java:374` (`Unknown action`), `:213,441` (positional `argAdaptersFor`) | `reconcile-synthetic-navigation-replay` |
| **MA-2** | G2 / F3 / G2 | **`saveForReplay` idempotency** — re-importing a command whose interaction-id exists creates a duplicate `CommandLogEntry` (and, as the JPA PK is the interaction-id, is liable to fail persistence) | MED-HIGH | `CommandLogEntryRepositoryAbstract.java:337-346` (unconditional create+persist; no `findByInteractionId` guard) | `reconcile-replay-import-idempotency` |
| **MA-3** (+**MA-13**) | G3 / F4 / G6* | **Replay-failure batch continuation** — a handled replay failure must be recorded in a `REQUIRES_NEW` transaction and then mapped to success so a bounded/multiple replay continues; main returns the failed `Try` and halts the whole batch, and records only the reason (not the exception), without maintenance's typed `Hidden:`/`Disabled:`/`Invalid:` prefix | HIGH | `ReplayableCommand.java:520-528` (`accept(...)` leaves `Try` a Failure), `:561-563` (`saveAnalysis(ex.toString())`, no prefix); `CommandLogEntry.saveAnalysis:643-644` (no `setException`); `CommandManager_replayOrRetryMultiple.java:85-92` (stops on `isFailure()`) | `reconcile-replay-failure-continuation` |

\* The fourth opinion's *final* ledger wrongly deemed MA-3 present; its raw `verify-4042.md` and both other opinions correctly found it missing. Both meta-analyses resolved this against code — see [`meta-analysis-1/ledger.md`](meta-analysis-1/ledger.md) §MA-3 and [`meta-analysis-2/README.md`](meta-analysis-2/README.md) §A4.

### Second-tier (P1/P2) — reference data, presentation, projection, export

| ID | 2nd/3rd/4th | Discrepancy | Sev | Current-HEAD anchor (verified) | Child change |
|---|---|---|---|---|---|
| **MA-4** | G4 / F8 / G5 | **`AppFeat implements RefData`** — SecMan permission-feature reference view-model is not reference data on main, so `addPermission`-style commands treat an `AppFeat` bookmark as an unknown export participant | MED | `ApplicationFeatureChoices.java:121-124` (`implements Comparable<AppFeat>, ViewModel` only) | `reconcile-appfeat-reference-data` |
| **MA-6** | — / F7a / G3 | **`getDto()` returns the export DTO incl. recorded result**, not the raw `CommandDto` (displayed YAML currently omits the result envelope/bookmark) | MED | `ReplayableCommand.java:365-373` (raw `CommandDto`); `CommandExportDto` infra already present | `reconcile-replayable-command-presentation` |
| **MA-7** | — / F7b / — | **`actualBookmarkFor` fallback for `UNDEFINED` as well as `OK`** — recorded-side `UNDEFINED` participants lose their actual-bookmark link on main | MED | `ReplayableCommand.java:349-355` (falls back only for `OK`) | `reconcile-replayable-command-presentation` |
| **MA-8** | — / F7c / — | **Replayable title from the full recorded bookmark**, not the `getTargetId()` value truncated to 10 chars | LOW | `ReplayableCommand.java:152-155,202-208` | `reconcile-replayable-command-presentation` |
| **MA-9** | D1 / F5 / G4+G10 | **`openTarget` RECORDED-vs-ACTUAL UI + top-level `getTarget`/`getActualTarget`** — inert unregistered `ReplayableCommand_openTarget[TR].java` dead files; no top-level target properties (the recorded-vs-actual **data** model already exists) | MED | `CausewayModuleExtCommandLogApplib` imports neither mixin; `ReplayableCommandPresentationTest` asserts they are unregistered; no top-level `@Property getTarget()/getActualTarget()` | `reconcile-replayable-command-presentation` |
| **MA-10** | — / F6 / — | **Per-command export actions** `ReplayableCommand_export` / `_exportTR` (row + table-row YAML export) — absent; only the bulk `CommandManager_exportSequence` exists | MED | neither source file present; not imported by the module | `reconcile-per-command-export` |

### Config / API-surface

| ID | 2nd/3rd/4th | Discrepancy | Sev | Current-HEAD anchor (verified) | Child change |
|---|---|---|---|---|---|
| **MA-11** | — / F9 / — | **Public/legacy command-YAML: accept the wrapped-export forms and fail (not silently empty) on unparseable input** — main's `fromYaml` reads only raw `CommandDto` list/multi-doc and `orElseGet(emptyList)`, so a wrapped export or a parse error becomes an empty list. Live via the legacy `CommandReplayManager.importCommands` (PROTOTYPING upload). The new unified importer already uses strict `fromYamlForReplay`. | MED | `CommandDtoUtils.java:178-182` (`getValue().orElseGet(Collections::emptyList)`); strict `fromYamlForReplay:195-233` throws | `reconcile-command-yaml-strictness` |

### Decisions taken (see "Product decisions" below)

| ID | 2nd/3rd/4th | Discrepancy | Decision | Child change |
|---|---|---|---|---|
| **MA-9 (UI)** | D1 / F5 / G4 | recorded-vs-actual target **UI parity** | **Port to parity** (enhance + register the existing `openTarget` mixins) | `reconcile-replayable-command-presentation` |
| **MA-12** | — / F10 / G12 | Wicket collection **summary-view disable switch** (v2 used a `getenv` stop-gap) | **Re-add as a real `causeway.viewer.wicket.*` config property** | `reconcile-collection-summary-view-config` |
| **MA-15** | — / F11 / (noted) | Retained **legacy export-state surface** (`EXPORTED`, `CommandExportManager`/`CommandReplayManager`, `makeSelectedExportable`) | **Keep** (already a documented v4 compatibility decision) but **harden the legacy importer** (folds into MA-11) | (decision only; importer via `reconcile-command-yaml-strictness`) |
| **MA-16** | D2 / — / (noted) | Unified-manager **page limit** (main 100 default / no cap vs v2 `MAX_LIMIT=320`) | **Restore the 320 cap** | `reconcile-command-manager-page-limit` |

### Non-issues (out of scope — no action)

| ID | Item | Resolution |
|---|---|---|
| **MA-14** | `queryResultsCache` clear in the replay transaction (4th G7 vs 3rd rejected) | **NON-ISSUE.** Main does not route replay lookups through `QueryResultsCache` (it calls `findByInteractionId` directly each time), so there is no stale cache to invalidate. No functional analogue to port. |
| — | `CommandDtoUtils.toYaml`/`toMultiDocYaml`/`toYamlExport` naming | v4 API naming/refinement; multi-doc + result-bearing output are available. MA-11 concerns the *reader's* failure semantics, not the writer names. |
| — | 4029 up/down move superseded by single move-after-target | Superseded on **both** branches (`CommandManager_moveCommands`). |
| — | 4034 `_navigate_to` `ActionOverloadingValidator` skip | Moot on main: synthetic ids are suffixed with the association id and the factory throws on true collisions. (Independent of MA-5's cross-version serialized-id problem.) |
| — | Developer-authored action occupies a synthetic id (meta-2 D4) | Main throws on a collision — a reasonable stricter v4 invariant. Accepted; not part of MA-5. |
| — | JDO commandlog replay-mapping deltas | N/A — the commandlog JDO adapter was deliberately removed on v4. |
| — | Pre-4010 groups (2445, 3891, 3945, 3951, 3956, 3958, 3970, 3976, …) | Backport-from-main or v2-only infra — present by origin or N/A. Their absence from the first ledger is a documentation gap only. |

## Product decisions

Taken 2026-08-10 (recorded here so they are not re-litigated per child change):

- **D-A — recorded-vs-actual target UI (MA-9): PORT TO PARITY.** Add the top-level `getTarget()`/`getActualTarget()`
  projections and **enhance and register** the existing `ReplayableCommand_openTarget` / `_openTargetTR` mixins
  with the `TargetType{RECORDED,ACTUAL}` choice, including disabling + user feedback when neither target resolves.
  (Correction to the earlier framing: those two source files are **not** dead code — they exist and open the
  recorded target, but are unregistered in `CausewayModuleExtCommandLogApplib`; porting to parity registers and
  extends them rather than deleting them.)
- **D-B — Wicket summary-view switch (MA-12): RE-ADD AS A CONFIG PROPERTY.** Re-introduce the capability the v4
  way — a real `causeway.viewer.wicket.summary-view-disabled` configuration property bound through
  `CausewayConfiguration`, returning `DOES_NOT_APPLY` when set — rather than copying the v2 `getenv` stop-gap.
- **D-C — unified-manager page limit (MA-16): RESTORE THE 320 CAP.** Cap a user-entered limit to `[1,320]`,
  matching maintenance. The standard menu launcher opens the manager at `320` (matching maintenance), while the
  framework-memento fallback of `100` is retained; the exact default is confirmed in the child change's design.
- **D-D — legacy export-state surface (MA-15): KEEP MANAGERS, HARDEN IMPORTER.** Retaining `EXPORTED` +
  `CommandExportManager`/`CommandReplayManager` is an already-documented v4 compatibility decision
  (`openspec/changes/archive/2026-08-06-reconcile-unified-command-manager/design.md`). Keep it, including the
  `makeSelectedExportable` action. Separately fix the legacy `CommandReplayManager.importCommands` malformed-YAML
  swallowing as part of MA-11 so no live surface silently discards a bad upload.

## Child-change sequence

One active OpenSpec change at a time, proposed → implemented → archived, in this order. Dependency order follows
the programme graph (metamodel before runtimeservices before command-log applib); within a tier, P0 first.

| # | Child change | Covers | New/Modified capability | Primary subsystem |
|---|---|---|---|---|
| 1 | `reconcile-mixin-domain-event-isolation` | MA-1 | **NEW** `mixin-domain-event-isolation` | `core/metamodel` |
| 2 | `reconcile-synthetic-navigation-replay` | MA-5 (incl. G9 `REFERENCES_PARENT` hide + G11 member-order sub-strands) | **MODIFIED** `synthetic-command-navigation` | `core/metamodel` + `core/runtimeservices` |
| 3 | `reconcile-replay-failure-continuation` | MA-3 + MA-13 | **MODIFIED** `command-replay-background-completion` + `replayable-command-projection` | commandlog applib |
| 4 | `reconcile-replay-import-idempotency` | MA-2 | **ADDED to** `command-result-metadata` | commandlog applib |
| 5 | `reconcile-appfeat-reference-data` | MA-4 | **MODIFIED** `command-export-refdata-marker` | secman applib |
| 6 | `reconcile-replayable-command-presentation` | MA-6, MA-7, MA-8, MA-9 (enhance + register `openTarget` mixins) | **MODIFIED** `replayable-command-projection` | commandlog applib |
| 7 | `reconcile-per-command-export` | MA-10 | **ADDED to** `replayable-command-projection` | commandlog applib |
| 8 | `reconcile-command-yaml-strictness` | MA-11 (+ legacy-importer hardening for MA-15/D-D) | **MODIFIED** `command-result-metadata` | `api/applib` + commandlog applib |
| 9 | `reconcile-collection-summary-view-config` | MA-12 (D-B) | **NEW** `collection-summary-view-config` | viewer-wicket + `core/config` |
| 10 | `reconcile-command-manager-page-limit` | MA-16 (D-C) | **MODIFIED** `unified-command-manager` | commandlog applib |

Slices 3–10 are independent of one another and of 1–2; they are ordered by severity and subsystem, not by hard
dependency. Adjacent small slices (e.g. 4, 5, 10) may be combined at the implementer's discretion, provided each
finding retains its own spec delta and acceptance evidence.

Because the repository permits only **one active OpenSpec change at a time**, only one of these lives under
`openspec/changes/` at any moment. Slices 2–10 are drafted ready-to-promote under `planned-changes/` in this
programme directory — each is a complete change folder (`proposal.md` / `design.md` / `tasks.md` / `specs/`) that
is moved into `openspec/changes/` verbatim, and re-validated, when its turn comes.

### Progress

| Slice | Status | Evidence |
|---|---|---|
| 1 `reconcile-mixin-domain-event-isolation` (MA-1) | **Archived** | spec `openspec/specs/mixin-domain-event-isolation/`; archive `openspec/changes/archive/2026-08-10-reconcile-mixin-domain-event-isolation/`; impl commit `f7d8b5e474f`; `core/metamodel`+`core/mmtest` green under JDK 21 (937 tests, 0 failures) |
| 2 `reconcile-synthetic-navigation-replay` (MA-5) | **Archived** | spec synced to `openspec/specs/synthetic-command-navigation/`; archive `openspec/changes/archive/2026-08-11-reconcile-synthetic-navigation-replay/`; impl commit `78e2e636126`; `core/mmtest`+`core/runtimeservices` green under JDK 21 (988 tests, 0 failures). G9/G11 parameter-derivation refinements deferred as accepted v4 adaptations (conflict with the shipped column-order spec; subsumed by identity-based replay binding) |
| 3 `reconcile-replay-failure-continuation` (MA-3, MA-13) | **Archived** | specs synced (`command-replay-background-completion`, `replayable-command-projection`); archive `openspec/changes/archive/2026-08-13-reconcile-replay-failure-continuation/`; impl commit `731e1b75fb5`; commandlog applib green under JDK 21 (154 tests, 0 failures) + JPA replay integ (3, 0). MA-14 (queryResultsCache clear) intentionally not ported — no replay cache on main |
| 4 `reconcile-replay-import-idempotency` (MA-2) | **Archived** | spec synced (`command-result-metadata`); archive `openspec/changes/archive/2026-08-13-reconcile-replay-import-idempotency/`; impl commit `6bffce726e0`; commandlog applib green under JDK 21 (157 tests, 0 failures) |
| 5 `reconcile-appfeat-reference-data` (MA-4) | **Archived** | spec synced (`command-export-refdata-marker`); archive `openspec/changes/archive/2026-08-13-reconcile-appfeat-reference-data/`; impl commit `0b38b62fed8`; secman applib green under JDK 21 |
| 6 `reconcile-replayable-command-presentation` (MA-6/7/8/9) | **Archived** | spec synced (`replayable-command-projection`); archive `openspec/changes/archive/2026-08-13-reconcile-replayable-command-presentation/`; impl commit `fd5c3d05c41`; commandlog applib green under JDK 21 (161 tests, 0 failures) |
| 7–10 | Drafted (not started) | `planned-changes/` |

## Acceptance and verification policy

Inherits the first-ledger policy ([`ledger.md`](ledger.md) §"Acceptance evidence policy") and adds:

- Each child change re-verifies its finding against current HEAD and cites current file:line (not the audited
  `a150e41682d` lines).
- Framework-level contracts (MA-1, MA-5) are tested in `core/metamodel` / `core/mmtest` / `core/runtimeservices`;
  command-log behaviour in commandlog applib integration tests and the applicable JPA adapter.
- The two P0 metamodel slices must include the specific regression maintenance added: MA-1 the **two-mixee
  isolation** scenario (annotated + plain mixee over the same faceted method); MA-5 the **import-and-replay of an
  actual final-maintenance collection-navigation DTO** whose filter parameters have changed.
- Any deliberate deviation from maintenance behaviour (e.g. D-C page-limit default, MA-15 retention) is recorded
  as an explicit v4 adaptation in the child change's design.

## Completion criterion

When child changes 1–10 are archived (or their findings explicitly waived), and MA-14 + the Section "Non-issues"
items remain accepted, the programme ledger may legitimately describe the forward port as **behaviourally
complete at the audited branch heads**, re-anchored to current HEAD.
