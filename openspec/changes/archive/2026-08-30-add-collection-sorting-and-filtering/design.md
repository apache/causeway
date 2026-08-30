## Context

Rich GraphQL collection windows currently materialize the authorized collection, apply an optional configured comparator, and slice by offset and size.
The web-component range broker therefore has no safe way to apply user sorting or filtering across all windows.
The existing metamodel `DataTableInteractive` already defines viewer-wide single-column sorting and optional `CollectionFilterService` quick-search semantics over authorized collection rows.
The pinned Vaadin Grid can host custom column-header renderers, but delegating state to Vaadin sorter or filter elements would create a second state machine and complicate bounded paging and native fallback.

## Goals / Non-Goals

**Goals:**

- Add backward-compatible collection-window criteria that run before slicing.
- Reuse existing Causeway table sorting and application filtering semantics.
- Add opt-in, reactive `sortable` and `filterable` collection presentation.
- Preserve paging, native fallback, authorization, focus, stale-response protection, and bounded requests.
- Demonstrate both controls in Petclinic.

**Non-Goals:**

- Database-level sorting, filtering, or paging.
- Multi-column sorting.
- Per-column filtering.
- Arbitrary client predicates, regular expressions, or query languages.
- Exposing Vaadin sorter, filter, data-provider, or item APIs.
- Persisting sort or filter state across routes.

## Decisions

### Extend `window` with optional Causeway criteria

The additive `window` field will accept optional `sortBy`, `sortDirection`, and `search` arguments while retaining existing offset and size defaults and documents.
`sortBy` identifies one accepted Causeway table column, `sortDirection` is a bounded enum, and `search` is a bounded string interpreted only by an applicable `CollectionFilterService`.
The result will advertise accepted sortable member ids, search support, and the bounded search prompt so clients enable only server-supported controls.
The filtered count becomes `totalCount`, and filtering and sorting run before offset slicing.

This is preferred to browser-only transforms because every page observes the same criteria.
It is preferred to a new endpoint because the existing window operation already owns bounded collection execution.

### Reuse `DataTableInteractive`

The resolver will create a managed collection table for the visible source object, set the optional search and column sort bindables, and consume its filtered-and-sorted rows.
This reuses the established column-discovery, configured comparator, `CollectionFilterService`, value comparison, and row authorization behavior used by existing viewers.
Unsupported sort members, directions, oversized searches, or non-empty searches without an applicable filter service will fail with bounded validation rather than silently degrade.

The resolver still materializes the complete domain collection before filtering, sorting, and slicing.
Documentation will continue to distinguish response bounds from persistence efficiency.

### Keep all criteria state in `<cw-collection>`

`sortable` and `filterable` will be observed boolean attributes and will default off.
The host will retain one immutable sort criterion and one bounded search string for its current route generation.
Changing either criterion will abort current loading, retire the range broker and row contexts, reset offset to zero, and issue one authoritative window request.
Removing the corresponding attribute will clear its active criterion through the same path.

Paging, refresh, virtual range requests, and bounded range requests will carry the current criteria in their cache identity.
Criteria are never applied to legacy unbounded `get` responses.

### Render Causeway-owned controls in both presentations

Native table headings and Grid header renderers will use ordinary accessible buttons that cycle one declared sortable member through ascending, descending, and unsorted states.
The host callback, not Vaadin, changes sorting and reloads data.
`aria-sort`, visible direction text, and focus restoration remain semantic and toolkit-independent.

Filtering will use one Causeway-owned labelled search field above collection content when the server advertises search support.
Input will be bounded and debounced, Escape or a clear control will remove it, and active criteria will remain visible during loading and empty states.

This is preferred to importing Vaadin sort/filter elements because it avoids toolkit-owned criteria, page-local array behavior, duplicate data-provider refreshes, additional closure policy, and inconsistent native fallback.

### Demonstrate selected Petclinic controls

The global owner list will opt into both `sortable` and `filterable` while retaining paging.
Owner pets will opt into sorting, and visit collections will retain their existing behavior unless their row type receives an explicit filtering service.
A Petclinic `CollectionFilterService` will provide bounded case-insensitive tokens for owner search fields without changing domain persistence or collection membership.

## Risks / Trade-offs

- [Risk] Full collection materialization can be expensive before filtering and sorting.
  → Mitigation: retain documented materialization semantics, bound only the serialized window, and make the feature opt-in.
- [Risk] A sort member can expose a property that is unsuitable or unauthorized.
  → Mitigation: accept only `DataTableInteractive` columns and reuse its managed access and comparison behavior.
- [Risk] Search implementations can perform expensive or unsafe matching.
  → Mitigation: accept only bounded plain text and delegate matching solely to registered `CollectionFilterService` implementations.
- [Risk] Rapid filter input can race paging or virtual range callbacks.
  → Mitigation: debounce input and reuse host revision, abort controller, request-key, broker teardown, and generation checks.
- [Risk] Grid and native controls can drift visually or semantically.
  → Mitigation: use the same host criterion and accessible button/search contracts in both presentations and test responsive transitions.

## Migration Plan

1. Add and test the backward-compatible GraphQL arguments and capability metadata.
2. Thread criteria through targeted introspection, operation generation, object-context caching, and range brokering.
3. Add host-owned sorting and filtering controls to native and Grid presentation.
4. Add Petclinic filtering tokens, markup, integration assertions, and browser journeys.
5. Run GraphQL, foundation, Petclinic, browser, RAT, strict OpenSpec, and diff validation.

Rollback removes the optional arguments and public attributes; existing offset-and-size documents and unopted collections require no migration.

## Open Questions

None.
