## Context

The preceding changes establish a framework-neutral GraphQL client, object context, semantic domain components, and complete property and action interaction behavior.
Those components are sufficient for application developers to build custom pages, but they do not provide routes or automatic composition for an application with no custom frontend.
This change adds an optional reference viewer that uses HTMX for its application shell and page lifecycle while continuing to delegate all Causeway member semantics and GraphQL execution to the component library.

## Goals / Non-Goals

**Goals:**

- Provide directly addressable generic domain-object pages.
- Compose a useful default page from standard GraphQL introspection and optional Causeway layout resources.
- Use HTMX for shell navigation, history, and fragment replacement without adding HTMX to component contracts.
- Allow a logical type to replace the generic page with an application page composed from the same semantic components.
- Preserve loading, partial-error, terminal-error, interaction, and navigation semantics across page transitions.
- Provide a reference theme and accessible page structure.

**Non-Goals:**

- Requiring HTMX for direct component-library consumers.
- Replacing the GraphQL schema with server-side metamodel access.
- Reimplementing property, action, parameter, collection, value, validation, or mutation semantics in the viewer.
- Full parity with Wicket application menus, home pages, authentication pages, standalone values, or every extension component.
- A general-purpose frontend framework.
- Server-side rendering of complete object state.

## Decisions

### Keep HTMX above the semantic component layer

HTMX will manage viewer routes, browser history, page-shell regions, loading indicators, and HTML fragment transitions.
The loaded fragments will compose semantic custom elements, and their nearest GraphQL client and object contexts will continue to own introspection, reads, interactions, and mutations.

The generic viewer will not add HTMX attributes to the public component contract and will not require an HTMX-to-GraphQL transport extension.
Another application can therefore replace the entire viewer layer while reusing the same component library.

### Use bookmark-based canonical object routes

A canonical viewer route will encode the logical type name and object identifier as separate safely encoded path values.
The viewer will listen for bubbling semantic navigation events from object links and action results, map their target bookmark to that route, and load the object-page fragment through HTMX with history enabled.
Direct navigation, refresh, back, and forward will resolve the same page state.

Routing remains a viewer concern, so component events continue to carry semantic bookmarks rather than URLs.

### Keep route fragments free of metamodel knowledge

The object route handler will validate and encode route values and return the viewer page shell containing a GraphQL client, one object context, and a page resolver.
It will not enumerate members or query Causeway metamodel services.
The browser-side page resolver will use the object context's cached schema description for both custom-page selection and generic composition.

This keeps GraphQL introspection as the only member-discovery mechanism and avoids a privileged HTML backend.

### Resolve custom pages before generic composition

Applications may register a page template or page factory keyed by Causeway logical type name.
The page resolver will choose the most specific exact logical-type registration and otherwise use the generic page definition.
Both custom and generic definitions will render beneath the route's existing object context.

A custom definition can compose standard properties, actions, collections, ordinary HTML, and application custom elements without reimplementing GraphQL execution.
The registration API belongs to the HTMX viewer and does not constrain applications that use React, Vue, Svelte, or another composition layer instead.

### Build generic pages from introspected member fields

The generic composer will enumerate properties, actions, and collections from the semantic type description derived from GraphQL introspection.
It will create standard member elements by semantic ID.
It will not request a GraphQL member-list metadata field and will not encode GraphQL documents in the generated page definition.

Dynamic hidden and disabled decisions remain owned by the created member components and object context.

### Prefer the Causeway grid with a deterministic fallback

When object metadata provides an enabled grid resource, the viewer will fetch and parse the Causeway layout and map recognized member references into page regions, groups, and ordering.
Unsupported layout instructions will generate diagnostics and fall back locally where possible.
If the resource is forbidden, absent, unreachable, or unusable, the viewer will compose a conventional page with object header, actions, properties, and collections in deterministic schema order.

The first implementation will treat layout as composition information and will not duplicate member runtime semantics from the grid.

### Preserve one route-level object context

The HTMX object fragment will contain one object context that survives page-definition selection and owns every generic or custom descendant requirement.
Switching between generic and custom definitions for the same route will release disconnected requirements and register the new definition's needs.
Navigating to a different bookmark will create a new context generation and prevent old responses from rendering into the new page.

### Treat interaction results as viewer policy

The viewer will provide default handling for semantic object, collection, scalar, and void action results.
Object results may navigate according to the default navigation policy, while scalar and collection results may render in a result region.
Applications can override this policy without replacing action components or context commands.

### Ship as an optional Causeway viewer module

The HTMX viewer, route handler, assets, and default theme will be enabled explicitly and will coexist with the existing GraphQL, REST, and Wicket viewers.
Its server-side module will serve shells and static or template fragments but will not become an alternative domain API.

## Risks / Trade-offs

- [HTMX may appear incidental because components own GraphQL] → Define its responsibility clearly around routes, history, shells, fragments, and transitions rather than forcing it into data transport.
- [Layout XML can contain presentation semantics not supported by the new components] → Implement a documented supported subset, preserve diagnostics, and provide deterministic fallback composition.
- [Large schemas can produce very large generic pages] → Use targeted introspection, lazy collections, collapsible regions, and component requirement release.
- [Custom page factories can become framework-specific] → Keep them isolated to the optional viewer and require only standard DOM output beneath the semantic object context.
- [Route encoding can expose malformed bookmarks] → Encode path segments, validate route input, and rely on GraphQL object lookup for authorization and existence.
- [Existing viewer parity can expand scope indefinitely] → Limit the first viewer capability to bookmark-addressable domain-object pages and record additional shells as separate changes.

## Migration Plan

The viewer is additive and disabled unless its module is included.
Applications can enable it alongside existing viewers, link to selected object routes, and incrementally register custom pages.
Rollback removes the optional module or routes without changing the GraphQL endpoint or component library.

## Open Questions

- Should the first route handler return server templates, static shell fragments, or both as equivalent page-definition sources?
- What subset of the Causeway grid format is essential for the initial generic page to feel representative?
- Which default action-result navigation rules best match Causeway expectations without surprising custom applications?
- Should the initial viewer include a minimal service-action entry page, or remain strictly object-route driven?
