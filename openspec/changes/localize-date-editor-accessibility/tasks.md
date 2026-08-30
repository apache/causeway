## 1. Locale-Aware Date Adapter

- [ ] 1.1 Add bounded locale resolution and reversible `Intl`-based date formatting, parsing, localized calendar labels, and optional first-weekday configuration.
- [ ] 1.2 Apply locale configuration to qualified `LocalDate` and `LocalDateTime` controls without changing their ISO semantic values.
- [ ] 1.3 Add unit tests for document-language precedence, browser fallback, locale date order, localized digits, ISO correction input, invalid dates, and safe locale fallback.

## 2. Keyboard Calendar Access

- [ ] 2.1 Qualify editable Vaadin date-picker calendar triggers with button semantics, field-specific accessible names, Tab focus, and Enter or Space activation.
- [ ] 2.2 Keep focus transitions to the shadow-DOM trigger inside the active Causeway editor and exclude read-only or disabled triggers.
- [ ] 2.3 Add unit and actual Vaadin-backed browser assertions for trigger focus, accessible naming, activation, localized display, and unchanged pending interaction behavior.

## 3. Documentation and Verification

- [ ] 3.1 Document locale precedence, accepted date entry, preserved ISO semantics, and calendar-trigger keyboard operation.
- [ ] 3.2 Run the complete foundation test suite and applicable Vaadin/Petclinic build checks.
- [ ] 3.3 Run strict OpenSpec validation and verify a clean implementation diff.
