## Context

`<cw-property>` currently renders an effective description inline and also gives its visible label a native `title`, while a disabled reason uses a bounded custom tooltip on that label.
`<cw-collection>` renders its effective description inline beneath the heading and intentionally suppresses the collection-level disabled reason.
Actions already keep descriptions out of normal layout and preserve their own accessible explanation, so this change is limited to properties and collections.

The component library uses light-DOM markup, semantic IDs and ARIA relationships, and mirrored `component-styles.css` and `component-styles.mjs` assets.
Any tooltip behavior must remain framework-neutral, keyboard reachable, escaped, bounded, responsive, and independent of GraphQL loading.

## Goals / Non-Goals

**Goals:**

- Let authored property and collection markup select visible-label or tooltip description presentation.
- Preserve visible descriptions as the default for backward compatibility.
- Present description and disabled reason as ordered, visually distinct tooltip sections when both exist.
- Preserve independent accessible description and disabled-reason semantics.
- React to attribute changes without reloading member data.

**Non-Goals:**

- Changing action description behavior.
- Adding GraphQL fields or changing metadata precedence.
- Introducing a third-party tooltip dependency or modifying toolkit internals.
- Making disabled collections editable or changing member/action authorization.

## Decisions

### Use one authored `description-as` attribute with a conservative fallback

Both components will observe `description-as`.
Values are normalized case-insensitively to `label` or `tooltip`; absent, blank, and unsupported values resolve to `label`.
This keeps all existing pages unchanged and avoids an unknown presentation state.

### Keep semantic text separate from visual tooltip payload

The effective description and disabled reason remain separate escaped DOM nodes with stable IDs so `aria-describedby` exposes them independently and in description-then-reason order.
In `label` mode the description node remains visible.
In `tooltip` mode it becomes visually hidden rather than being removed.

A shared tooltip trigger carries a bounded escaped visual payload.
When both texts exist, the payload places the description first and the disabled reason second with an explicit section break.
The styling preserves that break and provides one consistent pointer and focus presentation for both components.

This is preferred over relying only on `title`, which is not reliably keyboard accessible and cannot present structured sections consistently.

### Anchor tooltips to the semantic name

A property with a visible label uses that label as the trigger.
A collection uses its heading as the trigger.
A tooltip trigger is focusable only when tooltip content exists.
For `label-position="NONE"`, the property field shell becomes the visible/focusable tooltip trigger while the hidden label continues to provide the accessible name.

This avoids an unreachable tooltip while preserving explicit label suppression.

### Bound each source before composition

Description and disabled-reason text are trimmed and bounded independently before creating the visual payload.
The tooltip never renders raw HTML and does not expose stale data after rerender.
Independent bounds prevent one section from consuming the entire diagnostic budget.

### Preserve collection read-only semantics while restoring explanation on demand

A collection-level disabled reason remains absent from normal page layout.
It is exposed only through the heading tooltip and accessible hidden text.
Rows, paging, associated actions, and their own authorization remain unchanged.

## Risks / Trade-offs

- [Risk] Adding a focus stop to headings or property fields can increase keyboard traversal.
  → Mitigation: add focusability only when a tooltip has content and test both tooltip and default modes.
- [Risk] Pseudo-element tooltip text has limited rich structure.
  → Mitigation: use a preserved section break for visual grouping and separate ARIA description nodes for semantic grouping.
- [Risk] Mirrored stylesheet assets can drift.
  → Mitigation: update both assets together and retain parity regression tests.
- [Risk] Long metadata could produce oversized overlays.
  → Mitigation: bound each section and cap tooltip width to the viewport.

## Migration Plan

No markup migration is required because omitted and invalid values retain visible-label presentation.
Applications can opt in incrementally with `description-as="tooltip"` and can opt back out with `description-as="label"` or by removing the attribute.
