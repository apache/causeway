## Why

Action results are currently limited to inline placement, which can displace a long object page and force users to move between the invoking action and its outcome.
Applications need a declarative, accessible way to choose inline, modal-dialog, or right-sidebar result presentation without changing action invocation or result authority.

## What Changes

- Add a `presentation-style` attribute to `<cw-action-results>` with case-insensitive `INLINE`, `DIALOG`, and `SIDEBAR` values.
- Retain `INLINE` as the default and preserve the current in-page result lifecycle.
- Present `DIALOG` results in an accessible modal surface with bounded focus, Escape and dismiss handling, and focus restoration.
- Present `SIDEBAR` results in a responsive right-side panel with accessible naming, dismissal, and focus restoration.
- Keep the outlet passive: it does not invoke actions, subscribe globally, choose result destinations, navigate, or reinterpret result data.
- Extend HTMX result routing so the already-resolved outlet controls only the surface used for successful non-navigating results.
- Add Foundation and Petclinic browser qualification for all three styles under Vaadin and native toolkit policies.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Extend the passive action-result outlet contract with declarative inline, dialog, and sidebar presentation surfaces and their accessibility lifecycle.
- `generic-htmx-web-component-viewer`: Honor the resolved outlet presentation style while preserving result routing, ownership, replacement, dismissal, navigation, and responsive behavior.

## Impact

The change affects the public `<cw-action-results>` attribute contract, Foundation component styles and lifecycle APIs, HTMX result presentation policy, Petclinic page declarations, usage documentation, and browser accessibility and responsive tests.
It introduces no GraphQL schema, domain action, invocation, result payload, or server resource changes.
