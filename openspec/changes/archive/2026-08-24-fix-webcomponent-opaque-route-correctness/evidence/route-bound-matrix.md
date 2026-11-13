# Route bound and canonicalization matrix

## Safe Reference Application measurement

The authoritative `demo.CompositeValuesPage` identifier returned by `demo.CompositeValueTypeMenu.compositeValueTypes` measures 1,177 Java UTF-16 characters.
Its canonical UTF-8 percent-encoded segment measures 1,179 ASCII characters.
The value itself is intentionally absent from evidence, diagnostics, operation summaries, and test output.
The former decoded-character ceiling of 1,024 rejected it before GraphQL object reconstruction.
The corrected encoded-segment ceiling of 4,096 accepts it without changing any byte of identity.

## Shared codec policy

| Case | Browser generation | Server generation and parsing |
|---|---|---|
| Ordinary ASCII and unreserved punctuation | Preserve unreserved bytes | Preserve unreserved bytes |
| Space, query, fragment, and reserved punctuation | Uppercase UTF-8 percent encoding | Uppercase UTF-8 percent encoding |
| Multibyte Unicode | Encode exact UTF-8 bytes without normalization | Decode strict UTF-8 and require exact re-encoding |
| Long ASCII memento within 4,096 encoded characters | Accept | Accept |
| Exactly 4,096 unreserved ASCII characters | Accept | Accept |
| More than 4,096 UTF-8 bytes | Reject before encoding allocation | Reject before canonical path construction |
| UTF-8 within 4,096 bytes but percent encoding exceeds 4,096 characters | Reject final representation | Reject final representation |
| Empty, `.` or `..` | Reject | Reject |
| Slash, backslash, ISO control, or C1 control | Reject | Reject literal or encoded form |
| Unpaired UTF-16 surrogate | Reject with bounded route error | Reject with bounded route error |
| Malformed UTF-8 or percent escape | Not constructible | Reject |
| Lowercase or unnecessary percent escape | Not emitted | Reject through decode-then-reencode inequality |
| Whole raw route beyond two bounded segments plus fixed syntax | Not emitted | Reject before segment decoding |

## End-to-end paths

The same `canonicalObjectPath` producer serves semantic navigation requests, object results, object homes, collection links, and ordinary anchors.
The server parses every direct, HTMX, and history-restore request into `HtmxObjectRoute` before rendering one route context.
No composite-values special case, query parameter, short token, hash, alias, session state, or server-side identity registry exists.
