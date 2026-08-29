## Why

Toolkit-backed read-only fields currently show two nested borders because the global Causeway native-control theme also styles Vaadin's slotted internal input while Vaadin renders its own field boundary.
The duplicate boundary is visually distracting and makes one semantic field appear to be two controls.

## What Changes

- Prevent global native input, select, and textarea rules from styling toolkit-owned slotted field internals.
- Preserve one toolkit-owned read-only field boundary, genuine read-only semantics, visible focus, and native control styling outside toolkit fields.
- Add regression coverage for the stylesheet boundary and representative Petclinic field presentation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-semantic-presentation-adapters`: Require toolkit-backed read-only fields to present one coherent control boundary without inheriting duplicate native-control chrome.

## Impact

The change affects the foundation theme stylesheet, its stylesheet tests, and browser acceptance assertions for Vaadin-backed read-only field presentation.
It does not change component markup, GraphQL behavior, editor selection, native toolkit presentation, public selectors, or third-party dependencies.
