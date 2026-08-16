## Context

The preceding changes establish a framework-neutral GraphQL client, object context, semantic member components, a layout-aware `<causeway-object>`, and `<causeway-menubars>` with primary, secondary, and tertiary subcomponents.
Those components are sufficient for application developers to build custom pages and shells, but they do not provide routes, browser history, HTMX fragment lifecycle, or default page selection.
This change adds an optional reference viewer while delegating all domain, menu, grid, interaction, and GraphQL behavior to the component library.

## Goals / Non-Goals

**Goals:**

- Provide an application shell containing framework-neutral Causeway menu bars.
- Provide directly addressable generic domain-object pages.
- Use `<causeway-object>` as the default object page beneath one route context.
- Use HTMX for shell navigation, history, and fragment replacement without adding HTMX to component contracts.
- Allow a logical type to replace the default object page with an application page composed from the same semantic components.
- Preserve loading, partial-error, terminal-error, interaction, and navigation semantics across page transitions.
- Provide replaceable home-page, navigation, result, and theme policies.

**Non-Goals:**

- Requiring HTMX for direct component-library consumers.
- Replacing the GraphQL schema with server-side metamodel access.
- Parsing object grid or menu-bars layout resources in the viewer.
- Reimplementing property, action, collection, object composition, menu interaction, value, validation, or mutation semantics.
- Full parity with authentication pages, standalone values, or every extension component.
- A general-purpose frontend framework.
- Server-side rendering of complete domain state.

## Decisions

### Keep HTMX above the semantic component layer

HTMX manages viewer routes, browser history, page-shell regions, loading indicators, and HTML fragment transitions.
The shell and fragments compose semantic custom elements, whose GraphQL client and contexts continue to own application-entry reads, object reads, interactions, and mutations.

The viewer does not add HTMX attributes to public component contracts and does not require an HTMX-to-GraphQL transport extension.
Another application can therefore replace the viewer while reusing the same components.

### Compose menu bars in the stable shell

The stable application shell contains `<causeway-menubars>` outside the replaceable object-page region.
Menu service actions publish semantic results, and the viewer's replaceable result and navigation policy decides whether to navigate, update a region, or leave the result to an application handler.
The shell may resolve a discovered home-page object or invoke a discovered home service action according to configured viewer policy, but the menu component never does so automatically.

### Use bookmark-based canonical object routes

A canonical viewer route encodes logical type and object identifier as separate safely encoded path values.
The viewer listens for semantic navigation events from object links, object actions, and service actions, maps target bookmarks to routes, and loads object fragments through HTMX with history enabled.
Direct navigation, refresh, back, and forward resolve the same state.

Routing remains a viewer concern, so component events carry semantic bookmarks rather than viewer URLs.

### Keep route fragments free of metamodel and layout knowledge

The object route handler validates route values and returns a fragment containing a GraphQL client association, one object context, and a page resolver.
It does not enumerate members or query metamodel services.
The default definition places `<causeway-object>` beneath the route context and lets that component obtain schema and layout data through public contracts.

### Resolve custom pages before the default object component

Applications may register a page template or page factory keyed by Causeway logical type name.
The resolver chooses the exact logical-type registration and otherwise renders `<causeway-object>`.
Both custom and default definitions remain beneath the same route object context.

A custom definition can compose standard properties, actions, collections, `<causeway-object>`, ordinary HTML, and application custom elements without reimplementing GraphQL execution.
The registry belongs to the optional viewer and does not constrain other frameworks.

### Preserve one route-level object context

The HTMX object fragment contains one object context that owns every default or custom descendant requirement.
Switching page definitions releases disconnected requirements and registers the new definition's needs.
Navigating to a different bookmark creates a new context generation and prevents obsolete responses from rendering into the new page.

### Treat interaction results and home page as viewer policy

The viewer provides replaceable handling for semantic object, collection, scalar, and void action results from object and menu actions.
Object results may navigate by default, while scalar and collection results may render in a result region.
The configured home-page object or service action may supply the initial route or result according to policy.
Applications can replace these decisions without replacing action or menu components.

### Ship as an optional Causeway viewer module

The HTMX viewer, route handler, assets, shell, and default theme are enabled explicitly and coexist with GraphQL, REST, and Wicket viewers.
Its server module serves shells and fragments but does not become an alternative domain API.

## Risks / Trade-offs

- [HTMX may appear incidental] → Define its responsibility around routes, history, shell, fragments, and transitions rather than data semantics.
- [Stable menu shell and changing route fragments have different lifecycles] → Keep application-entry state outside the replaced object region and define invalidation explicitly.
- [Custom pages can bypass high-level components] → Permit deliberate low-level composition while requiring one shared route context.
- [Action results can originate from shell or object page] → Route all semantic results through scoped replaceable viewer policy.
- [Viewer parity can expand indefinitely] → Limit the initial capability to menu shell, home policy, and bookmark-addressable object pages.

## Migration Plan

The viewer is additive and disabled unless its module is included.
Applications can enable it alongside existing viewers, link to selected object routes, and incrementally register custom pages.
Rollback removes the optional module or routes without changing GraphQL or component contracts.

## Open Questions

- Should the first route handler return server templates, static shell fragments, or both?
- Should the default home-page policy invoke the configured action or first render a neutral shell state?
- Which default action-result navigation rules best match Causeway expectations?
- Should custom page registrations be able to replace only object content while retaining standard object header placement?
