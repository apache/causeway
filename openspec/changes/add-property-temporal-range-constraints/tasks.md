## 1. Local-temporal range model

- [x] 1.1 Expose shared local date, time, and date-time lexical validators from the temporal codec without changing accepted grammar.
- [x] 1.2 Implement immutable absolute and relative range normalization with local-calendar `today`, `tomorrow`, and `now` resolution.
- [x] 1.3 Implement precision-safe closed-interval comparison for minute, second, fractional, and date-time values without timezone conversion.
- [x] 1.4 Add unit coverage for valid, empty, malformed, incompatible, inverted, relative, boundary-equal, and out-of-range cases.

## 2. Property contract and validation lifecycle

- [x] 2.1 Add observed `min` and `max` attributes and matching `<cw-property>` JavaScript properties and contract documentation.
- [x] 2.2 Resolve and freeze the effective range when editing begins and expose valid or invalid diagnostic host state.
- [x] 2.3 Propagate valid resolved bounds through the stable editor context and Vaadin-to-native fallback.
- [x] 2.4 Render escaped `min` and `max` attributes on eligible native local-temporal inputs only.
- [x] 2.5 Reject out-of-range pending values before GraphQL validation or update while preserving correction, cancellation, canonical validation, and focus behavior.
- [x] 2.6 Add property interaction tests for no-request local rejection, later correction, save, invalid configuration, and non-temporal isolation.

## 3. Qualified Vaadin temporal controls

- [x] 3.1 Confirm current Vaadin date-picker, time-picker, and date-time-picker minimum and maximum APIs for the pinned toolkit.
- [x] 3.2 Carry resolved bounds on the semantic field host and apply them to editable qualified controls before assigning the current value.
- [x] 3.3 Preserve quarter-hour steps, seconds and fractional precision, localized date parsing, labelled picker triggers, clear controls, and read-only omission.
- [x] 3.4 Extend fake-adapter and real-browser audits across date, office-hour time, date-time, absent-range, invalid-range, and native fallback cases.

## 4. Petclinic demonstration and validation

- [x] 4.1 Declare an absolute minimum and `today` maximum on Petclinic `lastVisit` without changing domain rules.
- [x] 4.2 Extend Playwright acceptance for resolved control bounds, localized display, no-request future-date rejection, in-range recovery and save, cancellation, and both toolkit policies.
- [x] 4.3 Update Foundation usage and component contract documentation with ISO and relative examples and non-authoritative scope.
- [x] 4.4 Run complete Foundation Node and Maven suites, relevant Vaadin browser audits, Petclinic Playwright, IDE build or inspections, strict OpenSpec validation, and final diff checks.
