## Why

Qualified local-date controls currently display Vaadin's default US-style numeric date regardless of the viewer's configured document language, so dates can be ambiguous or unfamiliar to users in other locales.
The calendar icon is also pointer-only, preventing keyboard users from tabbing to it and opening the visual date chooser directly.

## What Changes

- Derive local-date display and parsing conventions from the active document language, with browser-locale fallback when no document language is declared.
- Apply locale-aware numeric formatting and parsing to both `LocalDate` and the date portion of qualified `LocalDateTime` controls without changing ISO semantic values sent through codecs and GraphQL.
- Apply the locale's first day of week to the calendar when the platform exposes that setting.
- Make each editable date picker's calendar trigger a labelled keyboard focus target in the normal Tab sequence.
- Support Enter and Space activation of the calendar trigger while preserving input focus, validation, cancellation, and commit behavior.
- Add unit and browser-level regression coverage for a non-US locale and calendar-trigger keyboard operation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require local temporal editors to present and parse local dates according to document or browser locale while preserving ISO semantic values.
- `vaadin-semantic-editor-families`: Require qualified date picker adapters to expose an accessible tabbable calendar trigger and preserve Causeway interaction semantics while it is used.

## Impact

The change affects the foundation field-widget adapter, local-temporal tests and Vaadin browser qualification, and usage documentation.
No GraphQL schema, domain value codec, persisted date representation, public custom-element vocabulary, or dependency version changes are expected.
