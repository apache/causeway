## Context

The framework-neutral components own GraphQL projections, domain behavior, effective object layout, menu layout, interaction state, and semantic results.
A generic viewer still needs a stable shell and a router that maps bookmarks and application entries to pages.

The customization review considered a page provider inside `<causeway-object>` but rejected it.
Every host framework already has a routing boundary that can select a custom page for a logical type or route to a generic page containing `<causeway-object>`.
HTMX expresses that boundary through server routes and replaceable HTML fragments.

## Goals / Non-Goals

**Goals:**

- Provide canonical bookmark routes, deep linking, refresh, and browser history.
- Resolve custom logical-type pages before the generic object page.
- Keep route fragments free of metamodel and layout implementation knowledge.
- Preserve one route-level object context for custom and generic pages.
- Compose menu bars and configurable home-page behavior in a stable shell.
- Provide replaceable result, navigation, page, and theme policy.

**Non-Goals:**

- Teaching `<causeway-object>` about custom pages or routers.
- Parsing grid or menu resources in the HTMX viewer.
- Translating GraphQL JSON or constructing domain operations in HTMX handlers.
- Reproducing authentication pages or every extension of existing viewers initially.
- Defining routing APIs for Vue or Svelte.

## Decisions

### Make routing the customization boundary

The server route resolver accepts a canonical bookmark route and determines the public logical type.
It first checks an application registry keyed by exact logical type.
A match renders the registered HTML fragment or factory beneath the route object context.
Absence renders the generic `<causeway-object>` fragment.

`<causeway-object>` is never asked whether a custom page exists.
It remains a pure semantic object renderer that uses the effective or fallback grid.

### Use canonical bookmark routes

The initial route grammar represents public logical type and identifier as independently encoded path values.
Parsing rejects malformed, ambiguous, unsupported, absent, or unauthorized bookmarks with bounded route states.

Semantic object-navigation events are converted to the same canonical route.
Direct loads, refresh, back, forward, and menu-originated navigation therefore converge on one resolver.

### Keep HTMX above the component data plane

HTMX owns shell navigation, fragment replacement, history, loading indicators, and route errors.
Semantic components continue to obtain all domain state through GraphQL contexts.
HTMX does not construct GraphQL documents, inspect member wrappers, parse layouts, or convert domain values.

### Preserve one route-level object context

Every custom or generic object page is rendered beneath one route-level `<causeway-object-context>`.
Custom pages can compose standard member components, `<causeway-object>`, ordinary HTML, and application custom elements without reimplementing GraphQL execution.
Changing routes creates a new generation and disconnects obsolete page requirements.

### Keep menus in the stable shell

The full-page shell owns `<causeway-menubars>` and the route-content region.
Object-fragment replacement does not recreate menu state.
Authorization, locale, or application-entry generation changes invalidate menus through their own contract rather than incidental page navigation.

### Keep home and result behavior replaceable

The configured home-page object or service action can supply the initial route or result under viewer policy.
Semantic scalar, object, collection, and void outcomes are passed to replaceable result handlers.
Object navigation is a viewer decision rather than automatic component behavior.

### Ship an optional server viewer module

The viewer is disabled unless its module is included and configured.
Its browser package includes HTMX and the Causeway web-component ESM assets under documented versions and content-security policy.
Applications can override fragments, route policy, theme, and result behavior without forking semantic components.

## Risks / Trade-offs

- [Server fragments can duplicate shell state] → Keep menus and global state outside the replaceable object region.
- [Custom fragments can bypass semantic contracts] → Require route context composition and document that domain state remains component-owned.
- [Identifiers contain reserved characters] → Define independent canonical encoding and round-trip route tests.
- [HTMX lifecycle can leave obsolete responses] → Couple fragment generation to route identity and object-context disposal.
- [Viewer routes may diverge from Vue and Svelte] → Maintain shared canonical route and fallback acceptance fixtures across all generic viewers.

## Migration Plan

The viewer is additive and opt-in.
Applications can enable it alongside existing viewers, adopt selected routes, and incrementally register custom logical-type pages.
Rollback removes the optional module or route mapping without changing GraphQL or component contracts.

## Open Questions

- The final canonical route prefix and whether an application can mount it beneath another base path.
- Whether custom fragment registration accepts templates, factories, or both in the first version.
- Whether non-object semantic results use a shell region or application-provided result routes by default.
