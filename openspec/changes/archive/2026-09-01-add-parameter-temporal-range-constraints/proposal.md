## Why

Action prompts currently cannot apply the same declarative local-temporal bounds that editable properties support, so authored pages must rely on canonical validation alone even when date and time controls could prevent or explain invalid input earlier.
Extending optional `<cw-parameter>` declarations closes that consistency gap and lets Petclinic demonstrate future-date and office-hour booking constraints without exposing Vaadin APIs.

## What Changes

- Add optional `min` and `max` attributes and JavaScript properties to `<cw-parameter>` declarations.
- Reuse the existing local date, time, and date-time range grammar, relative-token resolution, precision-safe comparison, invalid-range fallback, and native/Vaadin propagation.
- Resolve each declared range once when its action prompt begins and keep the result stable through validation, confirmation, correction, cancellation, and invocation.
- Reject out-of-range pending parameter values locally before canonical GraphQL preparation, action validation, or invocation while retaining values for correction.
- Keep undeclared parameters and non-local-temporal parameters unchanged, and preserve GraphQL authority over order, defaults, choices, required state, validation, confirmation, and invocation.
- Refactor Petclinic `bookVisit` presentation and action parameters into a future `LocalDate` and office-hours `LocalTime`, then combine them into the existing authoritative `LocalDateTime` visit value.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Extend optional `<cw-parameter>` presentation declarations with non-authoritative local-temporal range constraints and prompt-lifecycle validation behavior.
- `generic-htmx-web-component-viewer`: Demonstrate future-date and office-hour parameter bounds through Petclinic `bookVisit` under native and Vaadin toolkit policies.
- `vaadin-semantic-editor-families`: Propagate resolved parameter bounds to qualified local-temporal controls without changing application markup or adapter ownership.

## Impact

The change affects Foundation parameter configuration, action prompt state and validation, shared temporal editor contexts, Foundation tests and documentation, Vaadin field audits where needed, and the Petclinic `bookVisit` domain action, page declaration, integration tests, and Playwright acceptance.
It introduces no GraphQL schema field, third-party dependency, or breaking public element rename.
