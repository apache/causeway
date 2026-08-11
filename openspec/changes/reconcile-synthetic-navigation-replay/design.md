## Context

`SyntheticNavigationActionFactory` (a `record` since CAUSEWAY-4044) synthesizes one selector action per eligible
parented collection and one navigate-to action per eligible scalar reference. On HEAD both forms derive their id
from the same constant:

```
// core/metamodel/.../spec/impl/SyntheticNavigationActionFactory.java (HEAD 42ca10925fb)
:75   static final String ACTION_ID_PREFIX = "__causeway_navigate_to_";
:169  ACTION_ID_PREFIX + collection.getId(),   // collection selector
:196  ACTION_ID_PREFIX + reference.getId(),    // scalar reference
```

Maintenance (`ecp` @ `1683383878939`, CAUSEWAY-4038 commits `067e3ba4565`, `7a8d749f2b2`) distinguishes the forms:
`__causeway_navigate_to_one_of_<collectionId>` for a collection, `__causeway_navigate_to_<referenceId>` for a
reference. The id is serialized into the command DTO, so this is not a cosmetic rename — it is a wire-format
difference.

Replay resolves the DTO's member id by exact local lookup and throws on a miss:

```
// core/runtimeservices/.../command/CommandExecutorServiceDefault.java (identical to audited head)
:372-374   objectAction = specification.getAction(localActionId) … else throw "Unknown action '%s'"
:441-447   argAdaptersFor(actionDto): stream ParamDtos, recover each positionally by index
```

A collection selector's parameters are the collection's column **filter properties**. If the columns change
between recording and replay, the DTO's parameter list no longer aligns positionally with the current action's
parameters. Maintenance rebuilds them by parameter id / friendly name, padding absent filters
(`CommandExecutorServiceDefault.java:437-476`).

## Goals / Non-Goals

**Goals:**

- A synthesized parented-collection selector has id `__causeway_navigate_to_one_of_<collectionId>`.
- A command recorded against a collection selector replays on `main` even when the collection's filter columns
  were added, removed, or reordered after recording.
- Scalar-reference navigation is unchanged.

**Non-Goals:**

- No change to recording suppression, eligibility, layout metadata, disabled-state rules, or invocation
  semantics of the synthetic actions.
- No change to ordinary (non-synthetic) action replay, which remains positional.
- Not restoring the maintenance `ActionOverloadingValidator` skip: on `main` synthetic ids are association-id
  suffixed and the factory throws on a genuine collision, so the validator exemption is unnecessary
  (meta-analysis 2 "challenged claims"). Not adopting maintenance's lenient
  developer-authored-id behaviour: `main`'s throw at `SyntheticNavigationActionFactory:131-133` stays (accepted
  stricter v4 invariant, meta-analysis 2 D4).

## Decisions

### Adopt the maintenance collection id scheme (`one_of_` infix)

Change the collection branch to `ACTION_ID_PREFIX + "one_of_" + collection.getId()` and update the two
pre-existence/collision filters (`:119-120`) to match. This makes `main` both **record** and **synthesize** the
same id maintenance uses, so v2-recorded collection commands resolve on replay, and it disambiguates a collection
whose id equals a sibling reference id. References are untouched.

Rejected — teach the replay lookup to *also* accept the old single-prefix id: the id must round-trip through
recording too, so the metamodel must synthesize the maintenance form; a replay-only alias would leave `main`'s
own recordings on the wrong id.

### Bind synthetic-collection-navigation replay arguments by parameter identity, with padding

In `CommandExecutorServiceDefault`, detect a synthetic collection-navigation action (by the reserved
`one_of_` id form, or by the `ParentedCollectionNavigationFacet` on the resolved action) and, for it, match each
DTO `ParamDto` to a current action parameter by parameter id, falling back to friendly name; supply an
empty/no-op value for any current filter parameter with no corresponding DTO parameter. All other actions keep
positional `argAdaptersFor`.

### Parameter derivation refinements (G9, G11) — DEFERRED as accepted v4 adaptations

On re-verification against current HEAD, both are **not** ported:

- **G11 (member-order parameter ordering) conflicts with a deliberate, shipped Causeway 4 decision.** The
  `synthetic-command-navigation` spec already contains the requirement *"Selector action parameter prompts follow
  parented collection column order"* (with scenarios), and `SyntheticNavigationActionTest.collection_parameters_follow_columns_...`
  asserts exactly that order. Adopting maintenance's `byMemberOrderSequence` ordering would reverse that v4
  choice and break the test — for no replay benefit, because…
- **…the identity-based replay binding (above) makes parameter set/order irrelevant to replay.** A recorded
  command's parameters are matched to the current action by id/friendly name and padded, so the recording and
  replay environments need not derive the same parameter set or order. This subsumes **G9** (the extra
  `Where.REFERENCES_PARENT` exclusion) as well: whether or not such a property appears as a filter parameter,
  replay aligns by identity and pads. G9 would also risk changing Causeway 4's column-derived parameter set
  (`streamAssociationsForColumnRendering`), which is exercised by the same test.

Both are therefore recorded as accepted Causeway 4 adaptations (per the programme's adaptation policy) rather than
gaps. The meta-analyses rate both LOW severity and explicitly note they are subsumed by the argument-binding fix.
The P0 replay-compatibility behaviour (stable `one_of_` id + identity-based argument reconstruction) is delivered
in full.

### CAUSEWAY-4044 interaction

The factory is now a record and column/parameter selection flows through `_MembersAsColumns`; the id change is
applied within that structure (a new `COLLECTION_ACTION_ID_PREFIX` constant) rather than reintroducing the
pre-4044 shape, and the column-order parameter derivation is left intact (see the G9/G11 decision above).
`CommandExecutorServiceDefault` is unaffected by the merge (0-diff vs the audited head), so its replay-path edits
are as the audits described.

## Acceptance evidence

- Import an actual final-maintenance collection-navigation command DTO (id `__causeway_navigate_to_one_of_<id>`)
  and replay it successfully on `main`.
- Replay the same DTO against a metamodel whose collection filter columns have been added / removed / reordered,
  and assert the arguments bind by identity with padding (no "Unknown action", no positional mis-binding).
- `SyntheticNavigationActionTest` updated: collection selector id asserts the `one_of_` form; reference id
  unchanged; deterministic-id and collision scenarios still hold.
- A parameter-derivation test covering the `Where.REFERENCES_PARENT` hide and member-order ordering.
