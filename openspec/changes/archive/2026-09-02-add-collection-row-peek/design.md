## Context

`<cw-collection>` currently renders object links and optionally projected property columns through native list/table markup or the Vaadin Grid adapter.
The collection already creates hydrated `ObjectContextController` instances for rows, but it does not expose a row-owned component subtree.
Existing object pages demonstrate that ordinary semantic HTML can organize `<cw-property>`, `<cw-action>`, and `<cw-collection>` components once they consume the correct nearest object context.
The HTMX viewer already discovers bounded classpath collection-presentation resources and exposes them through a private inertly parsed endpoint, providing a close precedent for runtime-type preview defaults.

## Goals / Non-Goals

**Goals:**

- Let an authored collection opt into an accessible inline row preview through one direct `<cw-peek>` declaration.
- Support either reusable inline peek content or a safe default selected by the row's authoritative runtime logical type.
- Keep exactly one row expanded and give its live component subtree a dedicated hydrated row context.
- Preserve native and Vaadin Grid behavior, including paging, virtual ranges, sorting, filtering, responsiveness, focus, and bounded cleanup.
- Refresh and collapse the parent collection after successful actions or property updates inside the peek.
- Keep missing defaults invisible and malformed defaults isolated from the collection's authoritative data.

**Non-Goals:**

- Multiple simultaneously expanded rows are not supported.
- Expansion state is not preserved across sorting, filtering, paging, reload, responsive renderer replacement, parent refresh, or route replacement.
- `<cw-peek>` does not apply to `<cw-standalone-collection>` in this change.
- Preview declarations do not change authorization, metadata, validation, invocation, result routing, collection data, or canonical object navigation.
- The change does not introduce generic row, column, tab, or page-layout components.
- Preview member requirements are not added to the initial collection GraphQL projection eagerly.

## Decisions

### One direct declaration controls one expanded row

A collection accepts zero or one direct `<cw-peek>` child.
No declaration means current collection behavior remains unchanged.
More than one direct declaration fails closed with a bounded diagnostic and exposes no disclosure controls.
The collection owns one expanded row key, so opening a second row retires the first live preview before opening the next.

This accordion policy bounds active contexts and resource use, avoids duplicate live member IDs from cloned declarations, and matches the intentionally lightweight meaning of a peek.

Alternative considered: allow multiple expanded rows.
That would multiply GraphQL requirements, complicate virtual-range eviction and focus restoration, and permit duplicate IDs from repeated component templates.

### Declarations are captured as inert reusable templates

A direct peek declaration is configuration rather than a live row context.
The collection captures its descendants before they connect, keeps the declaration hidden and inert, and clones the captured template only for the current expanded row.
A non-empty inline declaration completely replaces any type default; declarations are never merged.
Meaningful element content or non-blank text makes a declaration non-empty, while comments and whitespace do not.

The live clone receives its context before connecting, ensuring descendants consume the selected row rather than the collection owner.
Only one clone is live at a time.

Alternative considered: leave authored children connected but hidden.
They would register requirements against the wrong object context and could invoke or load while still acting as configuration.

### Empty declarations resolve plural runtime-type preview resources

An empty declaration asks an optional host resolver for the row's authoritative `_meta.logicalTypeName`.
The HTMX host resolves `META-INF/causeway/webcomponents/previews/<logical-type-name>.html`, whose document has exactly one `<cw-peek>` root.
Resolution uses runtime row type rather than only a declared collection element type so polymorphic rows can receive distinct defaults.

The collection resolves each distinct type as part of the current row or virtual-range projection before emitting disclosure controls.
A missing resource resolves to no template and that row has no expander.
Malformed, unsafe, duplicate, oversized, or failed resources also expose no expander and publish bounded diagnostics without failing collection data.
Cached mode reuses immutable inert templates and clones them per expansion; reload mode re-resolves according to existing viewer policy.

Alternative considered: show an optimistic expander and discover a missing resource after activation.
That would expose a control with no useful outcome and introduce avoidable layout and accessibility churn.

### The preview parser accepts inert component layout only

The classpath loader applies bounded count, byte, UTF-8, filename, and duplicate-type checks consistent with existing collection-presentation loading.
The private preview endpoint uses logical-type validation, private no-store responses, and a distinguishing response header.
The client parser requires one `<cw-peek>` root, rejects executable or embedding elements, inline event handlers, unsafe URL-bearing markup, and unsupported root identity attributes, and allows bounded semantic layout markup plus known domain-component declarations.

Ordinary classes, headings, sections, lists, ARIA relationships, and component presentation attributes remain available so application CSS can organize preview rows and columns just as it organizes page fragments.
The preview root cannot select or override row identity; the collection always injects the authoritative hydrated row context.

Alternative considered: reuse complete page fragments.
Pages contain route-level ownership, headers, breadcrumbs, result outlets, and lifecycle behavior that must not be nested inside a collection row.

### Each expansion owns a dedicated hydrated row context

