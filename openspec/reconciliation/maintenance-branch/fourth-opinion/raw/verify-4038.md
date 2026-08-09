# CAUSEWAY-4038 — Fourth-opinion clean-room reconciliation audit

Adversarial verification of each strand of CAUSEWAY-4038 (v2 `maintenance-branch`) against `main`.

## Context / structural note

The synthetic parented-collection navigation feature did **not** exist at merge-base `65d64cd85b7`
(`ObjectSpecificationAbstract.java` exists there but has no `filterPropertiesOf`). It was introduced
**independently** on both branches:

- v2 (`ecp` / maintenance-branch): synthesis lives in
  `core/metamodel/.../specloader/specimpl/ObjectSpecificationAbstract.java`
  (inner class `ParentedCollectionNavigationActionUtil`).
- main (v4): synthesis was refactored out into
  `core/metamodel/src/main/java/org/apache/causeway/core/metamodel/spec/impl/SyntheticNavigationActionFactory.java`
  (landed via `42b9ecbb433 CAUSEWAY-3910: reconcile synthetic command navigation`).
  `ObjectSpecificationAbstract.java` no longer exists in main.

So this is a "diverged / partially reconciled" comparison, not a straight cherry-pick.

---

## Strand 1 — Deferred LayoutOrderFacet for the nav action (7a8d749f2b2) — **PRESENT**

