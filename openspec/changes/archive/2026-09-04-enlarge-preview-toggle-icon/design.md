## Context

The framework-neutral collection renderer creates each preview disclosure as a square button containing the text glyph `▸` when collapsed and `▾` when expanded.
At the rendered size shown in the Petclinic collection, those font-dependent glyphs occupy little visual area and their direction is difficult to distinguish.
The button's accessible label and `aria-expanded` state already communicate the correct semantics, so the change is limited to making the visible state indicator clearer in every host.

## Goals / Non-Goals

**Goals:**

- Make the collapsed right-facing direction and expanded downward-facing direction easy to distinguish.
- Increase the visible icon slightly without increasing the established preview column or button dimensions.
- Preserve semantic labels, `aria-expanded`, focus, keyboard activation, row alignment, responsive behavior, and preview lifecycle.
- Keep the implementation framework-neutral and equivalent in HTMX and Vue.

**Non-Goals:**

- Changing collection preview authorization, eligibility, expansion ownership, row identity, hydration, or lifecycle.
- Changing the preview button's accessible name, dimensions, placement, or public API.
- Adding a third-party icon dependency or host-specific icon implementation.
- Redesigning unrelated collection, paging, sorting, or action controls.

## Decisions

### Use one inline chevron geometry and rotate it from semantic state

The disclosure will render an `aria-hidden` inline SVG chevron with a stable view box and stroke rather than a font-dependent triangle character.
The collapsed state will show the chevron pointing toward the logical forward direction, and the existing `aria-expanded="true"` state will rotate the same geometry downward.
Using the semantic attribute as the styling selector keeps the visible direction synchronized with the authoritative accessible state.

An alternative was to retain the two Unicode glyphs and only increase `font-size`.
That would be smaller, but glyph weight, bounds, and baseline remain platform- and font-dependent and can still make the two directions difficult to compare.

### Size the icon independently from the control

The icon will use a shared custom property with a modest default of approximately `1.15rem`, while the preview button retains the established control-height square.
The SVG will use a clear rounded stroke and no fill so it remains legible in normal, dark, high-contrast, and forced-color presentations through `currentColor`.

An alternative was to enlarge the entire button and preview column.
That would add row height and whitespace without improving the icon itself and could disturb compact collection alignment.

### Verify shared presentation and unchanged semantics

Foundation tests will verify the SVG hook, size, state-driven rotation, accessible label, and unchanged `aria-expanded` behavior.
Existing HTMX and Vue Petclinic browser paths will verify collapsed and expanded directions, useful rendered dimensions, focus, and absence of horizontal overflow without adding host-owned behavior.

## Risks / Trade-offs

- [A larger icon could crowd the compact button] → Keep the button unchanged and bound the icon to a modest shared size below the control height.
- [Rotation could diverge from semantic state] → Select rotation exclusively from the button's existing `aria-expanded` attribute and test both states.
- [Inline SVG could become an extra accessibility target] → Mark it `aria-hidden="true"` and retain the button's existing state-specific accessible label.
- [Directional expectations differ in right-to-left layouts] → Use logical inline-direction styling for the collapsed state and verify the current left-to-right Petclinic presentation without changing preview semantics.
