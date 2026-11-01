## Context

`<cw-action-results>` is currently a passive light-DOM region whose host replaces direct result children and owns routing, dismissal, navigation, and announcements.
The HTMX viewer snapshots one unique route outlet at interaction start and otherwise uses the stable shell outlet.
Inline results work well near the invoking action but can displace long pages, while some applications need a temporary modal or right-side result surface.
The new surface choice must not affect action metadata, invocation, result normalization, outlet selection, canonical object navigation, or application result overrides.

## Goals / Non-Goals

**Goals:**

- Add declarative `INLINE`, `DIALOG`, and `SIDEBAR` presentation styles to the public result outlet.
- Keep `INLINE` fully backward compatible.
- Provide accessible modal focus containment and a responsive non-modal sidebar.
- Preserve passive outlet ownership and the existing host result lifecycle.
- Keep mounted result nodes in light DOM and retain semantic component behavior.
- Reuse shared focus, Escape, overlay, and responsive conventions where practical.

**Non-Goals:**

- Do not add presentation metadata to GraphQL actions or action declarations.
- Do not let a result style change invocation, validation, confirmation, projection, routing, or navigation.
- Do not make the outlet subscribe globally to action events or resolve collection fragments.
- Do not persist result surfaces across unrelated routes or browser sessions.
- Do not introduce arbitrary placement coordinates, resizable panels, or multiple simultaneous result surfaces.

## Decisions

### The result outlet owns a presentation-only attribute

`<cw-action-results presentation-style="INLINE">` is the public declaration.
The attribute accepts `INLINE`, `DIALOG`, and `SIDEBAR` case-insensitively, normalizes through a reflected `presentationStyle` property, and falls back to `INLINE` for an absent, blank, or unsupported value.
The name is `presentation-style` rather than `prompt-style` because the outlet presents completed outcomes rather than parameter prompts.

Alternative considered: configure style on `<cw-action>`.
That would couple reusable result placement to every invoking action and would conflict with the existing destination-outlet snapshot, so the resolved outlet remains authoritative for surface choice.

### Inline remains the direct-child compatibility mode

`INLINE` keeps the current direct result children, region semantics, automatic reveal, replacement, and clear behavior.
Inline, dialog, and sidebar layouts place the Dismiss control below the result content so it does not reduce the content's available inline size.
The content area has a configurable bounded block size and scrolls independently, leaving Dismiss visible and operable for long collections.
Existing markup and hosts require no migration.

Alternative considered: always wrap inline content in a surface element.
That would break established selectors, layout hooks, and direct-child tests without adding value.

### Dialog is modal and sidebar is non-modal

`DIALOG` uses a bounded labelled modal surface with a backdrop, initial focus, Tab containment, Escape dismissal, scrollable content, and originating-control focus restoration.
`SIDEBAR` uses a fixed panel at the viewport inline end with complementary region semantics, no backdrop, no page inerting, and no forced initial focus.
The sidebar remains keyboard reachable through normal document order, supports Escape while focus is within it, and uses the same explicit dismiss control.

Alternative considered: make both dialog and sidebar modal, matching parameter prompts.
Completed results are informational and often benefit from comparison with the current object, so a non-modal sidebar is less disruptive while the dialog remains available when attention must be constrained.

### Presentation nodes remain light-DOM and host-owned

For styled surfaces, the outlet creates only bounded structural surface nodes and places the host-supplied presentation nodes inside them.
The outlet does not clone, reinterpret, or independently render result data.
Replacement retires the previous surface and transient toolkit state before mounting the next one.
`clear()` closes any open surface, removes structural nodes, returns the outlet to hidden-empty state, and restores focus when an eligible connected origin remains.

Alternative considered: portal result nodes to `document.body`.
Keeping surfaces under the outlet preserves route ownership, disconnection safety, component queries, and deterministic cleanup.

### The host supplies focus restoration context additively

The outlet exposes a bounded presentation context API through which the owning host can identify the originating action control before mounting a result.
If no valid connected origin is supplied, dismissal closes safely without speculative focus movement.
The existing HTMX result-dismiss lifecycle supplies the same action origin for inline, dialog, and sidebar styles.

Alternative considered: capture `document.activeElement` during replacement.
Asynchronous completion and prompt closure make that value unreliable, especially for service-menu actions.

### Responsive and layering behavior is deterministic

Dialog dimensions are clamped to the visual viewport and its result content scrolls internally above the non-scrolling Dismiss control.
Sidebar width is bounded by a documented token and becomes viewport-width on narrow screens without horizontal document overflow.
Its result content consumes the available panel height and scrolls independently above the non-scrolling Dismiss control.
Both styled surfaces use established z-index tokens and must coexist safely with action prompts, confirmation dialogs, menus, and route replacement.
Reduced-motion preference disables optional transitions.

### HTMX routing remains unchanged

The HTMX viewer resolves and snapshots the destination outlet exactly as today.
After a successful non-navigating result, it supplies origin context and result nodes to that outlet; the outlet's style controls only the visual surface.
Application result claims, object-valued navigation, duplicate-outlet fallback, disconnected-outlet fallback, void refresh preservation, announcements, and collection projection remain unchanged.
Automatic viewport reveal applies only to `INLINE`; opening a dialog or sidebar does not scroll the underlying route.

## Risks / Trade-offs

- [Risk] Modal focus handling could conflict with an action prompt or confirmation that has not fully closed. → Open the result surface only after the interaction controller publishes completion and add topmost-surface browser tests.
- [Risk] A non-modal sidebar can cover narrow content. → Bound its width, switch to viewport width at the narrow breakpoint, provide an always-visible dismiss control, and prohibit horizontal overflow.
- [Risk] Wrapping nodes for styled modes can invalidate direct-child assumptions. → Preserve direct children for `INLINE`, document styled-mode structure as host-owned, and test node identity rather than fixed wrapper selectors.
- [Risk] Route replacement could leave an overlay visible or focus a stale origin. → Keep surfaces beneath the route-owned outlet, close on disconnection, and restore focus only to a connected eligible target.
- [Risk] Native dialog and browser focus behavior can vary. → Qualify Chromium behavior under Vaadin and native component-toolkit policies and retain explicit focus containment and cleanup tests.

## Migration Plan

Existing outlets remain `INLINE` because the attribute defaults safely.
Applications opt in by adding `presentation-style="DIALOG"` or `presentation-style="SIDEBAR"` to an authored route outlet or stable shell outlet.
Rollback consists of removing the attribute; no server, GraphQL, or result-resource migration is required.

## Open Questions

None for the initial implementation.
Resizable sidebars, persistent user preference, and action-specific destination selection remain possible later enhancements.
