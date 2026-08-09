# CAUSEWAY-4039 — fourth-opinion clean-room verification

merge-base: 65d64cd85b7
MAIN HEAD: a150e41682d (branch `main`)
SOURCE (v2): maintenance-branch @ ecp worktree

Note on shared object store: the CAUSEWAY-4039 commit hashes (d5cdc5da369, 13c67c8b674, 359de942386,
25b0b7ee646, e70bcee4463, 2a9d6bc848a) are visible from the `main` worktree via `git log --all` ONLY
because both worktrees share the object DB and `maintenance-branch` is checked out in the ecp worktree.
`git merge-base --is-ancestor <hash> HEAD` returns FALSE for all of them — they are NOT on `main`.
The forward-port to `main` was therefore done independently, in part under a DIFFERENT ticket
(CAUSEWAY-3910). Verdicts below reflect main's actual HEAD content.

---

## Strand 1 — Mixin domain-event facet ISOLATION (headline d5cdc5da369): MISSING

v2 mechanism (d5cdc5da369):
- `SynthesizeDomainEventsForMixinPostProcessor` stops calling `facet.initWithMixee(objectSpecification)`
  (shared-mutate of the shared mixin `FacetedMethod`); instead installs per-mixee LOCAL OVERLAY facets:
  `initActionWithMixee/initPropertyWithMixee/initCollectionWithMixee` call
  `*DomainEventFacet.createObjectTypeSpecificForMixin(mixeeSpec, holder)` and `member.addFacet(...)`.
- For actions it also installs a local `ActionInvocationFacetForAction.createObjectTypeSpecific(...)`
  overlay (execution reads the event holder from the invocation facet).
- Each facet gains a private `ObjectTypeSpecific` subclass overriding `isObjectTypeSpecific() -> true`.
- `ObjectActionMixedIn.execute(...)` routes via local invocation facet when present:
  `hasLocalActionInvocationFacet() ? this.executeInternal(...) : mixinAction.executeInternal(...)`
  where `hasLocalActionInvocationFacet()` = `this.getFacet(ActionInvocationFacet.class) != mixinAction.getFacet(...)`.

MAIN state (NONE of the above ported):
- `core/metamodel/.../postprocessors/members/SynthesizeDomainEventsForMixinPostProcessor.java:53,64,75`
  still calls the SHARED-MUTATE path:
    `facet->facet.initWithMixee(objectSpecification)` for action/property/collection.
  No `initActionWithMixee`/`createObjectTypeSpecificForMixin`, no invocation-overlay install.
- `core/metamodel/.../facets/.../ActionDomainEventFacet.java`, `PropertyDomainEventFacet.java`,
  `CollectionDomainEventFacet.java`, `ActionInvocationFacetForAction.java`:
  grep for `createObjectTypeSpecific` / `class ObjectTypeSpecific` = NONE. `initWithMixee` still present
  (shared-mutate) in all three DomainEvent facets + DomainEventFacetAbstract.
- `core/metamodel/.../spec/impl/ObjectActionMixedIn.java:179` (repackaged from specloader/specimpl):
  `return mixinAction.executeInternal(head, argumentAdapters, interactionInitiatedBy);` — UNCONDITIONAL,
  no `hasLocalActionInvocationFacet()` guard, no local `this.executeInternal(...)` branch.
- Last commit to touch main's mixin post-processor: d5d82aa7ec4 (an old master->spring6 merge), i.e.
  the CAUSEWAY-4039 isolation was never applied.

Behavioural gap: main still shares/mutates the single mixin FacetedMethod's domain-event facet across
all mixees, so one mixee's ANNOTATED_OBJECT default can pollute another mixee (the exact bug v2 fixed).
VERDICT: MISSING.

---

## Strand 2 — Navigation-action synthesis: POST_PROCESS as ONLY path + re-entrancy guard: PRESENT

Ported on main under CAUSEWAY-3910 (commit 42b9ecbb433 "reconcile synthetic command navigation"),
matching v2's FINAL design (13c67c8b674), not the transitional strategy-flag design (359de942386).

- `core/metamodel/.../postprocessors/members/navigation/SynthesizeNavigationActionsPostProcessor.java:50-53`
  `postProcessObject` -> `mutable.synthesizeNavigationActions()`. Javadoc:52 states this is the sole
  mechanism, gated per-type by `causeway.extensions.command-log.recording-support` read live from config
  (NOT via `isEnabled()`) — exactly v2's final commit.
