## Context

The shared `CausewayFieldEditorElement` serves both editable properties and action parameters.
For `time-picker` and `date-time-picker` it currently sets Vaadin `step` to `0.001`, which explicitly enables milliseconds.
Vaadin documents `step` in seconds and uses values of at least 60 seconds for minute-only presentation and entry.

The adapter already qualifies the date picker's shadow-DOM toggle as a labelled Tab target because Vaadin keeps its internal toggle hidden from accessibility APIs by default.
The same pinned `toggle-button` part exists on the time picker, including the time child inside `vaadin-date-time-picker`, but is not currently qualified.

The optional application theme applies a generic focus-visible outline to every light-DOM `[tabindex]` element.
A Vaadin text-area host exposes `tabindex` while also rendering its own internal focus ring, so both rings appear around a multiline property or parameter.

## Goals / Non-Goals

**Goals:**

- Use minute resolution for editable qualified local-time and local-date-time controls shared by properties and parameters.
- Make editable clock triggers pointer-operable, keyboard-focusable, labelled, and Enter/Space activatable.
- Retain one clear focus indicator on multiline Vaadin controls.
- Preserve codec authority, local temporal semantics, validation, cancellation, and native fallback.

**Non-Goals:**

- Do not reduce the accepted or persisted precision of authoritative values outside a user edit.
- Do not alter offset, zoned, legacy, or native temporal editors.
- Do not modify Vaadin source, shadow markup, or package versions.
- Do not remove focus indication from native textareas or other controls.

## Decisions

### Configure editable time steps to sixty seconds

The field adapter will set `step = 60` for editable `vaadin-time-picker` and `vaadin-date-time-picker` controls.
Read-only presentation continues to preserve the authoritative value's accepted precision because the request is specifically about entry.
Vaadin's documented step behavior then formats input at minute precision and emits minute-aligned values through the existing temporal codec.

Custom string slicing was rejected because it would create a second temporal parser and could diverge from Vaadin validation or the Causeway codec.
Rounding authoritative values on render was rejected because merely entering edit mode must not mutate object state.

### Generalize the qualified picker-trigger bridge

The existing calendar-trigger qualification will be generalized internally so date and time pickers receive the same bounded behavior while retaining distinct public markers and accessible names.
An editable time input's trigger will expose `role="button"`, `tabindex="0"`, `aria-label="Open <field> time picker"`, and a stable diagnostic attribute.
Forward Tab from the input enters the trigger, reverse Tab returns to the input, and Enter or Space activates the existing Vaadin toggle click behavior.
Pointer activation remains delegated to Vaadin's existing trigger handler.

A replacement light-DOM button was rejected because it would duplicate toolkit overlay state and styling.
Application-facing shadow-DOM customization was rejected because the adapter owns the pinned internal qualification boundary.

### Suppress only the redundant Vaadin text-area host outline

A later theme rule will remove the generic host outline from focused `vaadin-text-area` controls directly beneath `cw-field-editor`.
The internal Vaadin focus ring, driven by `--vaadin-focus-ring-color`, remains visible.
Native textareas, fallback controls, and all other application focus targets retain the existing theme outline.

Broadly disabling outlines on all Vaadin elements was rejected because each family must retain independently verified focus visibility.

### Verify shared property and parameter behavior

Foundation tests will cover step assignment in edit versus view mode, standalone and composite time-trigger qualification, keyboard and pointer activation, disabled/read-only omission, and the focused text-area theme selector.
Petclinic Playwright coverage will exercise the `bookVisit` date-time parameter and multiline reason parameter, and a representative editable temporal property where available.

## Risks / Trade-offs

- [Existing non-minute values could be displayed at lower precision during editing] → Keep authoritative state untouched until user change and verify cancellation restores the original value.
- [Pinned Vaadin shadow structure may change] → Extend family audit and browser tests to fail clearly when the `toggle-button` part disappears.
- [Custom trigger Tab handling could disturb composite date-time order] → Test date input, calendar trigger, time input, clock trigger, and subsequent-control progression in the real browser.
- [Removing the host outline could hide focus] → Scope the rule to Vaadin text areas and assert the internal toolkit focus ring remains visible.

## Migration Plan

The behavior changes automatically for qualified Vaadin editors and requires no application markup or data migration.
Native toolkit rollback remains available through existing configuration.
Reverting restores millisecond step and the prior theme outline without changing stored values.

## Open Questions

None.
