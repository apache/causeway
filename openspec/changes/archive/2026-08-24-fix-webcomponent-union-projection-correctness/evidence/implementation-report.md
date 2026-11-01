# Union projection implementation report

## GraphQL model correction

Repeated registration of one generated union now merges possible types by GraphQL name instead of returning the first immutable union unchanged.
Fields refer to the union by stable `GraphQLTypeReference`, allowing the registry entry to be replaced as incremental metamodel discovery adds members without creating duplicate named schema types.
The runtime resolver derives the concrete generated type, verifies that it is advertised by the completed union, and resolves it from the built schema.

## Foundation correction

The internal selection grammar now supports a reserved `__fragments` map.
Structural and interaction renderers sort fragment names, validate parent abstract kind, advertised membership, concrete type description, and nested fields, and emit `__typename` with GraphQL inline fragments.
Selection merge, difference, and runtime hydration coverage retain fragment structure.

Small abstract collections with at most eight advertised types use direct fragments.
Broader collections issue one typename-only side-effect-free probe, describe at most sixteen observed advertised types and requested wrappers, and replay identical list or window arguments once.
Only replay rows are published.
Unexpected probe typenames are rejected, changed replay types become bounded partial projections, and cancellation or stale generations stop both stages.
Mutating abstract action results remain single-shot and typename-only when direct expansion is unavailable.

## Reference Application outcomes

`rich__demo_ValueHolder__gqlv_union` now advertises 28 deterministic concrete entity types instead of the first discovered member only.
`demo.ActionChoicesFromPage.objects` reaches `ready` through a typename probe and concrete `rich__demo_ActionChoicesFromEntity` fragment, renders semantic object links, and preserves typed row values.
`demo.CollectionTypeOfPage.children` remains a supported concrete `typeOf` collection.
Its raw `otherChildren` collection exposes no readable `get` capability and remains a bounded local error rather than an invalid speculative union operation.

The long opaque composite bookmark remains a separate `invalid-route` gap.
