## Context

Synthetic parented collection navigation actions are generated in the metamodel when command-log recording support is enabled.
The current factory builds the generated action with the parent object type as parameter zero, then appends child filter parameters derived from the associated collection columns.
That parent parameter is defaulted from the action target and disabled, which means callers cannot use it as true input.
Command recording already captures the action target separately from action parameters, so the generated command DTO does not need a duplicate parameter for the same object.

## Goals / Non-Goals

**Goals:**

- Generate synthetic navigate-to actions whose parameters contain only the child filter values needed to identify one collection element.
- Continue using the action target as the parent object for collection access during validation and invocation.
- Preserve existing child filter eligibility, naming, ordering, matching, and command result behavior.
- Update tests and OpenSpec requirements to describe the target-as-context contract.

**Non-Goals:**

- Do not change the synthetic action id prefix, display name, layout association, icon, CSS class, or publishing metadata.
- Do not change how child filters are selected from collection columns.
- Do not add compatibility shims for recordings that still include the old disabled parent parameter.

## Decisions

- Remove the parent object from the synthetic action parameter type and name arrays.
  This keeps the metamodel contract aligned with the recording contract, where the target is already represented as the command target.
  Alternative considered: keep the parent parameter hidden instead of disabled.
  That still leaves an extra parameter in command DTOs and replay mappings, so it does not solve the recording noise.

- Pass the action target into navigation matching instead of reading the parent from argument zero.
  Validation and invocation already receive an interaction head or target context, so the implementation should first double-check the available API and then use that target as the parent object while filter arguments are read from index zero onward.
  Alternative considered: synthesize a parent argument internally before calling the existing matcher.
  That would preserve more code but keep confusing index-offset semantics in tests and helper methods.

- Remove parent-parameter-specific facets from synthetic navigation actions.
  Defaults, choices, mandatory, and disabled facets for the old parent parameter become obsolete once the parameter no longer exists.
  Alternative considered: leave the facet classes unused for potential future reuse.
  The implementation can remove unused classes if no other code references them.

## Risks / Trade-offs

- Existing command recordings that include the old first parameter will no longer line up with the generated synthetic action parameter model.
  Mitigation: treat this as a breaking change and update replay fixtures or mappings to omit the duplicate target argument.

- Matching code must be carefully re-indexed so the first filter parameter is not skipped.
  Mitigation: add focused tests asserting parameter ids and validation/invocation behavior with filter arguments starting at index zero.

- Direct tests or tooling may have assumed a mandatory disabled parameter exists.
  Mitigation: update assertions to verify that no parent parameter exists and that the action still uses the target object for collection access.
