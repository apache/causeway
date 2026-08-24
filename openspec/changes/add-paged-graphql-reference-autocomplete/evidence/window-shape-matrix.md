# Autocomplete window shape matrix

## Public GraphQL fields

| Context | Existing field | Additive field |
|---|---|---|
| Rich property wrapper | `autoComplete(search): [Item]` | `autoCompleteWindow(search, offset = 0, size = configuredDefault): Window` |
| Rich object-action parameter wrapper | Existing declared preceding arguments plus `search` | Same declared preceding arguments plus `search`, `offset`, and `size` |
| Rich service-action parameter wrapper | Existing declared preceding arguments plus `search` | Same declared preceding arguments plus `search`, `offset`, and `size` |

Every generated window contains `items`, `offset`, `requestedSize`, `returnedCount`, `totalCount`, `maximumSize`, `hasPrevious`, `hasNext`, and `ordering`.
`items` uses the same advertised output type as legacy autocomplete.
`ordering` is `APPLICATION`, meaning the authoritative application encounter order for that execution.
No cursor, persistence query pushdown, or cross-request snapshot is claimed.

## Bounds

| Request | Result |
|---|---|
| Offset omitted | Uses zero. |
| Size omitted | Uses `causeway.viewer.graphql.autocomplete.default-window-size`. |
| Valid offset and size | Invokes domain autocomplete once and returns at most `size` items. |
| Offset at or beyond total | Returns an empty successful window with accurate total and continuation metadata. |
| Negative offset | Bounded error before domain invocation. |
| Zero or negative size | Bounded error before domain invocation. |
| Size above configured maximum | Bounded error before domain invocation. |

Defaults are 20 items and a 100-item hard maximum.
The Reference Application qualification overrides both values to five.

## Safe deterministic Reference Application evidence

The search text itself and returned identifiers are omitted from evidence.
The approved one-character filter yields seven deterministic `TvCharacter` references in application encounter order.
The first five-item window reports five items and `hasNext=true`.
The second window at offset five reports two items, `hasPrevious=true`, and `hasNext=false`.
The concatenated window identities exactly match the unchanged seven-item legacy result.
An offset of 100 returns zero items and retains total count seven.

## Client behavior

Targeted introspection follows the generated window type, item type, concrete metadata type, defaults, and advertised scalar metadata.
Object and service contexts return one immutable semantic shape and retain existing list-returning methods.
Vaadin receives `{filter, page, pageSize}` and calls the semantic context with `offset = page * pageSize`; its callback receives normalized items and authoritative total count.
Native presentation renders the first bounded window and states when more matches require refinement.
Legacy-only servers retain the existing complete-response bound and visible over-bound failure.