- INLINE removed: config enum/property gone — grep `NavigationActionSynthesis` / `navigation-action-synthesis`
  in `core/config/src/main/java` = NONE. No `isNavigationActionPostProcessing()`,
  no `ensureNavigationActionsForMixedInAssociations()` anywhere in main metamodel.
- Gating per-spec: `ObjectSpecificationDefault.java:243-247` `synthesizeNavigationActions()` early-returns
  unless `recordingSupport().isEnabled()`.
- StackOverflow root cause removed: `streamDeclaredActions(...)` (ObjectSpecificationDefault.java:1017-1028)
  no longer triggers any navigation synthesis — only `mixedInActionAdder.trigger(...)`. Synthesis is off the
  lazy path entirely, so it cannot re-enter element-type introspection on a cyclic collection graph.
- One-shot / idempotency: synthesis runs once per type from postProcessObject and dedups via
  `existingActionIds` / `existingSyntheticActionIds` (lines 250-257) before `replaceActions(...)`; the
  `_Oneshot` pattern (mixedInActionAdder/mixedInAssociationAdder) is retained at lines 1160-1161 and
  `mixedInAssociationAdder.trigger(...)` is invoked at 249.
VERDICT: PRESENT (equivalent to v2 final; the transitional INLINE+enum design was correctly NOT carried over).

---

## Strand 3 — record result of a view model (e70bcee4463): PRESENT

Ported on main under CAUSEWAY-3910 (commit 79b093c2cfb "reconcile command result metadata").

- `core/runtimeservices/.../executor/MemberExecutorServiceDefault.java:385-396`
  `setCommandResultIfEntityScalar`:
    flush guarded by `entityState.isPersistable() && (!hasOid() || isDetached())` (lines 389-392),
    then `resultAdapter.getBookmark().ifPresent(bookmark -> command.updater().setResult(Try.success(bookmark)))`
    (394-395).
- Same net behaviour as v2's e70bcee4463: the old `ManagedObjects.bookmarkElseFail(...)` (which only worked
  for persistable entities and would fail/skip a view model) is replaced by the null-safe
  `getBookmark().ifPresent(...)`, so a view model (non-persistable but hasOid) now has its result recorded.
- Main is a cleaner variant of v2 (v2 kept an explicit `if(!(isPersistable()||hasOid())) return;` early guard
  and a warn-log). Case-by-case equivalence: view model -> both skip flush, both record bookmark;
  non-persistable/no-oid -> v2 returns early, main's getBookmark() is empty -> neither records. Equivalent.
VERDICT: PRESENT.

---

## Strand 4 — fixes mapping service for result (2a9d6bc848a): PRESENT

v2's 2a9d6bc848a fixed a copy-paste bug in `copyCommandExportDto`: result was set on the INPUT dto
(`commandExportDto.setResult(resultCopy)`) instead of the COPY (`commandExportDtoCopy.setResult(resultCopy)`).

- Main's `extensions/core/commandlog/applib/.../replay/ResultRemappingService.java` was rewritten: the old
  `copyCommandExportDto` is gone, replaced by `remapped(CommandExportDto)` (lines 68-78):
    `var exportCopy = new CommandDtoUtils.CommandExportDto();`
    `exportCopy.setCommand(remapped(recordedExportDto.getCommand()));`
    `exportCopy.setResult(copyAndRemap(recordedExportDto.getResult()));`  (line 76)
    `return exportCopy;`
- The result is written to the freshly-created `exportCopy`, never to the input — the bug the v2 commit fixed
  is structurally impossible in main's rewrite. Correct behaviour preserved.
VERDICT: PRESENT.

---

## Other net-behavioural scan (44 commits)

The remaining ~35 commits are consolidation/renames (CommandExportManager/CommandReplayManager -> CommandManager
family; export/replay renames), explicitly in scope to ignore. `95ee45c0a18 introduces a little caching` and
`2a9d6bc848a` are covered above / are within the reconciled CommandManager/ResultRemapping rewrite on main
(commits 42b9ecbb433, 79b093c2cfb, and CommandManager consolidation). No additional un-ported net-behavioural
strand found beyond Strand 1.

## SUMMARY
- Strand 1 (mixin domain-event ISOLATION, d5cdc5da369): **MISSING** — highest-priority gap.
- Strand 2 (nav-action POST_PROCESS-only + re-entrancy guard): PRESENT (via CAUSEWAY-3910 42b9ecbb433).
- Strand 3 (record result of view model): PRESENT (via CAUSEWAY-3910 79b093c2cfb).
- Strand 4 (ResultRemappingService result-on-copy fix): PRESENT (rewrite makes bug impossible).
