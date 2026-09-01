## Context

The prior Grid sizing correction sets `allRowsVisible` only for explicitly bounded presentation.
An unpaged collection with an authoritative count still qualifies as virtual Grid, so even a one-row collection retains Vaadin's default scrolling viewport.
Petclinic does not force that height; the behavior comes from the Foundation adapter's deliberate virtual-mode setting.

Direct collection-associated actions remain light-DOM `<cw-action>` children outside the owner-controlled primary rendering region.
The member composition currently inserts the primary region before those actions and CSS lays the action row after the full collection surface.
Effective-grid composition similarly emits the collection before its associated-action group.

## Goals / Non-Goals

**Goals:**

- Fit a virtual Grid to its content when an authoritative total proves that the complete collection is already represented by the loaded rows.
- Preserve the scrolling virtual viewport when more rows exist than the loaded authoritative window.
- Place collection-associated actions before the collection surface in matching visual, DOM, and sequential keyboard order.
- Right-align and responsively wrap the top action toolbar.
- Preserve property-associated action placement and every existing action and collection lifecycle invariant.

**Non-Goals:**

- Do not disable virtual range loading for materially larger collections.
- Do not move associated actions into Vaadin Grid, row cells, or private toolkit toolbars.
- Do not require Petclinic-specific wrappers or CSS.
- Do not change action declaration order, GraphQL authority, prompts, invocation, refresh, navigation, or focus restoration.

## Decisions

### Fit only an authoritative complete virtual window

The Grid adapter will set `allRowsVisible` for bounded mode and for virtual mode only when `totalCount` is a valid authoritative count no greater than the currently projected row count.
This uses data already frozen into the presentation and avoids guessing from viewport size, returned-row scarcity, or navigation flags.
A later presentation with more authoritative rows or a larger total reapplies the ordinary virtual viewport.

Using `totalCount <= pageSize` was rejected because the requested capacity does not prove that all rows were actually projected.
Applying `allRowsVisible` to every virtual Grid was rejected because large collections would lose bounded viewport behavior and render excessive height.
Adding application CSS height overrides was rejected because it would hide rather than correct the adapter mode decision.

### Put collection actions before the primary region structurally

For direct collection composition, the stable primary region will be maintained as the final collection child after hidden column declarations and associated actions.
This gives visible actions both DOM and keyboard order before collection search, sorting, rows, and paging while preserving action node identity.
The mutation observer will move only the stable primary wrapper when late parser or application children arrive; it will not recreate or reorder action nodes.

For effective-grid composition, collection-associated action markup will be emitted before its collection member while property-associated actions remain after their property.
A collection-specific association marker and shared structural CSS will right-align the action row and place the primary collection on the following full-width row.

CSS visual reordering alone was rejected because keyboard traversal would continue to encounter collection controls before visually preceding actions.
Moving actions inside owner-generated `innerHTML` was rejected because rerendering would destroy action identity and pending interaction state.

### Keep actions outside the collection's visual panel

The toolbar will occupy the top row immediately before the collection surface rather than being inserted into the collection heading or Vaadin Grid.
This works for canonical, directly authored, and effective-grid collections without requiring title duplication or coupling action controls to internal collection markup.
The toolbar remains close to the owning section heading in application layouts such as Petclinic and wraps above the collection at narrow widths.

## Risks / Trade-offs

- [A virtual Grid may change between compact and scrolling height after refresh] → Reapply the decision from each immutable authoritative presentation and add transition tests.
- [Moving the stable primary wrapper could trigger mutation observation recursively] → Move it only when it is not already the last collection child and verify bounded observer behavior.
- [Top actions could overflow beside long controls] → Give the toolbar a full responsive row, right-align at wide widths, and allow wrapping without overlap.
- [Generated and direct compositions could drift] → Exercise both structural paths and assert equivalent DOM and keyboard order.

## Migration Plan

No application migration is required.
Existing direct `<cw-action>` declarations and effective-grid associations retain their public syntax.
Rollback consists of restoring the previous virtual-only viewport rule and post-primary collection action ordering.

## Open Questions

None.