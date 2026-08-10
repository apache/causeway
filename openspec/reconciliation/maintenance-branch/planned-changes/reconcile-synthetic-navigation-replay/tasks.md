## 1. Re-verify the current-HEAD structure

- [ ] 1.1 Confirm the collection and reference id derivation sites in `SyntheticNavigationActionFactory` on HEAD
      and the current `_MembersAsColumns` column/parameter derivation entry points (post CAUSEWAY-4044).
- [ ] 1.2 Confirm `CommandExecutorServiceDefault` replay lookup (`findObjectAction`) and argument reconstruction
      (`argAdaptersFor`) are unchanged from the cited anchors.

## 2. Collection action id scheme

- [ ] 2.1 Change the parented-collection selector id to `__causeway_navigate_to_one_of_<collectionId>`; leave the
      scalar-reference id as `__causeway_navigate_to_<referenceId>`.
- [ ] 2.2 Update the pre-existence / synthetic-id collision filters to use the new collection id form; retain the
      throw-on-developer-collision behaviour.

## 3. Replay argument reconstruction

- [ ] 3.1 Add a synthetic-collection-navigation branch to replay argument reconstruction that matches DTO
      parameters to current action parameters by parameter id (fallback friendly name) and pads absent filter
      parameters with an empty/no-op value.
- [ ] 3.2 Keep positional reconstruction for all other actions.

## 4. Parameter derivation refinements

- [ ] 4.1 Exclude a filter property hidden at `Where.REFERENCES_PARENT` from the synthesized selector parameters.
- [ ] 4.2 Order the selector filter parameters by member-order sequence then id.

## 5. Tests

- [ ] 5.1 Update `SyntheticNavigationActionTest` for the `one_of_` collection id (reference id unchanged);
      keep deterministic-id and collision assertions.
- [ ] 5.2 Add a replay test that imports a final-maintenance collection-navigation DTO and replays it.
- [ ] 5.3 Add a replay test for a changed filter-column set (added/removed/reordered) asserting bind-by-identity
      with padding and no "Unknown action".
- [ ] 5.4 Add parameter-derivation tests for the `Where.REFERENCES_PARENT` hide and member-order ordering.

## 6. Verification

- [ ] 6.1 Run focused `core/metamodel` synthetic-navigation tests, `core/runtimeservices` command-executor tests,
      and the affected reactor under JDK 21, plus strict OpenSpec validation.
- [ ] 6.2 Confirm no change to reference navigation, recording suppression, eligibility, or command-log
      persistence.
