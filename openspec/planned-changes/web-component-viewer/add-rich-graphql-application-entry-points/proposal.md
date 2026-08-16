## Why

Rich GraphQL exposes contributing domain services and their actions, but a generic application shell cannot currently recover Causeway's primary, secondary, and tertiary menu structure or identify the configured home-page action through an explicit application-entry contract.
The reference application supplies `menubars.layout.xml`, service-action ordering and grouping, and home-page behavior that should be available without coupling clients to Wicket or exposing metamodel internals.

## What Changes

- Add a rich GraphQL application-entry contract that identifies the current menu-bars layout resource or equivalent structured source.
- Expose primary, secondary, and tertiary menu semantics, ordered menus and sections, service-action references, labels, descriptions, and icons permitted by policy.
- Resolve menu entries to existing rich service-action parameter, validation, invocation, hidden, disabled, and result contracts.
- Identify the configured home-page action and its owning service where available.
- Define authorization filtering, empty-menu behavior, caching, localization, stale-resource, and malformed-layout behavior.
- Avoid duplicating service-action execution or prescribing frontend navigation.

## Capabilities

### New Capabilities

- `rich-graphql-application-entry-points`: Defines framework-neutral rich GraphQL discovery of Causeway menu bars, service-action entries, and the configured home-page action.

### Modified Capabilities

None.

## Impact

- Affects rich GraphQL root metadata, menu-bars layout resource exposure or adaptation, service-action resolution, home-page discovery, tests, and documentation.
- Depends on the completed reference-app analysis and rich member metadata needed for local labels and hints.
- Is a prerequisite for `<causeway-menubars>` and its primary, secondary, and tertiary components.
- Does not implement menus, routing, authentication screens, or a viewer shell.
