## Context

Synthetic parented collection navigation actions are created in the metamodel for eligible parented collections when command-log recording support is enabled.
They expose filter parameters derived from child properties that are rendered as columns of the associated collection, and validation/invocation matches supplied parameter values against child objects.

The current generic boolean value semantics default mandatory boolean parameters to `false`, which makes a mandatory checkbox parameter behave as though the caller selected `false` even when no filter was intended.
The relevant implementation is in `ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil` and the matching facets under `core/metamodel/.../facets/actions/synthetic`.

## Goals / Non-Goals

**Goals:**

- Represent generated boolean selector parameters as optional tri-valued filters with an unselected state, explicit `true`, and explicit `false`.
- Ensure unselected boolean parameters do not constrain the parented collection match.
- Ensure explicit `true` and explicit `false` boolean parameters constrain the match by exact boolean equality.
- Ensure the generated parameter list preserves the same order as the associated parented collection columns.
- Cover the behavior with focused metamodel unit tests.

**Non-Goals:**

- Change ordinary developer-authored action parameter boolean semantics.
- Change collection rendering, row navigation, or scalar reference navigate-to actions.
- Introduce new configuration properties or dependencies.
- Change non-boolean scalar and reference filter matching semantics.

## Decisions

- Keep boolean selector parameters as generated action parameters but force them to remain optional.
  This keeps the synthetic action shape compatible with the existing selector-action mechanism while avoiding mandatory checkbox defaults.
  The alternative was to introduce a custom parameter type, but that would add viewer and serialization surface area for a targeted metamodel concern.

- Treat absence, null, unspecified, or empty boolean arguments as no predicate in the existing matching utility.
  This aligns boolean filters with existing optional scalar/reference filter behavior and preserves explicit `false` because only empty/unspecified arguments are skipped.
  The alternative was to add boolean-specific validation logic in the action facet, but centralizing it in matching keeps validation and invocation consistent.

- Derive parameter order from the collection column order before applying eligibility filters.
  This makes generated prompts match the user's visual model of the collection.
  The alternative was to continue sorting by child member order, but that can diverge from collection column order and produce confusing prompts.

- Add regression tests at the metamodel layer where synthetic action generation and matching are already exercised.
  This avoids a broader viewer-level test and keeps the fix focused on the generated action contract.

## Risks / Trade-offs

- Existing recordings may have assumed an omitted mandatory boolean meant `false` → Existing recorded commands that explicitly pass `false` should continue to filter by `false`; commands that omitted the value will become unconstrained, which matches the intended optional prompt semantics.
- Collection column order metadata can be assembled from more than one source → Tests should cover the static collection-column-order path already used by the existing parented collection navigation tests.
- Viewer prompt rendering may still display boolean parameters differently depending on optionality support → Metamodel tests should assert the generated parameter is optional and that null/empty arguments are ignored during matching.
