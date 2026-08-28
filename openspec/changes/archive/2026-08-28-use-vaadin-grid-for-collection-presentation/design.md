## Context

The Web Components foundation currently renders active collections as a Causeway-owned table when declarative columns exist and as an object-link list otherwise.
`CausewayCollectionElement` owns activation, metadata, window loading, aborts, generation checks, hydrated row-context lifetime, semantic state publication, cell rendering, associated actions, and disconnection cleanup.
The object-context controller already discovers and executes the bounded rich GraphQL `window(offset,size)` operation and normalizes offset, returned count, nullable total, previous and next availability, ordering, and row selection.
The current server contract bounds response rows but may materialize the complete domain collection before slicing, so browser virtualization cannot be represented as persistence query pushdown.
The archived member-presentation change established `component-toolkit=vaadin|native`, independently lazy closures, exact-hash CSP, family-scoped fallback, revision-bound asynchronous adapters, public `cw-*` boundaries, and release-qualified native rollback.
Grid introduces a different risk profile because it virtualizes rows, invokes asynchronous data-provider callbacks, recycles cell DOM, and normally expects a stable logical size.

## Goals / Non-Goals

**Goals:**

- Present qualified wide collections through an internal free-core Vaadin Grid while retaining `<cw-collection>` and `<cw-collection-column>` as the public contract.
- Preserve Causeway ownership of GraphQL, range selection, identity, authorization, values, renderers, errors, navigation, associated actions, state, cancellation, and lifecycle.
- Support a virtualized mode only for deterministic ordering with a safely available stable total.
- Support a bounded-window Grid mode with Causeway-owned previous and next controls when ordering is deterministic but total count is unavailable.
- Retain the native card/list presentation below the existing 48rem collection-container boundary.
- Provide independently lazy deterministic packaging, exact CSP, bounded failure, accessibility evidence, and complete native rollback.

**Non-Goals:**

- Do not add persistence query pushdown, a collection repository SPI, or a new GraphQL collection operation.
- Do not expose Vaadin Grid, data providers, item objects, renderers, events, callbacks, or Shadow DOM as supported application APIs.
- Do not add Grid Pro, commercial packages, Flow, Binder, server-side Vaadin state, telemetry, CDN assets, or external resources.
- Do not add application sorting, filtering, column resizing, column reordering, selection, inline editing, row actions, or mutation semantics merely because Grid can display those affordances.
- Do not replace narrow collection cards or lists in this tranche.
- Do not change canonical navigation, collection-column markup, effective-grid metadata, or existing renderer precedence.

## Decisions

### Keep one Causeway-owned collection state machine

`<cw-collection>` remains the stateful owner and chooses native or Grid presentation after metadata, window capability, ordering, total, columns, viewport qualification, toolkit policy, and family health are known.
A new internal `grid-widget.mjs` module and `cw-collection-grid` adapter receive frozen descriptors and callback functions from the host rather than discovering GraphQL or domain metadata.
The adapter is an implementation detail even though it uses a registered internal custom element, matching the existing field, reference, and action adapter pattern.

The alternative of letting Grid query GraphQL directly is rejected because it would duplicate authorization-sensitive selection construction, cancellation, errors, generation control, and row hydration outside the Causeway context controller.
The alternative of replacing `<cw-collection>` with application-authored `vaadin-grid` is rejected because it would make a third-party item and callback API public.

### Define three deterministic presentation outcomes

A collection qualifies for virtual Grid when it is active, visible, wide, exposes the bounded window operation, has deterministic cross-request ordering, reports a safe stable total count, has at least one supported visible column or object-identity column, and the Grid family is enabled and healthy.
A collection qualifies for bounded Grid when the same conditions hold except that total count is unavailable, in which case Grid receives only the current window and Causeway renders explicit previous and next paging controls from normalized window metadata.
A collection remains native when window capability is absent, ordering is encounter-only or unstable, responsive width is narrow, values or columns cannot preserve renderer semantics, the toolkit is native, or the Grid family has failed.

