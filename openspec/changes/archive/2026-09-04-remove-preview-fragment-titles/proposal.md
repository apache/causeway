## Why

Collection rows already display and link the authoritative object title beside their preview disclosure.
Repeating that title through `<cw-object-header>` inside the expanded preview adds visual noise without useful information.

## What Changes

- Remove `<cw-object-header>` from Petclinic's HTMX default and inline preview fragments.
- Remove `<cw-object-header>` from the equivalent Vue inline preview fragments.
- Keep page-level object headers and the preview's selected properties, actions, nested collections, context, accessibility label, and lifecycle unchanged.
- Update documentation and regression coverage to establish concise, title-free preview composition.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Petclinic default and inline preview fragments omit the redundant object header/title while preserving useful preview content.
- `generic-vue-web-component-viewer`: Vue's authored Petclinic previews preserve HTMX parity without repeating row titles inside expanded content.

## Impact

Affected files are Petclinic HTMX preview resources and page templates, Vue Petclinic page components and generated assets, preview documentation examples, and HTMX/Vue acceptance tests.
No foundation component contract, GraphQL query, identity, authorization, routing, preview lifecycle, or renderer qualification changes.
