## Why

Application-authored HTMX and framework hosts can currently compose semantic members but must fall back to arbitrary wrapper markup for layout rows, columns, fieldsets, and tabs.
They also lack a concise, authoritative way to present the effective `metadata` fieldset and its panel actions without manually duplicating framework member knowledge.

## What Changes

- Add public framework-neutral `<cw-fieldset>`, `<cw-row>`, `<cw-column>`, `<cw-tabgroup>`, and `<cw-tab>` layout components with explicit nesting contracts.
- Support `<cw-column span="1..12">` through a responsive twelve-column layout while rejecting or safely normalizing invalid spans.
- Give tab groups accessible selection, keyboard navigation, and lifecycle behavior without host-framework ownership.
- Add `<cw-metadata>` to load the current object's authoritative effective layout, render all properties assigned to fieldset id `metadata`, and place its associated actions in an accessible panel dropdown.
- Preserve canonical member identity, authorization, ordering, interaction, structural-resource security, and object-context lifecycle behavior.
- Document and qualify equivalent behavior in foundation, HTMX, and Vue usage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Define the new public declarative layout components, nesting rules, responsive spans, accessible tabs, diagnostics, and metadata panel behavior.
- `graphql-web-component-context`: Permit `<cw-metadata>` to consume the already-authorized effective grid resource and object schema through the existing coordinated object context.
- `generic-htmx-web-component-viewer`: Demonstrate and qualify authored layout components in the HTMX Petclinic application.
- `generic-vue-web-component-viewer`: Demonstrate equivalent authored layout composition in the Vue Petclinic application without Vue-owned domain behavior.

## Impact

The change affects foundation element contracts, registration, layout parsing/rendering helpers, styles, tests, documentation, Petclinic page composition, browser acceptance coverage, and generated Vue assets.
It introduces no new third-party dependency, GraphQL endpoint, host routing contract, or authority outside existing metadata and effective layout resources.
