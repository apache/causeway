## Context

Collection-associated actions are stable light-DOM action elements kept outside the collection's primary render wrapper so collection refreshes cannot recreate them or lose prompt and focus state.
The preceding change moved those actions before the primary wrapper and right-aligned them, but this necessarily gave them a separate flex row above the bordered collection panel.

The collection heading is currently generated inside the primary wrapper as part of each rendered shell.
It is plain semantic heading markup with no independent interaction state, while the action elements are stateful custom elements that must retain identity.
This distinction permits moving the ephemeral heading without moving the actions.

Effective-grid collection associations currently render an external associated-action group rather than the direct-child syntax used by authored collection pages.
Using direct collection children for generated collection actions will unify both paths behind the same header composition.

## Goals / Non-Goals

**Goals:**

- Share one bordered header row between the effective collection heading and existing associated action elements.
- Keep the heading first, followed by actions in declaration and sequential keyboard order.
- Use compact action controls in collection headers without changing ordinary actions.
- Preserve the stable primary body, action node identity, prompts, refresh behavior, and focus restoration.
- Stack the heading and actions safely when the collection becomes narrow.
- Make directly authored and effective-grid collection associations use the same component structure.

**Non-Goals:**

- Do not clone action labels, create proxy buttons, or route actions through collection callbacks.
- Do not place actions inside Vaadin Grid or the collection's refreshable `innerHTML`.
- Do not change property-associated action placement.
- Do not require application-specific Petclinic wrappers or CSS.
- Do not alter collection names, descriptions, search, paging, sorting, filtering, rows, GraphQL, or action semantics.

## Decisions

### Promote only the ephemeral heading

Collection rendering will remove any previously promoted heading, render the normal primary shell, and then promote the newly generated heading to a direct collection child immediately before the first associated action.
The heading retains its generated ID, text, tooltip, and accessible relationships, and the body section continues to reference it through `aria-labelledby`.
A hidden primary owner will not promote its heading, so an independently visible action does not reveal hidden owner metadata.

Moving the action elements into the generated shell was rejected because replacing the primary wrapper's `innerHTML` would disconnect or destroy stateful action controls.
Cloning action controls into the heading was rejected because it would duplicate accessible controls and invocation ownership.
Absolute positioning was rejected because variable labels, button widths, zoom, and localization would make overlap unavoidable.

### Make the host the integrated panel only when actions are present

A collection with a promoted heading will own the border, radius, subtle header background, and shadow previously owned by the nested collection shell.
The promoted heading and direct actions form the first wrapping flex row, and the stable primary wrapper occupies the full-width second row.
The nested shell becomes a borderless body with a normal surface background and a top divider.
Collections without associated actions retain their existing markup and presentation unchanged.

At wide widths, the heading flexes while actions remain compact and aligned to the inline end.
At the established narrow collection breakpoint, the heading takes the full row and actions wrap beneath it within the same header area before the body.

### Use a bounded compact action density

Collection-heading actions will set the pinned Vaadin button height variable and a matching native-button minimum height to a documented compact value.
Padding and icon-label spacing remain sufficient for readable labels and visible focus indicators.
The density is scoped only to direct associated actions in an integrated collection header.

Icon-only controls, prompt buttons, property actions, top-level actions, menus, and application toolbars retain their existing sizing.

### Generate effective-grid collection actions as direct children

When an effective grid associates actions with a collection, object-layout rendering will place the generated `<cw-action>` elements directly inside `<cw-collection>` in source order.
Property associations retain their existing external member-composition wrapper because their field-aligned placement differs.
This removes a separate generated collection-action layout path and lets direct and generated collection composition share heading promotion, lifecycle, CSS, and tests.

### Open shared tooltips below their triggers

The shared property-disabled, member, collection, parameter, and action tooltip pseudo-elements will use a block-start inset below their trigger rather than a block-end inset above it.
Their hidden transition starts slightly toward the trigger and settles downward into the visible position.
Using one placement for every tooltip family keeps behavior predictable and prevents an action tooltip from being clipped by the integrated collection panel's bounded top edge.

Increasing `z-index` or allowing the integrated panel to overflow was rejected because either approach would create a new stacking context competition or sacrifice the panel's rounded clipping.
Per-component placement was rejected because identical tooltip semantics would behave inconsistently.

## Risks / Trade-offs

- [Heading promotion could briefly duplicate an ID during rerender] → Remove the previous promoted heading before rendering the replacement and test repeated state transitions.
- [Moving a heading could break its accessible relationship] → Preserve the same node ID and assert the body section's `aria-labelledby` resolves to the promoted heading.
- [Compact buttons could become too small] → Keep a bounded minimum height, visible labels and icons, and browser geometry and focus tests.
- [Long localized headings and actions may not fit] → Use flex wrapping and a narrow breakpoint that gives the title and toolbar separate lines within the same header.
- [Generated layout behavior could drift from authored HTML] → Generate direct children and exercise both paths with structural tests.
- [A below-trigger tooltip can cover following content] → Keep tooltips non-interactive, bounded, elevated, and consistently layered above body content.

## Migration Plan

No application migration is required.
Existing nested `<cw-action>` declarations remain unchanged, and effective-grid markup becomes more directly equivalent to that syntax.
Rollback restores the separate pre-primary toolbar CSS and generated collection association wrapper.

## Open Questions

None.