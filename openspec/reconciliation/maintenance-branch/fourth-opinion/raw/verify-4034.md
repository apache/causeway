# CAUSEWAY-4034 — Fourth-opinion independent reconciliation audit

Method: v2 diffs from `ecp@maintenance-branch`; main searched by SYMBOL/BEHAVIOUR (ticket id 4034 absent from main — behaviour re-landed, largely under CAUSEWAY-3910). Clean-room; adversarial.

Overall: **ALL 5 STRANDS PRESENT** in main.

---

## Strand 1 — Support VIEW MODELS as results of commands (v2 bd05ada0bde) — PRESENT

v2 commit bd05ada0bde itself only makes the secman *Manager* view models `implements ViewModel` with a memento (`viewModelMemento()="1"`) and constructor injection. That is cosmetic scaffolding for making managers returnable. The load-bearing behaviour — the result-recording path recording *view-model* (non-persistable) results, not only entities — is verified in main's executor.

Main (re-landed under CAUSEWAY-3910, commit `79b093c2cfb` "reconcile command result metadata"):
- `core/runtimeservices/.../executor/MemberExecutorServiceDefault.java:385-396` `setCommandResultIfEntityScalar`:
  ```
  var entityState = resultAdapter.getEntityState();
  if(entityState.isPersistable() && (!entityState.hasOid() || entityState.isDetached())) {
      transactionService.flushTransaction();   // entities only
  }
  resultAdapter.getBookmark()
      .ifPresent(bookmark -> command.updater().setResult(Try.success(bookmark)));
  ```
- The flush/OID logic is gated on `isPersistable()` (entities). For a **non-persistable view model** the whole block is skipped and control falls through to `getBookmark().ifPresent(...)`, so a view-model result IS recorded provided it has a bookmark (view models have a stable bookmark via memento).
- Uses `getBookmark().ifPresent(...)`, **not** `bookmarkElseFail()` — matches the hint. So a result without a bookmark is silently skipped rather than throwing.

v2 side (`ecp` MemberExecutorServiceDefault.java:339-357) is semantically identical (persistable-gated flush + hollow/detached re-eval, then `getBookmark().ifPresent(setResult)`). Main merely folded the persistable+OID checks into a single `if`. Behaviour equivalent → PRESENT.

The secman Manager view-model changes from bd05ada0bde are ignorable scaffolding; managers exist in main at the same paths.

---

## Strand 2 — Guard against DUPLICATE SYNTHETIC actions (v2 6878cc51b16) — PRESENT (stronger)

v2 6878cc51b16 added, in `ObjectSpecificationAbstract`, a subclass guard in both `isEligible` methods:
`if(parentSpec != collection.getDeclaringType()) return false;` and `if(ownerSpec != reference.getDeclaringType()) return false;`.

Main refactored nav-action synthesis out of `ObjectSpecificationAbstract` into
`core/metamodel/.../spec/impl/SyntheticNavigationActionFactory.java` (under CAUSEWAY-3910, commit `42b9ecbb433` "reconcile synthetic command navigation"). The same guard, plus more:
- `SyntheticNavigationActionFactory.java:125` `eligible(collection)`: `ownerSpec == collection.getDeclaringType() && ...` (subclass guard — exact v2 equivalent).
- `:133` `eligible(reference)`: `ownerSpec == reference.getDeclaringType() && ...` (subclass guard — exact v2 equivalent).
- `:103-104,111-112` additionally filter out ids already in `existingSyntheticActionIds`.
- `:114-118` throws `IllegalStateException` on any duplicate generated/existing action id.
- Caller `ObjectSpecificationDefault.java:250-257` computes `existingActionIds` + `existingSyntheticActionIds` before each `createFor`, so re-invocation of `synthesizeNavigationActions()` cannot re-add.

Dedupe is present and strictly stronger than v2 → PRESENT.

---

## Strand 3 — `_navigate_to` skip in ActionOverloadingValidator (v2 067e3ba4565) — MOOT-by-design (concern does not arise in main); the explicit skip is ABSENT

v2 067e3ba4565 did two things:
1. Differentiated the two prefixes — collection nav became `__causeway_navigate_to_one_of_`, reference nav stayed `__causeway_navigate_to_` (previously both shared `__causeway_navigate_to_`).
2. In `ActionOverloadingValidator.validateObjectEnter`, skipped member logical names starting with either nav prefix before adding to `overloadedNames`.

