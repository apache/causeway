## Why

The Reference Application regression suite proves that executable parameterless service actions and valid parameterized object actions can still end in the generic visible error `Action invocation failed`.
The semantic interaction contexts currently recognize the advertised capability but do not dispatch every nested safe-query and flat top-level mutation shape correctly, so this Priority 0 correctness defect must be removed before identity work, autocomplete paging, broader Vaadin adapters, or a default-policy flip.

## What Changes

- Make object and service action dispatch derive operation placement, argument placement, target identity, and result projection from targeted rich-schema introspection rather than assumptions about one generated shape.
- Correctly execute advertised nested safe or idempotent invocation fields and flat top-level mutation fields for parameterless and parameterized actions.
- Preserve action validation, typed codec values, mutation serialization, cancellation, stale-response protection, authoritative refresh, and typed object, collection, scalar, and void outcomes.
- Replace successful-looking generic dispatch failures with bounded mapped GraphQL or capability errors at the action scope while keeping submitted protected values undisclosed.
- Convert the pinned Reference Application action-choice and representative object-action known-gap journeys into successful semantic interaction assertions.
- Add focused foundation, GraphQL integration, HTMX, Petclinic, and Reference Application regression coverage for nested query and flat mutation operation shapes.
- Keep versionless identity and preparation, polymorphic union projection, opaque route handling, paged autocomplete, Vaadin family expansion, and the Vaadin-default policy outside this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Strengthens semantic object and service action invocation so every advertised nested safe-query or flat mutation shape is dispatched, normalized, refreshed, and reported correctly.

## Impact

The change affects the framework-neutral object and service action contexts, GraphQL operation construction and result selection, the standard interaction controller's bounded error mapping, and representative sample and Reference Application tests.
It does not rename public GraphQL fields or operations, add schema capabilities, change Causeway elements or semantic event names, alter canonical routes, add dependencies, or change Vaadin packaging, CSP, lazy loading, or default-selection policy.
