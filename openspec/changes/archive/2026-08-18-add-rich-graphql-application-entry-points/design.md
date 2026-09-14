## Context

Causeway's menu-bars model contains three semantic bars.
Each bar contains ordered menus, each menu contains sections, and sections reference service actions by logical service type and action ID with optional names, descriptions, icons, and supported hints.
Applications may provide `menubars.layout.xml`, while runtime services load and normalize the effective menu model.
The current menu-bars XML format uses the `https://causeway.apache.org/applib/layout/menubars/bootstrap3` namespace.

The reference resource contains ten primary menus with 37 sections and 141 actions, three secondary menus with 17 sections and 48 actions, and one tertiary menu with four sections and eight actions.
Current rich service fields make those actions executable but do not identify their placement.

The reference home page is a `@HomePage` view-model type.
The public `HomePageResolverService` returns a domain object or null and has no semantic service-action descriptor, so this change supports object home pages only and does not advertise an unimplemented action kind.
The current normal rich object lookup requires an object input and cannot construct the configured home instance.

## Goals / Non-Goals

**Goals:**

- Discover the authorized effective primary, secondary, and tertiary menu resource.
- Keep the menu layout resource as the canonical structural source.
- Resolve menu action references to established rich service-action behavior.
- Discover and resolve the domain object returned by `HomePageResolverService`.
- Preserve ordering, labels, sections, icons, localization, and authorization outcomes.
- Remain framework-neutral and suitable for targeted capability discovery.

**Non-Goals:**

- Rendering HTML menus.
- Adding a duplicate structured GraphQL copy of the complete menu XML.
- Replacing the existing domain-service action schema.
- Exposing `MenuBarsService`, annotations, or metamodel objects directly.
- Defining authentication, user profile, routing, automatic home navigation, action-result navigation policy, or a new home-action configuration mechanism.
- Requiring clients to honor CSS hints.

## Decisions

### Add one targeted application-entry capability

The rich root exposes one discoverable application-entry field with optional menu-bars and home-page capabilities.
Clients discover its type through targeted standard introspection.
Existing object and service fields remain unchanged.

### Keep menu structure in the effective resource

The menu-bars capability returns a secured resource reference, media type, namespace format version, content generation, and explicit `private, no-store` cache policy.
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
The first implementation does not cache effective menu content and returns `private, no-store`, preventing reuse across user, role, locale, layout generation, or other menu-affecting contexts.

### Represent the supported home page as an object

The home capability identifies the supported kind as `OBJECT` and exposes the public logical type together with the current authorized concrete rich object through a generated union of eligible rich object types.
The object is obtained from `HomePageResolverService`, adapted inside the current Causeway interaction, and checked for framework and user visibility.

A missing, hidden, invalid, non-domain, or unresolvable home page returns documented absence together with a bounded diagnostic where appropriate.
GraphQL does not advertise a service-action kind because the current public resolver provides no such semantic descriptor.
GraphQL does not automatically invoke, render, or navigate to the object.

## Risks / Trade-offs

- [A resource requires client parsing] → Menu components own the supported format parser, while GraphQL avoids a duplicate structural API.
- [Menu visibility can be user-dependent] → Generate per request inside the interaction and return `private, no-store`.
- [Layout may reference missing actions] → Return bounded diagnostics and retain unrelated resource structure.
- [Home object construction differs from identity lookup] → Use the framework's configured home-page resolver and return the standard rich concrete output.
- [No public home-action descriptor exists] → Support only resolver-returned domain objects and leave action-based home configuration to a separate future proposal.
- [Generated fallback menus can change] → Return format and generation information and document cache invalidation.

## Migration Plan

The application-entry root capability is additive.
Existing service fields, object lookups, and invocations remain unchanged.
Clients discover support before requesting menu or home data.

## Open Questions

- Whether a future menu-bars namespace should remain a format marker or introduce explicit resource-version negotiation.
- Whether a future public home-entry API should represent service actions as well as objects.
