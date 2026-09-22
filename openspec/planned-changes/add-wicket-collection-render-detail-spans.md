# Add Wicket Collection Render Detail Spans

**Roadmap position:** Proposal 3, conditional on collection regions remaining broad bottlenecks.

## Why

A collection-region span can show that a collection is slow without distinguishing data loading, pagination or materialization, presentation selection, and markup generation.
More focused collection observations would make the next optimization decision evidence-based while retaining the collection as the semantic parent.

## Proposed Changes

- Split selected work beneath `causeway.wicket.collection.render` into bounded semantic operations.
- Identify collection data loading separately from page slicing or materialization where the code provides reliable contiguous boundaries.
- Identify presentation-specific work for table, icon, summary, hidden, and extension-provided presentations.
- Ensure automatic JDBC spans inherit from the collection-loading observation when database access occurs within that boundary.
- Add bounded metadata such as logical collection ID, presentation kind, configured page size, and returned count where semantically safe.
- Retain the no-per-row and no-per-cell policy.

Candidate stable names are:

```text
causeway.wicket.collection.load
causeway.wicket.collection.page
causeway.wicket.collection.presentation
```

## Non-Goals

- Do not create observations for every row, cell, or row action.
- Do not record collection elements, bookmarks, row identifiers, filter values, or exported content.
- Do not introduce persistence-adapter-specific assumptions into shared Wicket instrumentation.
- Do not replace automatic JDBC instrumentation.

## Prerequisites and Evidence Gate

The region-render proposal must show that one or more collection regions dominate representative traces.
Proceed only when the broad collection duration cannot already be explained by child JDBC spans or existing application observations.

## Design Questions

- Which collection presentations load data during rendering and which load it during preparation?
- Is a returned element count bounded operational metadata or potentially sensitive application data?
- Can custom collection presentations participate through a small SPI without coupling extensions to core instrumentation?
- Are paging and conversion boundaries shared across table and non-table presentations?

## Expected Exit Gate

A slow collection trace separates loading, paging or materialization, and presentation work without emitting row-level spans or exposing collection contents.
