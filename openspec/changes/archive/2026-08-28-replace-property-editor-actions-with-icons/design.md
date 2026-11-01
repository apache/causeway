## Context

Property edit mode renders Save and Cancel as ordinary textual buttons beneath the editor.
The controls already have stable native button semantics, action hooks, test identifiers, validation gating, and focus-restoration behavior.
The cohesive theme distinguishes the first action as primary, and that distinction must survive icon-only presentation.

## Goals / Non-Goals

**Goals:**

- Replace visible Save and Cancel text with conventional compact tick and cross icons.
- Preserve property-specific accessible names, pointer tooltips, native button semantics, validation gating, keyboard behavior, action hooks, and focus restoration.
- Preserve the primary visual treatment of Save and the secondary treatment of Cancel.
- Use deterministic local markup and styling without an external icon dependency.

**Non-Goals:**

- Change save or cancel command behavior, validation timing, error presentation, or interaction states.
- Move the actions into an editor implementation or Vaadin internal control.
- Change action-prompt buttons or introduce a general icon library.

## Decisions

Render one inline decorative SVG in each existing native button.
Use a tick path for Save and crossed diagonal paths for Cancel.
Set property-specific `aria-label` and `title` values such as “Save Name” and “Cancel editing Name”, and mark each SVG `aria-hidden="true"` and `focusable="false"`.

Keep `data-causeway-action`, test identifiers, `aria-disabled`, and `disabled` behavior on the existing button elements.
This preserves event delegation, focus capture and restoration, validation gating, and test automation.

Add a shared property-editor action class that gives each button a two-rem square hit area and a one-rem icon.
Keep the existing action container and first-child theme selectors so Save remains primary and Cancel remains secondary.

Unicode characters were rejected because tick and cross glyph weight, baseline, and emoji presentation vary across platforms.
An external icon package was rejected because two simple controls do not justify a dependency or asset lifecycle.
Replacing buttons with links or raw SVG event targets was rejected because it would weaken native keyboard, disabled, and accessibility behavior.

## Risks / Trade-offs

- [Risk] Icon meaning can be ambiguous. → Provide property-specific accessible names and pointer tooltips and use widely recognized tick and cross shapes.
- [Risk] Compact styling could reduce pointer usability. → Use a bounded two-rem square button rather than sizing to the SVG alone.
- [Risk] The SVG could interfere with delegated clicks or focus. → Keep the native button as the event target and make the SVG decorative with pointer events disabled.
- [Risk] Save and Cancel could become visually indistinguishable. → Retain the existing primary first-button and secondary button theme treatments.

## Migration Plan

No application migration is required because action hooks and test identifiers remain unchanged.
Rollback restores the textual button content and previous action sizing.

## Open Questions

None.
