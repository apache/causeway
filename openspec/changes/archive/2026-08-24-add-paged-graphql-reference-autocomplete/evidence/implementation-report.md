# Paged reference autocomplete implementation report

## GraphQL model

GraphQL configuration now supplies autocomplete default and maximum response-window sizes with validated defaults of 20 and 100.
Rich properties and action parameters that already expose `autoComplete` additionally expose `autoCompleteWindow`.
Existing field names, arguments, result types, and data fetchers remain unchanged.

A shared server element validates offset and size, invokes the existing authorized Causeway autocomplete semantic once, copies application encounter order, and returns an immutable response slice with complete count and continuation metadata.
Member-specific generated result types retain exact item output mapping.
Object and service action parameters retain only advertised preceding arguments through the existing negotiation and conversion model.
Errors are bounded and omit search text, pending arguments, identities, and values.
GraphQL type registration is synchronized and returns snapshots so parallel metamodel discovery cannot lose a generated type while the additive window types are registered.

## Semantic client

Targeted introspection follows window, item, abstract/concrete, and metadata types.
Object and service contexts add immutable window-returning methods while retaining every list-returning method.
Window operations select only advertised arguments and fields.
Servers without the additive field are normalized as marked legacy results and remain subject to the established complete-response bound.

Filter changes, dependent argument changes, prompt closure, route replacement, disconnect, and request generations abort or reject stale work.
Native autocomplete no longer submits raw search text as a domain-object pending value while the user is still filtering.
It renders the bounded first window and an accessible refinement message when `hasNext` is true.

## Internal Vaadin adapter

Pinned Vaadin 25.2.8 documentation and package metadata confirm the lazy callback contract `dataProvider({filter, page, pageSize}, callback)` and `callback(items, totalSize)`.
The internal Causeway adapter uses the introspected default page size, maps pages to semantic offset requests, normalizes rich object metadata to stable identities, and supplies authoritative total count.
Outdated filter callbacks are ignored.
Finite choices and legacy autocomplete retain the established items and fallback behavior.
No Vaadin API crosses the public Causeway component boundary.

## Reference Application

The qualification page and maximum are five against an existing deterministic seven-result filter.
The inventory normalizes the conditionally advertised abstract `ValueHolder.name` wrapper so clean and incremental schema traversals remain byte-identical without hiding a capability change.
GraphQL covers first, later, defaulted, empty, invalid, and legacy operations.
The candidate browser journey requests page one directly through the internal data provider and submits a reference absent from page zero.
Native mode obtains the first window, exposes the refinement message, selects an authoritative item, and submits through the same action contract.
