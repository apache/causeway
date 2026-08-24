## Context

The canonical route grammar stores the public logical type and authoritative object identifier as independent UTF-8 percent-encoded path segments.
Both Java and browser codecs currently reject decoded text longer than 1,024 UTF-16 characters before checking the existing 4,096-character encoded-segment envelope.
That decoded-character limit is not aligned with HTTP representation cost, differs subtly between Java and JavaScript Unicode models, and rejects the valid memento bookmark returned for `demo.CompositeValuesPage`.
The server consequently returns the safe `invalid-route` fragment before GraphQL can reconstruct the view model.

The correction crosses browser route generation, server parsing, direct loads, HTMX history, and Reference Application qualification.
It must retain strict canonicalization and denial-of-service bounds without interpreting an opaque Causeway bookmark.

## Goals / Non-Goals

**Goals:**

- Round-trip valid authoritative opaque identifiers whenever each canonical encoded segment is at most 4,096 ASCII characters.
- Use equivalent browser and server validation for Unicode, separators, controls, dot segments, canonical percent encoding, and encoded length.
- Preserve exact identity through direct loads, semantic navigation, action results, HTMX replacement, history restoration, back, and forward.
- Convert the pinned composite-values route into a ready supported regression.
- Retain bounded non-disclosing invalid-route behavior for malformed and overlong input.

**Non-Goals:**

- Do not interpret, decompress, normalize, truncate, hash, alias, persist, or otherwise transform opaque bookmarks.
- Do not add query-string, fragment, POST, cookie, session, or server-side route-token alternatives.
- Do not change the route grammar, public semantic navigation event, GraphQL identity, metamodel bookmark format, or application configuration.
- Do not alter unrelated union mismatches, value rendering, Vaadin policy, assets, dependencies, CSP, or native fallback.

## Decisions

### Bound canonical encoded representation rather than decoded UTF-16 length

Each independently encoded segment remains limited to 4,096 ASCII characters, and the complete server path retains its existing bound derived from two segments plus fixed route syntax.
Before encoding caller-provided text, each codec first rejects values whose UTF-8 byte representation already exceeds the encoded bound, then rejects a final percent-encoded representation over 4,096 characters.
Server parsing rejects incoming encoded segments over the same bound before allocation and requires decode-then-reencode equality.

This policy measures the actual route representation, accepts long ASCII-oriented mementos, and keeps allocation bounded.
Increasing the decoded character limit alone was rejected because Java UTF-16 length, JavaScript string length, UTF-8 bytes, and percent-encoded size are different units.
Removing all limits was rejected because opaque identity does not justify unbounded request or browser-history state.

### Retain strict segment canonicalization

The codecs continue to reject empty values, `.` and `..`, literal or encoded slash and backslash, ISO controls, malformed UTF-8, malformed percent escapes, lowercase or unnecessary percent escapes, non-ASCII literals, and unpaired UTF-16 surrogates supplied to route generation.
The browser codec explicitly validates Unicode before `encodeURIComponent` and reports the same bounded route error instead of leaking a native `URIError`.
No Unicode normalization is applied because it would alter identity.

Allowing alternate spellings and normalizing them was rejected because one semantic identity must have one history and cache key.
Allowing encoded separators was rejected because containers and proxies may decode paths at different stages.

### Keep identity transport end to end

All producers continue to call `canonicalObjectPath`, and the server continues to parse into `HtmxObjectRoute` before rendering one object context.
The correction therefore applies uniformly to object links, collection links, home objects, action results, direct URLs, and history navigation without special-casing composite values.
Tests compare the reconstructed context's exact `object-id` with the GraphQL-returned identifier and exercise route replacement and history restoration.

A server-side short-token registry was rejected because it would make bookmarks session-dependent, introduce state and authorization complexity, and break direct loads.
A query parameter was rejected because it changes the established public grammar and does not remove practical request-size limits.

### Preserve bounded failure and response behavior

Malformed or excessive routes still produce the existing non-disclosing `invalid-route` state and canonical root history instruction.
No rejected route content enters HTML, headers, errors, events, logs, or diagnostics.
Valid long identifiers are emitted only as safely escaped HTML attributes by the existing renderer and as encoded URLs by the route policy.

## Risks / Trade-offs

- [Longer accepted URLs may encounter external proxy limits below the viewer bound] → Keep the documented 4,096-character per-segment ceiling, qualify the embedded server, and document that deployments must permit supported canonical routes.
- [Browser and Java codecs could drift] → Maintain a shared vector matrix covering ASCII, multibyte Unicode, reserved punctuation, malformed Unicode, the maximum boundary, and one-character overflow.
- [Long attribute values could increase page size] → Permit at most the same bounded identity already present in the GraphQL response and render one route context only.
- [A container could decode separators before the application] → Continue rejecting both literal and encoded separators and test the raw request URI contract.

## Migration Plan

No data or configuration migration is required.
Deploy the corrected codecs together so browser-generated routes and server parsing use the same bound.
Rollback restores the former conservative rejection and causes long memento bookmarks to return `invalid-route`; it does not corrupt persisted data or identity.

## Open Questions

None.
