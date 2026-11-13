> **Execution note:** this slice touches subtle metamodel synthesis (`SyntheticNavigationActionFactory`) and
> runtime replay-argument reconstruction (`CommandExecutorServiceDefault`). It should ideally be implemented with
> **`high` reasoning effort** rather than the default.

## Why

Child change 2 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-5**;
third-opinion F1 HIGH, fourth-opinion G8–G11). Both meta-analyses rate this P0 and resolve it against code:
the third opinion was correct, the second understated it as "PRESENT", and the fourth's "id collision is moot"
answered a different (intra-type) question.

Maintenance-recorded synthetic **collection**-navigation commands cannot be replayed on `main`, for two
independent reasons:

1. **Action-id mismatch.** Maintenance distinguishes the two synthetic navigation forms by id —
   `__causeway_navigate_to_one_of_<collectionId>` for a parented collection, and
   `__causeway_navigate_to_<referenceId>` for a scalar reference. `main` uses a single reserved prefix
   `__causeway_navigate_to_` for **both** forms (`SyntheticNavigationActionFactory.java:75`, collection id at
   `:169`, reference id at `:196`). Because the action id is serialized into the command DTO, a command recorded
   against a maintenance (v2) collection selector names an id that `main` never synthesizes; replay does an exact
   local-id lookup and throws `"Unknown action '%s'"` (`CommandExecutorServiceDefault.java:372-374`).
2. **Positional argument reconstruction.** Even with the id repaired, `main` rebuilds action arguments
   **positionally** from the DTO (`CommandExecutorServiceDefault.java:441-447`, `argAdaptersFor`), with no
   synthetic-navigation compatibility branch. A parented-collection selector's parameters are its column filter
   properties, which change when collection columns are added, removed, or reordered between recording and replay.
   Maintenance recognises the generated collection-navigation action and rebuilds arguments by matching DTO
   parameters to current action parameters **by parameter id / friendly name, padding missing filter parameters**
   (maintenance `CommandExecutorServiceDefault.java:437-476`).

This change makes `main` synthesize and replay collection-navigation commands compatibly with maintenance. It is
re-anchored to current HEAD: the CAUSEWAY-4044 merge refactored `SyntheticNavigationActionFactory` into a record
and reworked the member-catalog/`_MembersAsColumns` path, but the single-prefix defect and the positional replay
remain, and `CommandExecutorServiceDefault` is byte-identical to the audited head.

## What Changes

- Change the synthetic **collection** selector action id to the reserved form
  `__causeway_navigate_to_one_of_<collectionId>` (insert the `one_of_` infix); leave the scalar **reference**
  navigation id as `__causeway_navigate_to_<referenceId>`. Update the collision/pre-existence checks accordingly
  (`SyntheticNavigationActionFactory` `:119-120`, `:127-128`). The developer-authored-id collision behaviour
  (throw at `:131-133`) is retained unchanged (an accepted stricter v4 invariant — meta-analysis 2 D4).
- In `CommandExecutorServiceDefault`, add a synthetic-collection-navigation replay path that binds DTO parameters
  to the current action's parameters by stable parameter id (falling back to friendly name) and pads any
  parameter present on the current action but absent from the DTO with an empty/no-op filter value, instead of
  binding positionally. Ordinary (non-synthetic) actions keep positional binding.
- Fold in the two lower-severity 4038 sub-strands: hide a filter property that is hidden at
  `Where.REFERENCES_PARENT` (G9), and order the selector's filter parameters by member-order sequence then id
  (G11) rather than purely by grid occurrence, so a maintenance-recorded parameter set aligns.
- No change to the reference-navigation id, to recording suppression, to eligibility, or to any command-log
  persistence.

## Capabilities

### Modified Capabilities

- `synthetic-command-navigation`: the parented-collection selector action id gains the `one_of_` infix, and a new
  requirement guarantees that a recorded synthetic collection-navigation command remains replayable when the
  collection's filter columns have changed (bind-by-parameter-identity with padding), including the
  `Where.REFERENCES_PARENT` hide and member-order parameter derivation.

## Impact

- Affects `core/metamodel` `SyntheticNavigationActionFactory` (collection id scheme, parameter derivation) and
  `core/runtimeservices` `CommandExecutorServiceDefault` (replay argument reconstruction for synthetic
  collection-navigation actions).
- **Compatibility:** adopting the maintenance id scheme is required for cross-version replay — a v2 upgrade
  carries command-log entries whose DTOs already contain `__causeway_navigate_to_one_of_<collectionId>`. `main`
  has not shipped these ids, so there are no in-field `main` DTOs to break.
- Requires a regression that imports an **actual final-maintenance collection-navigation DTO** — including one
  whose filter parameter set differs from the current metamodel — and replays it successfully; plus the existing
  `SyntheticNavigationActionTest` cases updated for the new collection id.
