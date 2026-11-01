## Why

The Vaadin free-core evaluation found that Combo Box and Multi-Select Combo Box can close the web-component viewer's highest-value reference-widget gap while preserving GraphQL authority and ordinary custom HTML composition.
Production adoption cannot begin until the viewer proves an acceptable strict-CSP integration, because the real Petclinic evaluation detected four component-originated inline-style violations under `style-src 'self'`.

## What Changes

- Add a stage-zero CSP feasibility gate that identifies every Vaadin style violation by component and operation, evaluates narrowly scoped remedies, requires security acceptance, and stops the pilot when compatibility would require an unacceptable blanket inline-style policy.
- Add a pinned, selective, free-core Vaadin build for Combo Box and Multi-Select Combo Box, invoked and verified through Maven with deterministic assets, licenses, notices, checksums, vulnerability evidence, and no runtime CDN or external request.
- Add internal Causeway single-reference and multi-reference adapters that consume existing public GraphQL choice, validation, mutation, and interaction operations without Flow or candidate-only endpoints.
- Preserve stable `<causeway-*>` elements, semantic events, canonical routes, route object contexts, `--causeway-*` variables, and current editors as the default or rollback path.
- Load the candidate assets only for explicitly enabled reference editors and keep routes without those editors free of Vaadin requests.
- Enable the pilot only in Petclinic and vanilla HTML samples while accessibility, lifecycle, CSP, performance, licensing, and compatibility gates are evaluated.
- Keep Grid, broad Vaadin field adoption, supported raw `<vaadin-*>` application APIs, Flow, and any server-side Vaadin viewer outside this change.

## Capabilities

### New Capabilities

- `vaadin-reference-widget-pilot`: Defines the CSP stop gate, selective free-core packaging, internal GraphQL-backed reference adapters, opt-in rollout, budgets, evidence, and rollback requirements.

### Modified Capabilities

- `domain-web-components`: Adds optional semantic single-reference and multi-reference editor behavior while retaining Causeway-owned state, events, validation, identity, and fallback editors.
- `generic-htmx-web-component-viewer`: Adds opt-in route-lazy candidate delivery, strict-CSP compatibility requirements, and sample-level rollout without changing generic or custom page composition.

## Impact

- Affected code may include the web-component foundation editor registry, reference controls, GraphQL adapter/controller modules, component styles, and tests.
- The foundation Maven build may gain a pinned frontend generation step and selective packaged Vaadin assets, but applications that do not enable the pilot remain unchanged.
- Petclinic and vanilla HTML samples gain explicit pilot configuration and browser acceptance coverage.
- The public GraphQL operation shapes do not change; the adapter must document and enforce a safe bound for the current search-only autocomplete response.
- Vaadin Flow, commercial Vaadin components, runtime CDN access, raw-widget compatibility promises, and production-wide default adoption are explicitly excluded.
