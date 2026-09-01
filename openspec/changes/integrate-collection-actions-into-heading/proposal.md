## Why

Moving collection-associated actions above the collection removed the need to scroll past rows, but the toolbar still consumes a separate full row and leaves excessive vertical space between the enclosing section title and the collection panel.
The controls should share the collection's own heading row when space permits while retaining a safe responsive fallback.

## What Changes

- Promote the effective collection heading into a stable direct header row when associated actions are present.
- Place the heading first and the existing action elements to its right in the same bordered collection header.
- Make heading-associated action buttons more compact than ordinary page and prompt actions while preserving readable labels, icons, focus indicators, and touch usability.
- Keep the collection description, search, table or Grid, paging, diagnostics, and empty states in the full-width body below the header row.
- Stack or wrap heading actions below the title when the collection becomes too narrow for a safe shared row.
- Preserve action node identity, declaration order, sequential keyboard order, GraphQL authority, prompts, invocation, refresh, and focus restoration.
- Add Foundation and Petclinic browser coverage for wide and narrow collection-heading action presentation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Integrate collection-associated action toolbars into the collection heading row with compact responsive presentation.
- `generic-htmx-web-component-viewer`: Qualify the Petclinic collection title and associated actions as one compact responsive header.

## Impact

The change affects collection shell rendering, stable member composition, structural and theme CSS, component documentation, Foundation tests, and Petclinic Playwright acceptance.
It changes no public element names, authored action syntax, GraphQL operations, action lifecycle, Vaadin version, routes, or domain behavior.