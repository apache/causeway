## Context

The foundation maps reversible `LocalDate`, `LocalTime`, and millisecond-compatible `LocalDateTime` values to Vaadin 25.2 picker controls through the internal `cw-field-editor` adapter.
The semantic value remains ISO lexical text for existing codecs and GraphQL, but Vaadin's date input currently uses its default display parser and formatter rather than the HTMX shell's configured `<html lang>`.
Vaadin exposes date-picker `i18n.formatDate`, `i18n.parseDate`, month and weekday names, and `i18n.firstDayOfWeek` for this purpose.
Its calendar affordance is currently an `aria-hidden` shadow-DOM `div`, so it is not in the Tab sequence and has no keyboard activation semantics.

## Goals / Non-Goals

**Goals:**

- Format and parse local calendar dates according to the document language or browser locale.
- Preserve ISO `LocalDate` and `LocalDateTime` values at the Causeway semantic boundary.
- Localize calendar month and weekday names and use the locale's first weekday where supported by the platform.
- Make the editable calendar affordance tabbable, labelled, and operable with Enter and Space.
- Avoid a false editor commit while focus moves between the date input and calendar trigger.
- Qualify behavior in unit tests and the actual Vaadin-backed Petclinic browser path.

**Non-Goals:**

- Changing time formatting, timezone behavior, date storage, or GraphQL codecs.
- Adding a translation catalog for Vaadin's Today and Cancel strings.
- Exposing Vaadin APIs or shadow-DOM selectors to application code.
- Replacing the pinned Vaadin dependency.

## Decisions

### Resolve locale from the semantic document

The adapter will prefer a nonblank `document.documentElement.lang`, then `navigator.language`, then `en`.
The HTMX shell already writes its configured language to the root element, making document language the application-controlled local setting while standalone component pages retain browser fallback.
An invalid locale will fall back safely rather than failing the entire field family.

### Build reversible date i18n with platform `Intl` APIs

A focused locale helper will use `Intl.DateTimeFormat(...).formatToParts()` to derive date-part order, separators, localized display, month names, and weekday names without adding a date-formatting dependency.
Parsing will normalize locale digits, enforce the locale-derived part order, validate the resulting calendar date, and also accept ISO `yyyy-MM-dd` as an unambiguous correction path.
Formatting will construct UTC dates to prevent browser-timezone rollover.
`Intl.Locale.weekInfo.firstDay` will configure Vaadin when available; absence of that optional platform information leaves Vaadin's safe default.
The helper is applied to direct date pickers and the date portion of date-time pickers, while the controls' values remain ISO lexical strings.

### Qualify Vaadin's internal calendar affordance inside the adapter

After Vaadin completes rendering, the internal adapter will locate the pinned control's `[part~="toggle-button"]` element, remove its hidden accessibility state, assign button semantics, add it to the Tab order, and give it a bounded field-specific accessible name.
Enter and Space will activate the existing Vaadin click behavior, so overlay state and date selection remain toolkit-owned.
Read-only and disabled controls will not expose an operable trigger.
This shadow-DOM dependency remains entirely inside the version-pinned adapter and will be covered by browser acceptance so Vaadin drift fails visibly.

### Treat shadow focus as internal editor focus

Focusout handling will recognize descendants reached through open shadow roots as remaining inside the same control.
Tabbing from the input to the calendar trigger therefore does not commit, validate, or close the property editor before a date is selected.

## Risks / Trade-offs

- **Risk: Locale input can be ambiguous.** → Parse only the active locale order plus unambiguous ISO input and reject impossible calendar dates.
- **Risk: Some locales use non-Latin digits.** → Generate a locale digit map and normalize before parsing.
- **Risk: Vaadin changes its internal toggle part.** → Keep the selector adapter-private and assert the real pinned component's role, Tab focus, label, and activation in browser acceptance.
- **Risk: Older engines lack `Intl.Locale.weekInfo`.** → Treat first-day metadata as optional while retaining localized date formatting.

## Migration Plan

No data migration is required because authoritative and submitted values remain ISO lexical strings.
Deploying the updated foundation assets changes only date presentation and calendar-trigger keyboard access.
Rollback restores the prior adapter behavior without changing stored values.

## Open Questions

None.
