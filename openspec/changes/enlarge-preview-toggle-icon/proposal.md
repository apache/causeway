## Why

The collection row preview disclosure uses a small text glyph whose right-facing and downward-facing states are difficult to distinguish at a glance.
The control needs a clearer, slightly larger directional icon without changing preview behavior or authority.

## What Changes

- Replace the small preview disclosure text glyph with a larger, consistently sized directional icon.
- Make collapsed and expanded directions visually distinct while retaining the existing accessible label and `aria-expanded` state.
- Keep the control dimensions, row alignment, keyboard behavior, focus behavior, and preview lifecycle unchanged.
- Apply the framework-neutral presentation everywhere the collection preview is used, including HTMX and Vue hosts.
- Add foundation and browser coverage for both disclosure states and responsive layout.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require the collection row preview disclosure to present a clearly discernible, appropriately sized collapsed or expanded direction indicator without changing semantic state or interaction behavior.

## Impact

The change affects framework-neutral collection preview markup, shared component styling, foundation tests, HTMX and Vue browser acceptance coverage, and related documentation.
It adds no dependency, custom element, public attribute, GraphQL behavior, route behavior, or host-specific preview implementation.
