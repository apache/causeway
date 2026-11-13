# Follow-on proposal outline: add Vaadin reference-widget pilot

## Suggested change name

`add-vaadin-reference-widget-pilot`

## Why

The free-core Vaadin Combo Box and Multi-Select Combo Box provide a maintained accessible replacement for the Select2 class of reference widget while allowing the GraphQL browser viewer to retain semantic Causeway elements and ordinary custom HTML composition.
The evaluation recommends only a conditional pilot because the current strict style CSP blocks component-originated inline styles and must be resolved before production use.

## What Changes

- Add a stage-zero CSP feasibility and security review with an automatic stop condition.
- Add a pinned selective free-core frontend build invoked and verified by Maven.
- Package only reference-selection modules, shared chunks, accepted licenses, and notices.
- Add Causeway-owned single and multi-reference adapters to the semantic editor registry.
- Map current values, fixed choices, autocomplete search, labels, stable identities, required state, disabled reasons, validation, cancellation, stale responses, and semantic change events through existing GraphQL contexts.
- Bound search-only autocomplete results explicitly without adding a private endpoint.
- Keep the current editor as fallback and leave it selected unless the pilot is explicitly enabled.
- Enable the candidate only in Petclinic and vanilla HTML samples during evaluation.
- Add headless keyboard, axe, dark, narrow, forced-color, reduced-motion, CSP, external-request, lifecycle, bundle, license, Maven, and existing Playwright gates.
- Keep Grid, raw supported Vaadin tags, Flow, and a server-side Vaadin viewer outside scope.

## Expected capability impact

### New capability

- `vaadin-reference-widget-pilot`: Defines the opt-in internal adapters, free-core packaging, autocomplete bounds, CSP gate, budgets, compatibility boundary, and rollback.

### Modified capabilities

- `domain-web-components`: Adds the internal editor-selection behavior and semantic reference-widget acceptance while preserving public tags and events.
- `graphql-web-component-context`: Clarifies bounded autocomplete consumption and cancellation if required without changing the public operation shape.
- `generic-htmx-web-component-viewer`: Adds route-lazy candidate assets and sample configuration while preserving custom fragment composition.

## Acceptance summary

- No Flow runtime, router, protocol, or state dependency.
- No Pro or ambiguous-license package.
- No blanket undocumented unsafe-inline policy.
- Zero unexpected CSP violations and external requests.
- Cold reference closure no more than 65 KB gzip.
- Zero critical or serious automated accessibility violations.
- Keyboard single, multi, clear, validation, cancel, and focus journeys pass.
- Existing semantic events, GraphQL state, routes, custom pages, foundation tests, and Petclinic tests pass.
- Applications not enabling the pilot remain unchanged.
- Disabling the candidate mapping fully restores the previous editor without data or API migration.
