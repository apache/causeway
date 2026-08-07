# Second-Opinion Ledger

Independent findings. Verdicts are against `main`'s **code**. Paths are relative to each
worktree (`main` = this worktree; `ecp` = `../ecp`, `maintenance-branch`).

Legend: **PRESENT** (behaviour faithfully in main) · **GAP** (maintenance behaviour missing)
· **DIVERGENCE** (present but semantically different; may be intentional — flagged for
confirmation) · **N/A** (v4 architecture legitimately makes it inapplicable) ·
**BACKPORT** (originated on main/v4, already present by origin).

## A. Confirmed gaps — recommend action

| # | Issue | Behaviour missing from `main` | Severity | Evidence |
|---|---|---|---|---|
| **G1** | CAUSEWAY-4039 (tail) | **Mixin domain-event facet isolation.** When one mixin action/property/collection is contributed to *multiple* mixee types that have differing `@DomainObject(...DomainEvent=...)` defaults, `main` mutates the **shared** facet on the mixin's faceted method, so the first mixee processed "wins" for all of them (cross-mixee pollution). Maintenance fixed this by installing per-mixee **object-type-specific overlay facets** on each mixed-in member (action, property, collection) plus a mixee-specific `ActionInvocationFacetForAction` so execution reads the right event holder. | **High** — general metamodel correctness, affects any app with a shared mixin over several object types; **outside the command-log scope of the first analysis**. | `main` `SynthesizeDomainEventsForMixinPostProcessor.java:48-78` calls `facet.initWithMixee(objectSpecification)`; `ActionDomainEventFacet.java:117-122` does `super.updateEventType(...)` guarded by `if(!getEventTypeOrigin().isDefault()) return`. `main` has **no** `createObjectTypeSpecific*` methods. `ecp` postprocessor `initActionWithMixee`/`installMixeeSpecificActionInvocationFacet` + `ecp` commit `d5cdc5da369`. |
| **G2** | CAUSEWAY-4037 | **`saveForReplay` is not idempotent.** Re-saving/re-importing a command with an interaction-id already present creates a **duplicate** `CommandLogEntry` on `main`. Maintenance looks up `findByInteractionId` and returns the existing entry. | **Medium** — duplicate replay rows on re-import; the maintenance commit exists precisely to make this idempotent. | `main` `CommandLogEntryRepositoryAbstract.java:337-346` (unconditional create+persist) vs `ecp` `:267-281` (guard + early return). `ecp` commit `4bc7b2c9f25`. |
| **G3** | CAUSEWAY-4042 | **Replay-failure handling: don't roll back the batch; record error and continue.** Maintenance: a failed replay (e.g. a pre-requisite / advisor veto) records `FAILED` (and the exception) in a `REQUIRES_NEW` transaction, then maps the failure to *success* so a bounded/multiple replay **continues to the next command**. `main` returns the failure, which makes `replayOrRetry*Multiple`/`replayOrRetrySelected` **halt the whole batch** on the first failed command, and does **not** populate the entry's exception field on failure. | **Medium** — changes observable batch-replay semantics and the recorded failure detail. | `ecp` commits `3f128a7a791`, `622ed00ba0c`. `main` `ReplayableCommand.java:509-529` (uses `.accept(...)`, returns failure), `:487-489`; `CommandLogEntry.java:639-647` (sets FAILED + reason only, not exception). Contrast `ecp` `ReplayableCommand.tryReplay` `mapFailureToSuccess(...)` and `CommandLogEntry.saveAnalysis` failure branch `setException(analysis)`. |
| **G4** | CAUSEWAY-4042 | **`AppFeat` no longer classified as reference data.** Maintenance makes the SecMan `AppFeat` view-model (the reference type for permission-feature choices) `implements RefData`, so commands whose target/reference-parameter is an `AppFeat` bookmark are accepted as export participants with no prior finder. On `main` `AppFeat` is not `RefData`, so such commands are treated as unknown participants (excluded from export / `knownParticipants=false`). | **Low/Medium** — narrower export reachability for SecMan permission-feature commands. Note `main`'s reconcile design deliberately scoped ref-data opt-ins to four SecMan types; the `AppFeat` opt-in (a later 4042 commit) was simply not carried over. | `ecp` `secman/applib/.../ApplicationFeatureChoices.java:126-128` (`AppFeat ... implements ..., RefData`) vs `main` `:121-124` (no `RefData`). `ecp` commit `6075f978367`. |

## B. Divergences — likely intentional; confirm they are wanted

| # | Issue | Difference | Assessment |
|---|---|---|---|
| **D1** | CAUSEWAY-4042 | **`openTarget` RECORDED-vs-ACTUAL refinement not ported.** Maintenance re-introduced and refined a `ReplayableCommand#openTarget` action offering a `TargetType {RECORDED, ACTUAL}` choice, backed by new `getTarget()` / `getActualTarget()` string properties. `main` reflects the *earlier* maintenance model (target inspection via participant rows only) and has no such action/properties. Additionally, two orphaned source files (`ReplayableCommand_openTarget.java`, `ReplayableCommand_openTargetTR.java`) remain **inert** in `main` (not registered as mixins). | `main`'s projection spec deliberately chose the participant-row model, so this is defensible as intentional — but the **final** maintenance UI capability (open the recorded vs actual target) is absent. Decide whether parity with the final maintenance UI is required; either way, delete the dead files. |
| **D2** | CAUSEWAY-4042 | **Unified-manager page limit.** Maintenance opens the manager at `MAX_LIMIT = 320` and caps a user-entered limit to `[1,320]`. `main` opens at `DEFAULT_LIMIT = 100` and its `validateNewLimit` only enforces `> 0` (no upper cap). | Minor; `main` is internally consistent with its own spec. Confirm the lower default page size and the removal of the 320 cap are intended. |

