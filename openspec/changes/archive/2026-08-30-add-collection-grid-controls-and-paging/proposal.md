## Why

`<cw-collection>` already owns bounded collection windows and an internal Vaadin Grid adapter, but application markup cannot choose a page size or enable the Grid's safe column sizing controls.
Petclinic currently declares inert `offset` and `size` attributes, so representative pages do not demonstrate selective collection paging through the public component contract.

## What Changes

- Add a bounded `paged="N"` attribute to `<cw-collection>` that requests the initial and subsequent Causeway-owned collection windows with page size `N`.
- Add `resizable-columns` and `reorderable-columns` attributes that map through the private adapter boundary to Vaadin Grid column resizing and reordering.
- Keep filtering disabled and do not expose it because the current GraphQL collection-window contract has no filter input.
- Keep sorting unchanged and do not claim collection-wide sorting because the current GraphQL collection-window contract has no ordering input; sorting only a loaded page would be misleading.
- Replace inert Petclinic `offset` and `size` markup with selective `paged` overrides on collections where paging is useful, while leaving smaller collections unpaged.
- Add component, adapter, integration, and browser regression coverage for bounded paging and opted-in Grid controls.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add declarative collection page-size and Grid-control attributes with bounded parsing and reactive behavior.
- `vaadin-collection-grid-adapter`: Map approved resizing and reordering options through the private adapter while retaining Causeway-owned paging and fallback.
- `generic-htmx-web-component-viewer`: Demonstrate selective collection paging through Petclinic HTML resource overrides.

## Impact

The change affects the web-component foundation collection host, private Vaadin Grid adapter, component documentation and tests, and selected Petclinic HTML pages and tests.
It does not change the GraphQL schema, collection ordering, filtering, native narrow presentation, toolkit packaging, production width policy, or application access to raw Vaadin elements.
