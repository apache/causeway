## Context

`<cw-collection>` is an object-context consumer for one authoritative collection member.
It registers a member requirement, loads and pages the member through GraphQL, creates hydrated row contexts, and may expose associated member actions.

A collection-valued action invocation instead already returns one complete normalized result through `causeway-action-result` as `{kind: "collection", value: [...]}`.
The interaction controller and generic HTMX viewer currently reduce that result to a count and an ad-hoc list, so collection presentation is duplicated and unavailable to other hosts.

The normalized action result currently carries the selected values only.
For domain objects, the action operation authoritatively selects `_meta` identity, title, and optional icon.
The standalone component therefore cannot assume unselected properties, a collection-member descriptor, paging metadata, or a row-loading capability.

## Goals / Non-Goals

**Goals:**

- Add a framework-neutral public element that owns and presents one already-normalized collection action result.
- Preserve authoritative object metadata and standard scalar rendering without issuing follow-up GraphQL requests.
- Share collection column, responsive native, and qualified finite-Grid presentation where the supplied rows contain enough data.
- Give the HTMX viewer one semantic component for collection-valued outcomes.
- Preserve bounded lifecycle, accessibility, styling, event, and toolkit-fallback behavior.

**Non-Goals:**

- Invoke the action or listen globally for every action result inside the component.
- Treat an action result as an object collection member.
- Add member loading, paging, range fetching, sorting, filtering, associated actions, row hydration, editing, or mutation.
- Fabricate missing row values or alter action invocation selection solely to satisfy presentation columns.
- Change object-result navigation or scalar and void result policy.

## Decisions

### Accept normalized results through a property-only contract

`<cw-standalone-collection>` will expose a `result` JavaScript property accepting the established normalized shape `{kind: "collection", value: Array}`.
The property will not reflect to an HTML attribute because action results can contain structured values and authoritative identifiers that must not be serialized into markup.

The component will snapshot the array container without mutating caller-owned data, advance an internal generation on replacement, disconnect obsolete toolkit state, and render the latest assignment only.
Null or an unsupported shape will produce a bounded local state rather than being coerced into a collection.

This is preferred to automatic document-level event listening because a host must retain ownership of result routing and decide which component receives which event.
It is also preferred to accepting raw JSON attributes, which would create escaping, size, identity, and lifecycle hazards.

### Keep presentation metadata declarative and non-authoritative

The element will support `named`, `described-as`, `description-as`, `resizable-columns`, and `reorderable-columns` presentation attributes and optional direct-child `<cw-collection-column>` declarations.
It will capture parser-created columns before registration and preserve those child nodes across rerenders.

A declared column is rendered only from a matching value already present in each returned row.
Missing or unsupported values remain explicit and cannot trigger a GraphQL request or synthesize domain state.
Paging, sorting, filtering, activation, and collection-member identifiers are intentionally absent because there is no corresponding authoritative capability.

### Reuse semantic row presentation without row contexts

Rows carrying valid `_meta.logicalTypeName` and `_meta.id` will render as `<cw-object-link>` using the authoritative title and optional icon.
Scalar rows will use the existing value-renderer registry.
Object column cells will use the same property-wrapper interpretation and value renderer as ordinary collection cells when those wrappers were supplied in the result.
Unsupported heterogeneous or malformed rows will receive bounded local presentation instead of speculative identity.

The component will not create hydrated row contexts because it owns no object-context loading contract and its descendants must not silently fetch fields absent from the action result.

### Qualify only complete finite snapshots for Grid

The standalone component may use the existing internal Grid adapter when policy, width, columns, renderers, identity, and lifecycle qualification pass.
The action result array is a complete authoritative finite snapshot, so its array length supplies the total and a static range provider slices the already-projected rows without network access.

Grid paging, server sorting, filtering, and range brokers remain unavailable.
If qualification or the adapter fails, native list or table presentation remains authoritative and receives the same rows.

This is preferred to copying Vaadin-specific markup into the component or pretending that an object member range loader exists.

### Reuse collection-state semantics and add stable vocabulary

The public contracts will add `cw-standalone-collection` and a corresponding semantic host class.
Result replacement will publish the established collection-state event with ready, empty, unsupported, or error information, allowing observers to use the existing collection lifecycle vocabulary while distinguishing the target element.

Registration, exports, pre-upgrade column capture, element-prefix tests, styles, and usage documentation will include the new element.

### Delegate result routing to the HTMX shell

For a collection-valued `causeway-action-result`, the HTMX result policy will create `<cw-standalone-collection>`, assign a bounded action-derived heading, then set its `result` property.
It will retain the existing application `handleResult` override first, object navigation, scalar and void handling, result-region announcement, interaction-controller dismissal, and route lifecycle.

The shell will no longer construct collection `<ul>` or object links itself.

## Risks / Trade-offs

- [Declared columns are usually absent from the current action result selection] → Render only supplied values, keep default metadata links useful, and never broaden GraphQL selection implicitly.
- [A very large action result can still consume browser memory] → Snapshot only the array container, keep rendering bounded by existing Grid/native policy, and do not duplicate row objects or add a range cache.
- [Grid qualification could accidentally imply paging] → Use a static finite range provider and omit pager, sort, filter, and range-broker controls.
- [Structured values may be malformed or application-supplied] → Escape native markup, use semantic renderers, validate identity before links, and expose bounded unsupported rows.
- [Refactoring shared collection presentation could regress member collections] → Keep extracted helpers pure and cover both ordinary and standalone components in Foundation and browser tests.

## Migration Plan

1. Add and register the component without changing existing result policy.
2. Add Foundation contract, lifecycle, native, Grid, and fallback tests.
3. Switch only HTMX collection-valued outcomes to the new component and retain all other result branches.
4. Add Petclinic action-result acceptance and accessibility coverage.
5. Roll back by restoring the HTMX ad-hoc collection branch; the additive component can remain harmlessly registered.

## Open Questions

None.