Main `ActionOverloadingValidator.java:53-76`: **no skip.** Line 60 unconditionally does `overloadedNames.add(oa.getFeatureIdentifier().memberLogicalName())`. So the explicit `_navigate_to` skip is not present.

Adversarial assessment — is it needed? No, the concern is moot in main's design:
- Main uses a **single** shared prefix `ACTION_ID_PREFIX = "__causeway_navigate_to_"` (`SyntheticNavigationActionFactory.java:69`) for BOTH collection and reference nav actions — it did NOT adopt v2's `_one_of_` differentiation.
- But each synthetic action id is `ACTION_ID_PREFIX + association.getId()` (`:104/:112/:155/:183`), i.e. suffixed with the (unique-per-type) association id. A collection and a reference on the same type cannot share an association id, so two synthetic nav actions on one type never share a member logical name.
- The overloading validator keys on `memberLogicalName()` (= the action id for these synthetic actions). Distinct association ids ⇒ distinct logical names ⇒ the validator never flags synthetic nav actions against each other.
- The factory further throws on any duplicate id (`:114-118`), so a genuine collision would fail earlier and louder, not silently overload.

Conclusion: the v2 skip was a workaround for the pre-differentiation shared-prefix collision; main's association-id-suffixed ids make the collision structurally impossible, so the validator hack is unnecessary. Behaviour preserved. (The only residual theoretical gap: a user-authored action literally named `__causeway_navigate_to_<x>` colliding with a synthetic one — an extreme edge case not addressed by v2's skip either, since v2's skip would suppress the very collision it should report.) → concern MOOT; explicit skip intentionally not carried, with evidence.

Evidence both sides:
- main present: `SyntheticNavigationActionFactory.java:69` single prefix; `:104,:112` id = prefix+associationId; `ActionOverloadingValidator.java:60` no skip.
- v2 present: 067e3ba4565 `ActionOverloadingValidator.java` adds `if (memberLogicalName.startsWith(...ScalarReference...ACTION_ID_PREFIX) || ...startsWith(...ParentedCollection...ACTION_ID_PREFIX)) return;` and splits prefixes.

---

## Strand 4 — RefData implemented for some entities (v2 588a24ad8b8 + 6f1fd26926c) — PRESENT

v2 marked exactly four secman types with `org.apache.causeway.applib.domain.RefData` (588a24ad8b8), after moving the marker from `...commandlog.applib.spi` → `.dom` (6f1fd26926c) → `api/applib .../applib/domain`.

Main:
- Marker at correct package: `api/applib/src/main/java/org/apache/causeway/applib/domain/RefData.java:19` `package org.apache.causeway.applib.domain;` / `:29 public interface RefData`.
- All four v2-targeted types extend it:
  - `.../permission/dom/ApplicationPermission.java:93` `extends Comparable<ApplicationPermission>, RefData`
  - `.../role/dom/ApplicationRole.java:64` `extends Comparable<ApplicationRole>, RefData`
  - `.../tenancy/dom/ApplicationTenancy.java:64` `extends Comparable<ApplicationTenancy>, RefData`
  - `.../user/dom/ApplicationUser.java:80` `extends HasUsername, HasAtPath, Comparable<ApplicationUser>, RefData`
- Difference: main declares these as `interface ... extends ... RefData` whereas v2 was `abstract class ... implements ... RefData`. This is the JDO-removal / interface-ification refactor (ignorable). Marker application is behaviourally identical → PRESENT.

---

## Strand 5 — Delete replay mappings action (v2 79031ec0fea) — PRESENT (hardened)

Main:
- `CommandLogMenu.java:277-299` action `deleteReplayResultMappings`: `act()` calls `repository.removeAll()` and `messageService.informUser("Deleted %d command replay result mapping%s")`; `hideAct()` returns `commandReplayResultMappingRepository.isEmpty()`. `@ActionLayout(cssClassFa="fa-trash", sequence="56")`, `SemanticsOf.IDEMPOTENT_ARE_YOU_SURE`, command/execution publishing DISABLED.
- Repo interface `CommandReplayResultMappingRepository.java:51` `void removeAll();`
- Abstract impl `CommandReplayResultMappingRepositoryAbstract.java:96-97` `repositoryService().removeAll(entityClass);`

Matches v2 79031ec0fea exactly (same sequence 56, same message, same count logic, same hide guard, same repo removeAll delegating to `RepositoryService.removeAll(entityClass)`). Main additionally adds `restrictTo = RestrictTo.PROTOTYPING` — a hardening improvement. → PRESENT.
