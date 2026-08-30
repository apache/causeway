## Why

`<cw-action>` currently cannot override its visible name with the same HTML-authored vocabulary used by properties and collections, and action descriptions, disabled reasons, and Font Awesome layout facets are not presented consistently across object buttons, prompts, and application-menu items.
This leaves application-authored pages less expressive and discards useful canonical action presentation metadata already present in the Causeway metamodel.

## What Changes

- Add a `named` attribute and reflected property to `<cw-action>`, with precedence over the compatible legacy `label` override and canonical metadata.
- Present canonical action descriptions as bounded accessible tooltips on ordinary object-action buttons and application-menu action items.
- Compose disabled reasons into the same tooltip as a separate section when a description is also present.
- Show the effective action name and its canonical description in parameterized action dialogs.
- Add narrow rich GraphQL action metadata for static Font Awesome classes and left/right position.
- Render declared Font Awesome action icons in ordinary action buttons and native and Vaadin-backed application menus without exposing arbitrary markup or broad metamodel internals.
- Extend selected Petclinic actions with representative HTML-authored names, descriptions, disabled reasons, icons, positions, and parameterized-dialog coverage while leaving other actions unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Define action naming precedence, tooltip composition, dialog heading and description presentation, and bounded Font Awesome icon rendering.
- `rich-graphql-member-metadata`: Expose nullable static Font Awesome class and position metadata for action wrappers.
- `graphql-web-component-context`: Select and preserve canonical action name, description, and icon metadata through ordinary rendering and parameterized interaction preparation.
- `vaadin-application-menubar-adapter`: Preserve composed action tooltips and positioned Font Awesome icons across native and Vaadin-backed application menus.
- `generic-htmx-web-component-viewer`: Deliver the same-origin Font Awesome stylesheet and demonstrate selected action presentation in Petclinic.

## Impact

The change affects rich GraphQL member metadata, object and service action discovery, `<cw-action>`, action widgets, interaction dialogs, effective-menu parsing and projection, Vaadin Menu Bar materialization, shared styling, the HTMX shell asset set, and Petclinic fixtures and acceptance tests.
It adds a pinned Font Awesome WebJar runtime asset to the generic HTMX viewer and remains additive for existing GraphQL clients and HTML pages.
