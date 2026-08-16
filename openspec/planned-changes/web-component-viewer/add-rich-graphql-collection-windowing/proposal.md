## Why

The current rich collection `get` shape returns an unargumented GraphQL list.
A generic component cannot bound network payloads, determine continuation, or present reliable paging for large reference-app collections.
Collection windowing should be a focused protocol capability with explicit compatibility and persistence-efficiency boundaries.

## What Changes

- Add deterministic bounded collection reads using the window model selected by the reference-app analysis.
- Return rows with requested and returned window metadata, continuation state, and total count when available.
- Apply supported Causeway configured ordering consistently before selecting a window.
- Preserve the established unargumented collection read during a documented compatibility period.
- Define stale-window, concurrent-change, out-of-range, partial-error, hidden, and disabled behavior.
- Document whether each implementation bounds only GraphQL response materialization or also persistence retrieval.

## Capabilities

### New Capabilities

- `rich-graphql-collection-windowing`: Defines bounded deterministic rich GraphQL collection reads and their compatibility, ordering, count, and continuation semantics.

### Modified Capabilities

None.

## Impact

- Affects rich collection schema shapes, data fetchers, context execution, ordering, tests, and documentation.
- Depends on the completed reference-app analysis.
- Enables lazy collection web components and composite object pages to avoid requesting every row.
- Does not promise database-level pagination where the Causeway programming model materializes a complete association.
