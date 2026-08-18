## Why

Causeway applications organize service actions into primary, secondary, and tertiary menu bars, but the framework-neutral component vocabulary currently stops at bookmark-addressable domain objects.
Applications need to render a complete application menu shell or one semantic bar without adopting HTMX, rebuilding effective-menu parsing, or inventing a second service-action protocol.
The rich GraphQL application entry point and authorized effective menu resource now provide the canonical structural source required by this component slice.
The reference evidence is recorded in `coverage-matrix.yaml` entries `REF-MENU-01`, `REF-COMPONENT-02`, `REF-ACTION-03`, and `REF-ACTION-04`.

## What Changes

- Add public `<causeway-menubars>` as the high-level application-menu coordinator.
- Add public `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>` components that can be coordinated by the composite or used independently.
- Discover the rich GraphQL application capability through targeted introspection, read its menu resource descriptor, and securely fetch and parse the authorized effective Causeway menu-bars XML.
- Share one bounded application-menu generation across composite child bars while allowing standalone bars to own an equivalent private generation and explicit refresh lifecycle.
- Serialize initial targeted discovery within one browser client and make the GraphQL viewer's lazy execution-source initialization safe for concurrent cold-start requests without enabling fetcher parallelism.
- Render ordered menus, sections, text-safe presentation hints, and visible service actions while coordinating current hidden and disabled action state by logical service type.
- Adapt the existing editor, prompt, validation, invocation, cancellation, stale-response, and semantic-result primitives to service actions without requiring an object bookmark or adding another invocation grammar.
- Provide accessible wide and narrow disclosure behavior, keyboard operation, focus restoration, landmarks, empty-state handling, light-DOM styling hooks, and bounded redacted diagnostics.
- Keep routing, browser history, automatic home-page behavior, authentication chrome, and action-result navigation under host policy.
- Add vanilla-HTML, Node, Maven, real-browser, responsive, theme, and accessibility acceptance coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds coordinated and standalone semantic components for Causeway application menu bars and service actions.

## Impact

- Affects the web-component foundation module, GraphQL client operations, GraphQL viewer cold-start initialization, shared structural-resource safety primitives, application-menu coordination, service-action interaction adaptation, styling, tests, sample HTML, and documentation.
- Depends on archived object-interaction correctness, resource-link safety, value semantics, application entry points, and component interaction work.
- Uses the consistent public spelling `causeway-menubar-*`; no `causeway-menu-bar-*` aliases are introduced.
- Adds no public framework-neutral application-context element because coordination remains internal to the composite or standalone bar.
- Requires no HTMX, frontend framework, Bootstrap JavaScript, or host-router dependency.
