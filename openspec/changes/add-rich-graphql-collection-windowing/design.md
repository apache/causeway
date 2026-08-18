## Context

`RichCollectionGet` currently derives a list output type and adds no paging or ordering arguments.
The web-component context can issue secondary collection operations, but those operations still receive the full list from GraphQL.
The reference probe requested `CollectionLayoutPagedPage.children`, whose presentation page size is five, and received all 13 rows from an unargumented `get` field.

The current Causeway collection association is generally materialized before GraphQL sees it.
A cursor would therefore imply stability or persistence efficiency that the present programming model cannot guarantee.
An additive offset window provides a bounded response with honest per-request consistency.

## Goals / Non-Goals

**Goals:**

- Bound collection response rows deterministically.
- Publish enough metadata for previous, next, and range presentation.
- Preserve supported configured ordering before slicing.
- Define per-request consistency and concurrent-change behavior.
- Retain existing clients through an additive compatibility policy.

**Non-Goals:**

- Guaranteeing database-level paging for every domain collection.
- Claiming stable cursor continuation across mutations.
- Adding arbitrary unrestricted client sorting or filtering.
- Defining the collection web-component UI.
- Treating `@CollectionLayout(paged=...)` as a transport guarantee.

## Decisions

### Add a dedicated offset window

Each eligible rich collection wrapper keeps its established `get` list and adds a `window(offset, size)` operation.
`offset` is zero-based and defaults to zero.
`size` is positive, required or given a documented bounded server default, and cannot exceed a configured hard maximum.

The result contains rows, requested offset, returned count, nullable total count, `hasPrevious`, and `hasNext`.
An offset beyond the current authorized collection returns an empty window at that requested offset rather than silently substituting the last page.

### Apply deterministic ordering before slicing

A supported Causeway comparator or ordering facet is applied consistently before offset selection.
Where deterministic ordering cannot be established, the result reports per-request ordering limitations and does not imply cross-request positional stability.
Arbitrary GraphQL field-name sorting remains excluded.

### Describe count and continuation honestly

When the complete authorized collection is already materialized, `totalCount` is exact.
If a future source can produce a bounded slice without a safe or efficient count, `totalCount` is null rather than zero.
`hasNext` is derived from count when available or from bounded lookahead under a documented implementation.

### Use per-request consistency

Each window reflects one execution-time view of the collection.
Insertions, removals, or reordering between requests can shift offsets, and the public contract states that limitation.
The object context continues to discard responses superseded by a newer local generation, but GraphQL does not claim a durable cursor generation.

### Preserve unargumented reads

The established `get` field remains schema-valid during migration.
The new `window` field is additive and has a distinct response type, so existing documents keep their list shape.
Components prefer `window` only after targeted introspection discovers it.

### Be explicit about materialization

Windowing always bounds serialized GraphQL rows.
Instrumentation and documentation identify whether the full association was materialized before slicing.
The capability does not claim persistence-level savings until a separate domain query source proves them.

## Risks / Trade-offs

- [Offset windows drift under concurrent changes] → State per-request consistency explicitly and avoid cursor claims.
- [A full collection may still be materialized] → Bound response rows and expose materialization behavior separately from payload bounds.
- [Total count may become expensive for future sources] → Keep count nullable and derive continuation without claiming zero.
- [Legacy `get` remains unbounded] → Preserve compatibility while documentation and components prefer the bounded operation.

## Migration Plan

Introduce `window` additively on supported rich collection wrappers.
Update object-context secondary operations and collection components to prefer it after capability discovery.
Retain and document legacy unargumented reads for the compatibility period.

## Open Questions

- The default and hard maximum window sizes and their configuration names.
- Whether materialization behavior should be a stable enum field or bounded diagnostics and documentation only.
