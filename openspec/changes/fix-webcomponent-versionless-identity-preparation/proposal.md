## Why

The pinned Reference Application exposes view models and collection rows whose rich metadata advertises stable identity and title but no `version` field.
The generic web-component viewer still hard-codes `_meta.version` in property-reference and collection-row projections, causing otherwise valid preparation, choice, autocomplete, editing, and collection operations to fail visibly.

## What Changes

- Derive identity metadata selections from each effective advertised metadata type rather than assuming `id`, `logicalTypeName`, `title`, and `version` are all present.
- Preserve version when advertised while allowing versionless identities to participate in property values, action-parameter preparation, choices, autocomplete, collection rows, and authoritative refresh.
- Define bounded behavior when metadata lacks fields required for semantic identity instead of inventing a version or bookmark.
- Add focused foundation fixtures and pinned Reference Application browser and integration coverage for versioned and versionless values and rows.
- Reclassify only capability-inventory entries whose executable evidence changes.
- Keep union-fragment projection, opaque route encoding, action placement, GraphQL schema changes, paging, and Vaadin policy outside this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require metadata-aware property, preparation, choice, autocomplete, collection-row, and refresh projections that request only advertised identity fields.
- `reference-application-viewer-regression-suite`: Require executable versionless view-model and row journeys and preserve unrelated union and opaque-route gaps.

## Impact

The change affects internal selection construction in the framework-neutral web-component foundation, its targeted-introspection descriptions and fixtures, and the pinned Reference Application regression corpus.
Public Causeway elements, semantic events, GraphQL operations and generated names, canonical routes, HTMX lifecycle, Vaadin qualification policy, dependencies, and production asset inputs remain unchanged.
