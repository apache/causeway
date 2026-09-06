## Context

Causeway's Bootstrap grid model already allocates unreferenced actions to one column or fieldset, unreferenced properties to one fieldset, and unreferenced collections to one column or tab group.
The foundation XML planner mirrors this by claiming every valid explicit member before traversal and allocating each remaining member once in authoritative member-map order.
Application-authored web-component pages cannot currently request the same behavior, so they must enumerate every member and can become incomplete as the domain model evolves.

Ordinary `<cw-property>`, `<cw-collection>`, and `<cw-action>` elements already register typed semantic requirements with the nearest object context.
Composite elements such as `<cw-metadata>` resolve and generate additional ordinary member elements asynchronously.
A catch-all implementation therefore has to distinguish explicit references from its own generated descendants, isolate nested object contexts, wait for pending composite claim producers, and coordinate duplicate destinations.

## Goals / Non-Goals

**Goals:**

- Add one declarative catch-all element for each domain member kind.
- Preserve the same global-explicit-claim and allocate-once semantics as `layout.xml` processing.
- Use the current authorized object description as the only member inventory and preserve its member order.
- Keep generated properties, collections, and actions as ordinary connected foundation components.
- Prevent stable duplicate rendering when metadata or another same-context composite resolves asynchronously.
- Support parser-late declarations, dynamic insertion and removal, context supersession, nested contexts, and deterministic lifecycle cleanup.
- Keep HTMX and Vue as equivalent thin hosts.

**Non-Goals:**

- Inferring member identity from labels, text, CSS classes, placement, or route paths.
- Changing domain authorization, hidden/disabled evaluation, validation, action invocation, collection ordering, or GraphQL operations.
- Adding include, exclude, sorting, grouping, or member-renaming lists to catch-all elements.
- Treating CSS visibility as reference ownership or moving members when a user changes tabs.
- Replacing effective-grid rendering by `<cw-object>` or changing the `layout.xml` schema.
- Supporting more than one effective allocation destination for the same member kind.

## Decisions

### Use three explicit automatic-composition boundaries

The public elements will be `<cw-unreferenced-properties>`, `<cw-unreferenced-collections>`, and `<cw-unreferenced-actions>`.
Dedicated elements keep asynchronous allocation, generated-child ownership, empty state, and diagnostics separate from the otherwise structural fieldset, tabgroup, and column elements.
This is preferred over adding `unreferenced-*` attributes to existing structural elements, even though the latter would be a more literal XML spelling.

`<cw-column>` will accept all three elements as valid direct children.
The catch-all elements will accept no authored member children; unsupported authored children will fail closed without preventing valid sibling layout.

### Coordinate allocation once per object-context boundary

A `CausewayObjectContextElement` will own one member-allocation coordinator for its current context generation and expose it through a bounded semantic request channel.
Each catch-all registers a sink containing its semantic kind, host element, and revision-safe render callback.
The coordinator will sort sinks in document order, allocate a kind only to its first valid sink, and leave later sinks empty with one bounded duplicate-destination diagnostic.

The coordinator will obtain the inventory from `context.describeObject().members` and identify members only by the exact pair `(kind, id)`.
Unknown or wrong-kind references will not consume an authorized member.
Remaining members will preserve the authoritative map's order, matching existing XML-plan allocation.

### Derive explicit claims from bound semantic requirements

`ObjectContextController.registerRequirement` will retain the originating consumer identity through an optional backward-compatible registration option and publish value-free member-reference revisions.
The object-context allocation coordinator will observe only property, collection, and action requirements whose consumers belong to its own boundary.
Consumers beneath a nested object context or any unreferenced catch-all host will not contribute explicit claims to the enclosing allocation.

This is preferred over querying tags from the DOM because requirement registration distinguishes bound semantic renderers from inert preview or action-result declarations and naturally follows connection and disconnection.
Hidden tabs still count because their member components remain bound to the context.
Associated actions nested beneath properties or collections also count through their ordinary action requirements.

### Let composite producers reserve their claims while pending

A same-context composite that will generate member renderers can register a value-free claim-source lifecycle with the boundary coordinator.
The lifecycle declares affected member kinds and transitions from pending to resolved claims or settled empty/error.
The coordinator will withhold only affected catch-all kinds while a producer is pending.

