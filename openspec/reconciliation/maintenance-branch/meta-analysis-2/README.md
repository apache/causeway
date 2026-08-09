# Maintenance-branch reconciliation: meta-analysis 2

## Status and scope

This is the second independent meta-analysis of the maintenance-branch reconciliation.
It deliberately does not use or inspect `meta-analysis-1`.

The audited revisions are:

- `main`: `a150e41682d09dcb48aca996cfb3cf906711ad6e`
- `maintenance-branch` in `../ecp`: `1683383878939601d11cd27ffb7b2c0384204ce4`
- merge base: `65d64cd85b7bebb14f82615747388f4901fca108`

The first analysis, second opinion, third opinion, and fourth opinion were cross-mapped claim by claim. Disputed and uniquely reported claims were then checked directly against the two source trees and, where useful, the maintenance commits and the archived v4 reconciliation designs.

This is a source-level reconciliation audit. No production code was changed.

## Executive verdict

The forward port is extensive, but the first ledger's conclusion that the relevant maintenance behavior has all been reconciled is not correct at these heads.

There are **four release-significant correctness gaps**:

1. final-maintenance synthetic collection-navigation commands are not replay-compatible on main;
2. mixin domain-event facets are still shared and mutable across mixees on main;
3. repeated command import is not idempotent on main;
4. a handled replay failure still aborts a main batch and loses part of the maintenance failure record.

There are also **five genuine, lower-risk final-maintenance gaps**:

5. final recorded-versus-actual target projection and navigation;
6. per-command YAML export actions;
7. several `ReplayableCommand` presentation semantics;
8. SecMan `AppFeat` reference-data classification;
9. final public/legacy command-YAML format and failure semantics.

Four other differences need either no action or an explicit product decision: the v2-only Wicket summary escape hatch, main's deliberately different manager limit policy, retained legacy export/replay workflows, and main's stricter response to a developer-authored synthetic-action id collision.

The result is not “twelve gaps” or “four gaps.” Those counts came from different grouping and scope choices. The authoritative unit is the behavior in the ledger below.

## Priority view

| ID | Authoritative finding | Priority | Final verdict |
|---|---|---:|---|
| A1 | Synthetic collection-navigation replay contract | P0 | Confirmed gap/error |
| A2 | Per-mixee domain-event facet isolation | P0 | Confirmed gap/error |
| A3 | Idempotent `saveForReplay` | P0 | Confirmed gap |
| A4 | Handled replay-failure persistence and batch continuation | P0 | Confirmed gap/error |
| A5 | Recorded-versus-actual target projection and actions | P1 | Confirmed gap |
| A6 | Per-command YAML export | P1 | Confirmed gap |
| A7 | Replayable-command DTO, participant, and title fidelity | P1/P2 | Confirmed gap |
| A8 | SecMan `AppFeat` is reference data | P1 | Confirmed gap |
| A9 | Public/legacy YAML accepts final formats and fails instead of becoming empty | P1 | Confirmed gap/error |
| D1 | Wicket summary-view disable switch | Decision | Explicitly v2-tagged; not a default port obligation |
| D2 | Manager launch limit and upper cap | Decision | Deliberate v4 policy divergence |
| D3 | Retained legacy managers and `EXPORTED` workflow | Accepted | Deliberate v4 compatibility surface |
| D4 | Developer-authored synthetic id collision | Decision | Deliberate stricter v4 invariant is reasonable |

`P0` means the discrepancy can break replay, persistence, or general metamodel correctness. `P1` is observable functional parity. `P2` is presentation or a narrow edge case.

## Cross-opinion map

Legend: `G` = reported as a gap/error; `D` = reported as a divergence or decision; `N` = explicitly rejected as a gap; `—` = not identified. “Fourth raw/final” calls out a contradiction between the fourth opinion's raw verification and its final ledger.

