## Context

`RichCollectionGet` currently derives a list output type and adds no paging or ordering arguments.
The web-component context can issue secondary collection operations, but those operations still receive the full list from GraphQL.
The reference application contains configured page sizes, ordering, table presentation, and collections large enough to require bounded client payloads.

## Goals / Non-Goals

**Goals:**

- Bound collection response size deterministically.
- Publish enough metadata for next, previous, and range presentation.
- Preserve stable configured ordering.
- Define behavior under concurrent changes and partial errors.
- Retain existing clients through explicit compatibility policy.

**Non-Goals:**

- Guaranteeing database-level paging for every domain collection.
- Adding arbitrary unrestricted client sorting or filtering.
- Defining the collection web-component UI.
- Mixing collection layout hints into the window protocol.

## Decisions

### Select one semantic window abstraction

The analysis will choose offset and size, opaque cursor and size, or an additive combination based on executable behavior and compatibility.
The public response includes requested position, returned count, continuation state, and total count only when it can be determined safely.

### Apply deterministic ordering before window selection

A configured Causeway comparator or ordering facet is applied consistently before a window is selected.
Where stable ordering cannot be guaranteed, the response reports that limitation rather than implying cursor stability.
Arbitrary GraphQL field-name sorting is excluded until an independently validated contract exists.

### Preserve unargumented reads temporarily

The established `get` field remains valid during migration.
The new bounded shape is additive or provided through optional arguments with a response form that does not invalidate existing documents.
The final schema shape is selected only after schema-compatibility tests.

### Be explicit about server efficiency

Response windowing always bounds serialized GraphQL rows.
Instrumentation and documentation state whether the underlying collection was fully materialized before slicing.
The capability does not claim persistence-level efficiency where the domain programming model cannot provide it.

### Define generation and stale behavior

A response identifies the object or collection generation information available from the current context.
Clients can discard superseded windows.
Concurrent changes may alter counts or positions and are represented through documented consistency semantics rather than hidden retries.

## Risks / Trade-offs

- [Offset windows drift under concurrent changes] → Prefer stable ordering and expose generation or consistency metadata; use cursors if analysis shows a sound implementation.
- [Cursor windows can conceal full materialization] → Document retrieval behavior separately from response shape.
- [Total count may be expensive] → Make it nullable or separately requested where necessary.
- [Changing `get` return type would break clients] → Preserve the old field or use an additive window field selected by compatibility testing.

## Migration Plan

Introduce the bounded contract additively.
Update collection components to prefer bounded reads only after the server capability is discoverable.
Retain and document legacy unargumented reads for the compatibility period.

## Open Questions

- Offset, cursor, or both?
- Should count be part of every response, separately selected, or omitted when expensive?
- Can Causeway collection facets expose stable comparator identity suitable for cursor continuation?
- Should concurrent mutation return a stale-window error or simply updated continuation metadata?
