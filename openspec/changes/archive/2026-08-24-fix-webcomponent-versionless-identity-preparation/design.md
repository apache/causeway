## Context

The foundation already derives object-header metadata fields from the advertised rich metadata wrapper, and the action-dispatch correction made action result and parameter-state selections type-aware.
Two older projection paths remain unconditional: object-valued property reads and collection-row reads always request `_meta { id logicalTypeName title version }`.
The pinned Reference Application proves that view models including `DependentArgsDemoItem`, `ActionSemanticsVm`, and `CollectionLayoutPagedChildVm` legitimately omit `version` while retaining `id`, `logicalTypeName`, and `title`.
Those reads currently fail GraphQL validation before existing identity, editor, cancellation, and row-hydration policies can run.

Identity and optimistic concurrency are distinct concerns.
A versionless object can still have a stable logical type and identifier suitable for values, choices, hydration, and navigation, while a mutation that requires an advertised concurrency token must not invent one.
The GraphQL schema remains authoritative, strict CSP and route-lazy delivery remain unchanged, and this change must not absorb union or opaque-route corrections.

## Goals / Non-Goals

**Goals:**

- Request only metadata fields advertised by the effective concrete object metadata type.
- Preserve `version` for versioned entities and omit it for versionless view models.
- Reuse one metadata-selection policy across property references, direct action/preparation values, and collection rows.
- Require `id` and `logicalTypeName` before claiming navigable or hydratable semantic identity.
- Prove preparation, choices, autocomplete, object values, and collection rows against versionless Reference Application targets.
- Preserve cancellation, stale-response, refresh, error, redaction, and native/Vaadin boundaries.

**Non-Goals:**

- Adding a nullable version or concurrency field to rich GraphQL.
- Defining optimistic-concurrency behavior for a mutation that explicitly requires an unavailable token.
- Selecting metadata from interfaces or unions through fragments.
- Changing opaque bookmark route encoding, decoding, or length limits.
- Changing action placement, reference paging, Vaadin adapters, dependencies, or public browser contracts.

## Decisions

### Centralize concrete metadata selection

A shared selection helper will inspect the effective object type, locate its `_meta` field, inspect the referenced metadata type, and select the advertised subset of `id`, `logicalTypeName`, `title`, and `version`.
Callers will use this helper instead of constructing metadata selections independently.

This extends the introspection-driven result-selection approach introduced by action dispatch and avoids maintaining a second list of versionless types.
The rejected alternative is to remove `version` globally, because that would discard an advertised concurrency value from ordinary entities and weaken existing refresh behavior.

### Keep semantic identity minimums explicit

A value is navigable or suitable for hydrated row context only when the effective metadata advertises both `id` and `logicalTypeName` and the response supplies both values.
`title` remains optional with the established identifier fallback, and `version` remains optional.
If the minimum fields are absent, the projection returns bounded non-navigable data or an existing local unsupported/error state rather than manufacturing identity.

The rejected alternative is to infer logical type from the enclosing member or generated GraphQL name, because polymorphism and aliases make that speculative.

### Merge row identity with authored columns

Collection row selection will start with the concrete row type's advertised metadata selection and then merge declared semantic column selections.
Versionless rows therefore retain identity and row hydration, while rows without concrete metadata remain bounded and await the separate union-projection change.
Windowing, offsets, ordering, partial errors, and lazy activation remain unchanged.

### Treat preparation as a regression boundary

Action and property preparation paths already route nested result selection through described types after the action-dispatch change.
This change will add focused fixtures and Reference Application assertions to prove that versionless defaults, choices, autocomplete results, and valid pending values remain usable without reintroducing unconditional metadata.
Production changes will be limited to any remaining unconditional paths revealed by those tests.

### Preserve schema and host boundaries

No public GraphQL operation or metadata field changes.
No route, event, custom-element, HTMX, Vaadin, CSP, or asset policy changes.
Capability inventory classifications change only when executable evidence changes, and unrelated union and opaque-route gaps remain visible.

## Risks / Trade-offs

- [A helper receives an incomplete type graph] → Return a bounded `__typename` or unsupported projection and add focused missing-description tests rather than speculating about metadata.
- [Versionless identity is mistaken for mutation concurrency] → Keep identity extraction separate from target-input construction and never manufacture `version`.
- [Collection column merging creates invalid nested selections] → Derive the base row selection from the concrete element type and retain existing member-wrapper column grammar.
- [Fixing concrete rows masks union failures] → Keep union targets classified and asserted as known gaps until fragment projection is implemented separately.
- [A stale Reference Application target changes upstream] → Pin tests to the copied corpus and validate the semantic target catalogue against introspection.

## Migration Plan

The change is a client-side projection correction with no persisted-data or schema migration.
Deploy through the existing same-origin foundation artifact and verify both versioned and versionless paths.
Rollback is a source revert with no application configuration or route migration.
