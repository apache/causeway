# Clean-room third-opinion audit of the maintenance-branch forward port

## Verdict

Main does not faithfully contain every genuine maintenance-branch behavioral change.
At the audited heads, ten discrepancy groups are confirmed and one additional legacy-workflow divergence is policy-sensitive.
The highest-risk gaps affect cross-version replay of synthetic collection navigation, per-mixee domain-event isolation, repeated command import, and continuation after a recorded replay failure.
The remaining gaps affect replay target/export actions and presentation, reference-data classification, malformed-YAML handling, and a Wicket summary-view switch.

## Audited revisions

- The authoritative maintenance worktree was `/Users/danhaywood/repos/github/apache/causeway/ecp` at `1683383878939601d11cd27ffb7b2c0384204ce4` on `maintenance-branch`.
- The target worktree was `/Users/danhaywood/repos/github/apache/causeway/main` at `a150e41682d09dcb48aca996cfb3cf906711ad6e` on local `main`, including its 24 commits ahead of `origin/main`.
- The fixed divergence base was `65d64cd85b7bebb14f82615747388f4901fca108`.
- The chronological universe was all 481 commits in `65d64cd85b7..maintenance-branch`, ordered by `git rev-list --reverse --topo-order`.
- No content under `openspec/reconciliation/maintenance-branch/` in either worktree was read to derive or assess the universe.
- Maintenance `openspec/specs` and `openspec/changes/archive` were consulted only after a commit was already in scope and only to clarify intent.

## Method

Eight read-only clean-room subagents each inspected a contiguous chronological slice of approximately 60 commits.
Every first-parent delta was inspected with the forbidden reconciliation path excluded.
The subagent ledgers were machine-checked to contain exactly one classification for each index from 1 through 481.
Candidate gaps were then re-read and challenged directly against final maintenance and main production code, registrations, call sites, and tests.
Equivalent v4 adaptations were accepted despite Jakarta names, record configuration, JDO removal, package movement, or renamed APIs when runtime semantics remained available.
The companion `2026-08-07-clean-room-commit-ledger.md` records every commit, classification, subject, and skip justification.

## Commit classification

| Classification | Count | Meaning |
|---|---:|---|
| Behavioral contribution | 173 | A commit contributing to a genuine final runtime or public-API behavior, including incremental fixes later consolidated into one claim. |
| Pure backport from main | 63 | Main antecedents or an explicitly chained backport series were identified, so no independent forward-port obligation remained. |
| V2-only CI, build, dependency, weaving, or JDO work | 42 | The change was confined to the v2 toolchain or an adapter absent by design in v4. |
| Nonbehavioral | 203 | Merge-only, proposal/spec/archive, documentation, test-only, cosmetic, mechanical, transient-and-reverted, or behavior-neutral refactoring work. |
| Total | 481 | Complete chronological coverage. |

Thirty-two issue or unkeyed groups contained at least one behavioral contribution.
The findings below consolidate incremental commits by final behavior rather than treating transient intermediate states as separate defects.

## Confirmed findings

### F1 — High — Synthetic collection-navigation commands are not replay-compatible

**Verdict:** ERROR.
Final maintenance uses `__causeway_navigate_to_one_of_<collectionId>` for collection navigation and `__causeway_navigate_to_<referenceId>` for scalar references.
Maintenance defines the collection prefix in `core/metamodel/.../ObjectSpecificationAbstract.java:1055` and finalizes the separation in commit `067e3ba4565` at index 391.
Main uses one `SyntheticNavigationActionFactory.ACTION_ID_PREFIX` of `__causeway_navigate_to_` for both forms at `core/metamodel/.../SyntheticNavigationActionFactory.java:69,155`.
Main performs exact action lookup and throws `Unknown action` when the DTO member id is absent at `core/runtimeservices/.../CommandExecutorServiceDefault.java:372-375`.
A collection command recorded on final maintenance therefore names an action that main does not synthesize.
Maintenance also recognizes generated collection actions and reconstructs replay arguments by parameter id or friendly name while padding missing filters at `CommandExecutorServiceDefault.java:437-476`, finalized by commit `7a8d749f2b2` at index 411.
Main reconstructs all action arguments positionally at `CommandExecutorServiceDefault.java:441-447` and contains no generated-navigation compatibility path.
This also breaks replay when collection columns add, remove, or reorder synthetic filter parameters even if the action-id mismatch is repaired.
Maintenance skips synthesis when a developer-authored action already occupies the generated id at `ObjectSpecificationAbstract.java:1012-1018`, whereas main throws during metamodel synthesis at `SyntheticNavigationActionFactory.java:114-117`.
The ten passing target `SyntheticNavigationActionTest` tests establish main's current behavior but do not exercise a final-maintenance DTO or parameter evolution.

