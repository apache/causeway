## Why

`<cw-collection>` can page, resize, and reorder qualified Grid presentations, but it cannot safely sort or filter because the current GraphQL window accepts only offset and size.
Enabling toolkit-only controls would affect one loaded window while appearing to affect the complete collection, so collection-wide server semantics are required first.

## What Changes

- Extend rich GraphQL collection windows with bounded, discoverable search and single-column sort criteria applied before slicing.
- Reuse Causeway's existing interactive table filtering and sorting semantics so authorization, configured ordering, application `CollectionFilterService`, and supported columns remain authoritative.
- Add opt-in `sortable` and `filterable` attributes to `<cw-collection>` with reactive offset-zero reload, stale-request retirement, and native fallback behavior.
- Render Causeway-owned sortable header affordances through both native tables and the private Vaadin Grid adapter while keeping toolkit events and data-provider hints internal.
- Render one Causeway-owned collection quick-search control for filtering rather than pretending that page-local or unsupported per-column filters are collection-wide.
- Keep the pinned Grid closure unchanged because sorting and filtering state remains Causeway-owned rather than delegated to toolkit sorter or filter elements.
- Demonstrate sorting and filtering selectively on Petclinic collections and add application filtering tokens for the demonstrated row types.
- Replace the prior sorting/filtering exclusion requirements with the new end-to-end contract.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `rich-graphql-collection-windowing`: Add bounded search and single-column sort arguments, capability metadata, validation, and pre-slice semantics.
- `domain-web-components`: Add public sortable and filterable collection behavior without permitting page-local approximation.
- `vaadin-collection-grid-adapter`: Map server-backed sorting through the private adapter and retain Causeway-owned filtering and state.
- `generic-htmx-web-component-viewer`: Demonstrate and verify selected sortable and filterable Petclinic collections.

## Impact

The change affects the rich GraphQL model and tests, web-component collection operation generation and state, the private Grid adapter, foundation documentation and tests, and Petclinic resources, filtering service, integration tests, and Playwright coverage.
The existing unargumented collection `get` field and offset-and-size-only window documents remain schema-compatible.
Applications that do not opt into `sortable` or `filterable` retain existing loading and presentation behavior.
