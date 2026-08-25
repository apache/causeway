## Why

The executable Reference Application analysis confirmed that a GraphQL field description can contain either a friendly name or `describedAs` text but cannot represent both independently.
It also confirmed that effective grid XML already owns structural layout such as positions, grouping, icons, CSS hints, and action placement.
Standalone semantic components nevertheless need a small editor-neutral metadata surface for labels and proactive input hints when no layout resource is available.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-METADATA-01` and `REF-METADATA-02`.

## What Changes

- Add one shared `metadata` object beneath existing rich property, collection, action, and action-parameter wrappers with independently queryable `friendlyName` and nullable `description` fields.
- Add nullable `maxLength`, `pattern`, `patternFlags`, `multiLine`, and `typicalLength` fields to that metadata object, populated only for rich property and action-parameter wrappers.
- Preserve the already-supported resource `fileAccept` fields in their established property-get and action-parameter locations rather than introducing a duplicate metadata field.
- Resolve metadata from static canonical metamodel facets at request time so translations follow the request locale without invoking domain-object naming or description methods.
- Preserve targeted introspection, GraphQL input nullability for requiredness, and authoritative server validation.
- Keep positions, grouping, sequence, icons, CSS, prompt style, redirects, paging, sorting, and complete presentation structure in grid or menu resources.
- Add compatibility, localization, authorization, schema-growth, and reference-derived tests and documentation.

## Capabilities

### New Capabilities

- `rich-graphql-member-metadata`: Defines a narrow additive set of canonical local names, descriptions, and editor-neutral constraints on known rich GraphQL wrappers.

### Modified Capabilities

None.

## Impact

- Affects selected rich wrapper types, one reusable metadata object, a reusable scalar metadata-field helper, metamodel facet adaptation, GraphQL tests, Reference Application evidence, and documentation.
- Depends on the completed Reference Application analysis but does not depend on a particular web-component toolkit.
- Improves standalone property, collection, action, and parameter components and deterministic fallback rendering when a layout resource is unavailable.
- Does not serialize the complete Causeway metamodel, add a member-list endpoint, replace grid or menu resources, or alter existing operation documents.
