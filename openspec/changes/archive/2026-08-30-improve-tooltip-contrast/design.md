## Context

Foundation tooltips are rendered with CSS pseudo-elements for disabled properties and for member or action descriptions.
Both currently use `CanvasText` as the background and `Canvas` as the text color, which produces the same dark treatment as prominent action controls in the light Petclinic theme.
The structural stylesheet is distributed both as `component-styles.css` and as the installable `CAUSEWAY_COMPONENT_STYLES` module string, while `theme.css` owns default design tokens.

## Goals / Non-Goals

**Goals:**

- Make explanatory tooltip surfaces visually distinct from nearby buttons.
- Retain high text contrast, a visible boundary against light pages, and current accessible interaction behavior.
- Keep every component-owned tooltip family visually consistent.
- Allow applications to customize the treatment through stable Causeway variables.
- Keep external and installable structural styles synchronized.

**Non-Goals:**

- Replacing CSS pseudo-element tooltips with a dialog, popover, or toolkit component.
- Changing tooltip content, activation, placement, timing, or semantic associations.
- Changing button colors or application-specific action presentation.

## Decisions

### Use a light neutral tooltip surface with dark text

The default tooltip background will be a near-white neutral rather than pure white, paired with near-black text.
A subtle border and shadow will separate the surface from both white page backgrounds and adjacent controls.
This directly addresses the confusing dark-button resemblance shown by the Petclinic action tooltip while maintaining strong contrast.

A dark tooltip with a different hue was considered, but it would remain visually close to a filled action control.
Automatically reversing the tooltip in dark mode was also considered, but a consistent light explanatory surface provides stronger separation from dark controls and follows the requested treatment.
Forced-colors mode remains under user-agent color adjustment rather than introducing fixed forced-color overrides.

### Expose shared tooltip design tokens

`theme.css` will define `--causeway-tooltip-background`, `--causeway-tooltip-color`, `--causeway-tooltip-border`, and `--causeway-tooltip-shadow`.
Structural tooltip rules will consume those variables with equivalent literal fallbacks so standalone installation remains usable without the optional theme.
The same tokens will cover disabled-property, member, action-control, collection, and action-parameter tooltip pseudo-elements.

### Preserve the duplicated stylesheet contract

The external structural stylesheet and the JavaScript module string will receive identical declarations.
Existing synchronization tests will remain authoritative, with additional assertions for token use and theme defaults.

## Risks / Trade-offs

- **Risk: A light tooltip may be less visually integrated with a dark application theme.** → Keep colors application-overridable and use a border and shadow to preserve clear boundaries.
- **Risk: A near-white tooltip can blend into a white page.** → Supply a visible neutral border and modest elevation shadow.
- **Risk: Structural and module styles can drift.** → Extend the existing synchronization and style regression tests.

## Migration Plan

The new defaults apply automatically when the updated styles are loaded.
Applications that need another treatment can override the four documented variables.
Rollback consists of restoring the previous token values or reverting the stylesheet change.

## Open Questions

None.
