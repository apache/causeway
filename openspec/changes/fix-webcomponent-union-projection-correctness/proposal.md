## Why

The pinned Reference Application exposes abstract property and collection values through GraphQL unions such as `rich__demo_ValueHolder__gqlv_union`.
The generic web-component viewer cannot currently render valid inline fragments for those unions, so it either submits invalid `_meta` selections or stops at a bounded abstract-row error instead of presenting the concrete objects returned by GraphQL.

## What Changes

- Merge repeated rich GraphQL union registration so `possibleTypes` retains every concrete type discovered during incremental metamodel construction.
- Extend targeted introspection to retain advertised union and interface possible types and describe only the concrete types needed by an executable projection.
- Add an internal selection representation and renderer for validated GraphQL inline fragments with `__typename` discrimination.
- Project small bounded abstract result sets directly through advertised concrete fragments.
- Resolve broad abstract collection reads through a bounded side-effect-free typename probe followed by one concrete-fragment read for observed types.
- Preserve a bounded typename-only outcome when a safe concrete projection cannot be planned, and never repeat a mutating action to discover its result type.
- Normalize concrete rows and values through existing metadata, identity, hydration, rendering, error, cancellation, and stale-response contracts.
- Add focused foundation and pinned Reference Application coverage while retaining the separate opaque-route gap.
- Keep GraphQL field names, generated type names, action placement, paging, Vaadin policy, and route encoding outside this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `rich-graphql-object-interaction-correctness`: Require repeated generated union registration to preserve the complete discovered concrete membership.
- `domain-web-components`: Require valid bounded inline-fragment projection and concrete semantic normalization for advertised union and interface values.
- `reference-application-viewer-regression-suite`: Require valid fragment evidence where schema membership permits it and an explicit bounded schema/runtime mismatch for the retained raw-list fixture.

## Impact

The change affects internal targeted introspection, executable selection rendering, abstract property and collection read planning, action-result projection policy, row hydration coverage, and the pinned Reference Application regression suite.
Public Causeway elements, semantic events, GraphQL fields and generated names, canonical routes, HTMX lifecycle, dependencies, Vaadin qualification policy, CSP inputs, and production asset URLs remain unchanged.
The advertised membership of an existing generated union is corrected to include all concrete types discovered for that union name.
