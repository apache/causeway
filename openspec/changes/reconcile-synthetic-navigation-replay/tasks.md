## 1. Re-verify the current-HEAD structure

- [x] 1.1 Confirm the collection and reference id derivation sites in `SyntheticNavigationActionFactory` on HEAD
      and the current `_MembersAsColumns` column/parameter derivation entry points (post CAUSEWAY-4044).
- [x] 1.2 Confirm `CommandExecutorServiceDefault` replay lookup (`findObjectAction`) and argument reconstruction
      (`argAdaptersFor`) are unchanged from the cited anchors.

## 2. Collection action id scheme

- [x] 2.1 Change the parented-collection selector id to `__causeway_navigate_to_one_of_<collectionId>`; leave the
      scalar-reference id as `__causeway_navigate_to_<referenceId>`.
- [x] 2.2 Update the pre-existence / synthetic-id collision filters to use the new collection id form; retain the
      throw-on-developer-collision behaviour.

## 3. Replay argument reconstruction

- [x] 3.1 Add a synthetic-collection-navigation branch to replay argument reconstruction that matches DTO
      parameters to current action parameters by parameter id (fallback friendly name) and pads absent filter
      parameters with an empty/no-op value.
- [x] 3.2 Keep positional reconstruction for all other actions.

## 4. Parameter derivation refinements — DEFERRED (accepted v4 adaptations)

- [x] 4.1 **Decided not to port (G9).** Excluding a `Where.REFERENCES_PARENT`-hidden filter property is subsumed by
      the identity-based replay binding (§3) and risks changing Causeway 4's column-derived parameter set; deferred
      as an accepted v4 adaptation. Recorded in `design.md`.
- [x] 4.2 **Decided not to port (G11).** Ordering by member-order sequence would contradict the existing,
      deliberately-spec'd Causeway 4 requirement "Selector action parameter prompts follow parented collection
      column order" and its test, and is moot for replay given identity-based binding; deferred as an accepted v4
      adaptation. Recorded in `design.md`.

## 5. Tests

- [x] 5.1 Update `SyntheticNavigationActionTest` for the `one_of_` collection id (reference id unchanged);
      keep deterministic-id and collision assertions.
- [x] 5.2 Add a focused replay-argument test proving identity-based binding (by id, then friendly name) with
      padding for a changed/removed/reordered filter-column set.
- [x] 5.3 (Folded into 5.2 — the argument-reconstruction test exercises added/removed/reordered columns.)
- [x] 5.4 (Not applicable — parameter-derivation refinements deferred per §4.)

## 6. Verification

- [x] 6.1 Run focused `core/metamodel` synthetic-navigation tests, `core/runtimeservices` command-executor tests,
      and the affected reactor under JDK 21, plus strict OpenSpec validation.
- [x] 6.2 Confirm no change to reference navigation, recording suppression, eligibility, or command-log
      persistence.
