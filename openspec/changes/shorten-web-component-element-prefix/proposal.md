## Why

The public `causeway-` custom-element prefix makes authored pages and selectors unnecessarily verbose, especially where semantic elements are composed repeatedly.
The shorter `cw-` namespace keeps the Causeway identity while making application markup more concise and readable.

## What Changes

- **BREAKING** Rename every Causeway-owned custom-element tag from `causeway-*` to `cw-*`, including the GraphQL provider, contexts, domain components, menu components, collection declarations, and internal field/reference editor hosts.
- Update registration constants, generated markup, type selectors, query selectors, layout composition, HTMX shell rendering, custom HTML pages, samples, tests, and documentation to use the new names.
- Do not register compatibility aliases for `causeway-*`; old application markup must migrate to `cw-*`.
- Preserve JavaScript class names, Java class and package names, semantic `causeway-*` event names, `causeway-*` CSS classes, `data-causeway-*` attributes, `--causeway-*` CSS variables, and `/causeway-*` asset paths.
- Add an audit that rejects remaining old custom-element tags while allowing those intentionally retained non-element contracts.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `graphql-web-component-context`: Rename the provider, object context, and minimal context-validation element vocabulary to `cw-*`.
- `domain-web-components`: Rename the public domain, editor, collection, and menu element vocabulary to `cw-*`.
- `generic-htmx-web-component-viewer`: Render and compose the HTMX shell, routes, custom pages, and generic fallback with `cw-*` elements.
- `web-component-theming-kit-analysis`: Treat `cw-*` elements as the stable element boundary while retaining existing Causeway classes, events, and variables.
- `vaadin-web-component-graphql-viewer-evaluation`: Compose evaluated widgets beneath the renamed `cw-*` context and domain elements.

## Impact

The change affects the complete Web Components source, HTMX server renderer and browser bridge, static and Petclinic samples, JavaScript and Java tests, Playwright selectors, documentation, and five main OpenSpec capabilities.
It is source-incompatible for applications that author or query old `<causeway-*>` tags.
It changes no GraphQL schema or operation, semantic event name, CSS customization token, server route, asset URL, Java API, or persisted data.
