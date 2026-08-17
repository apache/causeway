## Context

The current rich schema generates inputs and wrappers from declared object specifications.
The reference application uses abstract Java entity bases, concrete JPA implementations with shared public logical types, collections declared against abstract bases, collection-valued action parameters, and memento view models.

Executable probes found five related failures.
The generated logical-type enum omitted concrete public entity names required by an abstract input.
An abstract-element collection omitted `get` because no concrete output type could be selected at schema construction.
Action validation adapted raw input maps as scalar values instead of unmarshalling object references.
Bulk parameter choices received no usable argument map.
Property mutation returned a `ManagedObject`, after which output resolution reconstructed unchanged memento state.

## Goals / Non-Goals

**Goals:**

- Make generated identity inputs complete for every supported concrete bookmarkable specification.
- Represent assignable concrete runtime object types through an introspectable polymorphic output.
- Convert every argument according to its declared metamodel type before semantic negotiation or execution.
- Return authoritative mutated state and refreshed view-model identity.
- Keep errors bounded, semantic, and non-disclosing.

**Non-Goals:**

- Adding arbitrary Java class names to public inputs.
- Exposing persistence implementation classes instead of public logical types.
- Adding collection windows or database pagination.
- Changing value formats addressed by the value-semantics proposal.
- Implementing browser interaction behavior.

## Decisions

### Build identity choices from accepted concrete specifications

The logical-type input vocabulary is built from concrete bookmark-addressable object specifications that the corresponding generated input can resolve.
An abstract declared type accepts only assignable public logical types.
The server validates the logical type before identifier lookup and does not reveal unauthorized object state through error details.

Existing enum names and accepted values remain stable.
Newly reachable logical types are additive.

### Generate a polymorphic output over concrete rich types

A declared abstract object type maps to a generated GraphQL union or equivalent polymorphic shape whose possible types are the assignable concrete rich object types.
The runtime type resolver uses the Causeway object specification and public logical type rather than Java implementation-name guesses.

Generic clients select `__typename`, target-introspect the concrete type, and request concrete fragments.
The existing concrete rich object shapes remain canonical and are not duplicated under a generic metadata object.

### Use one argument conversion pipeline

Action defaults, choices, autocomplete, per-parameter validation, all-arguments validation, and invocation consume values produced by the same declared-type conversion service.
The service recursively handles nullable values, scalar values, object-reference inputs, and collection-valued inputs.

Negotiation fetchers receive the same converted prefix arguments that invocation would receive.
Missing optional arguments remain distinct from explicit null.
Invalid, ambiguous, stale, or unauthorized object references produce GraphQL errors without assertion failures or raw Java collection dumps.

### Return the mutated pojo and its new identity

Property mutation applies visibility, usability, and validity checks to the resolved target and converted value.
After mutation it returns the target pojo in the output form expected by rich member fetchers.
For memento view models, `_meta.id` is generated from the updated state.
For persistent entities, existing bookmark identity behavior remains unchanged.

### Keep fixtures reduced and deterministic

Tests model one abstract entity family with concrete public logical types, one abstract-element collection, one object-list action with negotiation methods, one bulk collection parameter, and one editable memento view model.
The external reference application remains evidence and is not added to the normal build.

## Risks / Trade-offs

- [A polymorphic output can enlarge the schema] → Generate it only for declared abstract types that have reachable concrete rich types and verify schema-size deltas.
- [Adding enum values can expose type names] → Include only public logical types already authorized for schema exposure and test non-disclosure boundaries.
- [Shared conversion can change edge-case behavior] → Preserve successful scalar documents with compatibility tests and compare validation with invocation.
- [Memento identifiers can be large] → Reuse the established bookmark codec and test only that identity reflects authoritative updated state.

## Migration Plan

The identity vocabulary and polymorphic output are additive.
Existing concrete object queries and scalar action documents remain valid.
Collection negotiation and view-model mutation defects are corrected in place because their current successful-looking behavior is not semantically valid.

## Open Questions

- Whether GraphQL Java unions or interfaces produce the smaller and clearest generated shape for the current schema strategy.
- Whether unreachable concrete implementations should fail schema construction or emit one bounded startup diagnostic.