### F2 — High — Main still mutates a shared mixin domain-event facet

**Verdict:** ERROR.
Maintenance commit `d5cdc5da3697` at index 462 creates action, property, and collection domain-event facets per mixee and installs a local action invocation facet.
Maintenance `SynthesizeDomainEventsForMixinPostProcessor.java:86-115` adds the mixee-specific action facet and invocation facet rather than mutating the mixin method's fallback facet.
Its focused test constructs annotated and plain mixees over the same `FacetedMethod` and verifies that the annotated mixee receives its object-level event while the plain mixee and shared method retain the default event.
Main `SynthesizeDomainEventsForMixinPostProcessor.java:48-75` looks up the layered facet and calls `initWithMixee` on it.
Main `ObjectActionMixedIn.java:90-92` layers over the shared mixin `FacetedMethod`, the event facet mutates its event type in `initWithMixee`, and action execution still delegates to the shared mixin action at `ObjectActionMixedIn.java:179-181`.
Main's nine passing `ActionAnnotationFacetFactoryTest_domainEvent` tests cover one mixee at a time and omit maintenance's two-mixee isolation scenario.
A generic mixin applied to differently annotated mixee types can therefore inherit whichever type last initialized the shared facet.

### F3 — High — Repeated command import is no longer idempotent

**Verdict:** GAP.
Maintenance commit `4bc7b2c9f25` at index 399 makes `CommandLogEntryRepositoryAbstract.saveForReplay` return the existing entry for the DTO interaction id.
The final maintenance implementation performs `findByInteractionId` before entity creation at `CommandLogEntryRepositoryAbstract.java:267-283`.
Main's method creates, initializes, and persists a new entity unconditionally at `CommandLogEntryRepositoryAbstract.java:337-345`.
Neither the unified nor legacy main import caller performs a duplicate check before calling the repository.
The JPA command-log primary key is the interaction id, so a repeated import is expected to fail persistence rather than reuse the existing row.
The four passing main `CommandManagerImportCommandsTest` tests do not include a duplicate-import case.

### F4 — High — A handled replay failure aborts the remaining batch

**Verdict:** ERROR.
Maintenance commits `3f128a7a7910`, `622ed00ba0c6`, `0d5ee322b170`, and `9a9d4444d172` at indices 465–468 make replay persist failure analysis in a new transaction and convert the handled failure into a successful `Try`.
Final maintenance `ReplayableCommand.tryReplay` uses `mapFailureToSuccess` after recording the failure at `ReplayableCommand.java:679-703`.
Main records the failure but returns the original failed `Try` at `ReplayableCommand.java:509-528`.
Both batch managers stop when `tryReplayOrRetry().isFailure()` is true, so maintenance continues after a recorded failure while main returns immediately.
Main's passing `ReplayableCommandMappingTest.notificationFailureFailsReplayAndRecordsAnalysis` explicitly asserts the divergent failed-`Try` behavior.
This affects ordinary command-execution failures and replay-result mapping conflicts.

### F5 — Medium-high — Final direct target navigation and target projection are unreachable or absent

