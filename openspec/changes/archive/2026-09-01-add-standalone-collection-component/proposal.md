## Why

Collection-valued action results currently fall back to an HTMX-specific count and ad-hoc list even though the semantic component library already owns collection presentation.
A framework-neutral standalone collection component will let action-result hosts present the normalized invocation result consistently without pretending that it is an object member or issuing another GraphQL load.

## What Changes

- Add public `<cw-standalone-collection>` vocabulary for a normalized collection-valued action invocation result supplied through a JavaScript property.
- Render loading-free ready, empty, unsupported, and bounded error states without requiring an object context or collection member identifier.
- Reuse authoritative object metadata, semantic object links, standard scalar value rendering, optional direct-child `<cw-collection-column>` declarations, responsive native table behavior, and qualified Grid presentation where the returned payload contains the required values.
- Keep the component presentation-only: it does not invoke actions, load collection members, fabricate row fields, page, sort, filter, or mutate the normalized result.
- Replace the generic HTMX viewer's ad-hoc collection-result list with `<cw-standalone-collection>` while preserving object-result navigation, scalar and void handling, announcements, dismissal, routing, and application result-policy overrides.
- Add Foundation and HTMX tests, public contract documentation, styling, registration, accessibility, lifecycle, and browser acceptance coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add the framework-neutral standalone collection result component and its public data, rendering, accessibility, and lifecycle contracts.
- `generic-htmx-web-component-viewer`: Present collection-valued action outcomes through the new semantic component instead of viewer-owned ad-hoc list markup.
- `vaadin-collection-grid-adapter`: Qualify finite standalone action-result rows for the existing optional Grid adapter without adding paging or range loading.

## Impact

The change affects Foundation component contracts, collection presentation helpers, registration and exports, styles, action-result tests and documentation, the HTMX result policy, and Petclinic browser acceptance.
It adds no third-party dependency, GraphQL schema field, server route, application markup requirement, or persistence behavior.
