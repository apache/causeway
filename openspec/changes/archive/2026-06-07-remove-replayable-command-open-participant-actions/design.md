## Context

`ReplayableCommand` currently exposes the participants collection and command-level `openTarget`, `openArgument`, and `openResult` actions associated with that collection.
Each participant row already exposes object-valued target, argument, and result properties.
In the UI, those object-valued properties can be followed directly from the participant row, making the command-level open actions redundant.

## Goals / Non-Goals

**Goals:**

- Remove `ReplayableCommand_openTarget`, `ReplayableCommand_openArgument`, and `ReplayableCommand_openResult`.
- Remove the corresponding module imports and registrations.
- Remove tests that verify the redundant open actions.
- Update the participant UI spec to rely on participant row links for navigation.

**Non-Goals:**

- Do not remove the participants collection.
- Do not remove target, argument, result, recorded bookmark, actual bookmark, or parameter name participant properties.
- Do not change participant derivation, object lookup, replay mapping semantics, or replay state behaviour.
- Do not change persistence schema or replay mapping SPI signatures.

## Decisions

- Delete the mixin classes rather than hiding or deprecating them because the actions were introduced only as recent refinements and are now intentionally superseded by direct participant-row navigation.
- Keep object-valued participant properties unchanged so end-users can still open linked target, argument, and result objects from the table.
- Update tests by removing action-specific cases while preserving participant resolution tests that prove the linked objects remain available.

## Risks / Trade-offs

- Consumers that automated calls to the action mixins will need to navigate through participant object links instead → Mark the removal as breaking in the proposal.
- Removing action tests could reduce coverage of object lookup → Keep and rely on participant object resolution tests for target, argument, and result properties.
