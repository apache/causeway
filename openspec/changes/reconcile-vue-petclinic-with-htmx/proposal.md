## Why

The Vue Petclinic acceptance application uses the same domain and web components as the HTMX application, but its shell, route compositions, and responsive layout currently present materially different navigation, content, and styling.
The HTMX application already demonstrates the intended Petclinic experience, so it should become the explicit reference against which the Vue host is reconciled without moving presentation authority into shared domain components.

## What Changes

- Treat the HTMX Petclinic shell and its HomePage, PetOwner, Pet, and Visit route compositions as the authoritative presentation reference for the Vue Petclinic application.
- Reconcile Vue branding, menu order and grouping, shell dimensions, colors, typography, content spacing, footer content, document titles, route focus treatment, and responsive behavior with the HTMX application.
- Provide exact-type Vue page registrations for the four Petclinic route types and reproduce the HTMX pages' section grouping, member selection, action placement, collection columns, paging, filtering, sorting, row previews, descriptions, and parameter presentation.
- Remove Vue-only exposure of technical or unintended members from those custom Petclinic pages and restore HTMX-only omissions currently present in Vue, including missing owner actions, derived properties, visit notes, and row-preview affordances.
- Preserve the generic Vue fallback as a viewer capability and acceptance scenario using a deliberately unregistered acceptance type or fixture rather than relying on a visible Petclinic route to remain inconsistent.
- Add headless cross-viewer acceptance coverage at representative desktop and narrow viewports, using semantic and computed-layout assertions rather than brittle pixel-perfect screenshot matching.
- Keep routing, GraphQL identity, metadata, validation, invocation, navigation, lifecycle, and component-toolkit policy unchanged and authoritative in their existing layers.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-vue-web-component-viewer`: Strengthen the executable Vue Petclinic acceptance requirement so its application-owned shell and exact-type pages remain presentation-equivalent to the authoritative HTMX Petclinic experience while generic fallback remains independently demonstrated.

## Impact

- Affects the Vue sample shell, application stylesheet, exact-type page registry, Vue single-file page components, generated frontend assets, documentation, and Playwright/integration tests under `viewers/webcomponents/sample-vue-petclinic`.
- May add a small acceptance-only logical type or equivalent fixture in `viewers/webcomponents/sample-petclinic-domain` if needed to retain an executable generic-fallback scenario without changing the four visible Petclinic routes.
- Uses `viewers/webcomponents/sample-htmx-petclinic` as a read-only reference and regression oracle; no HTMX presentation change is intended.
- Does not change the public Vue viewer API, canonical route syntax, GraphQL schema, foundation custom-element contracts, or Vue/Vue Router dependency policy.