v2 adds `LayoutOrderFacetForParentedCollectionNavigation` (defers the synthetic nav action's own
layout `sequence` to the associated collection's `LayoutOrderFacet`, fallback "1") and wires it in.

- v2: `core/metamodel/.../facets/actions/synthetic/LayoutOrderFacetForParentedCollectionNavigation.java`
  (new, 66 lines); wired in `ObjectSpecificationAbstract.installActionFacets` (+1 `FacetUtil.addFacet`).
- main: `core/metamodel/src/main/java/org/apache/causeway/core/metamodel/facets/actions/synthetic/LayoutOrderFacetForParentedCollectionNavigation.java`
  is **byte-for-byte identical** (66 lines, same fallback "1", same `getSequence()` delegating to
  `collection.lookupFacet(LayoutOrderFacet.class)`).
- Wired in `SyntheticNavigationActionFactory.createCollectionAction` line 163:
  `FacetUtil.addFacet(new LayoutOrderFacetForParentedCollectionNavigation(collection, facetedMethod));`

VERDICT: PRESENT (equivalent, class identical, wiring equivalent).

---

## Strand 2 — Hide nav-to params when property is hidden `Where.REFERENCES_PARENT` (db291cd28a5) — **MISSING / WRONG (semantic gap)**

v2 (final state, `ObjectSpecificationAbstract.filterPropertiesOf`) filters the child props with THREE
predicates, including a *standalone* REFERENCES_PARENT visibility filter:

```
.filter(visibleAccordingToHiddenFacet(Where.PARENTED_TABLES))
.filter(visibleAccordingToHiddenFacet(Where.REFERENCES_PARENT))   <-- db291cd28a5
.filter(referencesParent(parentSpec).negate())
```

Effect in v2: ANY property annotated `hidden = Where.REFERENCES_PARENT` is dropped from the nav-to
params, regardless of whether that property's type equals the parent spec.

main's equivalent path — `SyntheticNavigationActionFactory.filterPropertiesOf`
(`spec/impl/SyntheticNavigationActionFactory.java:214-226`) — delegates to
`collection.getElementType().streamAssociationsForColumnRendering(columnQuery)` with
`AssociationsLookup.AVAILABLE`. That resolves to `_MembersAsColumns.assembleAvailableColumns`
(`spec/impl/_MembersAsColumns.java:104-119`), which applies only:

```
.filter(visibleAccordingToHiddenFacet(columnQuery.where()))          // where()==PARENTED_TABLES
.filter(referencesParent(parentObject.objSpec()).negate())
.filter(assoc -> hideColumnUsingSpi(...))
```

There is **no** `visibleAccordingToHiddenFacet(Where.REFERENCES_PARENT)` filter anywhere in main's path.

Adversarial proof this is not subsumed:
- `ColumnQuery.where()` returns `Where.PARENTED_TABLES` for the parented case
  (`ObjectAssociationContainer.java:160-164`).
- `visibleAccordingToHiddenFacet(PARENTED_TABLES)` keeps an assoc unless its `HiddenFacet.where()`
  `.includes(PARENTED_TABLES)` (`ObjectAssociation.java:160-167`).
- Base `Where.includes` (`Where.java:226-230`): `context == this || (this.isAlways() && context.isAlways())`.
  → `REFERENCES_PARENT.includes(PARENTED_TABLES)` = `false`.
  So a property hidden `Where.REFERENCES_PARENT` PASSES main's PARENTED_TABLES visibility filter.
- main's `referencesParent(parentSpec).negate()` filter (`ObjectAssociation.java:170-184`) only drops
  a property when it is BOTH `hiddenWhereMatches(REFERENCES_PARENT)` AND its element type is
  `equivalent(parentSpec, childSpec)`. So a property hidden `REFERENCES_PARENT` whose type is NOT the
  parent's type is NOT dropped by main, but WOULD be dropped by v2's standalone REFERENCES_PARENT filter.

VERDICT: MISSING. Evidence — v2 `ObjectSpecificationAbstract.filterPropertiesOf` has
`.filter(visibleAccordingToHiddenFacet(Where.REFERENCES_PARENT))`; main
`_MembersAsColumns.assembleAvailableColumns` (`:110-116`) has no such filter, and
`REFERENCES_PARENT.includes(PARENTED_TABLES)==false` (`Where.java:226-230`) proves PARENTED_TABLES does
not subsume it. main only excludes such a property when it additionally references the parent type
(narrower behaviour than v2).

---

## Strand 3 — Standalone `target` property surfaced for ReplayableCommand (d80cda55d2f) — **MISSING**

v2 adds a `@Property getTarget()` on `ReplayableCommand` returning the stringified first-target Bookmark
(`OidsDto.getOid().get(0)` → `Bookmark.forOidDto(...).stringify()`), with
`@PropertyLayout(sequence="3.0", fieldSetId="details", hidden=Where.OBJECT_FORMS, describedAs="Target of the command")`,
plus a `target` line inserted into `ReplayableCommand.columnOrder.fallback.txt` (between `timestamp` and `member`).

- main `extensions/core/commandlog/applib/.../dom/replay/ReplayableCommand.java`: has **no** `getTarget()`
  `@Property`. Instead it has `@Programmatic getTargetType()` (`:192-199`) and `@Programmatic getTargetId()`
  (`:201-209`) — neither is surfaced as a property/column. The property sequence jumps `1.2` (timestamp,
  `:174-182`) → `3.1` (member, `:211-224`); there is no `3.0` target property.
- main `ReplayableCommand.columnOrder.fallback.txt` = `#interactionId / timestamp / member / replayState /
  hasResult / knownParticipants` — **no** `target` line.

Note (non-blocking): main represents the target adjacently via `getTargetType()`/`getTargetId()` and the
`ReplayableCommand_openTarget` / `_openTargetTR` mixins, but the specific single `target` bookmark-string
property + columnOrder line from d80cda55d2f is absent.

VERDICT: MISSING. Evidence — no `@Property getTarget()` in main `ReplayableCommand.java`; no `target`
line in `ReplayableCommand.columnOrder.fallback.txt`.

---

## Strand 4 — Ordering of navigate-to params (9937366e891, 0953577396f) — **WRONG (diverged approach)**

v2 FINAL ordering (net of the two commits: 0953577396f introduced a grid-occurrence comparator, then
9937366e891 reverted it) sorts child props by:

```
ObjectMember.Comparators.byMemberOrderSequence(false).thenComparing(ObjectAssociation::getId)
```

i.e. by `@PropertyLayout(sequence=...)` / member-order sequence, then id.

main orders the same params (`_MembersAsColumns.streamAssociationsForColumnRendering`,
`:75-100`) by:
- grid layout occurrence order via `propertyIdComparator` (`:130-164`) — first the props mentioned in the
  object's grid, in grid order; remaining props by natural id order — with LinkedHashMap declaration
  order as the base;
- SPI/patch reordering is **skipped** because `AssociationsLookup.AVAILABLE.isEnabled()==false`
  (`_MembersAsColumns.java:88`; `MetaModelService.AssociationsLookup.isEnabled()` `:228`).

There is **no** `byMemberOrderSequence` anywhere in main's navigation/columns path (grep returns nothing).

So main uses exactly the grid-occurrence comparator that v2 tried in the intermediate commit 0953577396f
and then DISCARDED in 9937366e891 in favour of member-order-sequence. The two branches converge on a
different final ordering.

VERDICT: WRONG (diverged). Evidence — v2-final `ObjectSpecificationAbstract.filterPropertiesOf` uses
`byMemberOrderSequence(false).thenComparing(getId)`; main `_MembersAsColumns.propertyIdComparator`
(`:130-164`) uses grid-occurrence-then-natural-id and no `byMemberOrderSequence` exists in main's path.
Whether this matters functionally depends on whether affected element types declare a grid; for
grid-less types main falls back to declaration order, v2 to member-order sequence.

---

## Padding sub-strand of 7a8d749f2b2 — replay-arg padding/reordering — **MISSING**

v2's 7a8d749f2b2 also reworks `CommandExecutorServiceDefault`:
- new constant `PARENTED_COLLECTION_NAVIGATION_ACTION_ID_PREFIX = "__causeway_navigate_to_one_of_"`;
- new overload `argAdaptersFor(ActionDto, ObjectAction)` that dispatches to
  `argAdaptersForParentedCollectionNavigation(...)` when the action id starts with that prefix;
- `argAdaptersForParentedCollectionNavigation` indexes ParamDtos by `name`, then walks the live action
  parameters, matching by `param.getId()` (fallback `getCanonicalFriendlyName()`), and **pads missing
  filters with `ManagedObject.empty(elementType)`** — so legacy/older DTOs with fewer or reordered
  params still align. Call site updated: `argAdaptersFor(actionDto, objectAction)`.
- new test `CommandExecutorServiceDefaultTest` (pads_and_reorders_legacy... ).

main `CommandExecutorServiceDefault.java`:
- call site (`:213`) is `argAdaptersFor(actionDto)` — the single-arg form only.
- only one `argAdaptersFor` method exists (`:441`, `private Can<ManagedObject> argAdaptersFor(ActionDto)`).
- **no** `PARENTED_COLLECTION_NAVIGATION_ACTION_ID_PREFIX`, no
  `argAdaptersForParentedCollectionNavigation`, no padding-by-name / by-id logic.
- no `CommandExecutorServiceDefaultTest` in `core/runtimeservices/src/test/.../command/`.

Also note: main's synthetic action id prefix is `__causeway_navigate_to_` (`SyntheticNavigationActionFactory.ACTION_ID_PREFIX`, `:69`) — a single prefix for both collection and reference nav actions — whereas v2's padding keyed off the more specific `__causeway_navigate_to_one_of_`. So even the id prefix the padding would key on differs.

VERDICT: MISSING. Evidence — main `CommandExecutorServiceDefault.java:213` uses `argAdaptersFor(actionDto)`
with no ObjectAction overload; no padding method / prefix constant present.

---

## Summary table

| Strand | Verdict | Note |
|---|---|---|
| 1. LayoutOrderFacet for nav action | PRESENT | class byte-identical, wired in `SyntheticNavigationActionFactory:163` |
| 2. Hide params on `Where.REFERENCES_PARENT` | MISSING | main lacks standalone REFERENCES_PARENT visibility filter; PARENTED_TABLES does not subsume it |
| 3. `target` property on ReplayableCommand | MISSING | no `@Property getTarget()`, no `target` in columnOrder fallback |
| 4. Nav-to param ordering | WRONG (diverged) | main uses grid-occurrence order; v2-final uses `byMemberOrderSequence` |
| (7a8d749) replay-arg padding | MISSING | no navigation padding branch in `CommandExecutorServiceDefault` |
