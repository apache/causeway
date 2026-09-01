## Why

Collection-valued action results currently use generic identity-only presentation in a shell-owned location, so applications cannot declaratively choose useful result columns or place action outcomes within their page layout.
The viewer needs reusable type defaults, action-specific overrides, and an explicit result outlet while retaining one authoritative action invocation and host-owned result policy.

## What Changes

- Discover default standalone collection presentations from `META-INF/causeway/webcomponents/collections/<logicalTypeName>.html`.
- Allow a direct-child `<cw-standalone-collection>` within `<cw-action>` to override the default presentation for that action without becoming the live result node.
- Add `<cw-action-results>` as a passive page outlet into which the viewer can mount successful action-result presentation.
- Resolve and snapshot collection presentation before invocation so declared columns can be validated and included in the original GraphQL action-result selection.
- Keep returned GraphQL values authoritative and prohibit row hydration, follow-up member reads, inferred values, paging, sorting, filtering, or mutation by standalone results.
- Preserve application result overrides and existing object, scalar, void, announcement, focus, dismissal, route, and stable-shell fallback behavior.
- Advertise the authoritative collection result element logical type through additive rich GraphQL action metadata so clients do not infer domain identity from generated GraphQL names.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Define nested standalone collection action presentation, action result outlet semantics, pre-invocation column selection, immutable presentation snapshots, and lifecycle safety.
- `generic-htmx-web-component-viewer`: Define classpath collection-presentation discovery, deterministic outlet resolution, fallback behavior, and HTMX action-result routing.
- `rich-graphql-member-metadata`: Advertise authoritative action result collection element logical type metadata for presentation lookup.

## Impact

The change affects Foundation action declaration capture, interaction preparation and invocation selection, action-result event detail, standalone collection configuration, public element contracts, and component registration.
It affects the HTMX classpath resource loader and registry, page result routing, shell fallback region, diagnostics, and application result policy integration.
It adds an optional field to rich GraphQL action metadata and corresponding metamodel-backed tests without changing invocation authority or domain validation.
It requires Foundation, HTMX, GraphQL, resource-loading, browser accessibility, native-policy, Vaadin Grid, route-lifecycle, and Petclinic acceptance coverage.
