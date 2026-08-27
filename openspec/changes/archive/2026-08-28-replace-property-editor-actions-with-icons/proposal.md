## Why

After entering property edit mode, the full textual Save and Cancel buttons remain visually heavier than the editor they control and are inconsistent with the newly compact edit affordance.
Compact tick and cancel icons can keep the actions clear while reducing visual noise around the input.

## What Changes

- Replace visible Save text with a compact tick icon and visible Cancel text with a compact cross icon.
- Keep property-specific accessible names and pointer tooltips for both icon-only controls.
- Keep native button semantics, action hooks, test identifiers, validation gating, keyboard operation, and focus restoration unchanged.
- Add deterministic local SVG markup and compact baseline and cohesive-theme styling without adding an icon dependency.
- Add regression coverage for icon presentation, accessibility semantics, state gating, focus compatibility, and styling.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine property editor Save and Cancel presentation to compact accessible icon controls.

## Impact

The change affects the foundation property custom element, shared component styles, the cohesive theme, and browser-side tests.
It changes no GraphQL operation, interaction state, semantic event, editor codec, validation rule, or application configuration.
