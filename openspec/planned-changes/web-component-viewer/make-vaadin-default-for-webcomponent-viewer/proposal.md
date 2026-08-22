## Why

Vaadin free-core has been selected as the default internal widget toolkit for the generic web-component viewer.
After Reference Application qualification, value-correctness fixes, paged autocomplete, and editor-family adapters pass their gates, the viewer should apply that decision consistently rather than require pilot opt-in.

## What Changes

- **BREAKING** Change eligible semantic editor selection from native-first or pilot-opt-in to Vaadin-first by default.
- Replace the pilot-named boolean with a documented selection policy such as `causeway.viewer.webcomponents.htmx.editor-toolkit=vaadin|native`, defaulting to `vaadin` while retaining compatibility mapping for the old property during a deprecation period.
- Keep native semantic editors as explicit rollback, unsupported-shape fallback, load-failure fallback, and diagnostic comparison.
- Preserve route-lazy family loading so default selection does not cause Vaadin requests on routes without eligible controls.
- Apply reviewed exact-hash CSP policy by default only for enabled packaged families and retain `style-src-attr 'none'` with no blanket inline permission.
- Remove pilot and sample-scoped language from production documentation, support policy, configuration metadata, and specifications.
- Define supported browser, accessibility, dependency-update, CSP-hash, license, vulnerability, bundle-budget, and rollback obligations as release gates.
- Require the Reference Application, Petclinic, vanilla sample, existing viewer suites, and clean Maven packaging to pass with default configuration and with explicit native rollback.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `vaadin-reference-widget-pilot`: Promotes qualified reference controls from opt-in pilot behavior to the supported default internal implementation and retains native rollback.
- `vaadin-semantic-editor-families`: Makes every qualified editor family Vaadin-first under the common selection policy.
- `generic-htmx-web-component-viewer`: Changes default configuration, CSP delivery, documentation, and regression expectations.
- `domain-web-components`: Clarifies that toolkit choice remains internal while Causeway semantic elements and events remain the stable application API.
- `reference-application-viewer-regression-suite`: Makes default and native-rollback viewer modes mandatory release qualifications.

## Impact

Default visual and interaction details of eligible controls change, and enabled HTMX responses gain reviewed CSP style hashes for the applicable default families.
Public Causeway elements, semantic events, GraphQL operations, canonical routes, persisted data, and ordinary custom-page composition remain compatible.
Applications can restore the prior implementation immediately with the documented native policy, but the compatibility property and pilot terminology become deprecated.
