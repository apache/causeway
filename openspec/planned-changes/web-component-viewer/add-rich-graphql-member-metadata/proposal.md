## Why

The executable reference-application analysis confirmed that GraphQL field descriptions can contain either a friendly name or `describedAs` text but cannot represent both independently.
It also confirmed that effective grid XML already carries most member positions, grouping, labels, descriptions, icons, CSS hints, action positions, and property layout hints.
The GraphQL change should therefore remain small and provide only independent local semantics needed by standalone components or deterministic fallback, without copying the grid vocabulary.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-METADATA-01` and `REF-METADATA-02`.

## What Changes

- Add independently queryable canonical friendly name and description to existing known rich wrappers.
- Add only confirmed local property and parameter constraints that a standalone editor needs, such as maximum length, regular-expression intent, accepted-file values, multiline, and typical length.
- Preserve standard targeted introspection for member discovery and existing GraphQL input nullability for requiredness.
- Keep positions, grouping, sequence, icons, CSS, prompt style, redirects, paging, sorting, and complete presentation structure in grid or menu resources.
- Add localization, authorization, compatibility, and reference-derived tests.

## Capabilities

### New Capabilities

- `rich-graphql-member-metadata`: Defines a narrow set of independent local names, descriptions, and editor-neutral constraints on known rich GraphQL wrappers.

### Modified Capabilities

None.

## Impact

- Affects selected rich wrapper types, metamodel facet adaptation, schema descriptions, tests, and documentation.
- Depends on completed reference-app analysis but is not a prerequisite for effective-grid object composition or menu-bar structure.
- Improves standalone property, action, parameter, and collection components and fallback rendering when a layout resource is unavailable.
- Does not serialize the complete Causeway metamodel or replace grid and menu resources.
