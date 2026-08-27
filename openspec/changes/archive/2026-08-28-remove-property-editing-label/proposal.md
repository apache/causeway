## Why

An editable property currently displays an “Editing” status beneath its editor even though the active editor and Save/Cancel controls already communicate that mode.
The redundant label adds visual noise and can make focus placement appear haphazard.

## What Changes

- Remove the visible “Editing” status from an active property editor.
- Preserve meaningful transient and exceptional statuses such as preparing, validating, saving, correction required, and unsupported editing.
- Add regression coverage proving that ordinary edit mode renders no redundant status row or label.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine property editing presentation so ordinary edit mode does not render a redundant textual status while meaningful workflow statuses remain available.

## Impact

The change affects only the foundation property custom element and its browser-side interaction tests.
It changes no GraphQL operation, semantic interaction state, editor behavior, focus algorithm, public event, or dependency.
