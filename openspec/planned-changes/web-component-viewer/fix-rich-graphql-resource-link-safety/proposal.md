## Why

The executable reference-application analysis confirmed that rich GraphQL resource references begin with `///graphql/object/`, which standards-compliant browsers interpret as a protocol-relative URL whose host is `graphql`.
It also confirmed that Blob link fields remain present under the default `FORBIDDEN` policy even though the endpoint returns HTTP 403, and that one policy currently controls both structural metadata resources and sensitive value content.
This is immediate contract and safety work needed before components retrieve effective grids, icons, menu layouts, Blob content, or Clob content.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-RESOURCE-02`, `REF-RESOURCE-03`, and `REF-LAYOUT-01`.

## What Changes

- Generate valid same-origin resource references that retain the configured GraphQL application path and optional deployment prefix.
- Omit or return no link capability for forbidden resource categories rather than publishing unusable object-bearing references.
- Separate structural metadata resource policy from binary and character value-content policy.
- Recheck authorization and object visibility when every resource is dereferenced.
- Return bounded content types, cache controls, not-found behavior, and errors without leaking identifiers or policy rules.
- Add deployment-prefix, reverse-proxy, policy, authorization, grid, icon, Blob, and Clob tests.

## Capabilities

### New Capabilities

- `rich-graphql-resource-link-safety`: Defines valid, policy-aware, same-origin references for rich GraphQL metadata and value resources.

### Modified Capabilities

None.

## Impact

- Affects common rich object metadata fetchers, resource URL construction, resource endpoint policy, Blob and Clob wrappers, configuration, tests, and documentation.
- Corrects malformed URLs in place and may remove resource fields or values when policy forbids their category.
- Is a prerequisite for effective-grid consumption, application menu resources, resource widgets, composite object rendering, and the generic viewer.
- Does not add resource upload, new scalar marshalling, layout parsing, or web components.