`<cw-metadata>` will reserve properties and actions before loading the effective grid, then publish the exact authorized metadata fieldset claims before or atomically with connecting its generated children.
`<cw-object>` will reserve the member kinds it generates while its effective or fallback layout is unresolved.
Producer supersession and disconnection will retire reservations and trigger deterministic reallocation.

### Reuse ordinary declarative structures for output

`<cw-unreferenced-properties name="Other">` will generate one owned `<cw-fieldset>` containing an ordinary `<cw-property>` for each remaining property.
Its optional boolean `editable` attribute will be the only mechanism that opts generated properties into edit affordances; domain capability remains authoritative after that application opt-in.

`<cw-unreferenced-collections name="Other collections">` will generate one owned `<cw-tabgroup>` with one tab per remaining collection.
Each tab will contain the required row and twelve-span column around one ordinary `<cw-collection>`, and the collection's authoritative friendly name will label the tab and collection.
A single collection will remain a single tab rather than collapsing its tab group.

`<cw-unreferenced-actions name="Other actions">` will generate ordinary `<cw-action>` elements inside an owned wrapping horizontal action group with an accessible group name.
The action group will reuse established action spacing and focus behavior.

Generated children will carry an internal ownership marker and remain connected to the original object context and interaction controller.
The hosts will not invoke, hydrate, filter, reorder, or recreate them.

### Define reference ownership independently of visibility

A valid member renderer bound anywhere under the same nearest object-context boundary counts as referenced even when its tab or containing region is hidden.
Changing tab selection, disclosure state, or responsive layout will not reallocate members.
Inert preview declarations do not count against the parent context, while a live preview's members belong to its nearest row object context.

Dynamic connection or removal of an explicit member will recalculate the corresponding catch-all after a bounded microtask, without reordering unaffected generated members.
Duplicate explicit references count as one allocation claim while their existing component diagnostics and behavior remain unchanged.

### Fail closed and omit empty presentation

Each host will expose `data-causeway-unreferenced-state` with `loading`, `ready`, `empty`, or `error` and a stable kind-specific class hook.
Loading hosts remain non-presentational, empty hosts render no fieldset, tabgroup, heading, or action group, and error hosts expose no inferred members.
Missing context, description failure, duplicate sinks, malformed claims, stale generations, and unsupported authored children will produce bounded redacted layout-component diagnostics.

Generated content and subscriptions will be retired on context replacement, supersession, sink replacement, and disconnection.
No response payload, authorization rule, or inferred member identity will be exposed through diagnostics.

### Keep host integration declarative

HTMX resources and Vue templates may place the three custom elements directly beneath valid layout columns.
Both hosts will rely on foundation registration, context lookup, allocation, generated structure, state, styling, and interaction behavior.
Browser coverage will exercise actual context boundaries and equivalent generated semantics without adding host adapters or host-specific state.

## Risks / Trade-offs

- [Risk] Attaching consumer identity to requirement registrations broadens an important context API. → Mitigation: keep the option additive, value-free, internal to allocation observation, and covered by release and nested-context tests.
- [Risk] An asynchronous composite could cause temporary duplicates. → Mitigation: require pending claim-source registration before its asynchronous work and publish resolved claims before generated members become presentable.
- [Risk] A claim producer that never settles could withhold a category indefinitely. → Mitigation: producers must settle empty/error on every terminal path and retire on supersession or disconnect; tests cover all paths.
- [Risk] Dynamic declarations could create render/remove loops. → Mitigation: exclude descendants of every catch-all host from explicit claims and batch revisions through one context coordinator.
- [Risk] Runtime authorization can still hide an introspected member after allocation. → Mitigation: generated ordinary components retain existing hidden/error handling; the allocator never overrides their effective state.
- [Risk] Multiple sinks make placement ambiguous. → Mitigation: retain one effective destination per kind, deterministic document-order selection, empty duplicate hosts, and bounded diagnostics.
- [Risk] Automatically editable remaining properties could exceed application intent. → Mitigation: default to read-only and require the authored `editable` opt-in.

## Migration Plan

Add the value-free reference observation and allocation coordinator without changing existing requirement callers, then register the three elements and their strict column grammar.
Integrate metadata and object-layout claim-source lifecycles before enabling catch-all rendering, add host-neutral and browser coverage, and update HTMX and Vue documentation.
Existing pages remain unchanged until they author a catch-all element, so rollback removes the new elements and additive coordination APIs without data migration.

## Open Questions

None.
