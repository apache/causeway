## Context

Causeway's effective menu-bars model has three semantic bars: primary, secondary, and tertiary.
Each contains ordered menus, menu sections, and references to contributing service actions by logical service type and action ID.
The reference application's `menubars.layout.xml` demonstrates extensive explicit grouping and ordering.

The current web-component interaction controller handles parameter editors, defaults, choices, autocomplete, validation, modal prompts, cancellation, and semantic results for object actions.
Menu actions are top-level service actions, so menu components need an application-entry coordinator and a service-action adapter that reuse those interaction primitives without requiring an object bookmark context.

## Goals / Non-Goals

**Goals:**

- Render all three Causeway bars through one high-level component.
- Allow each bar component to be used independently.
- Load and share effective application-entry data once where possible.
- Preserve menus, sections, order, presentation metadata, visibility, and service-action semantics.
- Reuse standard editors, prompts, validation, result events, and cancellation.
- Provide accessible pointer, keyboard, desktop, and narrow-screen behavior.
- Keep light-DOM styling and host integration straightforward.

**Non-Goals:**

- Adding HTMX, routes, browser-history policy, or automatic action-result navigation.
- Rendering authentication, impersonation, notifications, or profile chrome beyond service actions present in the effective menu model.
- Duplicating the GraphQL service-action invocation API.
- Exposing authorization rules or hidden menu entries.
- Reproducing Wicket markup or Bootstrap JavaScript.

## Decisions

### Use consistent public element names

The public names are `<causeway-menubars>`, `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>`.
The compound `menubar` spelling matches the Causeway MenuBars terminology and avoids one inconsistent `menu-bar` name.

### Coordinate once and permit independent bars

`<causeway-menubars>` obtains application-entry data from the nearest GraphQL client, owns one shared generation and cache scope, and composes or supplies data to the three child bar components.
Declaratively supplied bar children are reused; missing bar children are generated in primary, secondary, tertiary order.
A standalone bar can obtain its own application-entry projection from the nearest client under the same contract.

### Adapt established interaction behavior to service actions

Menu entries address the existing rich service-action wrapper by logical service type and semantic action ID.
A service-action adapter reuses editor registry selection, parameter negotiation, modal prompting, validation, stale-response handling, cancellation, and semantic result publication.
It does not need an object bookmark, and it does not create a second GraphQL invocation grammar.

### Render menus as accessible disclosure navigation

Each semantic bar is a labelled navigation landmark when rendered independently, while the composite provides coordinated labels to avoid redundant unnamed landmarks.
Menus use native buttons to open ordered groups of service-action buttons.
Keyboard behavior includes Tab, Shift+Tab, Enter, Space, Escape, Home, End, and documented arrow navigation without requiring hover.
Opening one menu closes its sibling menu within the same bar, and focus returns predictably when a menu closes.

### Preserve semantics across responsive presentation

Wide presentation may arrange bars and menus horizontally.
Narrow presentation may collapse bars or menus into disclosure panels, but DOM order, labels, action order, focus behavior, and semantic events remain unchanged.
Primary, secondary, and tertiary are exposed as data and styling hooks rather than hard-coded visual importance.

### Keep result and navigation policy external

Service action results publish the same semantic scalar, object, collection, and void result events used by object actions.
The host may navigate, render a result region, close a shell menu, or do nothing according to policy.
The components never invoke a home-page action automatically.

### Handle dynamic and partial states locally

Hidden entries are omitted without revealing metadata.
Disabled entries remain represented according to established action semantics and explain their disabled reason accessibly.
An invalid menu reference or one failed service action does not discard unrelated menus or bars.
Empty sections, menus, and bars collapse in that order.

## Risks / Trade-offs

- [Application-entry data can vary by user and locale] → Scope shared state and invalidation to the GraphQL execution context and application-entry generation.
- [Menus can become keyboard traps] → Follow disclosure navigation patterns, rely on native controls, and test focus restoration and Escape extensively.
- [Service actions lack object context] → Reuse interaction primitives through a dedicated service adapter rather than faking an object bookmark.
- [Generated bars can conflict with declarative children] → Capture declarative bar children before custom-element upgrade and generate only missing roles.
- [Large menu models can overwhelm narrow screens] → Preserve hierarchy with collapsible bars and menus and avoid rendering hidden entries.

## Migration Plan

The menu components are additive.
Applications can first use one standalone bar, then adopt the composite.
The generic HTMX viewer can later place `<causeway-menubars>` in its shell without changing component contracts.

## Open Questions

- Should standalone bars share data through an explicit public application context element in a later proposal?
- Should disabled actions remain visible in closed-menu counts?
- Which ARIA disclosure or menubar pattern best matches responsive service-action menus without overloading arrow-key behavior?
- Should the composite generate all three bars or only bars present in effective application-entry data?
