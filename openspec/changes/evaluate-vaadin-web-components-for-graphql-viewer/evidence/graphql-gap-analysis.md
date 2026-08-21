# GraphQL gap analysis

## Summary

The current public rich GraphQL API can support a useful Vaadin reference-selector and read-only lazy Grid integration without private endpoints.
It cannot expose every data-provider capability that Vaadin offers.
The analysis keeps unsupported behavior disabled and records exact follow-up deltas.

## Reference selection

### Supported today

- Fixed choices.
- Autocomplete by search string.
- Property and action-parameter disabled state.
- Default values.
- Required and optional state.
- Validation through current argument values.
- Property mutation and action invocation.
- Canonical logical type and opaque object identity.
- AbortSignal cancellation and generation-scoped stale-response suppression.
- List-valued action arguments where exposed by the schema.

These operations are sufficient to reproduce current Wicket Select2 parity because its provider receives a page number but does not actually page the Causeway result set.
The prototype pages the returned autocomplete set locally for Vaadin's callback.

### Missing

- Server-side autocomplete offset or page.
- Requested page size.
- Total matching result count.
- Continuation token or stable result generation.
- Explicit maximum returned match count in the browser contract.
- Confirmed editable multi-reference property support across all member shapes.

### Consequence

A first reference-widget pilot can use the existing search operation if the server response is explicitly bounded and documented.
True scalable Combo Box paging requires a new GraphQL requirement and should not be hidden inside the widget adapter.

## Collections and Grid

### Supported today

- Bounded zero-based `window(offset, size)`.
- Configured maximum size.
- Returned and total row count.
- Previous and next indicators.
- Configured versus encounter ordering metadata.
- Semantic row projection.
- Abortable reads and cache keys by member, projection, offset, and size.
- Canonical row-object navigation when identity is projected.

This is enough for a read-only lazy Grid with a configured server order.

### Missing

- User-requested sort columns and direction.
- User-requested filter expressions.
- Search text over collection rows.
- Stable ordering guarantee for encounter-ordered collections.
- Server-side aggregation, grouping, and editable-cell semantics.
- An explicit row-key contract independent of projected metadata.

### Consequence

The first Grid prototype correctly disables interactive sort and filter rather than applying them to only one loaded page.
A production Grid may display configured columns and lazy windows now, but full business-grid behavior requires a separate rich GraphQL collection query proposal.

## Typed fields and interactions

Current scalar marshallers, semantic editor selection, validation, disabled reasons, action argument negotiation, invocation, cancellation, concurrency, and normalized outcome kinds map cleanly to candidate controls.
Vaadin value formats must remain subordinate to Causeway conversion, especially locale-sensitive dates, times, decimals, reference identities, and null values.

No candidate widget may invoke domain behavior directly or retain a second authoritative pending value after Causeway rejects it.

## Route and lifecycle

The existing one-context-per-route design maps well to widget adapters.
Abortable transient commands and route generations are sufficient to prevent stale autocomplete and window responses.
The custom fragment resolver needs no GraphQL change.

The remaining production integration gap is CSP rather than state management.

## Recommended API sequencing

1. Adopt no new GraphQL operation for a bounded reference-widget pilot if current autocomplete responses are demonstrably bounded.
2. Specify paged autocomplete before claiming unbounded remote reference scalability.
3. Specify collection sort, filter, stable row key, and stable ordering before enabling corresponding Grid UI.
4. Keep advanced Grid grouping, aggregation, editing, and export outside the initial viewer scope.
