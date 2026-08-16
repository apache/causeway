## Why

The rich schema exposes behavioral member state and standard GraphQL descriptions, while many framework-neutral Causeway member semantics remain available only through annotations or layout resources.
A client composing members individually needs canonical names, descriptions, constraints, invocation hints, and fallback ordering without treating a friendly name as a description or reproducing the complete metamodel.

## What Changes

- Add structured metadata to existing rich property, action, parameter, collection, object-meta, and service wrappers.
- Separate canonical friendly names from descriptions.
- Expose the framework-neutral subset of property constraints, editing hints, action semantics and prompt hints, collection presentation and ordering hints, icons, CSS hints, and fallback sequence information confirmed by analysis.
- Retain `_meta.grid`, `_meta.layout`, and menu layout resources as the complete structural composition sources.
- Continue discovering member identifiers through targeted standard GraphQL introspection without adding a duplicate member-list API.
- Add authorization, compatibility, introspection, and reference-derived tests.

## Capabilities

### New Capabilities

- `rich-graphql-member-metadata`: Defines structured framework-neutral metadata on known rich GraphQL object, service, property, action, parameter, and collection wrappers.

### Modified Capabilities

None.

## Impact

- Affects rich GraphQL wrapper types, metamodel facet adaptation, schema descriptions, tests, and documentation.
- Depends on the completed reference-app analysis and may be narrowed by its classification.
- Enables composite object and menu-bar components to render better labels, constraints, ordering, and fallback composition.
- Does not serialize the complete Causeway metamodel or replace grid and menu layout resources.
