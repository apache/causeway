## Context

Semantic menubars already collapse transient menu panels after enabled action selection, outside pointer activation, Escape, or opening a sibling menu.
They do not observe focus leaving the menubar, so ordinary Tab or Shift+Tab traversal can leave an expanded panel floating above route content while focus continues elsewhere.

## Goals / Non-Goals

**Goals:**

- Collapse expanded menu panels when focus leaves the owning menubar.
- Preserve an expanded panel while focus moves anywhere within that same menubar.
- Keep `aria-expanded`, controlled-panel visibility, action dispatch, and focus behavior synchronized.
- Verify desktop keyboard traversal in the real Petclinic shell and the component DOM shim.

**Non-Goals:**

- Do not trap focus or implement a modal menu.
- Do not change disclosure activation, arrow-key navigation, Escape focus restoration, or action focus policy.
- Do not collapse the responsive bar container merely because a nested menu panel closes.
- Do not add document-global focus listeners.

## Decisions

### Use bubbling `focusout` on each semantic menubar

The menubar element will listen for `focusout` and inspect `event.relatedTarget`.
If the next focused element is absent or outside the same menubar, all expanded menu disclosures in that bar will close without moving focus.
If the next focused element remains inside the menubar, disclosure state will remain unchanged.

Listening on the component boundary keeps ownership local and automatically covers forward Tab, reverse Shift+Tab, scripted focus, and pointer-induced focus changes.
A document-global `focusin` listener was rejected because it broadens lifecycle cleanup and couples independent bars.
Keying directly on Tab was rejected because focus can leave through other mechanisms and keydown does not reliably identify the final focus destination.

### Reuse the existing close primitive

Focus-leave handling will call the existing expanded-menu closure path so `aria-expanded` and controlled `hidden` state remain synchronized.
It will not request focus restoration because the browser has already moved focus intentionally.
Escape and action selection retain their established explicit restoration behavior.

### Exercise the reported browser sequence

Petclinic acceptance will expand a menu, move focus into a service action, Tab until focus leaves the menubar, and require the disclosure and panel to be collapsed while the newly focused page control remains focused.
Foundation tests will cover both internal focus movement and external focus departure.

## Risks / Trade-offs

- [Risk] Focus movement between controls inside one bar could close the panel prematurely. → Test `relatedTarget` containment before closing.
- [Risk] Closing on focus leave could steal focus back to the disclosure. → Use the non-restoring close path.
- [Risk] A null `relatedTarget` can represent focus moving outside the document. → Treat it as leaving the bar and close stale transient UI.

## Migration Plan

Deploy the foundation module and regression tests together.
Rollback restores the prior focusout behavior without data, route, GraphQL, or resource migration.

## Open Questions

None.
