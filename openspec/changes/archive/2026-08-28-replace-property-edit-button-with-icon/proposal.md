## Why

Each editable property currently uses a full-width textual “Edit <property>” button that competes visually with the property value and makes dense object forms feel unnecessarily busy.
A compact pencil affordance can keep editing discoverable while better integrating the control with the value row.

## What Changes

- Replace the visible “Edit <property>” button text with a compact pencil icon.
- Keep a property-specific accessible name and tooltip so the icon-only control remains understandable to assistive technology and pointer users.
- Keep the edit control in the existing value-row action column and preserve edit activation, keyboard operation, focus restoration, disabled behavior, and test hooks.
- Add baseline and theme styling that gives the control a compact square hit area and deterministic icon dimensions.
- Add regression coverage for icon-only presentation and accessibility semantics.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine the editable-property affordance to use a compact accessible icon control adjacent to the property value.

## Impact

The change affects the foundation property custom element, shared component styles, the optional cohesive theme, and browser-side tests.
It adds no dependency and changes no GraphQL operation, semantic event, interaction state, public edit command, or application configuration.
