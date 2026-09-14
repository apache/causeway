## 1. Shared tooltip presentation

- [x] 1.1 Add normalized `description-as` resolution, bounded section composition, escaped tooltip attributes, and accessible hidden text for member descriptions and disabled reasons.
- [x] 1.2 Add shared pointer, keyboard, responsive, and section-break styling while keeping CSS and ECMAScript stylesheet assets synchronized.

## 2. Property and collection behavior

- [x] 2.1 Update `<cw-property>` rendering across loading, error, ready, disabled, and edit states, including `label-position="NONE"` tooltip anchoring and reactive attribute changes.
- [x] 2.2 Update `<cw-collection>` shells and Grid accessibility across loading, error, inactive, empty, native, and toolkit states, including collection disabled reasons and reactive attribute changes.
- [x] 2.3 Preserve existing action presentation and member/action authorization behavior.

## 3. Documentation and tests

- [x] 3.1 Document property and collection `description-as="label|tooltip"` usage, fallback behavior, and combined disabled tooltip sections.
- [x] 3.2 Add component tests for defaults, supported and invalid values, metadata and HTML descriptions, duplicate suppression, disabled composition, escaping, bounds, accessibility, label suppression, state coverage, and reactivity.
- [x] 3.3 Add stylesheet regression tests for tooltip trigger parity, pointer/focus behavior, section breaks, responsive bounds, and absence of action regressions.
- [x] 3.4 Run foundation tests, Maven verification, RAT, strict OpenSpec validation, diff checks, and IntelliJ compilation or inspections for changed source files.
