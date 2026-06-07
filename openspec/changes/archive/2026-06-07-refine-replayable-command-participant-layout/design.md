## Context

`ReplayableCommandParticipant` is a view model row that represents target, reference parameter, and result participants for a replayable command.
It already stores `owningInteractionId` in its memento so instances can identify the owning replayable command.
The current participant object view exposes too much technical data directly and does not tailor role-specific fields enough.

## Goals / Non-Goals

**Goals:**

- Add a derived `replayableCommand` property using the owning interaction id and replay context.
- Hide `owningInteractionId` while preserving it in the memento.
- Keep `logicalTypeName` available only in metadata.
- Add or refine the participant title so rows have a useful human-readable label.
- Replace the participant fallback layout with a three-column layout that separates identity, recorded-side information, and actual-side information.
- Hide `target`, `argument`, and `result` except for their matching participant roles.

**Non-Goals:**

- Do not change participant construction or memento format unless required to support the parent link.
- Do not change participant role names or bookmark mapping semantics.
- Do not change replay command participant collection ordering.
- Do not change persistence schema or replay mapping SPI signatures.

## Decisions

- Derive `replayableCommand` from `owningInteractionId` because the participant already carries that identifier and the parent is a view model keyed by interaction id.
- Hide `owningInteractionId` with property-level metadata instead of removing it because it remains useful for memento reconstruction and parent lookup.
- Keep `logicalTypeName` in a metadata fieldset because it is technical diagnostic information rather than business identity.
- Use hide support methods for role-specific object properties so irrelevant fields disappear from object details while the table column order remains stable.
- Keep the layout in fallback XML so applications without custom layout metadata get the improved default view.

## Risks / Trade-offs

- Parent derivation may need access to `ReplayContext` or another lookup service that is not currently held by the participant → Inject or pass the smallest required collaborator without changing the memento contract.
- Hiding role-specific properties can make table columns appear sparse differently than object details → Preserve existing column-order metadata and apply role-aware hiding primarily to object details.
- Adding layout XML can be brittle if component ids do not match property ids exactly → Validate with focused tests and an IntelliJ build.
