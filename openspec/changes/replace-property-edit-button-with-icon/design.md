## Context

Editable properties render their value and edit affordance in a three-column property grid.
The current button includes the full text “Edit <property label>”, even though its column is intended as a compact action area and the property label already names the row.
The control must remain understandable without relying on the pencil shape alone.

## Goals / Non-Goals

**Goals:**

- Make the property edit affordance visually compact and subordinate to the value.
- Preserve a property-specific accessible name, pointer tooltip, keyboard behavior, test hook, and focus restoration target.
- Keep the control adjacent to the property value in the existing action column.
- Use deterministic local markup and styling without an icon dependency or external request.

**Non-Goals:**

- Move the control inside the eventual form input after edit mode begins.
- Change edit interaction states, Save/Cancel controls, focus algorithms, validation, GraphQL, or semantic events.
- Introduce a general icon library or redesign other action buttons.

## Decisions

Render a small inline pencil SVG as decorative content inside the existing native button.
Give the button `aria-label` and `title` values of “Edit <property label>”, preserving context for assistive technology and pointer discovery while removing visible repeated text.
Keep `data-causeway-action="edit"` and existing test identifiers unchanged so command handling and focus restoration remain stable.

Style the button as a compact square inline-flex control with a bounded icon size in both baseline component styles and the cohesive theme.
The existing third grid column continues to place it adjacent to the value, including multiline layouts and responsive theme variants.

A Unicode pencil character was considered but rejected because glyph shape, alignment, and emoji presentation vary across platforms.
An external icon package was rejected because one simple affordance does not justify a dependency, asset request, or release obligation.
Moving the button into a new wrapper around the value was rejected because the theme relies on direct grid children and such a wrapper would disrupt multiline and responsive placement.

## Risks / Trade-offs

- [Risk] An icon-only control can be ambiguous. → Retain a property-specific accessible name and tooltip and use the conventional pencil shape.
- [Risk] A smaller visual control can reduce the pointer target. → Keep a compact but bounded square hit area rather than sizing the button to the SVG alone.
- [Risk] Inline SVG could receive focus or be announced separately. → Mark it decorative with `aria-hidden="true"` and `focusable="false"`.
- [Risk] Theme overrides could restore oversized padding. → Add explicit compact dimensions to the baseline and cohesive theme selectors and cover them with style tests.

## Migration Plan

No application migration is required because the edit command, DOM action hook, and test identifier remain unchanged.
Rollback restores the text content and prior property-edit sizing rules.

## Open Questions

None.
