## Why

Vaadin free-core has passed strict-CSP, accessibility, packaging, GraphQL, lifecycle, native-parity, and broad Reference Application qualification for references and the basic, numeric, and local-temporal editor families.
The viewer should now apply the accepted architecture consistently instead of requiring separate pilot opt-ins while retaining native controls as a one-property rollback and fail-safe implementation.

## What Changes

- **BREAKING** Make qualified Vaadin free-core adapters the default internal implementation for references and the basic, numeric, and local-temporal editor families.
- Add the common HTMX selection property `causeway.viewer.webcomponents.htmx.editor-toolkit=vaadin|native`, defaulting to `vaadin`.
- Give an explicitly configured common policy precedence over deprecated pilot properties.
- When the common property is absent but either deprecated property is explicitly configured, preserve the old independent reference and field-family behavior for a bounded compatibility period.
- Keep `causeway.viewer.webcomponents.htmx.vaadin-reference-widgets` and `causeway.viewer.webcomponents.htmx.vaadin-field-families` readable but deprecated, and document their replacement.
- Keep native semantic editors as explicit rollback, unsupported-shape fallback, family-load fallback, and diagnostic comparison implementations.
- Preserve route-lazy family loading so default selection requests no Vaadin asset on a route without an eligible connected editor.
- Apply only reviewed exact style hashes for effective default families while retaining same-origin scripts, `style-src-attr 'none'`, and no blanket inline permission.
- Replace pilot and sample-scoped production language with supported-default terminology and release obligations.
- Require default and explicit-native Petclinic, vanilla sample, Reference Application, CSP, accessibility, deterministic packaging, license, vulnerability, and compressed-budget gates.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `vaadin-reference-widget-pilot`: Promote qualified reference controls from opt-in pilot behavior to the supported default internal implementation while retaining native rollback and deprecated-property compatibility.
- `vaadin-semantic-editor-families`: Make every qualified field family Vaadin-first under the common selection policy.
- `generic-htmx-web-component-viewer`: Change default configuration, precedence, CSP delivery, documentation, and regression expectations.
- `domain-web-components`: Clarify that default toolkit choice remains internal while Causeway semantic elements, events, GraphQL contexts, and native fallback remain stable.
- `reference-application-viewer-regression-suite`: Make default and explicit-native viewer modes mandatory release qualifications.

## Impact

Eligible controls use reviewed Vaadin visual and interaction details by default, and default HTMX responses include the exact accepted style hashes for qualified packaged families.
Public Causeway elements, semantic events, GraphQL operations, canonical routes, persisted data, and ordinary custom-page composition remain compatible.
No Vaadin Flow, Binder, server-side Vaadin state, Pro component, Grid behavior, upload adapter, telemetry, CDN asset, or raw application-facing Vaadin API is introduced.
Applications can restore native controls immediately with `causeway.viewer.webcomponents.htmx.editor-toolkit=native` without changing GraphQL, routes, data, or application markup.
