## Why

The accepted Vaadin integration currently covers only single-reference and supported multi-reference inputs.
Making Vaadin the viewer-wide internal default requires qualified Causeway-owned adapters for the remaining common input families without exposing raw Vaadin APIs or creating one eager broad bundle.

## What Changes

- Add approved free-core adapters for text, multiline text, boolean, enum and bounded choices, exact numeric values, date, time, date-time, and supported resource upload inputs.
- Select adapters by introspected semantic capability and corrected value codec rather than by toolkit-specific application markup.
- Build deterministic route-lazy component-family closures with independent checksums, license manifests, CSP style hashes, and compressed budgets.
- Keep each family independently recoverable to the existing native semantic editor on unsupported shape, load failure, policy failure, or explicit configuration.
- Map Causeway labels, descriptions, required and disabled state, validation, pending values, focus, cancellation, themes, reduced motion, forced colors, and semantic events into each internal control.
- Extend the CSP matrix, accessibility suite, lifecycle suite, Petclinic, vanilla sample, and Reference Application coverage for every adopted family.
- Exclude Flow, Binder, Pro components, raw application-facing Vaadin tags, Grid query behavior, and server-side Vaadin state.

## Capabilities

### New Capabilities

- `vaadin-semantic-editor-families`: Defines the approved free-core field-family adapters, selective delivery, security, accessibility, lifecycle, and fallback boundaries.

### Modified Capabilities

- `domain-web-components`: Adds internal toolkit-backed implementations for existing semantic editor contracts without changing public elements or events.
- `generic-htmx-web-component-viewer`: Adds route-lazy same-origin delivery and CSP policy for the approved editor-family closures.

## Impact

The change expands pinned npm inputs, generated browser assets, CSP hashes, packaged licenses, Maven verification, foundation adapters, HTMX shell configuration, and browser evidence.
Every new dependency revision remains coupled to checksum, license, vulnerability, CSP, accessibility, and performance review.
The change qualifies the families but does not yet change the default selection policy.
