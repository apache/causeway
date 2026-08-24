# Versionless identity implementation report

## Implementation

Targeted object description now follows concrete object-valued `get` and collection-window `rows` types to their advertised metadata descriptions.
A shared `metadataSelectionForType(...)` helper derives the available subset of `id`, `logicalTypeName`, `title`, and `version`.
Direct interaction results, object-valued properties, preparation values, choices, autocomplete results, and concrete collection rows use that shared policy.

Versioned entity values retain `version`.
Versionless view models omit it while preserving identity, title, pending-value conversion, validation, object-link rendering, and row hydration.
Missing type descriptions and abstract results retain bounded typename-only selection, and an abstract collection row is rejected locally before a speculative metadata operation.
Runtime response checks still require `id` and `logicalTypeName` before creating a hydrated row context or publishing navigable identity.

## Reference Application correction

The deterministic `demo.CollectionLayoutPagedPage.children` window now reaches `ready` rather than a missing-version GraphQL error.
Its thirteen `demo.CollectionLayoutPagedChildVm` rows render as navigable semantic object links without `_meta.version`.
`moreChildren` also reaches `ready`.
Existing action choices and Action Semantics journeys continue to prepare and invoke concrete versionless values successfully.

The collection polymorphism journey remains visibly bounded according to its effective shape, and the long `demo.CompositeValuesPage` bookmark continues to assert `invalid-route`.
General union fragments and opaque route handling remain separate follow-on changes.

## Compatibility

No server GraphQL schema or generated name changed.
No public Causeway element, context method, event, result kind, route, history policy, HTMX behavior, Vaadin selection policy, dependency, CSP hash, or asset URL changed.
The correction is internal to targeted introspection and operation selection.
