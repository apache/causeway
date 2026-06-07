## Context

Synthetic parented collection selector actions are currently created when command-log recording support is enabled and the collection association is eligible.
The action validation facet already prevents invocation when no child object matches the supplied parameters.
However, for an empty collection there can never be a matching child, so the action should be disabled before the user opens or submits it.

## Goals / Non-Goals

**Goals:**

- Disable each synthetic selector action dynamically when its associated collection is empty for the current action target.
- Keep selector actions enabled for non-empty collections so existing selection, filtering, validation, and invocation behavior is unchanged.
- Use the standard action disabled facet mechanism so viewers and command recording surfaces consume the behavior normally.
- Cover empty and non-empty collection usability with metamodel tests.

**Non-Goals:**

- Hide selector actions for empty collections.
- Remove selector actions from the metamodel when collections are empty.
- Change no-match or ambiguous-match validation messages.
- Change selector parameter discovery, matching, return values, styling, or command publishing.
- Add viewer-specific rendering logic.

## Decisions

- Add a dedicated disabled facet for synthetic parented collection selector actions.
  The parent parameter already has its own disabled facet, but action-level empty-collection usability is a separate concern and should be represented on the action itself.
- Evaluate emptiness from the current action target and the selector facet's associated collection.
  This keeps the behavior dynamic and avoids making collection size part of metamodel synthesis.
- Disable only when the collection has no elements.
  For non-empty collections, validation remains responsible for checking whether the supplied filter values identify exactly one child.
- Return a clear disabled reason such as `No items to select.`.
  The exact text can be concise because detailed no-match feedback remains in action validation after parameters are supplied.

## Risks / Trade-offs

- [Risk] Checking emptiness may evaluate the collection when rendering action usability.
  → Mitigation: selector validation and invocation already evaluate the collection, and the check only needs to determine whether any item exists.
- [Risk] Some collections may be lazily loaded or computed.
  → Mitigation: the disabled facet should use the same metamodel collection access path as selector invocation rather than introducing a separate mechanism.
- [Risk] Empty collection disabled state may change as data changes.
  → Mitigation: evaluate dynamically for the current target each time action usability is requested.
