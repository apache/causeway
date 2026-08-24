## Context

Rich property and action-parameter wrappers currently expose `autoComplete(search)` as an unbounded list-valued field.
Causeway's metamodel autocomplete semantics return an authoritative ordered collection but do not expose persistence-level offset, cursor, count, or snapshot APIs.
The foundation client therefore downloads the complete list, and property and action controllers reject it when it exceeds `referenceMaximumResults`.
The internal Vaadin adapter assigns a finite `items` array and locally slices it, which is correctly documented as presentation bounding rather than server paging.

This change adds a public GraphQL response-window contract without pretending that domain autocomplete itself performs query pushdown.
Each request invokes the existing authorized Causeway semantic once, materializes that request's result, preserves its encounter order, and returns only the requested response slice plus count and continuation metadata.
That bounds browser transfer and enables Vaadin's lazy data-provider protocol while preserving backward compatibility.

## Goals / Non-Goals

**Goals:**

- Add discoverable bounded offset windows beside existing property and action-parameter autocomplete fields.
- Bound every response using GraphQL configuration and validate offset and size before invoking autocomplete.
- Preserve authorized application encounter order, argument dependencies, semantic identity, cancellation, and per-request consistency.
- Let object and service contexts prefer windows and normalize legacy fallback honestly.
- Let Vaadin Combo Box request later pages without application-facing Vaadin APIs or local truncation.
- Keep native editors usable with a bounded first page and an explicit additional-results/refine indication.
- Exercise later-window selection against deterministic Reference Application data.

**Non-Goals:**

- Do not claim database query pushdown, cursor stability, transaction snapshots across requests, or stable ordering across domain-state changes.
- Do not remove or alter `autoComplete(search)`.
- Do not introduce collection Grid sorting/filtering, Flow, Binder, Java `DataProvider`, Pro components, telemetry, CDN assets, or raw application-facing Vaadin elements.
- Do not change validation, mutation, action invocation, routes, identity, CSP, native fallback, or default toolkit policy.

## Decisions

### Add sibling `autoCompleteWindow` fields

Every rich property or action parameter that advertises `autoComplete` also advertises:

`autoCompleteWindow(search: String!, offset: Int! = 0, size: Int! = <configured default>)`.

The result is a generated member-specific object containing `items`, `offset`, `requestedSize`, `returnedCount`, `totalCount`, `maximumSize`, `hasPrevious`, `hasNext`, and `ordering`.
Member-specific output types preserve the exact existing item output type, including concrete, abstract, enum, scalar, and versionless metadata behavior.
The existing list field remains byte-for-byte compatible.

A top-level generic autocomplete endpoint was rejected because property and action-parameter semantics require their current source object, authorization, and preceding arguments.
Replacing the old field was rejected because it would break existing documents.

### Use bounded offset windows and application encounter order

GraphQL configuration gains `autocomplete.default-window-size` and `autocomplete.max-window-size`, defaulting to 20 and 100.
Offset must be non-negative, size positive, and size no greater than the configured maximum.
Validation occurs before invoking the domain autocomplete semantic.

One request invokes the existing autocomplete semantic once, copies its encounter order, calculates total count, and returns an immutable sub-list.
`ordering` is `APPLICATION`, meaning the application-provided encounter order is authoritative for that execution.
No cross-request snapshot is promised; clients restart at offset zero whenever filter text or dependent arguments change.

Sorting by title or bookmark was rejected because it would destroy application relevance ordering and may invoke additional semantics.
Cursor tokens were rejected because the metamodel provides neither a stable cursor nor a snapshot from which an honest token could be derived.
Calling the domain method repeatedly to skip rows was rejected because it would add cost without improving consistency.

### Share server construction and result normalization

Property and action-parameter window elements use one package-local builder and slicing utility for arguments, result type fields, validation, metadata, and immutable result maps.
Generated type names extend the existing property or parameter type name with `_autocomplete_window`.
A dedicated safe exception reports invalid windows without including search text, preceding arguments, identities, or returned values.

Foundation contexts add window methods rather than changing list-returning methods.
They inspect `autoCompleteWindow`, describe its generated result and `items` type, issue only advertised fields, and normalize the result to one immutable semantic window shape.
When the sibling field is absent they invoke legacy autocomplete once and return a marked legacy window only if the existing result bound is respected.

### Separate filter generations from page requests

A filter generation owns one search text and current dependent-argument snapshot.
Changing either aborts every outstanding page request, clears accumulated pages, and starts at offset zero.
Pages in one generation may complete out of order but are applied only to their requested offsets after generation, connection, route, prompt, and abort checks.
Duplicate page requests share or supersede an offset-scoped request rather than appending duplicate items.
Selection is reconciled by semantic identity and is not discarded merely because its page is not currently loaded.

The normalized window includes total and continuation metadata, so native presentation can state that more matches exist without pretending its first-page datalist is complete.

### Use Vaadin's lazy data-provider callback internally

When window capability is advertised, the internal reference editor configures Combo Box page size and data provider.
The callback dispatches an internal composed request carrying filter, offset, size, and a one-shot responder.
The owning Causeway property or action controller performs the semantic context command and returns normalized items and total count.
The adapter invokes Vaadin's callback only while connected and within the same filter generation.

When only legacy autocomplete is available, the adapter retains the established finite-items behavior and visible over-bound refine/fallback policy.
Applications continue to see Causeway pending values and semantic events only.

Local slicing under a Vaadin callback was rejected because it would recreate the false paging behavior this change removes.
Exposing the Vaadin data provider to applications was rejected because it would breach the stable Causeway boundary.

### Qualify with bounded deterministic Reference Application pages

The Reference Application uses its existing deterministic 13-character autocomplete set with a qualification page size below the total.
Integration tests request at least three windows, verify non-overlap, order, counts, boundaries, dependent arguments, invalid sizes, and selection identity.
Browser tests search, request a later page through the internal adapter, select an item not present in the first page, validate or submit it, and exercise filter supersession and route disposal.

This is deliberately larger than the configured qualification page, not represented as a large persistence query benchmark.

## Risks / Trade-offs

- [The server still materializes the complete domain result per page request] → State this explicitly, bound response transfer, preserve the application semantic, and defer backend pushdown until a public metamodel contract exists.
- [Application order can change between page requests] → Declare request consistency rather than snapshot consistency and restart paging when filter or dependent arguments change.
- [Window metadata adds generated schema types] → Use deterministic member-specific names, registry deduplication, and targeted introspection.
- [Vaadin callback races route or prompt disposal] → Use abort signals, filter generations, offset request keys, connection checks, and one-shot responders.
- [Legacy servers remain unpaged] → Preserve the current bound and honest refine/fallback state without truncation.
- [A low configured maximum could make the UI chatty] → Provide separate default and maximum values and let the adapter request an accepted page size.

## Migration Plan

Ship additive GraphQL fields and client discovery together.
Existing clients continue using `autoComplete` unchanged.
New clients prefer windows when advertised and degrade to legacy bounded behavior otherwise.
Rollback removes client preference and new fields without data, route, application, or persisted-state migration.

## Open Questions

None.