A zero total and an unavailable total remain distinct.
The adapter never invents a total, treats returned count as total, or requests an unbounded range.
If a count or ordering guarantee changes during refresh, the host starts a new generation and requalifies rather than mutating an existing Grid size silently.

### Keep narrow presentation native

The existing 48rem container boundary is the qualification boundary rather than a user-agent or device test.
At or below that boundary the host renders the established native list or card-compatible presentation and does not request the Grid closure solely for a narrow collection.
A connected Grid that crosses into the narrow state is removed after Causeway captures focus identity, and any later in-flight callback is ignored by revision and connection guards.
Crossing back to wide may lazily reinstall Grid without changing loaded collection data or issuing a request until the adapter needs a missing range.

The alternative of horizontally scrolling Grid on narrow layouts is rejected because it weakens the no-page-overflow and readable-card contracts.

### Add a bounded Causeway range broker

The host exposes a private range callback backed by the existing object-context `loadCollection` operation.
Every callback is bounded by the configured maximum and carries component generation, policy revision, connection generation, collection configuration revision, requested offset, requested size, and an abort signal.
The broker deduplicates identical in-flight and cached ranges, limits cached windows and concurrent requests, aborts or ignores obsolete requests, and never publishes stale rows, totals, errors, focus, or paging state.
The initial active collection load remains the authoritative first window and seeds the range cache.
Virtual Grid callbacks may request additional windows, while bounded Grid only changes windows through Causeway-owned previous and next controls.
Refresh, column change, object generation, member identity change, policy change, renderer-registry change, and disconnect invalidate the broker and disconnect hydrated row contexts exactly once.

The alternative of routing every Grid callback through the current single `load()` revision is rejected because one overlapping request would cancel another valid visible range.
The alternative of an unbounded browser cache is rejected because virtual scrolling could retain domain snapshots and contexts for the route lifetime.

### Project semantic cells through Causeway descriptors

The host creates immutable row and column descriptors containing only the selected row snapshot, metadata identity, normalized member descriptor, hidden and disabled state, bounded row-relative errors, renderer result, test identity, and navigation identity.
The Grid adapter creates and recycles DOM containers, but Causeway callbacks populate each container through established object-link and value-renderer output.
Application renderer authority remains explicit and cannot be mistaken for a standard renderer because a renderer identifier happens to match.
Hidden cells remain absent from accessibility and interaction, disabled reasons remain associated with the rendered cell, and row-relative errors remain bounded and value-safe.
Grid selection, sorting, filtering, editing, drag, resize, reorder, and details APIs remain disabled unless a future proposal qualifies them.

The alternative of converting every value to plain text is rejected because it would lose references, resources, null semantics, errors, and application renderer precedence.

### Preserve focus navigation and associated actions

Causeway records focus identity as collection member, row domain identity, column member, and semantic role before refresh, mode switch, paging, or fallback.
Focus is restored only when the same route generation, collection, row, column, visibility, and usability remain current.
Recycled Grid cells do not become persistent identity anchors.
Object-link activation continues to emit the existing Causeway navigation request exactly once.
Associated `<cw-action>` children remain outside Grid and retain declaration order, independent visibility, prompting, invocation, and focus restoration.

### Use the common policy with one independent Grid family

Explicit `component-toolkit=native` disables Grid and every other Vaadin adapter and contributes no Grid CSP hash or request eligibility.
Explicit `component-toolkit=vaadin` and the default policy enable qualified Grid together with the already qualified presentation families.
Explicit deprecated `editor-toolkit=vaadin|native` retains its documented mapping to the complete component policy and therefore enables or disables qualified Grid together with every other adapter when `component-toolkit` is absent.
Former independent pilot properties retain only their old editor subsets and do not enable Grid, because broadening a pilot input to collections would be surprising.
The shell publishes Grid eligibility and family health through bounded value-free diagnostics.
One Grid family failure disables only Grid presentation and causes connected collections to rerender natively; fields, actions, and references remain independently operational.

