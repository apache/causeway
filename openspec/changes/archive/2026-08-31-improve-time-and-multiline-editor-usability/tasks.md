## 1. Minute-resolution temporal adapter

- [x] 1.1 Configure editable qualified `LocalTime` and `LocalDateTime` Vaadin controls with a sixty-second step while preserving read-only precision and native fallback.
- [x] 1.2 Add foundation tests for property and parameter descriptors, edit-versus-view step behavior, minute-aligned emitted values, cancellation, and local lexical semantics.

## 2. Operable clock triggers

- [x] 2.1 Generalize the bounded picker-trigger qualification so standalone and composite time pickers expose labelled clock buttons without regressing calendar triggers.
- [x] 2.2 Implement forward and reverse Tab handling plus Enter and Space activation for editable clock triggers, retaining Vaadin's existing pointer activation and Causeway interaction ownership.
- [x] 2.3 Extend adapter audit and foundation tests for standalone, composite, disabled, read-only, delayed-upgrade, missing-part, keyboard, pointer, and accessible-name behavior.

## 3. Single multiline focus ring

- [x] 3.1 Add a narrowly scoped theme rule that suppresses only the redundant focused Vaadin text-area host outline while retaining the internal toolkit ring and native fallback outline.
- [x] 3.2 Add style and component tests proving multiline properties and parameters have one focus indicator without changing clear, resize, validation, or description behavior.

## 4. Browser acceptance and documentation

- [x] 4.1 Extend Petclinic Playwright coverage for minute-resolution date-time parameter entry and keyboard and pointer clock-trigger overlay activation without premature invocation.
- [x] 4.2 Extend Petclinic Playwright coverage for a single visible focus ring on the multiline action parameter and clean cancellation or submission lifecycle.
- [x] 4.3 Update component usage documentation for minute-resolution qualified time entry and accessible clock-trigger behavior.

## 5. Validation

- [x] 5.1 Run the complete foundation Node and Maven test suites.
- [x] 5.2 Run focused Petclinic Playwright acceptance tests and verify console, page-error, CSP, external-request, overlay, focus, and overflow diagnostics.
- [x] 5.3 Run relevant IDE inspections or project compilation and strict OpenSpec validation.