The collection creates a dedicated `ObjectContextController` from the selected row identity, row data, and authoritative row selection when expansion begins.
The live `<cw-peek>` provides that controller through the existing object-context request protocol and disconnects it on collapse or supersession.
Existing hydrated fields can satisfy matching requirements immediately, while additional properties, actions, and nested collections register their normal requirements and load lazily.

Grid projection may carry an internal immutable preview payload sufficient for the collection to create this context, but the Grid adapter does not interpret domain rows or context state.
No GraphQL schema change is required.

Alternative considered: retain and expose every eagerly created row context through the Grid range broker.
A dedicated expansion context has simpler ownership, remains independent of virtual-range eviction, and bounds interactive work to the one open row.

### Native and Vaadin renderers share a neutral details contract

Native list presentation adds a labelled disclosure button beside the object link and places the live peek after the selected row content.
Native table presentation adds a compact leading disclosure column and one full-width details row immediately after the selected data row.
The disclosure uses `aria-expanded`, `aria-controls`, and a row-title-based accessible name.

The neutral Grid presentation gains an optional row-details descriptor owned by the collection.
The Vaadin adapter maps it to a compact leading renderer, `rowDetailsRenderer`, and exactly one `detailsOpenedItems` entry while delegating live preview creation back to the collection.
Absence of a usable template yields no interactive disclosure for that item.
Widget failure continues to fall back through the established native renderer without changing expanded-state authority.

Alternative considered: disqualify Vaadin Grid whenever a peek exists.
That would unnecessarily remove resizing, reordering, virtual paging, and the viewer's normal wide-screen presentation from opted-in collections.

### Collapse and focus behavior follow disclosure semantics

Opening a peek leaves focus on the disclosure button so ordinary Tab order enters its interactive content.
Activating the open disclosure collapses it.
Escape from anywhere inside the live peek collapses it, retires its context, and restores focus to the connected disclosure button.
Opening another row moves authority to the newly activated disclosure after retiring the previous details subtree.

Criteria changes, page changes, reload, parent generation changes, responsive renderer replacement, and disconnection collapse without trying to preserve expansion.
If focused content is retired by a mutation refresh, the stable collection host becomes the programmatic fallback focus target unless focus has already moved into navigation or an action-result surface.
For an action originating inside a peek, action-result focus restoration uses the stable enclosing collection rather than the soon-to-be-retired action control.

Alternative considered: automatically move focus into the preview when it opens.
Disclosure patterns conventionally retain toggle focus and allow users to enter details through normal forward navigation.

### Successful mutations refresh and collapse the parent collection

The collection observes successful action-result and property-updated semantic events whose origin is inside the live peek.
It schedules one generation-safe forced collection reload, preserving the current sort, filter, and page criteria but not expansion.
The live preview and dedicated row context are retired before the reload becomes authoritative.

Action-result routing, object navigation, application claims, announcements, and void route refresh remain host-owned.
If navigation or route replacement supersedes the collection first, normal connection and generation checks cancel the local refresh.
Property edits use the same refresh boundary as action invocation so projected parent cells cannot remain indefinitely stale after either mutation path.

Alternative considered: refresh only the live row context.
That would update components inside the preview but leave projected cells in the containing collection stale.

### Diagnostics and generations bound asynchronous work

Template resolution, row expansion, context creation, Grid details rendering, and mutation refresh each carry the current collection load, render, and row identity generation.
Late resource responses, obsolete virtual ranges, retired preview contexts, and prior mutation refreshes cannot reopen or overwrite the current row.
Diagnostics expose stable classifications and bounded messages without resource bodies, row values, or executable markup.

## Risks / Trade-offs

- [Risk] Resolving a default before rendering disclosure can delay an opted-in collection or virtual range. → Resolve once per distinct runtime type, cache immutable templates, bound fetches, and never perform preview lookup when no empty declaration exists.
- [Risk] Long or deeply nested preview content can make a row unwieldy. → Keep one row open, provide ordinary layout and overflow hooks, load nested collections through existing bounded policies, and make Escape consistently available.
- [Risk] A parent reload removes the invoking member control before normal focus restoration. → Use the stable collection host as the action-result origin and mutation fallback when the action belongs to a live peek.
- [Risk] A malformed default could introduce executable or misleading markup. → Apply server bounds and strict inert client validation before a row receives an expander.
- [Risk] Grid virtualization can retire an item while asynchronous details work is pending. → Give expansion its own context and generation, close on renderer or range supersession, and reject late mounts.
- [Risk] Refreshing after every successful action includes non-mutating actions. → Prefer coherence and the explicit contract; caching and current collection paging bounds limit the reload, while navigation supersession cancels unnecessary work.

## Migration Plan

Existing collections have no `<cw-peek>` declaration and remain behaviorally and visually unchanged.
Applications opt in incrementally with inline declarations or empty declarations backed by preview resources.
Removing the declaration or preview resource rolls a collection back to its current row presentation without data migration.

## Open Questions

None.
