## Why

An authored `<cw-property>` can request an editable local date, time, or date-time control, but it cannot currently express a page-specific admissible range such as no past dates or office hours from 08:00 through 18:00.
Applications need type-safe declarative bounds that guide picker choices and prevent an out-of-range property mutation without replacing canonical domain validation.

## What Changes

- Add optional HTML-inspired `min` and `max` attributes and matching properties to `<cw-property>`.
- Accept ISO `LocalDate`, `LocalTime`, and `LocalDateTime` lexical bounds only when they match the authoritative property datatype.
- Support `today` and `tomorrow` as edit-time-relative `LocalDate` bounds and `now` as an edit-time-relative `LocalDateTime` bound.
- Resolve relative bounds once when an edit begins so the admissible interval remains stable throughout that interaction.
- Propagate valid resolved bounds to native and qualified Vaadin date, time, and date-time controls.
- Reject an out-of-range pending value locally before GraphQL validation or mutation while retaining the value for correction.
- Ignore malformed, incompatible, or inverted authored ranges atomically and expose a deterministic diagnostic hook without degrading the canonical editor.
- Leave read-only display, non-local temporal types, action parameters, authoritative metadata, defaults, choices, domain validation, and GraphQL mutation semantics unchanged.
- Demonstrate a bounded Petclinic `LocalDate` property and add Foundation, Vaadin browser, and Petclinic acceptance coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add declarative, type-safe temporal range constraints to editable local temporal properties.
- `generic-htmx-web-component-viewer`: Demonstrate and qualify bounded property dates in Petclinic.
- `vaadin-semantic-editor-families`: Propagate resolved property bounds through qualified local date, time, and date-time controls.

## Impact

The change affects `<cw-property>` attributes and editor context, temporal range normalization and validation, native temporal editor markup, Vaadin field-adapter configuration, component contracts and documentation, Foundation tests, Vaadin browser audits, and Petclinic HTML and Playwright acceptance.
It adds no dependency, GraphQL field, domain annotation, route, action-parameter behavior, timezone conversion, or public Vaadin requirement.