**Verdict:** GAP.
Maintenance commit `d80cda55d2f` at index 413 exposes the command target as a top-level `ReplayableCommand` table property.
Maintenance commits `da9b73c2cc2e`, `102fbb50cd31`, `d47921837405`, `8ed3c8c4e5d2`, `8efd1dd67724`, and `dc0aee6c8b8f` at indices 469–477 add and register recorded-versus-actual target properties and object/table-row target actions.
Final maintenance exposes `getTarget` and `getActualTarget` at `ReplayableCommand.java:250-285` and imports both `ReplayableCommand_openTarget` mixins in `CausewayModuleExtCommandLogApplib`.
Main retains source files named `ReplayableCommand_openTarget` and `ReplayableCommand_openTargetTR`, but the module imports neither one.
Main has no top-level `ReplayableCommand.getTarget` or `getActualTarget` property.
Main's passing `ReplayableCommandPresentationTest` explicitly asserts that the target mixins are not registered.
Participant rows provide an indirect route to some objects, but maintenance retains participants and still adds the direct row/form behavior, so participants are not an equivalent replacement.

### F6 — Medium — Per-command export actions are absent

**Verdict:** GAP.
Maintenance commits `73ee2fc20ae1` and `6c0e6406b724` at indices 445–446 add and register per-command YAML export for object forms and table rows.
Final maintenance contains and imports `ReplayableCommand_export` and `ReplayableCommand_exportTR`.
Main contains neither source file and imports neither mixin.
Main's bulk `CommandManager_exportSequence` preserves sequence export but does not replace the per-command form/table-row action surface.

### F7 — Medium — Replayable-command presentation lost recorded-result and actual-participant fidelity

**Verdict:** GAP.
Maintenance commit `f9c7562b9f9` at index 252 renders `ReplayableCommand.getDto` as a `CommandExportDto` containing the command and optional recorded result.
Final maintenance builds `CommandDtoUtils.CommandExportDto.of(commandDto, result)` at `ReplayableCommand.java:394-403`.
Main renders the raw `CommandDto` at `ReplayableCommand.java:365-373`, so the displayed YAML omits the result envelope and result bookmark.
Maintenance participant refinements at indices 263, 264, 268, 272, 284, and 287 treat unchanged recorded bookmarks as actual bookmarks when replay state is `UNDEFINED` or `OK`.
Main's `actualBookmarkFor` falls back only for `OK` at `ReplayableCommand.java:345-355`, so recorded-side `UNDEFINED` entries lose actual participant links without an explicit remapping.
Maintenance commit `e5bf9694d91` at index 266 changes the replayable title to use the complete recorded target bookmark.
Main builds the title from `getTargetId`, which truncates identifiers to ten characters at `ReplayableCommand.java:202-208`.
These are presentation defects rather than command-execution defects, but each is a final maintenance behavior missing from main.

### F8 — Medium — Secman application-feature choices are not recognized as replay reference data

**Verdict:** GAP.
Maintenance commit `6075f978367a` at index 472 makes `ApplicationFeatureChoices.AppFeat` implement `RefData`.
Final maintenance declares `AppFeat implements Comparable<AppFeat>, ViewModel, RefData` at `extensions/security/secman/applib/.../ApplicationFeatureChoices.java:125-128`.
Main declares only `Comparable<AppFeat>, ViewModel` at the corresponding file's lines 121-124.
Main's only `CommandReplayReferenceDataService` implementation recognizes classes assignable to `RefData`, and no Secman-specific classifier exists.
An `AppFeat` participant is therefore not an export root on main unless an earlier result independently establishes the same bookmark.

### F9 — Medium — Malformed public command YAML is silently accepted as empty

**Verdict:** ERROR.
Maintenance's final multi-format `CommandDtoUtils.fromYaml` retries supported representations and calls `ifFailureFail` when none parse at `api/applib/.../CommandDtoUtils.java:191-220`.
Main `CommandDtoUtils.fromYaml` calls `YamlUtils.tryReadAsList(...).getValue().orElseGet(Collections::emptyList)` at lines 178-182.
A failed `Try` has no value, so main turns malformed input into an empty list.
A runtime JShell probe against main HEAD passed `not: [valid`; the public API returned a list of size zero while `fromYamlForReplay` threw `JacksonYAMLParseException`.
The active unified importer uses strict `fromYamlForReplay`, but the public API and registered legacy `CommandReplayManager.importCommands` use the swallowing path.
Maintenance commits `b29b16aea96b` and the subsequent YAML refinements establish the final fail-on-unparseable behavior.

