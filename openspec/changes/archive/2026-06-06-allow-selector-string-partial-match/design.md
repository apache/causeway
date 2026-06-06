## Context

Synthetic parented collection selector actions validate and invoke by filtering the parented collection using provided scalar parameter values.
The selector must still identify exactly one child object before invocation is valid.
String-valued child properties are common human-readable filters, and partial text can be sufficient to select a unique child while making recorded navigation commands less brittle.

## Goals / Non-Goals

**Goals:**

- Use containment matching for provided string filter values.
- Keep exact equality matching for non-string scalar values.
- Keep null or absent optional filter parameters ignored as they are today.
- Keep the existing exactly-one-match contract for validation and invocation.
- Cover unique, no-match, and ambiguous-match outcomes for partial string matching.

**Non-Goals:**

- Do not change which child properties become selector parameters.
- Do not change parameter types, names, ordering, mandatory flags, defaults, or disabled state.
- Do not introduce viewer-specific wildcard syntax or regular expressions.
- Do not change matching for non-string scalar parameters.

## Decisions

- Implement the behavior in the selector matching predicate used by validation and invocation.
  This keeps validation and direct invocation consistent because both paths depend on the same collection matching semantics.
  The alternative was to special-case validation only, but that would allow direct invocation to behave differently from validated invocation.
- Treat string matching as simple `contains` semantics when both the supplied filter value and child property value are non-null strings.
  This satisfies the requested partial-match behavior while avoiding more complex wildcard or regex parsing.
  The alternative was prefix matching, but the requested behavior is containment rather than starts-with.
- Keep matching case sensitivity aligned with Java `String.contains` unless implementation already normalizes values elsewhere.
  This minimizes scope and avoids introducing locale-sensitive behavior.
  The alternative was case-insensitive matching, but that was not requested and can be proposed separately if needed.
- Preserve exact equality for non-string scalar parameters.
  This avoids surprising behavior for dates, numbers, booleans, enums, and other value types where partial matching is either ambiguous or not meaningful.

## Risks / Trade-offs

- [Risk] A string value that previously matched no child exactly may now match multiple children partially.
  Mitigation: keep the existing ambiguous-match validation and invocation failure behavior.
- [Risk] Case-sensitive containment may not match all user expectations.
  Mitigation: document and test containment behavior only; defer case-insensitive matching to a separate explicit change if required.
- [Risk] Matching code duplicated across validation and invocation could drift.
  Mitigation: implement the behavior in the shared matching helper or predicate used by both paths.
