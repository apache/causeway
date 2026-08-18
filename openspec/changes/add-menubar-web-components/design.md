## Context

Causeway's effective menu-bars model has three semantic bars: primary, secondary, and tertiary.
Each bar contains ordered menus, menu sections, and references to contributing service actions by public logical service type and semantic action ID.
The rich GraphQL application entry point exposes a request-scoped descriptor for the authorized effective menu resource, and that XML preserves explicit or generated Causeway ordering and presentation metadata.
Hidden and invalid action references are already filtered from that resource, while visible disabled actions remain so clients can obtain their current state from the established rich service-action wrappers.

The current web-component interaction controller handles parameter editors, defaults, choices, autocomplete, validation, modal prompts, cancellation, stale results, and semantic outcomes for object actions.
Menu actions are top-level service actions, so this change needs application-menu coordination and a service-action adapter that reuse those primitives without pretending that a service is a bookmark-addressable object.

## Goals / Non-Goals

**Goals:**

- Render all present primary, secondary, and tertiary bars through one high-level component.
- Allow each semantic bar component to be used independently.
- Discover, fetch, securely parse, and share one effective application-menu generation within a composite.
- Preserve effective menus, sections, ordering, text labels, descriptions, icon hints, visibility, availability, and service-action semantics.
- Coordinate current action state by logical service type rather than issuing one GraphQL request per menu entry.
- Reuse standard editors, prompts, validation, invocation, semantic results, cancellation, and stale-response handling.
- Provide accessible pointer, keyboard, desktop, and narrow-screen behavior using native controls and navigation landmarks.
- Keep generated light DOM, styling hooks, diagnostics, and host integration framework neutral.

**Non-Goals:**

- Adding HTMX, routes, browser-history policy, or automatic action-result navigation.
- Adding a public application-context custom element or a generic framework-neutral page-provider abstraction.
- Rendering authentication, impersonation, notifications, or profile chrome beyond service actions present in the effective menu model.
- Duplicating the GraphQL application-entry or service-action invocation APIs.
- Exposing authorization rules, hidden menu entries, raw response bodies, or arbitrary XML markup.
- Reproducing Wicket markup, ARIA application-menu mode, or Bootstrap JavaScript.

## Decisions

### Use consistent public element names

The public names are `<causeway-menubars>`, `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>`.
The compound `menubar` spelling matches Causeway MenuBars terminology and remains consistent across all three semantic bars.

### Coordinate application state internally

`<causeway-menubars>` requests the nearest configured GraphQL client and owns an internal `MenuBarsContextController` for one application-menu generation.
The controller uses targeted introspection to discover the configured rich root, `application` field, and optional `menuBars` descriptor instead of assuming that structural resources are enabled.
It reads application-entry metadata once per generation, validates the opaque origin-relative resource descriptor, fetches it with same-origin credentials and no-store semantics, and provides one immutable menu plan to child bars.
An explicit `refresh()` starts a new generation, aborts superseded metadata, resource, and service-state requests, and re-evaluates authorization-sensitive and locale-sensitive content.
The controller does not retain menu documents or action state across users, GraphQL-client instances, page loads, or explicit generations.

No public application-context element is introduced in this slice.
A bar beneath `<causeway-menubars>` consumes the composite's internal context, while a standalone bar requests the nearest GraphQL client and owns an equivalent private controller.

### Preserve declarative children and generate only present roles

The composite captures declaratively supplied semantic bar children before registration or upgrade and never replaces them merely because it generates layout.
After a usable plan is available, it generates only missing primary, secondary, or tertiary children that contain effective entries.
A declarative child for an absent or empty role receives empty state and exposes no empty interactive landmark.
Generated bars appear in primary, secondary, tertiary document order without duplicating declarative roles.
The composite generates one standard interaction controller for its service actions unless an authored direct child already provides one; a standalone bar does the equivalent only when it is not consuming composite state.

### Reuse bounded structural-resource primitives

The effective menu resource uses the same opaque path, same-origin credentials, no-store cache, redirect rejection, bounded body, abort, and non-disclosing error rules as effective object grids.
Shared structural-resource loading and safe XML tokenization are factored so grid and menu parsing do not drift on security boundaries.
Menu parsing retains the established one-mebibyte input limit, depth limit of 64, node limit of 4,096, and diagnostic limit of 20.
Document type declarations, entity declarations or references, processing instructions beyond the XML declaration, executable elements, malformed XML, wrong namespaces, and oversized content are rejected without rendering response markup.

### Interpret only the canonical menu subset