| Behavior | Initial | Second | Third | Fourth | Meta-analysis 2 |
|---|---:|---:|---:|---:|---|
| Collection synthetic-action id compatibility | — | — | G | — | **G: A1** |
| Navigation argument matching/padding | — | — | G | G | **G: A1** |
| Navigation `REFERENCES_PARENT` filtering/order | — | — | partial | G | **G: A1, lower severity** |
| Per-mixee domain-event facets | — | G | G | G | **G: A2** |
| Idempotent replay import | — | G | G | G | **G: A3** |
| Replay failure continues batch | — | G | G | raw G / final incorrectly present | **G: A4** |
| Typed/persisted replay failure detail | — | partial | partial | G | **G: A4** |
| Direct recorded/actual target UI | — | D | G | G | **G: A5** |
| Per-command export | — | — | G | — | **G: A6** |
| Result-bearing displayed DTO | — | — | G | G | **G: A7** |
| Participant fallback/full target title | — | — | G | — | **G: A7** |
| `AppFeat implements RefData` | — | G | G | G | **G: A8** |
| Public YAML formats/failure handling | — | — | G | — | **G: A9, broadened by direct check** |
| Wicket summary disable | — | — | G | G | **D: v2-specific** |
| Manager limit 100 versus 320 | — | D | — | D | **D: deliberate v4 policy** |
| Retained legacy export state/actions | — | — | D | N | **Accepted v4 compatibility** |
| Query-results-cache clearing | — | — | N | G | **N: no corresponding cache path on main** |

The main value of the second, third, and fourth opinions is complementary coverage. Agreement raises confidence for A2, A3, and A8, but majority vote was not used as the decision rule. A1, A6, and A9 remain genuine even though only the third opinion found them. The fourth opinion's cache claim is rejected even though it is a literal source difference.

## Authoritative confirmed findings

### A1 — Synthetic collection-navigation replay contract

**Verdict: confirmed error; P0.** This combines four related final-maintenance behaviors, with the first two being the urgent ones.

#### A1.1 Stable action id

Final maintenance distinguishes collection and scalar-reference navigation:

- collection: `__causeway_navigate_to_one_of_<collectionId>`
- scalar reference: `__causeway_navigate_to_<referenceId>`

The collection prefix is defined in maintenance `core/metamodel/.../ObjectSpecificationAbstract.java:1052-1056`. Main uses the single prefix `__causeway_navigate_to_` for both forms in `core/metamodel/.../SyntheticNavigationActionFactory.java:67-70,138-183`.

Main resolves a replay DTO by exact local action id and throws `Unknown action` if it is absent (`CommandExecutorServiceDefault.java:361-375`). Therefore a collection command recorded by final maintenance cannot be replayed by main. This is not a cosmetic rename because the id is serialized into the command DTO.

The third opinion is correct here. The fourth opinion noticed the two prefixes while discussing the overloading validator but failed to follow the serialized id through main's exact replay lookup.

#### A1.2 Name-based argument reconstruction and padding

Final maintenance recognizes generated collection-navigation actions, matches DTO parameters to current action parameters by stable id or friendly name, reorders them, and supplies empty values for newly added filters (`maintenance CommandExecutorServiceDefault.java:437-476`). Main reconstructs every action argument positionally from the DTO (`main CommandExecutorServiceDefault.java:441-446`).

Even after the id mismatch is fixed, main remains incompatible when collection columns—and hence generated filter parameters—are added, removed, or reordered between recording and replay.

#### A1.3 Parameter selection and ordering

Final maintenance additionally:

- excludes a filter property hidden at `Where.REFERENCES_PARENT`;
- orders parameters by member order sequence and then id.

Main's `_MembersAsColumns` path honors `Where.PARENTED_TABLES`, parent-reference removal, grid occurrence, and table-column services, but does not apply the separate `Where.REFERENCES_PARENT` veto (`_MembersAsColumns.java:104-119`). Its grid-occurrence order can differ from final maintenance's member-order sequence (`SyntheticNavigationActionFactory.java:214-225` versus maintenance `ObjectSpecificationAbstract.java:1115-1128`).

These are real, narrow action-form and DTO-shape divergences. They should be reconciled while fixing A1.1/A1.2, but are not independently P0.

### A2 — Per-mixee domain-event facet isolation

**Verdict: confirmed error; P0.** All three later opinions agree, and direct inspection confirms them.

Main's `SynthesizeDomainEventsForMixinPostProcessor` obtains the layered action/property/collection facet and calls `initWithMixee` on it (`core/metamodel/.../SynthesizeDomainEventsForMixinPostProcessor.java:47-76`). The underlying shared facet is therefore mutated as multiple mixee types are processed.