## C. Command-log capability nodes — verified PRESENT (faithful)

All of the following were checked against `main`'s code and found behaviourally faithful
(often a strict superset). These correspond to the first analysis's capability nodes.

- **C1 recording suppression / pause-resume / property-edit recording** — pause-depth
  nesting (non-negative resume), owner-OR-target suppression marker, enum replacing boolean
  flags, property edits remaining recording-eligible even under `@Property(commandPublishing=DISABLED)`,
  fixture-install suppression via finally-guaranteed resume. PRESENT.
- **C2/C3/C4b recording-aware safe-action publishing + synthetic selector/navigate actions**
  — 26 distinct behaviours (parameter omission, single-row validation, reference/string
  matching, column-order filter params, view-model selectors, empty-selector disabling,
  select→navigate rename, scalar-reference navigate action). PRESENT.
- **C4a/D1/D2/M1/M2/M3 result capture, export/import DTOs, deep-copy, mapping SPI,
  in-memory + persistent listeners, advisor policy** — PRESENT (JDO adapter deliberately
  removed = accepted N/A). The advisor-policy config `InteractionAdvisorPolicy`
  (CHECK / CHECK_BUT_IGNORE / NO_CHECK, default NO_CHECK) — itself a CAUSEWAY-4042 addition —
  **was** captured, folded into `command-execution-advisor-policy`. The replay-execution
  semantics (G2, G3) are the only parts of this cluster that diverge.
- **P1 replayable-command projection** — eligibility, pending-or-failed bypass, result
  presence, participant derivation, actual-bookmark mapping rules, identity mementos,
  adjacent navigation, `knownParticipants`, layouts/column order. PRESENT (except the
  D1 openTarget refinement).
- **R1/R2 reference-data marker + known-participant reachability** — dependency-neutral
  `RefData` marker, metamodel-only default classifier (no object load), SecMan opt-ins,
  export-root OR ref-data reachability, known-target + known-reference-parameter
  validation, explicit tracker context (no request-global scratchpad). PRESENT (except the
  G4 `AppFeat` opt-in).
- **P2/E1/W1 unified manager, export/import, workflows** — baseline/limit memento, four
  review collections, legacy-manager compatibility shims (hidden launchers, bookmarks not
  rewritten), exclusion/restoration/deletion, autoselect, single bidirectional
  `moveCommands` with squash option, JPA retimestamping without schema change, strict
  canonical + legacy import, move-baseline-to-oldest-on-import, nextPage/prevPage removal.
  PRESENT (except the D2 limit divergence).
- **B1/B2 background-completion gates** — global pending-background detection via the
  existing repository query, recording rejects only new foreground entries, all replay
  surfaces (row-level, unified, legacy) share the same gate, bounded replay pauses without
  waiting for background commit. PRESENT (superset of maintenance surfaces).

## D. Non-command issue groups

| Issue | Classification | Verdict |
|---|---|---|
| 3989 initial command replay + YAML baseline | genuine (main is origin/ahead) | PRESENT |
| 3998 import from oldest baseline | genuine | PRESENT (`moveBaselineToOldest`, limit applied) |
| 3997 listing / column-order fixes | backport | PRESENT |
| 3973 qualified facets / FacetRank / layout variants | backport | PRESENT |
| 3957 table actions / stable values / action links / form exec / collection mementos | backport | PRESENT |
| 3968 simplified method filtering | backport (+v2 weaving infra N/A) | PRESENT |
| unnumbered "update-only publishing" (PR #3465) | genuine | PRESENT (`Publishing.ENABLED_FOR_UPDATES_ONLY`, wired) |
| 3899, 3942, 3950, 3969, 3972, 3979, 3983, 3985, 3995, 3996 | shared/backport | PRESENT |
| 3891, 3945, 3951, 3958, 3976 | backport from main/v4 | PRESENT (by origin) |
| 3955 deprecate legacy Identifier/LogicalType APIs | genuine, v2-only | N/A (v4 already removed those APIs) |
| 2445, 3956 | v2 CI (Maven 4 build) | N/A |
| 3952 | v2 versioning/build | N/A |
| 3970 | EclipseLink dep bump (v2 JDO) | N/A |

The eight pre-4010 groups the first ledger did not enumerate (2445, 3891, 3945, 3951, 3956,
3958, 3970, 3976) are all in this table as BACKPORT or N/A — correctly requiring no forward
port. Their absence from the first ledger is a documentation gap only, not a behavioural one.

## Summary

- **4 confirmed behavioural gaps** (G1–G4), concentrated in CAUSEWAY-4042 and the
  CAUSEWAY-4039 tail — i.e. the maintenance work that post-dated the consolidated specs the
  first analysis relied on. **G1 (mixin domain-event isolation) is the most important**: it
  is general-metamodel, not command-log, and would not have been visible from the
  command-log-scoped specs.
- **2 divergences** (D1, D2) that are probably intentional but should be confirmed.
- Everything else the first analysis covered is faithfully ported (frequently a superset),
  and every issue group it omitted is a backport or v2-only infra change.
