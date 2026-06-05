## Context

Synthetic parented collection selector actions are generated in the metamodel when the command-log extension opt-in is enabled.
The current parameter generation uses each child scalar property as an optional equality filter.
That is useful for ordinary scalar fields, but blob and clob properties can be large and expensive to compare.
Technical properties named `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` are also poor user-facing filters and should not appear in selector action prompts or command DTO parameters.

## Goals / Non-Goals

**Goals:**

- Keep the existing parent selector parameter unchanged.
- Continue exposing ordinary scalar child properties as optional selector filters.
- Exclude blob and clob value properties from selector filters before parameter types and names are derived.
- Exclude child properties named `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` from selector filters before parameter types and names are derived.
- Ensure validation and invocation receive the same filtered property list used to define the action signature.

**Non-Goals:**

- Do not change selector action ids, names, layout association, or enablement configuration.
- Do not change ordinary collection rendering or direct row navigation.
- Do not add command export or replay result-mapping behavior.
- Do not attempt to infer uniqueness or indexing quality for arbitrary scalar values.

## Decisions

- Add a single selector-parameter eligibility predicate alongside `scalarPropertiesOf`.
  This keeps parameter generation, matching, validation, and invocation aligned because all downstream code already consumes the same `Can<ObjectAssociation>`.
  The alternative was to filter only at DTO or viewer rendering time, but that would leave hidden parameters in the action signature and could still perform expensive matching.

- Treat blob and clob exclusion as value-type based rather than annotation based.
  This targets the actual large value semantics regardless of how the property is declared.
  The alternative was to check names or annotations, but that would be less reliable and could miss framework-provided blob or clob value types.

- Treat `logicalTypeName`, `id`, `version`, `objectIdentifier`, `datanucleusVersionLong`, and `datanucleusVersionTimestamp` exclusion as exact property-id based.
  This matches the metamodel identifiers used for generated action parameter names and avoids changing similarly named business properties such as `externalId` or `versionLabel`.
  The alternative was a broader substring or case-insensitive rule, but that could remove legitimate domain filters unexpectedly.

## Risks / Trade-offs

- Existing recorded selector commands that include newly excluded parameters may no longer match the generated action signature after the change.
  Mitigation: the selector action feature is still opt-in and recent, and the removed parameters were not intended to be stable user-facing filters.

- A domain model may have a business property exactly named `id` or `version` that would otherwise be useful for selection.
  Mitigation: the requirement intentionally favors removing technical metadata from synthetic prompts, and other business scalar fields remain available.

- Blob or clob detection depends on metamodel value-type information being available during selector action creation.
  Mitigation: use the same element type metadata already required for scalar parameter generation and cover the behavior with focused metamodel tests.
