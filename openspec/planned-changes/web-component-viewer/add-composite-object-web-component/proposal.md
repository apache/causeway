## Why

Applications can currently compose object headers, properties, actions, and collections explicitly, but every generic consumer must repeat member discovery, layout-resource parsing, fallback placement, and accessibility work.
A framework-neutral `<causeway-object>` component should provide the high-level semantic object projection and decompose it into the established lower-level components using the effective Causeway grid layout.

## What Changes

- Add `<causeway-object>` as a public high-level component beneath an authoritative `<causeway-object-context>`.
- Discover object members through the context's targeted rich-schema description and request the effective grid through object metadata.
- Interpret the supported Causeway grid subset for rows, columns, tab groups, tabs, field sets, domain-object headers, actions, properties, collections, and unreferenced-member placement.
- Use deterministic fallback composition modeled on `core/metamodel/.../GridFallbackLayout.xml` when the effective layout is absent or unusable.
- Decompose into existing `<causeway-object-header>`, `<causeway-property>`, `<causeway-action>`, and `<causeway-collection>` elements without duplicating their GraphQL or interaction behavior.
- Provide light-DOM layout regions, diagnostics, customization hooks, responsive behavior, accessible tabs and groups, and real-browser acceptance coverage.
- Revise the generic HTMX viewer to consume `<causeway-object>` rather than implementing its own browser-side object composer.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds a high-level layout-aware semantic object component that composes the existing member component vocabulary.

## Impact

- Affects the web-component foundation module, layout parsing, component registration, styling, tests, sample HTML, and documentation.
- Depends on the reference-app analysis and consumes rich member metadata and collection windowing when available.
- Does not require HTMX and does not own routes, browser history, or application menus.
- Preserves explicit low-level component composition for applications that do not want automatic object layout.