### F10 — Medium — The Wicket summary-view disable switch was not ported

**Verdict:** GAP.
Maintenance commits `5f91fe27e5d6` and `703432fa76c5` at indices 140–141 add the environment switch `causeway.viewer.wicket.summary-view-disabled` and require a case-insensitive value of `true`.
Final maintenance reads the switch in `CollectionContentsAsSummaryFactory.java:60-69` and returns `DOES_NOT_APPLY` when disabled.
Main's renamed factory constructor has no switch and always applies whenever a `BigDecimal` column exists at the corresponding file's lines 56-68.
A clean-room search found no replacement property, environment binding, or test in main.

### F11 — Medium-low, policy-sensitive — Removed legacy export-state behavior remains registered

**Verdict:** PARTIAL / ERROR if final maintenance is the required public workflow.
Maintenance commits `1b5c7a4a45e7`, `cce505a5fef2`, and `486796c17b0f` at indices 429, 431, and 432 remove `ReplayState.EXPORTED`, stop export from mutating state, and replace separate export/replay managers with `CommandManager`.
Main's unified `CommandManager` and non-mutating sequence export are present.
Main nevertheless retains `ReplayState.EXPORTED`, `CommandExportManager`, `CommandReplayManager`, `ReplayableCommand_makeExportable`, and registered `CommandExportManager.makeSelectedExportable` actions.
The legacy bulk action remains executable and changes `EXPORTED` entries back to `UNDEFINED` at `CommandExportManager.java:270-274`.
Keeping view-model memento compatibility would not require keeping these mutating actions registered.
If this extra surface is an explicit v4 compatibility policy, treat it as accepted additional behavior; otherwise it is a remaining forward-port error.

## Challenged candidates not counted as gaps

- Main's `CommandDtoUtils.toYaml` keeps YAML-list output while `toMultiDocYaml` supplies the maintenance multi-document behavior, and operational result-bearing export uses `toYamlExport`, so this was accepted as a v4 API naming/refinement adaptation.
- Maintenance adds `QueryResultsCache` lookups and explicit transaction-boundary invalidation, while main directly queries `findByInteractionId` on every lookup, so the same freshness semantics are available without a cache to invalidate.
- With recording support disabled and global action publishing policy `ALL`, main preserves the pre-existing policy, which matches the maintenance specification's stated intent even though one maintenance implementation path could suppress it.
- JDO persistence deltas were excluded because main intentionally has no JDO adapter.
- Record-based configuration and Jakarta package differences were ignored throughout.

## Verification performed

Main was built with Java 21 while running focused tests in `core/mmtest` and `extensions/core/commandlog/applib`.
Forty-four focused tests passed with zero failures or errors across command YAML, replay import, synthetic navigation, action domain events, replay mapping, replay presentation, and command-manager import.
Those passing tests are useful reachability evidence but several intentionally assert main's divergent behavior, including failed replay outcomes and unregistered target mixins.
The malformed-YAML JShell probe independently demonstrated the public-versus-strict parser divergence at runtime.
A standalone maintenance metamodel test invocation could not resolve local `4.0.0-SNAPSHOT` reactor dependencies without a larger `-am` build, so maintenance-side evidence for mixee isolation remains its final production code and focused committed tests.
No production code was changed.

## Conclusion

The forward port is substantial and most behavioral areas are present, but it is not complete or fully faithful.
F1 through F4 should be treated as the first remediation tier because they can break replay compatibility, cross-type event dispatch, import retry safety, or batch completion.
F5 through F10 are concrete missing public behavior and presentation/configuration gaps.
F11 requires an explicit compatibility-policy decision before removal, but its state-changing legacy actions should not be mistaken for harmless memento compatibility.
