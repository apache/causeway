## Why

Object titles and references currently appear without their domain icon, and the main object title is plain text even though it identifies the current navigable object.
Making titles consistently navigable and icon-bearing improves recognition and navigation without requiring application-specific markup.

## What Changes

- Render the current domain object's displayed title as a semantic link back to that same object.
- Extend semantic object links with an optional decorative icon sourced from authoritative rich GraphQL metadata.
- Request and propagate available icon metadata for object headers, object-valued properties, collection rows, and breadcrumb ancestors.
- Keep links usable when icon metadata is absent, unavailable in an older schema, or resolves to no resource.
- Add accessible, styleable presentation and automated coverage for self-links and icon-bearing navigation links.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require semantic object links, object headers, object references, collection rows, and breadcrumbs to present available domain icons while retaining accessible navigation and icon-free fallback behavior.
- `generic-htmx-web-component-viewer`: Require generic and application-authored object pages to expose the current title as a canonical self-navigation affordance and preserve semantic routing for icon-bearing links.
- `rich-graphql-navigable-breadcrumbs`: Add optional icon metadata to navigable ancestor entries so breadcrumb links can use the same authoritative domain icon contract.

## Impact

The change affects rich GraphQL breadcrumb metadata, semantic GraphQL selections, `<cw-object-link>`, object-header, property-reference, collection-row, and breadcrumb rendering, shared component styling, and foundation/GraphQL/Petclinic verification.
The additive optional icon field and optional component attribute preserve compatibility with older schemas and existing authored markup.
