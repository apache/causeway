## Context

Causeway's menu-bars model contains three semantic bars.
Each bar contains ordered menus, each menu contains sections, and sections reference service actions by logical service type and action ID with optional names, descriptions, icons, and supported hints.
Applications may provide `menubars.layout.xml`, while runtime services load and normalize the effective menu model.

The reference resource contains ten primary menus with 37 sections and 141 actions, three secondary menus with 17 sections and 48 actions, and one tertiary menu with four sections and eight actions.
Current rich service fields make those actions executable but do not identify their placement.

The reference home page is a `@HomePage` view-model type rather than a service action.
The current normal rich object lookup requires an object input and cannot construct that configured home instance.

## Goals / Non-Goals

**Goals:**

- Discover the authorized effective primary, secondary, and tertiary menu resource.
- Keep the menu layout resource as the canonical structural source.
- Resolve menu action references to established rich service-action behavior.
- Discover and resolve the configured home-page object or supported home action.
- Preserve ordering, labels, sections, icons, localization, and authorization outcomes.
- Remain framework-neutral and suitable for targeted capability discovery.

**Non-Goals:**

- Rendering HTML menus.
- Adding a duplicate structured GraphQL copy of the complete menu XML.
- Replacing the existing domain-service action schema.
- Exposing `MenuBarsService`, annotations, or metamodel objects directly.
- Defining authentication, user profile, routing, automatic home navigation, or action-result navigation policy.
- Requiring clients to honor CSS hints.

## Decisions

### Add one targeted application-entry capability

The rich root exposes one discoverable application-entry field with optional menu-bars and home-page capabilities.
Clients discover its type through targeted standard introspection.
Existing object and service fields remain unchanged.

### Keep menu structure in the effective resource

The menu-bars capability returns a secured resource reference, media type, format version, and bounded generation or cache information.
The referenced resource represents the effective primary, secondary, and tertiary menu model after Causeway has loaded explicit or generated fallback layout.

GraphQL does not duplicate every bar, menu, section, and entry as another nested schema tree.
The resource-link safety capability owns same-origin URL construction, structural resource policy, and dereference authorization.

### Keep service actions canonical

Each menu resource entry carries the logical service type and semantic action ID needed to address the existing rich service-action wrapper.
Hidden, disabled, parameter, validation, invocation, and result semantics remain owned by that wrapper.
The application-entry contract does not add another invocation endpoint.

Invalid menu references produce bounded diagnostics and do not prevent unrelated valid entries from being consumed.
Optional presentation hints remain optional to clients.

### Filter current visibility without exposing policy

The effective resource omits entries hidden from the current request context or uses an equally non-disclosing effective representation.
No authorization rules, hidden values, or disabled-reason internals are serialized into the menu resource.
Caches are scoped by user or authorization context, locale, layout generation, and other inputs that affect effective menus.

### Represent home page by semantic kind

The home capability identifies whether the configured entry is an object or service action.
For an object home page, it exposes the public logical type and a resolver that returns the current authorized concrete rich object through the corrected polymorphic output contract.
For a supported action home page, it exposes the owning service logical type and semantic action ID and reuses the existing action wrapper.

A missing, hidden, invalid, or unresolvable home page returns documented absence or a bounded diagnostic.
GraphQL does not automatically invoke, render, or navigate to it.

## Risks / Trade-offs

- [A resource requires client parsing] → Menu components own the supported format parser, while GraphQL avoids a duplicate structural API.
- [Menu visibility can be user-dependent] → Scope resource generation and caches to authorization and locale context.
- [Layout may reference missing actions] → Return bounded diagnostics and retain unrelated resource structure.
- [Home object construction differs from identity lookup] → Use the framework's configured home-page resolver and return the standard rich concrete output.
- [Generated fallback menus can change] → Return format and generation information and document cache invalidation.

## Migration Plan

The application-entry root capability is additive.
Existing service fields, object lookups, and invocations remain unchanged.
Clients discover support before requesting menu or home data.

## Open Questions

- The stable format/version marker for current and future menu-bars XML namespaces.
- Whether bounded invalid-reference diagnostics belong beside the application entry or only in opt-in GraphQL diagnostics.
