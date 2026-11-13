## Context

Causeway's metamodel already resolves one navigable parent for an object through `ObjectSpecification.getNavigableParent(Object)`, backed by `NavigableParentFacet` and normally declared with `@PropertyLayout(navigable = Navigable.PARENT)`.
The rich GraphQL `_meta` object currently exposes the current object's bookmark identity and title, but no navigable hierarchy.
The browser object context coordinates metadata and member selections for semantic consumers such as `<cw-object-header>`, while `<cw-object-link>` already owns framework-neutral navigation events.

The feature crosses metamodel-backed GraphQL resolution, targeted schema discovery, coordinated browser projection, component rendering, HTMX navigation, and the Petclinic sample.
It must remain bounded, authorization-safe, cycle-safe, framework-neutral, and compatible with clients that do not select the new field.

## Goals / Non-Goals

**Goals:**

- Project the current object's navigable ancestors from the established Causeway facet.
- Give `<cw-breadcrumbs>` one semantic context requirement and no knowledge of generated GraphQL names.
- Render accessible root-to-current breadcrumbs using existing semantic object links and navigation events.
- Demonstrate owner → pet → visit hierarchy in Petclinic.
- Keep schema discovery, runtime traversal, response size, and diagnostics bounded.

**Non-Goals:**

- Infer hierarchy from arbitrary references, JPA relationships, collections, URLs, or browser history.
- Persist, cache globally, or mutate breadcrumb relationships.
- Add an application-specific breadcrumb provider or a second navigation event contract.
- Make breadcrumbs part of every object header or generic page automatically.
- Render dropdown ancestors, sibling menus, icons, or an unbounded tree.

## Decisions

### Add a bounded ancestor list beneath rich object metadata

Each generated rich `_meta` type will expose a nullable `breadcrumbs` field containing root-to-immediate-parent entries with non-null `logicalTypeName`, `id`, and `title` fields.
The current object is not repeated in the list because its existing metadata already supplies identity and title and `<cw-breadcrumbs>` needs to mark it as the non-link current item.
The shared breadcrumb-entry GraphQL type will be registered once and reused across generated object metadata types.

A metadata list is preferred to recursively returning arbitrary generated rich object types because a navigable parent can have a different logical type at every level and the component needs only navigation identity and title.
It also permits one bounded object read instead of sequential browser round trips for each ancestor.
Adding a flag to member metadata is rejected because the runtime hierarchy belongs to the object and the navigable-parent facet can be imperative.

### Traverse on the server through the existing facet

`CommonMetaFetcher` will start from the current managed object, call `objSpec().getNavigableParent(pojo)`, adapt each returned parent, obtain its bookmark and title, and repeat from that parent.
Traversal will stop at a null parent or an unavailable bookmark.
It will preserve ancestor order by collecting upward and reversing the result before returning it.

The traversal will accept at most 32 ancestors and track bookmark identities already visited, including the current object.
A cycle, facet exception, or depth overflow will fail only the nullable `breadcrumbs` field with a bounded safe GraphQL error rather than returning a misleading partial chain or invalidating sibling metadata fields.
No Java class names, arbitrary object strings, or exception details will enter the client diagnostic.

The bound is fixed rather than configurable because it is a protocol safety limit, not an application presentation preference.
Client-side traversal is rejected because it multiplies requests and asks the browser to reproduce server metamodel semantics.

### Treat breadcrumbs as one semantic context requirement

`ObjectContextController` will accept `{kind: 'breadcrumbs'}` and translate it to the current metadata identity, title, and nested breadcrumb entry selection.
Targeted introspection will include the breadcrumb-entry object type only when reached from metadata.
The requirement will participate in the existing initial coalescing, delta loading, refresh, cancellation, immutable snapshots, and metadata-path partial-error behavior.

A standalone component issuing GraphQL directly is rejected because it would bypass context coordination and duplicate generated-name handling.
Piggybacking every header read is rejected because pages that do not contain breadcrumbs should incur no breadcrumb traversal or response bytes.

### Compose existing semantic links

`<cw-breadcrumbs>` will extend `CausewayContextConsumerElement`, register the breadcrumbs requirement, and render a `<nav aria-label="Breadcrumb">` containing an ordered list.
Each ancestor will be represented by `<cw-object-link>` using returned identity and title, while the current object will be escaped text marked with `aria-current="page"`.
The component therefore reuses the established bubbling and composed `causeway-navigation-request` event and remains independent of HTMX and URL formats.

The component will render local loading, unsupported, partial-error, and terminal-error states consistently with other consumers.
With no ancestors it will render only the current item rather than disappear, so assistive technology retains a stable location landmark.
Malformed entries will be omitted defensively, but the server contract and tests remain authoritative.

### Use opt-in page composition

The component will be registered, exported, documented, and styled as part of the public foundation vocabulary, but it will not be inserted automatically by `<cw-object>` or the HTMX viewer.
Applications decide where breadcrumbs belong by writing `<cw-breadcrumbs></cw-breadcrumbs>` in custom HTML or another host composition.

Petclinic will mark `Pet.petOwner` and `Visit.pet` as `Navigable.PARENT` and include the component on its custom object pages.
The owner has no parent, a pet has its owner, and a visit has its pet and owner, providing deterministic zero-, one-, and two-ancestor coverage.
HTMX's existing semantic navigation bridge will turn ancestor link events into canonical routes without component-specific integration.

## Risks / Trade-offs

- [Risk] An imperative parent method can be expensive or throw. → Evaluate it only when breadcrumbs are selected, bound traversal to 32, and isolate failures to the nullable metadata field.
- [Risk] Cyclic parent relationships could loop or produce misleading navigation. → Track bookmarks, reject cycles with a bounded diagnostic, and never return a truncated cycle.
- [Risk] A parent cannot be bookmarked. → Stop before that parent because no stable semantic navigation target exists.
- [Risk] Ancestor titles can vary by locale or state. → Resolve titles per request through the current managed objects and do not globally cache chains.
- [Risk] Adding a nested metadata type expands targeted introspection. → Discover exactly one shared support type only for consumers that register breadcrumbs.
- [Risk] Petclinic hierarchy annotations can affect other Causeway viewers. → Use the standard existing `Navigable.PARENT` semantic intentionally and cover Wicket/HTMX startup and navigation regressions.
- [Risk] Long titles can distort responsive layout. → Use wrapping and overflow-safe CSS while preserving full escaped accessible text.

## Migration Plan

Add the optional GraphQL field, then add browser discovery and context support, then register the component, and finally opt Petclinic pages and domain properties into the feature.
Existing GraphQL documents, components, and pages remain unchanged unless they select or compose breadcrumbs.
Rollback removes `<cw-breadcrumbs>` from application pages and the Petclinic navigable annotations; the additive GraphQL field can remain without affecting clients.

## Open Questions

None.
