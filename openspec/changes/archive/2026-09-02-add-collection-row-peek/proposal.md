## Why

Collections currently provide links and projected columns but require navigation to inspect an object's richer properties, actions, and nested collections.
An optional row peek would let users inspect and interact with one collection element in context without leaving the containing object page.

## What Changes

- Add an optional direct `<cw-peek>` declaration to `<cw-collection>` that enables one expanded row at a time.
- Let a non-empty declaration provide reusable inline preview content composed from ordinary semantic HTML, `<cw-property>`, `<cw-action>`, and `<cw-collection>` elements.
- Let an empty declaration resolve a runtime-type default from `META-INF/causeway/webcomponents/previews/<logical-type-name>.html`, while hiding the row expander when no safe default exists.
- Inject a dedicated hydrated row object context into the live peek so existing member components retain their established metadata, authorization, validation, invocation, and loading behavior.
- Collapse the current peek on Escape, another expansion, collection criteria changes, paging, reload, responsive renderer replacement, or parent lifecycle replacement.
- Refresh and collapse the parent collection after a successful action invocation or property update originating inside the peek.
- Add native and Vaadin Grid row-detail presentation with accessible disclosure, focus restoration, bounded resource handling, deterministic cleanup, and Petclinic qualification.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add the `<cw-peek>` declaration, single-row expansion lifecycle, row-context ownership, accessible collapse, and mutation-driven parent refresh requirements.
- `generic-htmx-web-component-viewer`: Add bounded classpath preview resources, runtime-type resolution, inert parsing, caching, diagnostics, and Petclinic browser qualification.
- `vaadin-collection-grid-adapter`: Add accessible single-row details rendering while preserving Grid paging, virtualization, focus, sorting, responsiveness, and failure isolation.

## Impact

The change affects Foundation collection rendering, context provisioning, component registration, styles, semantic events, Grid projection and widget presentation, and related Node tests.
The HTMX viewer gains preview resource loading, registry and controller support, a client resolver and cache, configuration-compatible reload behavior, integration tests, and diagnostics.
The Petclinic sample gains inline and type-default preview declarations, preview resources, a deterministic mutating row interaction, and Vaadin/native Playwright journeys.
No GraphQL schema change or new third-party runtime dependency is expected because existing row identity, hydration, member requirements, and Vaadin row-details support are reused.