Maintenance commit `d5cdc5da3697` instead creates and installs object-type-specific action, property, and collection overlay facets. For actions it also installs a local `ActionInvocationFacetForAction` so execution consults the per-mixee event facet (maintenance postprocessor lines 86-141).

Impact: a mixin contributed to differently annotated mixee types can use the event default of whichever mixee initialized the shared facet, rather than the current mixee's event type. This is a general metamodel correctness defect, not a commandlog presentation issue.

### A3 — Idempotent `saveForReplay`

**Verdict: confirmed gap; P0.** All three later opinions agree.

Maintenance `CommandLogEntryRepositoryAbstract.saveForReplay` first looks up the DTO interaction id and returns the existing entry (`extensions/core/commandlog/applib/.../CommandLogEntryRepositoryAbstract.java:267-283`). Main unconditionally creates and persists a new entry (`:337-345`). No main import caller performs the missing guard.

The main test named `legacyAndRepeatedImportDelegateEveryCommandToRepository` confirms only delegation twice; it does not establish repository idempotency. With interaction id as persistence identity, a repeated import can fail rather than reuse the existing row.

### A4 — Handled replay-failure persistence and batch continuation

**Verdict: confirmed error; P0.** The second and third opinions are correct. The fourth opinion's raw `verify-4042.md` also says the return-success behavior is missing, but its final ledger accidentally reverses that conclusion.

Maintenance executes the command in `REQUIRES_NEW`, persists the replay failure in another `REQUIRES_NEW` transaction, then uses `mapFailureToSuccess` so the failure is considered handled (`ReplayableCommand.java:679-703`). Main persists the failure in a new transaction but returns the original failed `Try` (`ReplayableCommand.java:509-528`). Main's replay-multiple loop stops when that result is a failure (`CommandManager_replayOrRetryMultiple.java:81-92`).

Consequences on main:

- a bounded batch stops at the first handled replay failure instead of continuing;
- `CommandLogEntry.saveAnalysis` records `FAILED` and a short reason but does not also populate the exception field, while final maintenance does (`CommandLogEntry.java:741-748` on maintenance versus `:639-647` on main);
- main uses `ex.toString()` and lacks final maintenance's categorized advisor-error text (`Disabled:` or `Invalid:`; maintenance maps both hidden and disabled exceptions to `Disabled:`). This last point is low severity but belongs in the same correction.

The transaction rollback itself is already handled on main. The missing semantics are the outward successful outcome after recording the failure and the final failure metadata.

### A5 — Recorded-versus-actual target projection and actions

**Verdict: confirmed gap; P1.** The second opinion called this a likely intentional divergence, but that assessment relied on the earlier participant-row design. Final maintenance subsequently restored and refined the direct target capability.

Final maintenance exposes:

- full recorded and actual target properties;
- abbreviated recorded and actual table columns;
- object-form and table-row `openTarget` actions;
- a `RECORDED`/`ACTUAL` choice;
- action disabling and user feedback when neither target can be resolved.

Main has older `ReplayableCommand_openTarget` and `ReplayableCommand_openTargetTR` source files that open only the recorded target, but neither is imported by `CausewayModuleExtCommandLogApplib` (`:85-137`). Main also lacks the final top-level `getTarget()` / `getActualTarget()` projections. Participant rows are useful but not equivalent: final maintenance retains participants and adds these direct surfaces too.

### A6 — Per-command YAML export

**Verdict: confirmed gap; P1.** Only the third opinion reported it, but direct inspection confirms it.

Maintenance commits `73ee2fc20ae1` and `6c0e6406b724` add and register object-form and table-row per-command export actions. They emit a result-bearing `CommandExportDto`, optionally remap results, and provide file naming controls. Main has the bulk `CommandManager_exportSequence` action but has neither per-command export source file nor registration.

Bulk sequence export does not replace the observable ability to export one `ReplayableCommand` directly.

### A7 — Replayable-command DTO, participant, and title fidelity

**Verdict: confirmed presentation gap; P1/P2.** These are three small final-maintenance behaviors grouped as one presentation slice.

