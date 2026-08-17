## Why

The executable reference-application analysis confirmed that several generated rich GraphQL object contracts exist in the schema but cannot be used correctly.
Abstract entity identities cannot name their concrete public logical types, abstract-element collections omit their value field, collection-valued validation receives raw maps, bulk choices fail, and a valid memento view-model property mutation returns unchanged state.
These are correctness defects in established object interaction paths and should be fixed before adding broader value, collection, or composition capabilities.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-OBJECT-02`, `REF-PROP-02`, `REF-ACTION-03`, `REF-ACTION-04`, and `REF-COLLECTION-03`.

## What Changes

- Make every concrete bookmark-addressable logical type accepted by generated rich object inputs discoverable and usable.
- Add a polymorphic rich output contract for collections or results declared using an abstract domain-object type.
- Route scalar, object, and collection-valued action arguments through one declared-type unmarshalling pipeline before defaults, choices, autocomplete, validation, or invocation.
- Make property mutation return the mutated domain object rather than a wrapper or stale memento reconstruction.
- Produce bounded GraphQL validation or coercion errors instead of HTTP 500 assertion failures for malformed object or collection inputs.
- Add reduced deterministic fixtures derived from the reference-application cases.

## Capabilities

### New Capabilities

- `rich-graphql-object-interaction-correctness`: Defines correct identity, polymorphic output, argument unmarshalling, and property mutation behavior for rich GraphQL domain objects.

### Modified Capabilities

None.

## Impact

- Affects rich input generation, logical-type enumeration, abstract output mapping, action argument conversion, action negotiation fetchers, property mutation, errors, tests, and documentation.
- Preserves existing generated concrete object types and established successful scalar interaction documents.
- Is a prerequisite for complete value semantics, collection windowing over abstract element types, menu service actions with collection parameters, composite object editing, and the generic viewer.
- Does not add collection pagination, new scalar formats, member metadata, or web components.
