## Why

The accepted Vaadin integration covers references only, while corrected toolkit-neutral codecs already support the common scalar and temporal input families needed before a safe Vaadin-first policy can be considered.
Those families now need qualified internal adapters, independently lazy and reversible delivery, and executable security and accessibility evidence without changing Causeway's public semantic boundary.

## What Changes

- Add approved free-core internal adapters for single-line text, multiline text, protected text, boolean, enum and bounded choices, exact and machine numeric values, local date, and millisecond-representable local time and local date-time inputs.
- Select adapters from introspected semantic capability and the existing reversible Causeway value codec; unsupported, offset-bearing, zoned, resource, custom, or otherwise unqualified values remain on native or explicit unsupported paths.
- Package a deterministic route-lazy Vaadin field-family closure independently from the existing reference closure, with pinned inputs, checksums, licenses, vulnerability review, compressed budget, and exact CSP style hashes.
- Preserve Causeway ownership of labels, descriptions, required and disabled state, validation, pending values, protected-value redaction, focus, cancellation, themes, semantic events, and GraphQL submission.
- Keep each family independently recoverable to its native semantic editor on unsupported shape, module-load failure, policy failure, or explicit configuration.
- Extend foundation, HTMX, Petclinic, vanilla sample, Reference Application, CSP, accessibility, lifecycle, keyboard, responsive, theme, and route-isolation coverage.
- Continue to exclude Vaadin Flow, Binder, Pro components, Grid behavior, raw application-facing `<vaadin-*>` elements, telemetry, CDN assets, server-side Vaadin state, and speculative resource upload semantics.
- Do not change the default editor policy; this change qualifies adapters for the later `make-vaadin-default-for-webcomponent-viewer` change.

## Capabilities

### New Capabilities

- `vaadin-semantic-editor-families`: Defines qualified free-core field adapters, reversible value-family eligibility, selective delivery, security, accessibility, lifecycle, and fallback boundaries.

### Modified Capabilities

- `domain-web-components`: Adds internal toolkit-backed implementations for existing semantic scalar, choice, numeric, and local temporal editor contracts without changing public elements or events.
- `generic-htmx-web-component-viewer`: Adds independently configurable route-lazy same-origin delivery and exact-hash CSP policy for the approved Vaadin field closure.
- `reference-application-viewer-regression-suite`: Adds deterministic native and candidate qualification journeys across the adopted editor families while retaining visible unsupported resource and custom-value classifications.

## Impact

The change affects the foundation editor registry and interaction elements, a new pinned Vaadin field build closure, HTMX configuration and shell attributes, packaged browser assets and legal metadata, samples, Reference Application targets, and browser evidence.
It adds no application-facing Vaadin API, GraphQL operation, route format, persisted-data migration, or default-policy change.
Every new dependency or generated-byte revision remains coupled to checksum, license, vulnerability, CSP, accessibility, performance, and native-rollback review.
