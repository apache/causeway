## Why

Production diagnostics captured a 395-level metamodel introspection chain containing 395 distinct types, where mixed-in member discovery repeatedly fully introspected candidate mixins before checking whether they applied to the target type.
The chain is finite but exhausts the JVM stack, so mixin applicability must be determined using type-level metadata before member introspection is requested.

## What Changes

- Load each candidate mixin only through type introspection before reading its `MixinFacet` and evaluating applicability to the target type.
- Fully introspect a candidate mixin only after it is known to contribute an action, property, or collection to the target type.
- Apply the same two-stage loading rule to mixed-in action discovery and mixed-in association discovery.
- Preserve deterministic class-name traversal, existing mixin applicability semantics, member construction, explicit member ordering, and validation behavior.
- Add regression coverage showing that large sets of inapplicable mixins do not create a deeply nested full-introspection chain while applicable mixins still contribute all expected members.
- Retain the recently added introspection diagnostics to verify and guard the reduced nesting depth.

## Capabilities

### New Capabilities

- `mixin-introspection-prefiltering`: Determines mixin applicability from type-level metadata before requesting full member introspection, bounding avoidable recursive metamodel loading.

### Modified Capabilities

None.

## Impact

The change affects mixed-in action and association discovery in `ObjectSpecificationAbstract` and the metamodel tests that cover mixin contribution and navigation-action synthesis.
It introduces no new dependency, configuration property, public API, or domain-programming-model behavior.
Applicable mixins will still be fully introspected, while inapplicable mixins will remain type-introspected until another legitimate consumer requires their members.
