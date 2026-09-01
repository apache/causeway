## 1. Parameter declaration contract

- [ ] 1.1 Add observed `min` and `max` attributes and matching JavaScript properties to `<cw-parameter>`.
- [ ] 1.2 Preserve explicit null-versus-blank bounds through bounded normalized parameter configuration.
- [ ] 1.3 Propagate bounds through direct-child capture, action presentation, rerendering, and declaration-order deduplication.
- [ ] 1.4 Add unit coverage for attributes, properties, configuration events, normalization, late declarations, and undeclared parameters.

## 2. Prompt range lifecycle and request gating

- [ ] 2.1 Resolve immutable parameter ranges after canonical preparation and default recomputation using authoritative semantic types.
- [ ] 2.2 Retain resolved ranges and separate local range errors in generation-safe prompt state across recomputation and rendering.
- [ ] 2.3 Add valid bounds and deterministic valid or invalid status hooks to rendered parameter editor contexts and wrappers.
- [ ] 2.4 Reject committed out-of-range values before GraphQL preparation while retaining pending values, local reasons, and focus.
- [ ] 2.5 Reject out-of-range submission before GraphQL preparation, action validation, confirmation, or invocation and focus the first invalid control.
- [ ] 2.6 Clear local reasons on correction and preserve canonical preparation, validation, confirmation, invocation, cancellation, and reopening.
- [ ] 2.7 Add controller tests for absolute and relative bounds, precision, invalid intervals, non-temporal isolation, no-request rejection, correction, submission, and cancellation.

## 3. Native and qualified temporal editors

- [ ] 3.1 Verify parameter editor contexts reuse the existing native temporal `min` and `max` attributes without adapter-specific markup.
- [ ] 3.2 Verify qualified date, time, and date-time parameter controls receive bounds before values while preserving steps, triggers, clear controls, and fallback.
- [ ] 3.3 Extend Foundation and browser coverage for Vaadin and native parameter range behavior and diagnostic cleanliness.

## 4. Petclinic demonstration and validation

- [ ] 4.1 Refactor `bookVisit` to accept a future `LocalDate` and inclusive office-hours `LocalTime`, combine them into the persisted `LocalDateTime`, and retain canonical validators.
- [ ] 4.2 Declare `min="tomorrow"` for `visitDate` and `min="08:00" max="17:00"` for `visitTime` through nested `<cw-parameter>` elements.
- [ ] 4.3 Extend Petclinic integration and Playwright coverage for bounds, local no-request rejection, correction, invocation, cancellation, picker accessibility, and both toolkit policies.
- [ ] 4.4 Update Foundation usage and component contract documentation, run complete Foundation Node and Maven suites, Vaadin audits, Petclinic tests, IDE validation, strict OpenSpec validation, and final diff checks.
