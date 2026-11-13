## Why

Small unpaged collections currently retain Vaadin Grid's virtual scrolling viewport even when the authoritative total fits within the already loaded range, leaving a large blank area below a few rows.
Collection-associated actions are also rendered after the complete collection surface, separating the controls from the collection heading and forcing unnecessary scrolling.

## What Changes

- Make an unpaged virtual Grid fit its rows when the authoritative collection total is small enough to fit within one requested window.
- Retain the bounded scrolling viewport for larger virtual collections and retain current-row fitting for explicitly paged bounded collections.
- Present collection-associated actions as an ordered responsive toolbar before the collection's primary surface rather than after the table or Grid.
- Right-align the collection action toolbar at wide widths and allow it to wrap without overlap or overflow at narrow widths.
- Preserve action declaration order, keyboard order, node identity, visibility, usability, prompting, invocation, refresh, and focus behavior.
- Add Foundation and Petclinic browser coverage for compact small Grids and top-placed collection actions.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-collection-grid-adapter`: Fit authoritative one-window virtual collections to their rows while retaining virtualization for larger totals.
- `domain-web-components`: Place collection-associated action regions before the collection surface without changing property-associated action placement or action authority.
- `generic-htmx-web-component-viewer`: Qualify compact Petclinic owner-pet Grid presentation and responsive top collection-action placement.

## Impact

The change affects Foundation Grid presentation, associated-action structural CSS, collection and Grid unit tests, component usage documentation, and Petclinic Playwright acceptance.
It changes no public element names, GraphQL operations, domain semantics, Vaadin dependency versions, route grammar, or action lifecycle.