1. **Displayed DTO includes the recorded result.** Maintenance `getDto()` renders `CommandExportDto.of(commandDto, result)` (`ReplayableCommand.java:387-403`). Main renders the raw `CommandDto` (`:358-373`), omitting the result envelope and result bookmark.
2. **Recorded commands have usable actual participants.** Maintenance treats both `UNDEFINED` and `OK` as executed successfully (`ReplayState.isExecutedOk`) and falls back from an unmapped actual bookmark to the recorded bookmark. Main falls back only for `OK` (`ReplayableCommand.java:345-355`), so `UNDEFINED` target, argument, and result rows can lose actual links.
3. **Title uses the complete target bookmark.** Maintenance builds the title from the complete recorded target. Main truncates the identifier to ten characters in `getTargetId()` and uses that in the title (`:152-154,201-208`). This is cosmetic but is a verified final-state difference.

The DTO item is functional enough to include at P1. Participant fallback is a navigation/presentation defect. Title truncation is P2.

### A8 — SecMan `AppFeat` is reference data

**Verdict: confirmed gap; P1.** All three later opinions agree.

Final maintenance declares `ApplicationFeatureChoices.AppFeat implements Comparable<AppFeat>, ViewModel, RefData` (`extensions/security/secman/applib/.../ApplicationFeatureChoices.java:125-128`). Main declares only `Comparable` and `ViewModel` (`:121-124`). Main has no SecMan-specific classifier that supplies the missing result.

As a result, an `AppFeat` bookmark used by permission-feature commands is not an export root on main unless some other known-participant path happens to establish it. This was a later CAUSEWAY-4042 opt-in omitted from the earlier v4 reference-data reconciliation.

### A9 — Public/legacy YAML format and failure semantics

**Verdict: confirmed error; P1.** Only the third opinion reported it, but the source difference is unambiguous.

Final maintenance's public `CommandDtoUtils.fromYaml` attempts four forms: raw `CommandDto` list, raw multi-document commands, wrapped `CommandExportDto` list, and wrapped multi-document exports. For wrapped forms it returns the embedded commands and intentionally discards result metadata. It calls `ifFailureFail` if none parse (`api/applib/.../CommandDtoUtils.java:191-221` and its `from_yaml_accepts_wrapped_export_shape_and_ignores_result_metadata` test).

Main attempts only raw `CommandDto` list/multi-document input and calls `getValue().orElseGet(Collections::emptyList)` (`:178-182`). A wrapped export or a parse failure therefore becomes an empty command list instead of either producing its embedded commands or throwing.

Main's unified importer is protected because it uses strict `fromYamlForReplay`. The public API and registered legacy `CommandReplayManager.importCommands` still use the permissive method, so malformed input can be silently treated as “imported nothing.” This is a real public-contract and legacy-workflow discrepancy, not a defect in the new unified importer.

## Explicit divergences and decisions

### D1 — Wicket summary-view disable switch

Final maintenance reads the environment variable `causeway.viewer.wicket.summary-view-disabled` and disables the BigDecimal collection summary view when it equals `true`; main has no equivalent.

This is a real runtime difference, but both commits are explicitly tagged `[v2]`, use a dotted environment-variable lookup rather than framework configuration, and were not part of the command-replay feature line. The authoritative default is therefore **not a required forward port**. If the operational capability is still wanted on v4, implement it as an explicit v4 configuration decision rather than blindly copying the v2 mechanism.

### D2 — Manager launch limit and upper cap

Final maintenance is internally mixed by entry point: its framework-memento fallback is 100, while the standard menu launcher constructs a limit of 320 and `HasLimit_changeLimit` caps input at 320. Main launches at 100, accepts any positive limit, and explicitly specifies that policy in `openspec/specs/unified-command-manager/spec.md:7-12,89-102`.

This is observable but deliberate and tested v4 behavior. Retain it unless product owners specifically require final-maintenance console sizing.

### D3 — Retained legacy managers and `EXPORTED` workflow

Final maintenance removes `ReplayState.EXPORTED` and the separate export/replay managers. Main retains them and some state-mutating legacy actions alongside the unified manager.

This was an explicit v4 compatibility choice, not an accidental miss. The archived v4 design says to preserve `EXPORTED`, old logical types, bookmarks, collections, and existing workflow actions (`openspec/changes/archive/2026-08-06-reconcile-unified-command-manager/design.md:5,59-67`). It is therefore an accepted superset under the current reconciliation policy. Reconsidering the old actions would be a compatibility cleanup change, not completion of the existing forward port.

