## Context

Synthetic parented collection selector actions currently compute matching child rows inside the invocation facet.
The invocation facet returns the single match and throws an exception when there are no matches or multiple matches.

The requested behavior is to reject non-unique selector inputs before the action is invoked.
This aligns selector actions with ordinary Causeway action validation, where invalid parameter combinations are exposed through validation advisors rather than deferred to action execution.

## Goals / Non-Goals

**Goals:**

- Add an action validation facet for synthetic parented collection selector actions.
- Reuse the same matching semantics for validation and invocation so both paths agree on uniqueness.
- Return clear validation reasons for no-match and ambiguous-match cases.
- Preserve the existing invocation result for exactly one matching child row.
- Keep existing selector action ids, parameters, layout association, safe semantics, and command publishing behavior unchanged.

**Non-Goals:**

- Do not change how selector action parameters are synthesized.
- Do not make scalar selector parameters mandatory.
- Do not add command export or replay result-mapping behavior.
- Do not change ordinary collection row navigation.

## Decisions

- Implement validation as an `ActionValidationFacet` installed on each synthetic selector action.
  Alternative considered: keep throwing from `ActionInvocationFacetForParentedCollectionSelector` only.
  Validation is preferred because it prevents invocation and gives viewers and command execution flows a standard invalidity reason.

- Share row matching logic between validation and invocation.
  Alternative considered: duplicate matching in the validation facet.
  A shared helper keeps no-match, ambiguous-match, and exactly-one behavior consistent and reduces drift.

- Keep defensive invocation checks for non-unique matches.
  Alternative considered: assume validation always runs before invocation and remove invocation-time checks.
  Keeping the checks preserves safety for direct metamodel invocation paths and protects against callers that bypass validation.

- Treat missing or empty parent arguments as invalid input.
  Alternative considered: let invocation throw for a missing parent argument.
  Validation should report the missing parent when enough argument context is available because the selector cannot inspect a collection without a parent.

## Risks / Trade-offs

- Validation inspects the collection before invocation, which may duplicate collection traversal when invocation follows immediately.
  The expected selector collection sizes are user-facing collection rows, and correctness is more important than avoiding a second traversal.

- Validation can observe collection state that changes before invocation.
  Defensive invocation checks remain in place so stale validation cannot cause an incorrect child to be returned.

- Error wording may be asserted by tests or surfaced to users.
  Use stable, concise messages that distinguish no match from multiple matches without exposing internal implementation details.
