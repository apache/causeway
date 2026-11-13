## Why

The HTMX route codec rejects valid Causeway view-model bookmarks longer than 1,024 decoded characters even when their canonical UTF-8 percent-encoded path remains within the existing bounded route envelope.
The pinned Reference Application exposes this defect through `demo.CompositeValuesPage`, whose authoritative opaque identifier currently ends in `invalid-route` instead of reconstructing the object context.

## What Changes

- Define one byte-oriented canonical segment policy shared by browser route construction and server route parsing.
- Accept authoritative opaque identifiers up to the documented encoded-route bound without decoding, shortening, interpreting, hashing, aliasing, or fabricating bookmark content.
- Preserve rejection of malformed escapes, non-canonical encodings, separators, control characters, dot segments, invalid Unicode, and genuinely overlong routes.
- Preserve exact identifiers through semantic navigation, action-result routing, direct loads, HTMX replacement, history restoration, back, and forward navigation.
- Convert the Reference Application composite-values route from a retained `invalid-route` gap into a passing executable regression while retaining unrelated route and union mismatch behavior.
- Keep GraphQL operations, public Causeway elements and events, strict CSP, route-lazy assets, Vaadin policy, and native fallback unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Refine canonical bookmark routing so bounded long opaque identifiers round-trip consistently across browser and server codecs.
- `reference-application-viewer-regression-suite`: Replace the retained composite-values invalid-route assertion with direct-load and history qualification for the exact authoritative identifier.

## Impact

The change affects the HTMX Java and browser route codecs, route unit tests, HTMX documentation, and focused Reference Application integration and Playwright coverage.
It adds no dependency, public GraphQL field, route grammar, application configuration, asset, CSP source, or Vaadin surface.
