## Context

`CausewayCollectionElement` already loads GraphQL collection windows and owns bounded previous/next controls, while `CausewayCollectionGridElement` privately creates Vaadin Grid controls for qualified wide collections.
The public collection contract does not currently observe page-size or Grid interaction attributes, and Petclinic's existing `offset` and `size` attributes are inert.

Vaadin Grid directly supports `columnReorderingAllowed` on the grid and `resizable` on each column.
Its sorting and filtering controls instead emit data-provider hints that must be applied to the entire data set.
The current GraphQL collection-window operation accepts only `offset` and `size`, so applying sort or filter only to a loaded window would produce misleading collection-wide behavior.

## Goals / Non-Goals

**Goals:**

- Add a bounded declarative page-size override to `<cw-collection>`.
- Keep paging state and requests owned by Causeway rather than Vaadin.
- Map explicit column resizing and reordering options through the private adapter.
- Demonstrate paging selectively in Petclinic HTML overrides.
- Preserve native fallback, responsive qualification, accessibility, focus, authorization, and bounded range behavior.

**Non-Goals:**

- Adding GraphQL ordering or filtering inputs.
- Enabling page-local sorting or filtering that could be mistaken for collection-wide behavior.
- Exposing raw Vaadin controls or events to applications.
- Persisting user column widths or order across rerenders or routes.
- Changing collections that do not opt into `paged`.

## Decisions

### Use `paged` as a bounded positive page-size attribute

`paged="N"` will parse an integer from 1 through 100.
Absent, blank, malformed, zero, negative, or oversized values will be treated as no override rather than producing unbounded requests.
A valid value will be supplied to the initial load and retained by previous, next, refresh, and generation changes.

The collection host will force bounded Grid presentation for an opted-in paged collection even when a total count is available, because Causeway-owned paging controls are the public behavior being requested.
The server-returned window metadata remains authoritative for actual requested size, maximum size, offsets, and terminal-page state.

This is preferred to reusing `size`, which is currently inert application markup and does not communicate that visible paging controls are requested.

### Keep Grid controls explicit and adapter-private

`resizable-columns` will set each generated Vaadin column's `resizable` property.
`reorderable-columns` will set the Vaadin grid's `columnReorderingAllowed` property.
Both attributes default to absent and false, apply only while Grid is qualified, and trigger a safe rerender when changed.
Native table/list fallback remains unchanged.

Column order changes remain presentation-local to the current Grid instance.
Causeway's declarative column selection and GraphQL projection order remain authoritative after rerender, refresh, fallback, or navigation.

### Descope sorting and filtering

No `sortable` or `filterable` public behavior will be added in this change.
Vaadin's sorter and filter elements pass sort orders and filters to the data provider, but the Causeway range provider has no corresponding server contract.
Ignoring those hints or applying them to one page would violate deterministic cross-window behavior.

A later change may introduce collection-wide sorting or filtering only after the GraphQL operation, authorization behavior, canonical value comparison, range-cache keys, and stale-request semantics are specified end to end.

### Apply paging selectively in Petclinic

The global owner list and owner visit history will opt into bounded paging with a small demonstration size.
The upcoming-visit summary and an owner's usually small pet list will remain unpaged.
Existing inert `offset` and `size` attributes will be removed from touched collection markup.

## Risks / Trade-offs

- [Risk] A configured GraphQL maximum below the declarative page size can reject the first request.
  → Mitigation: keep the public bound conservative, surface the existing collection error state, and use Petclinic values below the default server maximum.
- [Risk] Column reordering appears persistent while the current adapter is connected but resets after rerender.
  → Mitigation: document it as presentation-local and preserve declarative order as authoritative.
- [Risk] Changing paging attributes during an in-flight request can publish stale data.
  → Mitigation: use the existing load revision, abort controller, range-broker teardown, and host revision before reloading from offset zero.
- [Risk] Toolkit properties drift in a future Vaadin upgrade.
  → Mitigation: adapter tests assert the reviewed `resizable` and `columnReorderingAllowed` mappings against the pinned closure.

## Migration Plan

1. Add and test normalized collection presentation attributes.
2. Thread the page size into collection loading and bounded qualification.
3. Map the approved controls in the private Grid adapter.
4. Update usage documentation and selective Petclinic markup and regression tests.
5. Regenerate no toolkit assets because the required APIs already exist in the pinned Grid closure.

Rollback removes the new attributes and restores the prior Petclinic markup without data migration.

## Open Questions

None.