The parser accepts the Causeway bootstrap menu-bars namespace and the structural sequence `menuBars`, semantic bar, `menu`, `named`, `section`, and `serviceAction`.
A service action must have bounded valid `objectType` and `id` values, and optional `named`, `describedAs`, `cssClass`, and `cssClassFa` content is treated only as text or documented data attributes.
Arbitrary XML is never inserted into `innerHTML`, and untrusted presentation hints are not applied as executable markup, URLs, or unsanitized CSS classes.
Unknown local content produces a bounded diagnostic and is skipped locally where the surrounding bar remains usable.
The server's effective explicit or generated model remains canonical, so the component does not invent a second client-side fallback menu hierarchy when the resource is absent.

### Coordinate action state by logical service type

Menu entries identify an existing rich service-action wrapper by public service logical type and semantic action ID.
The GraphQL client describes each referenced service type through targeted introspection and the application controller batches hidden and disabled state reads by logical service type for one generation.
Schema descriptions reuse the client cache, and a failed service or action remains a bounded local error without discarding unrelated services, menus, or bars.
No action label or sensitive metadata is rendered when the canonical wrapper reports hidden.
Visible disabled actions remain represented, count as visible entries, cannot be invoked, and expose their established disabled reason accessibly.
Empty sections, menus, and bars collapse after current action state is applied.

### Adapt established interaction behavior to service actions

A cached service adapter bound to one logical service type implements the action-context methods consumed by the existing interaction controller.
It reuses editor registry selection, parameter negotiation, defaults, choices, autocomplete, validation, modal prompting, cancellation, stale-response handling, and semantic result publication.
Safe invocation uses the advertised rich service-action query field, while mutating invocation uses the existing top-level mutation field when required.
Mutating service actions for one application-menu controller are serialized in submission order, while superseded transient negotiation requests are aborted or ignored.
The adapter never supplies an object bookmark or `_target` argument for a service action.

Service action request and result events retain their established names and typed results.
Their additive target detail identifies `{kind: "service", logicalTypeName}` while object-action event identity remains unchanged, so a host can distinguish service and object interactions without inferring from generated GraphQL names.
Routing, navigation, shell closing, and result presentation remain host decisions.

### Use disclosure navigation rather than ARIA application-menu mode

Each rendered semantic bar is a labelled navigation landmark.
Menus use native buttons with `aria-expanded` and `aria-controls` to disclose ordered groups of native service-action buttons.
The components do not assign `role="menubar"` or `role="menuitem"`, because these application-mode roles would replace ordinary document navigation and require a different interaction model.
Tab and Shift+Tab follow native document order.
Enter and Space toggle a focused disclosure, Escape closes the active disclosure and restores its originating button, and Home, End, and documented arrow keys move among peer menu buttons without trapping focus.
Opening one menu closes its siblings in the same bar.

### Preserve semantics across responsive presentation

Wide presentation arranges bars and menu disclosures horizontally where space permits.
At the documented narrow threshold, each non-empty bar exposes a native bar disclosure and menus stack in unchanged document order.
Responsive presentation never changes semantic bar order, service-action order, event details, action state, or focus visibility.
Primary, secondary, and tertiary roles are exposed through stable data attributes and CSS variables rather than hard-coded visual importance.

### Keep diagnostics bounded and host policy external

Layout, capability, resource, reference, and service-state failures publish bounded diagnostic codes with service type and action identifiers only when those identifiers are already public structural inputs.
Diagnostics never include response bodies, authorization decisions, credentials, submitted parameter values, or raw exception text from remote content.
Partial failures preserve unrelated successful bars and actions.
The components never resolve or invoke a home page, select routes, mutate browser history, render authentication chrome, or navigate action results.

## Risks / Trade-offs

- [Application-entry resources vary by user and locale] → Scope state to one GraphQL client and explicit generation, use no-store requests, and expose `refresh()`.
- [Many menu actions can cause excessive network work] → Batch current state by logical service type and reuse targeted schema descriptions.
- [Menus can become keyboard traps] → Use native controls and navigation landmarks rather than ARIA application-menu mode, and verify focus restoration and Escape behavior.
- [Service actions lack object identity] → Publish an explicit service interaction target instead of manufacturing a bookmark.
- [Generated bars can conflict with declarative children] → Capture direct semantic children and generate only missing effective roles.
- [Unsafe XML or presentation hints can affect the host page] → Reuse bounded structural parsing and expose hints only as inert text or documented data.
- [Large menu models can overwhelm narrow screens] → Preserve hierarchy with bar and menu disclosures and collapse empty content after authorization state is known.

## Migration Plan

The menu components are additive.
Applications can first use one standalone bar, then adopt the composite without changing service-action or host-event contracts.
The generic HTMX viewer can later place `<causeway-menubars>` in its stable shell while keeping route pages and `<causeway-object>` independent.

## Resolved Planning Questions

- This slice uses an internal coordinator and does not add a public application-context element.
- Visible disabled actions remain in the menu and in visible-entry counts.
- Navigation landmarks and native disclosure buttons are used instead of ARIA `menubar` application mode.
- The composite generates only effective non-empty bar roles and preserves declarative children for all roles.
