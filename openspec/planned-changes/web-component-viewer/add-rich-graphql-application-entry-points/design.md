## Context

Causeway's menu-bars model contains three semantic bars.
Each bar contains ordered menus, each menu contains sections, and sections reference service actions by logical service type and action ID with optional names, descriptions, icons, and CSS hints.
Applications may provide `menubars.layout.xml`, while runtime services can load, normalize, and marshal the effective menu model.

Current rich GraphQL top-level service fields make actions executable but do not identify their placement in the effective menu-bars model or the configured home-page action.
A browser should not have to infer menus from schema order or call Wicket internals.

## Goals / Non-Goals

**Goals:**

- Discover effective primary, secondary, and tertiary menu structure.
- Resolve each visible entry to existing rich service-action behavior.
- Discover the configured home-page action.
- Preserve ordering, labels, sections, icons, localization, and authorization outcomes.
- Remain framework-neutral and suitable for targeted capability discovery.

**Non-Goals:**

- Rendering HTML menus.
- Replacing the existing domain-service action schema.
- Exposing `MenuBarsService`, annotations, or metamodel objects directly.
- Defining authentication, user profile, routing, or action-result navigation policy.
- Requiring clients to honor optional CSS hints.

## Decisions

### Expose effective application-entry semantics

The contract represents the menu model after Causeway has loaded defaults or application layout and applied relevant metadata.
It does not expose raw annotation instances or require clients to merge multiple sources.
The final analysis decides whether GraphQL returns a secured layout resource reference, a structured wrapper, or both.

### Keep service actions canonical

A menu entry carries the logical service type and semantic action ID needed to address the existing rich service-action wrapper.
Hidden, disabled, parameter, validation, invocation, and result semantics remain owned by that wrapper.
The application-entry contract does not introduce another invocation endpoint.

### Preserve three semantic bars

Primary, secondary, and tertiary remain explicit rather than arbitrary styling labels.
Order within bars, menus, sections, and entries is stable according to the effective Causeway model.
Missing or empty bars are represented without synthetic actions.

### Filter by current visibility without exposing policy

Entries unavailable to the current user are omitted or marked only according to the established hidden contract.
No authorization rules or hidden action metadata are disclosed.
Caches are scoped by all context that affects menu visibility or localization.

### Represent home page as an action reference

The home-page contract identifies the owning service and semantic action ID and then reuses existing rich service-action interaction.
GraphQL does not prescribe whether a client invokes it automatically or how it displays or navigates to the result.

## Risks / Trade-offs

- [Returning structured menus may duplicate XML] → Choose one canonical semantic model and use a resource plus targeted metadata only when each has a clear role.
- [Menu visibility can be user-dependent] → Scope caches correctly and preserve dynamic service-action checks.
- [Layout may reference missing actions] → Return bounded diagnostics and omit invalid entries without losing unrelated menus.
- [Home-page invocation may have parameters unexpectedly] → Expose the actual action contract and let clients enforce their own landing-page policy.

## Migration Plan

The application-entry root capability is additive.
Existing service fields and invocations remain unchanged.
Clients discover support before requesting menu or home-page data.

## Open Questions

- Should GraphQL expose the marshalled menu-bars resource, a structured shape, or both?
- Where should localized menu labels be resolved relative to schema and resource caching?
- Should disabled service actions remain visible in menu data or be resolved only when their wrapper is requested?
- How should applications with no explicit menu-bars layout expose Causeway's generated fallback menus?
