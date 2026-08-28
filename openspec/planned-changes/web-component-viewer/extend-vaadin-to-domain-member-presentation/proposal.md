## Why

Vaadin free-core is now the default internal toolkit for eligible editors, but ordinary read-only properties and action controls still use separately styled Causeway-native markup.
That hybrid limits visual continuity between view and edit states and leaves Causeway maintaining parallel scalar-field and button presentation even when the Vaadin toolkit is selected.

## What Changes

- Extend the selected Vaadin toolkit from editor-only behavior to eligible read-only scalar property presentation and ordinary action controls.
- Render qualified read-only text, multiline, protected, boolean, enum, bounded-choice, numeric, date, time, and date-time values through reviewed Vaadin field components in read-only form.
- Keep object references, object links, nulls, resources, LOBs, custom values, unsupported values, collections, and other non-field presentations on their established semantic renderers unless separately qualified.
- Render `<cw-action>` controls through internal `vaadin-button` adapters while preserving visibility, usability, descriptions, disabled reasons, loading state, invocation, result handling, and focus contracts.
- Qualify property edit, save, cancel, clear, and action-prompt controls separately before replacing their native buttons; an unqualified control remains native rather than receiving an approximate adapter.
- Preserve `<cw-property>`, `<cw-action>`, Causeway events, GraphQL contexts, canonical routes, and documented `causeway-*` styling hooks as the public application contract.
- Keep raw `vaadin-*` tags, events, renderer callbacks, and Shadow DOM details internal and unsupported for application integration.
- Evolve the existing toolkit policy so explicit native mode restores both native presentation and native editing without changing application markup, GraphQL operations, or persisted data.
- Accept that eligible read-only object pages request the independently packaged Vaadin field and button closures; route-lazy delivery now means no request until an eligible presentation connects rather than no request until editing begins.
- Require pinned inputs, deterministic generated assets, checksums, licenses, vulnerability review, exact CSP hashes, compressed budgets, accessibility, keyboard, responsive, theme, lifecycle, and fallback evidence.

## Capabilities

### New Capabilities

- `vaadin-semantic-presentation-adapters`: Defines qualified internal read-only field and action-button adapters, eligibility, toolkit selection, delivery, accessibility, lifecycle, theming, and native fallback boundaries.

### Modified Capabilities

- `domain-web-components`: Use qualified Vaadin presentation adapters behind existing property and action semantics while retaining non-field renderers and public Causeway contracts.
- `vaadin-semantic-editor-families`: Coordinate view and edit family selection and fallback so toolkit choice remains coherent across property interaction states.
- `generic-htmx-web-component-viewer`: Broaden toolkit configuration, CSP, asset delivery, rollback, and documentation from editors to eligible presentation controls.
- `reference-application-viewer-regression-suite`: Add default-Vaadin and explicit-native comparison journeys for read-only fields, action buttons, transitions, accessibility, and route loading.

## Impact

Most object pages contain eligible scalar properties or actions and will therefore load Vaadin assets under the default toolkit policy.
Causeway retains semantic state, GraphQL execution, labels, descriptions, validation, redaction, focus restoration, and event ownership, while Vaadin supplies qualified visual controls and their internal interaction behavior.
The change affects field and action renderers, toolkit configuration naming and compatibility, packaged browser closures, CSP, themes, samples, documentation, and browser evidence.
It introduces no Vaadin Flow, Binder, server-side Vaadin state, Pro component, CDN asset, telemetry, raw application-facing Vaadin API, Grid behavior, or Menu Bar behavior.
