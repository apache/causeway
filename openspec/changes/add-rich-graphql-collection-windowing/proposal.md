## Why

The executable reference-application analysis confirmed that a collection configured with a presentation page size of five still exposes an unargumented rich `get` field returning all 13 rows.
The wrapper has no count, bound, or continuation metadata, so a generic component cannot limit network payloads or present reliable range controls.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-COLLECTION-01` and `REF-COLLECTION-02`.
Abstract-element output correctness is handled first by `fix-rich-graphql-object-interaction-correctness` under entry `REF-COLLECTION-03`.

## What Changes

- Add an additive offset-and-size collection window operation with a configured hard maximum.
- Return rows, requested offset and size, returned count, nullable total count, configured maximum, ordering mode, and previous or next availability.
- Apply supported Causeway configured ordering consistently before selecting a window.
- Preserve the established unargumented collection read during a documented compatibility period.
- Define fail-fast invalid-range, out-of-range, concurrent-change, partial-error, hidden, and disabled behavior.
- Document whether each implementation bounds only GraphQL response materialization or also persistence retrieval.

## Capabilities

### New Capabilities

- `rich-graphql-collection-windowing`: Defines additive bounded offset windows and their compatibility, ordering, count, continuation, and materialization semantics.

### Modified Capabilities

None.

## Impact

- Affects rich collection schema shapes, data fetchers, context secondary operations, ordering, tests, and documentation.
- Depends on completed reference-application analysis and corrected polymorphic object output.
- Enables lazy collection web components and composite object pages to avoid requesting every row.
- Does not promise database-level pagination where the Causeway programming model materializes a complete association.
