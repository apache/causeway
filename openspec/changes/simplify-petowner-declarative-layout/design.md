## Context

The PetOwner HTMX and Vue pages currently place semantic foundation components inside three application-owned macro-layout divs and several card sections.
Inside the details column, full-width fieldsets are also wrapped in twelve-span rows and columns that add no layout choice.
Inside the collections column, application sections repeat the semantic section and heading already rendered by `<cw-collection>`.

Foundation rows already provide twelve tracks and responsive stacking, while columns already accept fieldsets, collections, and metadata.
The missing composition needed for the page is a direct tabgroup child in a column, which is also a natural correspondence to a `layout.xml` column containing tabs.

## Goals / Non-Goals

**Goals:**

- Make the visible PetOwner macro layout directly legible from semantic custom elements.
- Preserve the existing one-third/two-thirds wide layout through spans 4 and 8.
- Let shared column layout provide vertical child spacing and responsive stacking.
- Preserve Identity/Metadata tabs, Contact and Details fieldsets, Pets and Visits collections, Agreement reading, actions, previews, and host parity.
- Keep strict direct-child validation and bounded diagnostics.

**Non-Goals:**

- Allowing arbitrary HTML children in rows, columns, fieldsets, or tabs.
- Turning declarative components into general-purpose CSS layout primitives.
- Changing domain metadata, collection ordering, authorization, PDF behavior, routes, or application ownership.
- Replacing object-heading or action-result structure that is outside the macro object grid.
- Changing generic object-page composition outside this representative sample.

## Decisions

### Extend the column grammar only for tabgroups

`<cw-column>` will accept direct `<cw-tabgroup>` children alongside fieldsets, collections, and metadata.
It will not accept arbitrary divs, sections, properties, actions, nested columns, or rows.
This narrow extension is preferred over allowing generic children because it preserves fail-closed grammar and corresponds to a meaningful layout structure.

### Make columns vertical layout containers

A connected layout column will stack its valid children in authored order with the shared layout gap and start alignment.
Its existing span behavior remains responsible for horizontal placement within a row.
This is preferred over retaining Petclinic details and collections classes solely to add `display: grid` and a gap.

### Use one shared row for the macro object grid

The application-owned details and collections wrappers will become direct columns with spans 4 and 8 under one row.
At the shared narrow breakpoint, both columns will span all tracks and retain details-before-collections document order.
The sample will adopt the shared foundation breakpoint rather than retaining a separate Petclinic-only object-grid breakpoint.

### Remove only semantically redundant wrappers

Contact and Details will become direct fieldset children of the details column because their former twelve-span wrappers did not affect layout.
The rows inside each tab remain because `<cw-tab>` intentionally accepts only rows.
Pets and Visits will become direct collection children because each collection already renders its own labelled semantic section.
Agreement will become a direct named fieldset containing the PDF property, because a standalone property is not a valid column child and the group title remains useful.

### Preserve user-facing labels through the semantic owner

When an outer application heading is removed, the corresponding foundation component will carry the user-facing panel label.
Collection descriptions, actions, previews, paging, sorting, and filtering remain unchanged.
The simplification MUST NOT add a second title or infer domain identity from presentation text.

## Risks / Trade-offs

- [Risk] Broadening column children could weaken the strict grammar. → Mitigation: add only `cw-tabgroup`, retain unsupported-child hiding and diagnostics, and test rejection of all other child kinds.
- [Risk] Shared column gaps could alter existing layouts with multiple children. → Mitigation: use the existing shared layout gap token, preserve authored order, and run foundation and browser visual geometry coverage.
- [Risk] Removing outer sections could change accessible headings or labels. → Mitigation: verify each collection's internal labelled section and use explicit representative labels where needed.
- [Risk] Adopting the shared 48rem breakpoint changes the former Petclinic-only 60rem transition. → Mitigation: verify no overflow across intermediate and narrow widths and document that foundation owns responsive behavior.
- [Risk] Generated Vue markup could diverge from HTMX after simplification. → Mitigation: assert equivalent direct-child trees and regenerate production assets through the established build.

## Migration Plan

Extend and test the foundation column contract first, then simplify HTMX and Vue declarations, remove now-unused sample CSS selectors, update tests and documentation, regenerate Vue assets, and run native and Vaadin browser checks.
Rollback restores the wrapper declarations and CSS while removing tabgroup from the column allow-list; no data or API migration is required.

## Open Questions

None.
