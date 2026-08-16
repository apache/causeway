## Why

Causeway applications organize service actions into primary, secondary, and tertiary menu bars, but the framework-neutral component vocabulary currently begins at bookmark-addressable domain objects.
Applications should be able to render complete application menus or only one semantic bar without adopting HTMX or rebuilding menu-layout parsing and service-action interaction.
The evidence is recorded in `coverage-matrix.yaml` entries `REF-MENU-01`, `REF-COMPONENT-02`, `REF-ACTION-03`, and `REF-ACTION-04`.

## What Changes

- Add public `<causeway-menubars>` as the high-level application menu coordinator.
- Add public `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>` components that can be composed by the coordinator or used independently.
- Consume the rich GraphQL application-entry contract and effective Causeway menu-bars layout.
- Render ordered menus, sections, labels, descriptions, icons, and visible service actions while reusing established action parameter, editor, validation, invocation, result, and cancellation semantics.
- Provide accessible desktop and narrow-screen disclosure behavior, keyboard operation, focus management, landmarks, empty-state handling, light-DOM styling hooks, and semantic events.
- Keep routing, automatic home-page invocation, authentication chrome, and action-result navigation under host policy.
- Add vanilla-HTML and real-browser acceptance coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds high-level and per-bar semantic components for Causeway application menu bars and service actions.

## Impact

- Affects the web-component foundation module, application-entry coordination, service-action interaction adaptation, styling, tests, sample HTML, and documentation.
- Depends on corrected service-action argument interaction, safe structural resource links, `rich-graphql-application-entry-points`, and the established interaction components; narrow rich member metadata is optional.
- Uses the consistent element spelling `causeway-menubar-*`; no `causeway-menu-bar-secondary` alias is proposed initially.
- Does not require HTMX and can be consumed by any host framework.
