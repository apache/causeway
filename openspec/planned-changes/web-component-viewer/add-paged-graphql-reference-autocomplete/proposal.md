## Why

The current public GraphQL autocomplete operation returns one search result with no page, size, total, continuation, or stable-ordering contract.
The bounded client correctly refuses oversized responses, but that limitation is unsuitable for default reference selection over large production domains.

## What Changes

- Add a backward-compatible public GraphQL reference-autocomplete window operation with explicit search text, requested offset or cursor, requested size, configured maximum, stable ordering, and continuation or count metadata.
- Preserve authorization, stable object identity, declared parameter dependencies, cancellation, and per-request consistency.
- Keep the existing non-paged autocomplete operation available to current clients.
- Update semantic reference adapters to request real server windows rather than locally slicing one response.
- Define filter-generation, stale-response, route-supersession, and disconnect behavior across overlapping windows.
- Add Reference Application fixtures and tests with result sets above the current client bound.
- Keep unsupported servers on the existing bounded operation with an honest refinement or fallback state.

## Capabilities

### New Capabilities

- `rich-graphql-reference-autocomplete-windowing`: Defines discoverable bounded server-side windows for property and action-parameter reference autocomplete.

### Modified Capabilities

- `domain-web-components`: Allows semantic reference editors to consume public server windows while preserving Causeway-owned pending values and events.
- `vaadin-reference-widget-pilot`: Replaces local presentation slicing with real server paging when the capability is advertised.

## Impact

The change affects the rich GraphQL schema generator, operation naming and discovery, reference choice services, client GraphQL contexts, reference adapters, cancellation behavior, and large-result integration tests.
It adds operations rather than changing existing operation shapes.
It does not add collection Grid sorting or filtering.
