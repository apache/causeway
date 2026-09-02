## Why

The HTMX viewer currently manufactures `<cw-graphql-client>` and `<cw-object-context>` around application content, even though those elements are framework-neutral public composition boundaries.
Making applications author the semantic hierarchy directly leaves HTMX, Vue, Svelte, and Angular responsible primarily for routing and route-value binding, while preserving one reusable web-component data plane.

## What Changes

- **BREAKING** Require HTMX application shell markup to declare its shared `<cw-graphql-client>` provider instead of having `HtmxPageRenderer` create that element.
- **BREAKING** Require each resource-backed or factory-backed HTMX object page to declare exactly one route-level `<cw-object-context>` instead of receiving an injected wrapper.
- Add a bounded HTMX binding step that supplies the configured GraphQL endpoint and canonical route identity to the declared elements without creating semantic context elements.
- Move the route page, interaction controller, generic `<cw-object>` fallback, and related semantic hierarchy into declarative HTML resources.
- Fail closed with bounded diagnostics when authored shell or object-page markup omits, duplicates, or incorrectly nests required context boundaries.
- Preserve the stable shared GraphQL client, menu shell, schema cache, canonical routing, result policy, authentication executor, and deterministic context teardown.
- Update the planned Vue and Svelte viewers to bind route values into application-authored context elements rather than creating those elements in framework adapters.
- Add a parallel planned Angular Router viewer using the same declarative ownership and thin-router contract.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `graphql-web-component-context`: Make declarative application ownership of GraphQL-client and object-context elements an explicit framework-neutral composition contract, including inert missing-identity behavior suitable for router binding.
- `generic-htmx-web-component-viewer`: Replace backend-manufactured semantic wrappers with validated, application-authored shell and object-page context boundaries whose runtime values are bound by the HTMX adapter.

## Impact

- Affects the HTMX page renderer, page registries/loaders, controller responses, route bootstrap module, HTML resource locations, and diagnostics.
- Requires migration of Petclinic object-page resources and HTMX test fixtures to include their own route-level context hierarchy.
- Changes HTMX extension contracts for resource and factory page authors; legacy inner-content-only pages will fail closed rather than receive implicit wrappers.
- Updates planned-change documentation for generic Vue and Svelte viewers and introduces corresponding Angular planning artifacts.
- Does not change GraphQL schema authority, canonical route meaning, semantic component APIs, or domain interaction behavior.
