## Why

The native collection renderer now uses the larger SVG preview chevron, but the separate Vaadin Grid cell renderer still emits the former small Unicode triangle.
Consequently preview-enabled collections such as PetOwner Pets show a smaller, less distinguishable icon than native collections such as Visits.

## What Changes

- Give native and Vaadin Grid preview disclosures one shared SVG icon definition.
- Remove the remaining Grid-specific Unicode triangle rendering.
- Preserve the established button size, accessible label, `aria-expanded` state, focus restoration, and single-preview lifecycle.
- Add adapter-specific tests and Petclinic browser coverage for native and Vaadin parity.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-collection-grid-adapter`: Require Grid preview disclosures to use the same clearly sized, state-driven icon presentation as native collection previews.

## Impact

The change affects the shared preview icon utility, native collection markup, the Vaadin Grid row-details disclosure renderer, foundation tests, Petclinic browser acceptance coverage, and related documentation.
It adds no dependency, public element, GraphQL behavior, route behavior, authorization behavior, or host-owned presentation.
