## Why

Applications can currently compose object headers, properties, actions, and collections explicitly, but every generic consumer must repeat member discovery, layout-resource parsing, fallback placement, and accessibility work.
A framework-neutral `<causeway-object>` component should provide the high-level semantic object projection and decompose it into the established lower-level components using the effective Causeway grid layout.
The evidence is recorded in `coverage-matrix.yaml` entries `REF-LAYOUT-01`, `REF-COMPONENT-01`, `REF-METADATA-02`, and `REF-COLLECTION-02`.

## What Changes

- Add `<causeway-object>` as a public high-level component beneath an authoritative `<causeway-object-context>`.
- Discover object members through the context's targeted rich-schema description and request the effective grid through object metadata.
- Interpret the supported Causeway grid subset for rows, columns, tab groups, tabs, field sets, domain-object headers, actions, properties, collections, and unreferenced-member placement.
- Use deterministic fallback composition modeled on `core/metamodel/.../GridFallbackLayout.xml` when the effective layout is absent or unusable.
- Decompose into existing `<causeway-object-header>`, `<causeway-property>`, `<causeway-action>`, and `<causeway-collection>` elements without duplicating their GraphQL or interaction behavior.
- Provide light-DOM layout regions, bounded diagnostics, customization hooks, responsive behavior, accessible tabs and groups, and real-browser acceptance coverage.
- Leave adoption by the later generic HTMX, Vue, and Svelte viewers to their separate changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds a high-level layout-aware semantic object component that composes the existing member component vocabulary.

## Impact

- Affects the web-component foundation module, layout parsing, component registration, styling, tests, sample HTML, and documentation.
- Depends on the archived object-interaction, structural-resource, value-semantics, and collection-windowing contracts and the completed reference-application analysis.
- Uses the existing targeted rich-schema description and effective-grid resource without depending on the deferred member-metadata change.
- Does not require HTMX and does not own routes, browser history, or application menus.
- Preserves explicit low-level component composition for applications that do not want automatic object layout.