### Package and qualify an independent free-core closure

A new `foundation/vaadin-grid` directory pins only the reviewed free-core Grid entry points and build tools.
The production entry point imports Grid and the minimum free-core column support required by the accepted projection.
Build output removes telemetry and development-mode side effects, rejects Flow, Binder, Pro, commercial, Menu Bar, external-resource, and unapproved entry points, emits legal and CSP manifests, and is copied as same-origin Maven resources.
The pre-generation Grid gzip budget is 196608 bytes, and the all-closure aggregate budget becomes 533504 bytes by adding that cap to the previously accepted 336896-byte aggregate.
An unaffected or explicit-native route retains a zero-byte Grid budget.
The accepted policy records actual checksum, raw and gzip bytes, package graph, licenses, vulnerabilities, style hashes, and browser output before implementation is complete.

### Qualify accessibility and failure before default use

Browser qualification covers keyboard row and cell movement, focus entry and exit, visible focus, object-link activation, labels, descriptions, disabled reasons, partial errors, busy and empty states, paging, refresh, wide and narrow transitions, zoom, responsive containment, dark theme, reduced motion, forced colors, and exact-hash CSP.
The audit asserts zero unexpected console errors, page errors, external requests, duplicate controls, stale cells, overlays, clipped focus, or page overflow.
Delayed module imports, custom-element definition failures, data-provider failures, route replacement, disconnect, policy revision, width revision, and family recovery receive deterministic tests.
No failure event or diagnostic contains row values, protected values, GraphQL variables, full errors, or serialized domain snapshots.

## Risks / Trade-offs

- [Grid requires a stable size for full virtualization] → Enable virtual mode only with a safe total and use bounded Grid with explicit Causeway paging when count is unavailable.
- [Encounter ordering can duplicate or omit rows across requests] → Reject Grid qualification unless ordering is deterministic across windows.
- [Virtual callbacks can outlive routes or recycled cells] → Bind every callback to generation and connection revisions and cap requests, caches, and row contexts.
- [Renderer HTML may not fit Grid recycling] → Use Causeway-owned container population with explicit cleanup and fall back for renderers that cannot preserve semantics.
- [Grid can expose unsupported affordances] → Disable sorting, filtering, selection, editing, reorder, resize, drag, and Pro APIs and enforce source and bundle audits.
- [The closure may be too large for its presentation benefit] → Keep an independent pre-accepted budget and preserve native default fallback if qualification exceeds it.
- [Wide/narrow switching can lose focus] → Record semantic focus identity and restore only to a still-current visible Causeway target.
- [Exact Vaadin styles may require new CSP hashes] → Capture candidate-originated styles under enforcing CSP and reject blanket inline-style permissions.
- [Application renderers may return complex interactive markup] → Preserve authority but qualify only lifecycle-safe output for Grid; otherwise keep the collection native.

## Migration Plan

1. Record native collection behavior, request traces, accessibility trees, responsive states, and accepted Grid budgets before adding assets.
2. Build and qualify the independent Grid closure without changing collection selection.
3. Add adapter, range broker, semantic-cell projection, lifecycle guards, and unit tests behind explicit internal configuration.
4. Extend common policy, same-origin packaging, CSP, diagnostics, samples, and default/native browser profiles.
5. Enable Grid qualification under default Vaadin policy only after all foundation, Petclinic, Reference Application, strict-CSP, accessibility, budget, vulnerability, legal, and RAT gates pass.
6. Roll back operationally with `causeway.viewer.webcomponents.htmx.component-toolkit=native` without GraphQL, route, persisted-data, or application-markup changes.

## Open Questions

No design question blocks implementation.
Actual Grid closure checksum, transitive package graph, style hashes, and compressed size remain evidence outputs to record after the first deterministic candidate build.
Application renderer qualification may identify specific interactive renderers that must remain native, and those exclusions will be recorded rather than approximated.
