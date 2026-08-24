# Union shape matrix

## Registration evidence

The generated union `rich__demo_ValueHolder__gqlv_union` was registered repeatedly while the Reference Application metamodel was built.
Before correction, introspection retained only the first registration and advertised one possible type, `rich__demo_ActionChoicesFromEntity`.
After correction, the pinned corpus advertises 28 distinct concrete entity types, including `rich__demo_ActionChoicesFromEntity`.
Repeated names are deduplicated and fields refer to the generated union through one stable type reference, so schema construction contains no duplicate named types.

## Executable operation shapes

| Shape | Expected behavior |
|---|---|
| `objects { get { __typename } }` on `demo.ActionChoicesFromPage` | Returns `rich__demo_ActionChoicesFromEntity` rows. |
| `objects { get { _meta { id } } }` | Rejected because `_meta` is not a field of `rich__demo_ValueHolder__gqlv_union`. |
| `objects { get { __typename ... on rich__demo_ActionChoicesFromEntity { _meta { id logicalTypeName title } name { get } } } }` | Returns concrete identity and value through a valid advertised fragment. |
| `children { get { __typename _meta { id logicalTypeName title } } }` on `demo.CollectionTypeOfPage` | Uses the concrete type supplied by `@Collection(typeOf=...)`; no union projection is required. |
| `otherChildren` on `demo.CollectionTypeOfPage` | Exposes no readable `get` capability because the raw collection lacks usable element type metadata; the viewer retains a bounded local error. |

## Client bounds

The direct fragment limit is eight advertised concrete types.
The broad collection probe accepts at most sixteen distinct observed advertised typenames.
A broad collection performs at most one typename probe and one fragment replay with identical list or window arguments.
An unadvertised probe typename is rejected before fragment construction.
A type appearing only during replay remains typename-only with a local partial-projection error and does not trigger another request.

## Retained route gap

The long opaque identifier returned for `demo.CompositeValuesPage` still reaches `invalid-route`.
Union projection preserves the identifier exactly and does not alter route encoding, decoding, or length policy.
