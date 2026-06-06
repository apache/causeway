## Context

Synthetic parented collection selector actions are built during metamodel startup in `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil`.
They currently discover visible child one-to-one associations, keep only value types, and pass that ordered association list through synthetic action creation, validation, and invocation facets.
That value-type-only rule excludes reference properties even when the programming model has installed facets that make the reference selectable through a bounded list, explicit choices, or autocomplete.

The implementation must remain startup-safe.
It should continue to use facets already present on the child property or referenced type, and it should not invoke viewer services, table-column SPIs, grid normalization, repositories, or user-specific services while synthesizing actions.

## Goals / Non-Goals

**Goals:**

- Include child reference properties as optional selector parameters when a selectable-value facet is installed.
- Keep existing scalar parameter behavior, ordering, naming, optionality, and technical-property exclusions.
- Match supplied reference parameter values by exact object equality, while preserving partial contains matching for strings.
- Cover bounded, choices, autocomplete, and unconstrained reference cases with metamodel tests.

**Non-Goals:**

- Do not synthesize selector parameters for arbitrary child collections or unconstrained references.
- Do not add new configuration properties.
- Do not change the command DTO shape or export/import schema.
- Do not call choice or autocomplete providers during selector synthesis.

## Decisions

- Rename the internal concept from `scalarProperties` to a broader `filterProperties` or equivalent.
  This keeps one ordered association list flowing through parameter creation, validation, and invocation while allowing both value properties and selected reference properties.
  Alternative considered: maintain separate scalar and reference lists, but that would complicate parameter index alignment without adding useful behavior.

- Treat a reference property as eligible when it is one-to-one, non-value, non-collection, visible in parented tables, not technically excluded, and has a selectable facet.
  Selectable means the property has `PropertyChoicesFacet` or `PropertyAutoCompleteFacet`, or the referenced type has the object-value `ChoicesFacet` installed by bounded semantics.
  Alternative considered: inspect annotations directly, but facet checks are less coupled to annotation sources and match the existing metamodel design.

- Check only for facet presence during synthesis.
  The selector only needs to know that a viewer can present a controlled selector; it must not evaluate choices or autocomplete results during metamodel startup.
  Alternative considered: verify that choices/autocomplete return values, but that could invoke domain code, depend on runtime state, and violate startup safety.

- Keep matching centralized in `ParentedCollectionSelectorMatchingUtil`.
  Strings continue to use contains matching, and all other values, including references, use exact equality after unwrapping managed objects.
  Alternative considered: add specialized bookmark or identity matching for references, but command replay should already reconstruct reference arguments as domain objects before selector matching.

## Risks / Trade-offs

- Reference equality may be sensitive to proxies or detached instances → Use the existing `ManagedObject` unwrapping path and add tests using the same object instances created by the metamodel fixture.
- Bounded-type detection may use a facet installed on the referenced specification rather than the property → Encapsulate selectable-reference eligibility in a helper so the facet lookup can check both the property and element type consistently.
- More parameters may make selector actions broader than before → Limit references to selectable facets and keep hidden, blob/clob, collection, and technical exclusions.
- Existing tests and helper names are scalar-specific → Update names carefully while preserving behavior and adding regression coverage for existing scalar cases.
