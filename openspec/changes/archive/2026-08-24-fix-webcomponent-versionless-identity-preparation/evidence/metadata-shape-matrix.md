# Metadata shape matrix

## Reviewed concrete shapes

| Target | Effective metadata type | Advertised fields relevant to identity | Required selection |
|---|---|---|---|
| `demo.BigDecimalEntity` | `rich__demo_BigDecimalEntity__gqlv_meta` | `id`, `logicalTypeName`, `title`, `version` | `_meta { id logicalTypeName title version }` |
| `demo.ActionSemanticsVm` | `rich__demo_ActionSemanticsVm__gqlv_meta` | `id`, `logicalTypeName`, `title`; no `version` | `_meta { id logicalTypeName title }` |
| `demo.CollectionLayoutPagedChildVm` | `rich__demo_CollectionLayoutPagedChildVm__gqlv_meta` | `id`, `logicalTypeName`, `title`; no `version` | `_meta { id logicalTypeName title }` |
| `demo.ActionChoices` parameter choices | concrete choice object metadata | `id`, `logicalTypeName`, `title`; version availability follows the effective type | Advertised subset only |

Targeted introspection is issued as one `__type` operation per metadata type to remain compatible with GraphQL Java's good-faith introspection limit.
The checked-in integration test proves that the versioned entity advertises `version` and the two representative view models do not.

## Representative executable row operation

```graphql
query {
  rich {
    demo_CollectionLayoutPagedPage(object: {id: "<page-id>"}) {
      children {
        get {
          _meta { id logicalTypeName title }
          value { get }
        }
      }
    }
  }
}
```

The operation returns concrete `demo.CollectionLayoutPagedChildVm` rows with non-empty identifiers, logical type, title, and value.
Adding `_meta.version` is invalid for this type.
The corrected browser operation requests only the advertised metadata and reaches `ready` with thirteen navigable object links in the deterministic fixture.

## Identity minimum and optional fields

`id` and `logicalTypeName` are the minimum fields for a navigable bookmark or hydrated row context.
`title` is optional and uses the established identifier presentation fallback.
`version` is optional and remains an advertised concurrency value rather than part of semantic identity.
The client never infers missing identity from a generated GraphQL type name and never manufactures a version.

## Retained focused gaps

`rich__demo_ValueHolder__gqlv_union` requires concrete fragments before member metadata can be projected and remains assigned to the union-projection change.
The long opaque identifier returned for `demo.CompositeValuesPage` still reaches the visible `invalid-route` state and remains assigned to the route-handling change.
Neither gap is evidence that concrete versionless identity failed.