### D4 — Developer-authored action occupies a synthetic id

Maintenance skips collection-action synthesis when an existing action occupies the generated id. Main throws an `IllegalStateException` for a collision (`SyntheticNavigationActionFactory.java:114-117`). This is a genuine edge-case difference, but the main behavior enforces that framework-reserved ids cannot be silently captured by application code.

Treat main's stricter behavior as a reasonable v4 invariant unless compatibility with an application deliberately using these internal ids is required. This decision is separate from A1's serialized maintenance action-id mismatch.

## Challenged claims that are not gaps

- **Query-results-cache clearing:** maintenance caches command-entry/domain-service lookups and must clear `QueryResultsCache` at transaction boundaries. Main's `ReplayContext.lookupCommandLogEntry` directly calls `findByInteractionId` and domain-service classification directly uses the specification loader (`ReplayContext.java:86-108`). There is no corresponding replay cache to invalidate. Literal code is absent; behavior is not.
- **`ActionOverloadingValidator` synthetic-id skip:** final maintenance needs an explicit validator exemption around its generated action scheme. Main creates association-id-suffixed synthetic ids and detects true collisions in the factory. The explicit validator hack is not independently required. This does not cure A1's cross-version serialized id problem.
- **Directional up/down movement:** those actions were superseded on final maintenance by the same move-after-target workflow that main implements. They are not missing final behavior.
- **Autoselect exportable and `getExportable`:** main's known-participant sequence automatically filters the export set and `isKnownParticipants` is the successor projection. The behavior is present under the unified model.
- **View models as command results:** main captures bookmarkable view-model results through the v4 result-capture path; the exact v2 SecMan view-model implementation strategy is not required.
- **JDO replay mapping:** intentionally not applicable because the commandlog JDO adapter was removed on v4.
- **YAML writer method names:** main keeps legacy list output in `toYaml`, adds `toMultiDocYaml`, and uses `toYamlExport` for result-bearing operational export. That naming/API adaptation is acceptable; A9 concerns failure semantics in the reader.

## Why the analyses differed

The first analysis used the 23 consolidated maintenance OpenSpec specifications as its normative source and focused primarily on command recording/replay capability nodes. That captured the large body of work well but missed three classes of evidence:

1. work after or outside the consolidated specs, especially CAUSEWAY-4042 and the tail of CAUSEWAY-4039;
2. general metamodel and loose `[v2]` commits outside the commandlog capability graph;
3. serialized/API compatibility details that can disappear when a broad capability is judged “present.”

The second opinion found the clearest late-tail issues but did not exhaustively decompose CAUSEWAY-4038/4039 or public YAML behavior. The third opinion had the best complete-commit coverage and found the action-id, per-command export, YAML, and participant details. The fourth opinion had the best fine-grained CAUSEWAY-4038 decomposition and found the hidden/order and typed-message details, but its final ledger dropped or reversed some of its own raw findings.

The reliable synthesis is therefore:

```text
complete git universe from third/fourth
        + late-tail focus from second
        + fine-grained 4038/4042 checks from fourth raw evidence
        + direct two-tree adjudication
        = A1–A9 above
```

## Recommended remediation order

1. **A1** — restore the final collection synthetic id contract and compatible name-based/padded replay arguments; cover import of an actual final-maintenance DTO.
2. **A2** — port per-mixee action/property/collection event overlays and the local action invocation facet with the two-mixee regression scenario.
3. **A3** — make `saveForReplay` interaction-idempotent and add repository/integration coverage for repeated canonical and legacy import.
4. **A4** — return success after a persisted handled replay failure, retain exception/failure detail, and prove a batch continues to the next command.
5. **A8 and A9** — small functional fixes for SecMan export reachability and malformed YAML handling.
6. **A5–A7** — reconcile the final replay-console target/export/presentation tail.
7. Fold A1's lower-severity visibility/order details into the A1 implementation, then explicitly record decisions D1–D4 so they are not repeatedly rediscovered.

After A1–A9 are resolved or explicitly waived, the programme ledger can legitimately describe the forward port as behaviorally complete at the audited branch heads.
