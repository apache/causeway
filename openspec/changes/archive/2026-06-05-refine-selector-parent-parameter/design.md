## Context

Synthetic parented collection selector actions use a mandatory first parameter typed as the collection owner.
The action is associated with the collection and is intended to be invoked in the context of a parent object, so that first parameter should be populated from the action target and should not be editable.
Handcrafted actions express equivalent parameter behavior through parameter default facets and disabled facets derived from `default...` and `disableParam...` support methods.

## Goals / Non-Goals

**Goals:**

- Provide a default value for the synthetic selector action’s first parent parameter using the current action target.
- Disable the first parent parameter so the user cannot change it in a prompt.
- Use the existing `ActionParameterDefaultsFacet` and `DisabledFacet` contracts consumed by viewers and metamodel tooling.
- Keep the parent parameter mandatory and typed as the collection owner.

**Non-Goals:**

- Do not disable scalar child filter parameters.
- Do not change selector invocation semantics or filtering rules.
- Do not change action identity, layout association, display name, or command publishing behavior.

## Decisions

### Default the parent parameter from the action target

Install an `ActionParameterDefaultsFacet` on parameter zero that returns the action target from the `ParameterNegotiationModel`.
This mirrors the effective behavior of a handcrafted default support method while keeping synthetic action construction annotation-free.
Returning the target also aligns the prompt with invocation semantics, where the collection is read from the supplied parent object.

Alternative considered: rely only on parent choices containing the current target.
Choices make the target selectable, but they do not pre-populate the prompt and still allow the user to pick a different parent.

### Disable only the parent parameter

Install a `DisabledFacet` on parameter zero with a clear reason that the parent is fixed by the selector action target.
This mirrors the effective behavior of a handcrafted `disableParam...` support method.
Scalar child filter parameters remain editable so recordings can provide enough values to identify exactly one child.

Alternative considered: omit the parent parameter entirely and always use the action target internally.
That would simplify the prompt, but it would change the command DTO parameter shape already established for selector actions.

## Risks / Trade-offs

- Some tooling might have expected all parameters to be editable.
  Mitigation: only parameter zero is disabled, and it represents contextual navigation rather than user-supplied selection criteria.
- Defaulting from the action target depends on the target being available in the parameter negotiation model.
  Mitigation: selector actions are associated with and invoked against the parent object, and tests should verify the default uses that target.
