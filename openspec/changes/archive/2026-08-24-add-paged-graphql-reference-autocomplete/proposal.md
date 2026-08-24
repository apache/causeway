## Why

The current public rich GraphQL autocomplete field returns one unpaged list with no requested size, total, continuation, or ordering metadata.
The web-component client correctly rejects responses above its configured bound, but that safe failure prevents reference selection from scaling beyond small result sets and blocks a truthful Vaadin-default rollout.

## What Changes

- Add backward-compatible `autoCompleteWindow(search, offset, size)` fields beside existing property and action-parameter `autoComplete(search)` fields.
- Return bounded window metadata including items, requested offset and size, returned count, total count, configured maximum, previous/next availability, and the declared application encounter-order policy.
- Configure a GraphQL default window size and hard maximum, validate requests before slicing, and keep every response bounded.
- Invoke the authoritative Causeway autocomplete semantic once per request, preserve its authorized encounter order, and slice only the response window; explicitly do not claim persistence/query pushdown or a cross-request snapshot.
- Preserve preceding action-parameter dependencies, object identity, advertised metadata, protected-value handling, cancellation, and per-request consistency.
- Keep existing non-window autocomplete fields unchanged for current clients.
- Make semantic property, object-action, and service-action contexts discover and prefer the window capability while retaining the old bounded refine-search fallback on older servers.
- Add generation-safe overlapping search and page requests so filter changes, route replacement, prompt closure, and disconnect cannot publish stale items.
- Connect the internal Vaadin Combo Box data-provider callback to real GraphQL windows instead of locally slicing a complete result.
- Keep native editors functional with a bounded first window and an honest refine-search indication when additional results exist.
- Qualify deterministic Reference Application result sets larger than the configured client page and verify selection from a later window.

## Capabilities

### New Capabilities

- `rich-graphql-reference-autocomplete-windowing`: Defines discoverable bounded server-side response windows for property and action-parameter reference autocomplete.

### Modified Capabilities

- `domain-web-components`: Allows semantic reference editors to request and consume authoritative autocomplete windows while preserving Causeway-owned pending values, validation, events, cancellation, and fallback.
- `vaadin-reference-widget-pilot`: Replaces local presentation slicing with a lazy internal data provider backed by advertised GraphQL windows.
- `reference-application-viewer-regression-suite`: Adds deterministic multi-window property or parameter autocomplete coverage and later-window selection.

## Impact

The change affects GraphQL configuration, generated rich property and action-parameter types, output type naming and discovery, autocomplete data fetchers, foundation object and service contexts, property and action controllers, the internal Vaadin adapter, native fallback presentation, documentation, and regression fixtures.
It adds public GraphQL fields and types without changing existing operation shapes.
It adds no dependency, Pro component, Flow API, application-facing Vaadin element, collection Grid sorting/filtering contract, or persistence query-pushdown claim.
